package com.ruoyi.crm.datajob.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmContact;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.domain.LifecycleStage;
import com.ruoyi.crm.customer.mapper.CrmContactMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.service.CustomerService;
import com.ruoyi.crm.datajob.config.DataJobProperties;
import com.ruoyi.crm.datajob.domain.CrmDataJob;
import com.ruoyi.crm.datajob.domain.DataImportType;
import com.ruoyi.crm.datajob.domain.DataJobStatus;
import com.ruoyi.crm.datajob.domain.DataJobType;
import com.ruoyi.crm.datajob.domain.ImportRowResult;
import com.ruoyi.crm.datajob.mapper.CrmDataJobMapper;
import com.ruoyi.crm.datajob.service.DataJobService;
import com.ruoyi.crm.followup.domain.CrmFollowUp;
import com.ruoyi.crm.followup.domain.CrmFollowUpContact;
import com.ruoyi.crm.followup.mapper.CrmFollowUpContactMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpMapper;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.crm.permission.ScopeType;
import com.ruoyi.system.api.model.LoginUser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据作业服务实现（导入导出）
 * <p>
 * 导入：上传—预检（VALIDATED）—确认执行，逐行反馈；<br>
 * 导出：提交时捕获当前用户数据范围，异步生成文件，短期下载，过期标记 EXPIRED。
 *
 * @author ruoyi-crm
 */
@Service
public class DataJobServiceImpl implements DataJobService
{
    private static final Logger log = LoggerFactory.getLogger(DataJobServiceImpl.class);

    /** 导出表头 */
    private static final String[] EXPORT_HEADERS = {
            "客户编码", "客户名称", "经营状态", "生命周期阶段", "重要程度", "客户来源", "行业",
            "省", "市", "区/县", "详细地址", "主负责人", "下次跟进时间", "最近有效跟进时间",
            "跟进状态", "主要联系人", "联系电话"
    };

    /** 重要程度合法值 */
    private static final Set<String> IMPORTANCE_VALUES = new HashSet<>(Arrays.asList("一般", "重要", "非常重要"));

    /** 导出异步执行器（单线程串行，避免大文件并发压力） */
    private final ExecutorService exportExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "crm-export-worker");
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private CrmDataJobMapper dataJobMapper;

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private CrmContactMapper contactMapper;

    @Autowired
    private CrmFollowUpMapper followUpMapper;

    @Autowired
    private CrmFollowUpContactMapper followUpContactMapper;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private DataJobProperties properties;

    // ==================== 导入 ====================

    @Override
    public CrmDataJob uploadImport(MultipartFile file, String importTypeValue)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        checkPermission(PermissionCode.CRM_CUSTOMER_IMPORT);
        DataImportType importType = DataImportType.fromString(importTypeValue);

        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !(originalName.endsWith(".xlsx") || originalName.endsWith(".xls")))
        {
            throw new IllegalArgumentException("仅支持 .xlsx / .xls 格式的 Excel 文件");
        }

        // 1. 解析 Excel
        List<?> rows;
        try (InputStream is = file.getInputStream())
        {
            rows = readImportRows(is, importType);
        }
        catch (Exception e)
        {
            log.error("导入文件解析失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("文件解析失败，请确认为有效的 Excel 文件");
        }
        if (rows == null || rows.isEmpty())
        {
            throw new IllegalArgumentException("文件中没有数据行");
        }
        if (rows.size() > properties.getImportMaxRows())
        {
            throw new IllegalArgumentException("单次导入不能超过 " + properties.getImportMaxRows() + " 行");
        }

        // 2. 逐行预检（不写业务数据）
        List<ImportRowResult> results = validateImportRows(tenantId, importType, rows);

        // 3. 保存源文件与作业记录
        Long jobId = idGenerator.nextId();
        String storageKey = saveUploadFile(tenantId, jobId, file);

        CrmDataJob job = new CrmDataJob();
        job.setJobId(jobId);
        job.setTenantId(tenantId);
        job.setJobType(DataJobType.IMPORT.name());
        job.setImportType(importType.name());
        job.setStatus(DataJobStatus.VALIDATED.name());
        job.setFileName(originalName);
        job.setStorageKey(storageKey);
        job.setTotalCount(rows.size());
        job.setSuccessCount(0);
        job.setFailedCount(0);
        job.setRowResults(JSON.toJSONString(results));
        job.setOperatorId(operatorId);
        job.setOperatorName(operatorName);
        job.setVersion(0);
        job.setDelFlag("0");
        job.setCreateBy(operatorName);
        job.setUpdateBy(operatorName);
        dataJobMapper.insert(job);

        log.info("Import pre-checked: tenantId={}, jobId={}, importType={}, rows={}, operator={}",
                tenantId, jobId, importType, rows.size(), operatorName);
        return job;
    }

    @Override
    public CrmDataJob confirmImport(Long jobId)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        checkPermission(PermissionCode.CRM_CUSTOMER_IMPORT);

        CrmDataJob job = requireJob(tenantId, jobId, DataJobType.IMPORT);
        if (!DataJobStatus.VALIDATED.name().equals(job.getStatus()))
        {
            throw new IllegalArgumentException("作业当前状态不可确认执行：" + job.getStatus());
        }
        if (!operatorId.equals(job.getOperatorId()))
        {
            throw new IllegalArgumentException("仅上传人可确认执行该导入作业");
        }

        DataImportType importType = DataImportType.fromString(job.getImportType());
        List<ImportRowResult> results = JSON.parseArray(job.getRowResults(), ImportRowResult.class);
        List<?> rows = readSourceRows(tenantId, job, importType);
        if (rows.size() != results.size())
        {
            throw new IllegalStateException("源文件行数与预检结果不一致，请重新上传");
        }

        // 标记开始执行
        job.setStatus(DataJobStatus.RUNNING.name());
        job.setStartTime(new Date());
        job.setUpdateBy(operatorName);
        dataJobMapper.update(job);

        int[] counts;
        switch (importType)
        {
            case CONTACT:
                counts = confirmContactRows(tenantId, castRows(rows, ImportContactRow.class),
                        results, operatorName);
                break;
            case FOLLOW_UP:
                counts = confirmFollowUpRows(tenantId, castRows(rows, ImportFollowUpRow.class),
                        results, operatorId, operatorName);
                customerMapper.refreshLastEffectiveFollowUpAt(tenantId, operatorName);
                break;
            case CUSTOMER:
            default:
                counts = confirmCustomerRows(tenantId, castRows(rows, ImportCustomerRow.class),
                        results, operatorId, operatorName);
                break;
        }
        int success = counts[0];
        int failed = counts[1];

        // 当前状态枚举没有 PARTIAL；只要存在写入失败就必须标记 FAILED，
        // 避免作业中心把“0 成功、全部失败”展示成成功。
        job.setStatus(failed == 0 ? DataJobStatus.SUCCESS.name() : DataJobStatus.FAILED.name());
        job.setSuccessCount(success);
        job.setFailedCount(failed);
        job.setErrorMsg(failed == 0 ? null : "存在 " + failed + " 行写入失败，请查看逐行结果");
        job.setRowResults(JSON.toJSONString(results));
        job.setFinishTime(new Date());
        job.setUpdateBy(operatorName);
        dataJobMapper.update(job);

        // 审计：导入执行
        recordAudit(tenantId, jobId, operatorId, operatorName, "IMPORT_" + importType.name(),
                "{\"total\":" + rows.size() + ",\"success\":" + success + ",\"failed\":" + failed + "}");

        log.info("Import confirmed: tenantId={}, jobId={}, importType={}, success={}, failed={}, operator={}",
                tenantId, jobId, importType, success, failed, operatorName);
        return job;
    }

    // ==================== 导出 ====================

    @Override
    public CrmDataJob submitExport(CrmCustomer query)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        checkPermission(PermissionCode.CRM_CUSTOMER_EXPORT);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        ScopeType scopeType = permissionService.getScopeType(tenantId, operatorId);

        Long jobId = idGenerator.nextId();
        CrmDataJob job = new CrmDataJob();
        job.setJobId(jobId);
        job.setTenantId(tenantId);
        job.setJobType(DataJobType.EXPORT.name());
        job.setStatus(DataJobStatus.PENDING.name());
        job.setFileName(buildExportFileName(operatorName));
        job.setQueryCondition(JSON.toJSONString(query));
        job.setOperatorId(operatorId);
        job.setOperatorName(operatorName);
        job.setExpireTime(new Date(System.currentTimeMillis()
                + properties.getExportRetentionHours() * 3600_000L));
        job.setVersion(0);
        job.setDelFlag("0");
        job.setCreateBy(operatorName);
        job.setUpdateBy(operatorName);
        dataJobMapper.insert(job);

        // 审计：导出提交（记录导出人与导出条件）
        recordAudit(tenantId, jobId, operatorId, operatorName, "EXPORT", job.getQueryCondition());

        // 异步生成文件（数据范围在提交时已捕获，不依赖请求上下文）
        final CrmCustomer capturedQuery = query;
        final ScopeType capturedScope = scopeType;
        exportExecutor.submit(() ->
                runExport(tenantId, jobId, capturedQuery, capturedScope, operatorId, operatorDeptId, isAdmin));

        log.info("Export submitted: tenantId={}, jobId={}, scope={}, operator={}",
                tenantId, jobId, scopeType, operatorName);
        return job;
    }

    /**
     * 异步执行导出：按提交时捕获的数据范围查询并生成 Excel 文件
     */
    private void runExport(String tenantId, Long jobId, CrmCustomer query, ScopeType scopeType,
                           Long operatorId, Long operatorDeptId, boolean isAdmin)
    {
        TenantContext.setTenantId(tenantId);
        try
        {
            CrmDataJob job = dataJobMapper.selectByJobId(tenantId, jobId);
            if (job == null)
            {
                return;
            }
            job.setStatus(DataJobStatus.RUNNING.name());
            job.setStartTime(new Date());
            dataJobMapper.update(job);

            // 按数据范围过滤（与 CustomerService.list 一致）
            switch (scopeType)
            {
                case ALL:
                    break;
                case DEPT:
                    if (query.getOwnerDeptId() == null)
                    {
                        query.setOwnerDeptId(operatorDeptId);
                    }
                    break;
                case SELF_CREATED_OR_MEMBER:
                default:
                    query.setPrimaryOwnerId(operatorId);
                    break;
            }
            List<CrmCustomer> customers = customerMapper.selectList(tenantId, query);

            String storageKey = writeExportFile(tenantId, jobId, customers, isAdmin);

            job.setStatus(DataJobStatus.SUCCESS.name());
            job.setStorageKey(storageKey);
            job.setTotalCount(customers.size());
            job.setSuccessCount(customers.size());
            job.setFailedCount(0);
            job.setFinishTime(new Date());
            dataJobMapper.update(job);

            log.info("Export finished: tenantId={}, jobId={}, rows={}", tenantId, jobId, customers.size());
        }
        catch (Exception e)
        {
            log.error("Export failed: tenantId={}, jobId={}, error={}", tenantId, jobId, e.getMessage(), e);
            CrmDataJob failed = dataJobMapper.selectByJobId(tenantId, jobId);
            if (failed != null)
            {
                failed.setStatus(DataJobStatus.FAILED.name());
                failed.setErrorMsg(e.getMessage());
                failed.setFinishTime(new Date());
                dataJobMapper.update(failed);
            }
        }
        finally
        {
            TenantContext.clear();
        }
    }

    // ==================== 查询与下载 ====================

    @Override
    public List<CrmDataJob> listJobs(String jobType)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        List<CrmDataJob> jobs = dataJobMapper.selectList(tenantId, jobType);
        if (isAdmin)
        {
            return jobs;
        }
        // 非管理员仅可见本人作业
        List<CrmDataJob> mine = new ArrayList<>();
        for (CrmDataJob job : jobs)
        {
            if (operatorId.equals(job.getOperatorId()))
            {
                mine.add(job);
            }
        }
        return mine;
    }

    @Override
    public CrmDataJob detail(Long jobId)
    {
        String tenantId = TenantContext.getTenantId();
        CrmDataJob job = requireJob(tenantId, jobId, null);
        checkJobVisible(job);
        return job;
    }

    @Override
    public void downloadExport(Long jobId, HttpServletResponse response)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        checkPermission(PermissionCode.CRM_CUSTOMER_EXPORT);

        CrmDataJob job = requireJob(tenantId, jobId, DataJobType.EXPORT);
        checkJobVisible(job);

        if (!DataJobStatus.SUCCESS.name().equals(job.getStatus()))
        {
            throw new IllegalArgumentException("导出文件尚未就绪：" + job.getStatus());
        }
        if (job.getExpireTime() != null && job.getExpireTime().before(new Date()))
        {
            // 下载时过期即时标记
            if (!DataJobStatus.EXPIRED.name().equals(job.getStatus()))
            {
                job.setStatus(DataJobStatus.EXPIRED.name());
                job.setUpdateBy(operatorName);
                dataJobMapper.update(job);
            }
            throw new IllegalArgumentException("导出文件已过期，请重新导出");
        }

        File file = resolveStorageFile(job.getStorageKey());
        if (!file.exists())
        {
            throw new IllegalArgumentException("导出文件不存在，请重新导出");
        }

        // 审计：下载
        recordAudit(tenantId, jobId, operatorId, operatorName, "DOWNLOAD", job.getFileName());

        try (InputStream is = new FileInputStream(file))
        {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(job.getFileName(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setContentLengthLong(file.length());
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1)
            {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("导出文件下载失败: jobId={}, error={}", jobId, e.getMessage(), e);
            throw new IllegalStateException("文件下载失败");
        }
    }

    // ==================== Private helpers ====================

    /**
     * 校验操作权限（导入/导出为租户级操作，不针对具体客户对象）
     */
    private void checkPermission(PermissionCode code)
    {
        Long operatorId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(operatorId);
        ctx.setOperatorDeptId(operatorDeptId);
        ctx.setAdmin(SecurityUtils.isAdmin(operatorId));
        ctx.setPermissionCode(code);
        permissionService.check(ctx);
    }

    private List<ImportRowResult> validateImportRows(String tenantId, DataImportType importType, List<?> rows)
    {
        List<ImportRowResult> results = new ArrayList<>();
        if (importType == DataImportType.CUSTOMER)
        {
            Set<String> seenNameKeys = new HashSet<>();
            int rowNum = 0;
            for (ImportCustomerRow row : castRows(rows, ImportCustomerRow.class))
            {
                results.add(validateImportRow(tenantId, row, ++rowNum, seenNameKeys));
            }
            return results;
        }

        Map<String, CrmCustomer> customers = loadCustomerMap(tenantId);
        Set<String> seenSourceIds = new HashSet<>();
        int rowNum = 0;
        if (importType == DataImportType.CONTACT)
        {
            for (ImportContactRow row : castRows(rows, ImportContactRow.class))
            {
                results.add(validateContactImportRow(row, ++rowNum, customers, seenSourceIds));
            }
        }
        else
        {
            for (ImportFollowUpRow row : castRows(rows, ImportFollowUpRow.class))
            {
                results.add(validateFollowUpImportRow(row, ++rowNum, customers, seenSourceIds));
            }
        }
        return results;
    }

    private ImportRowResult validateContactImportRow(ImportContactRow row, int rowNum,
                                                       Map<String, CrmCustomer> customers,
                                                       Set<String> seenSourceIds)
    {
        List<String> errors = new ArrayList<>();
        String sourceDataId = trimToNull(row.getSourceDataId());
        String customerName = trimToNull(row.getCustomerName());
        String contactName = trimToNull(row.getName());
        if (sourceDataId == null)
        {
            errors.add("数据id不能为空");
        }
        else if (!seenSourceIds.add(sourceDataId))
        {
            errors.add("文件内数据id重复");
        }
        if (customerName == null)
        {
            errors.add("客户名称不能为空");
        }
        if (contactName == null)
        {
            errors.add("联系人姓名不能为空");
        }
        CrmCustomer customer = customerName == null ? null : customers.get(normalizeNameKey(customerName));
        if (customerName != null && customer == null)
        {
            errors.add("未找到关联客户：" + customerName);
        }
        validateSourceDates(row.getSourceCreateTime(), row.getSourceUpdateTime(), errors);

        String label = customerName == null ? contactName : customerName + " / " + contactName;
        ImportRowResult result = new ImportRowResult(rowNum, label, errors.isEmpty(),
                errors.isEmpty() ? (trimToNull(row.getPhone()) == null
                        ? "预检通过（手机号为空）" : "预检通过") : String.join("；", errors));
        if (customer != null)
        {
            result.setCustomerId(customer.getCustomerId());
        }
        return result;
    }

    private ImportRowResult validateFollowUpImportRow(ImportFollowUpRow row, int rowNum,
                                                        Map<String, CrmCustomer> customers,
                                                        Set<String> seenSourceIds)
    {
        List<String> errors = new ArrayList<>();
        String sourceDataId = trimToNull(row.getSourceDataId());
        String customerName = trimToNull(row.getCustomerName());
        if (sourceDataId == null)
        {
            errors.add("数据id不能为空");
        }
        else if (!seenSourceIds.add(sourceDataId))
        {
            errors.add("文件内数据id重复");
        }
        if (customerName == null)
        {
            errors.add("客户名称不能为空");
        }
        if (trimToNull(row.getContent()) == null)
        {
            errors.add("跟进内容不能为空");
        }
        CrmCustomer customer = customerName == null ? null : customers.get(normalizeNameKey(customerName));
        if (customerName != null && customer == null)
        {
            errors.add("未找到关联客户：" + customerName);
        }
        validateSourceDates(row.getSourceCreateTime(), row.getSourceUpdateTime(), errors);

        String message = errors.isEmpty() ? "预检通过" : String.join("；", errors);
        if (errors.isEmpty() && trimToNull(row.getSourceAttachmentRefs()) != null)
        {
            message = "预检通过；原附件引用仅留档，本次不迁移附件文件";
        }
        ImportRowResult result = new ImportRowResult(rowNum, customerName, errors.isEmpty(), message);
        if (customer != null)
        {
            result.setCustomerId(customer.getCustomerId());
        }
        return result;
    }

    private void validateSourceDates(String createTime, String updateTime, List<String> errors)
    {
        if (trimToNull(createTime) != null && parseDate(createTime) == null)
        {
            errors.add("创建时间格式错误");
        }
        if (trimToNull(updateTime) != null && parseDate(updateTime) == null)
        {
            errors.add("更新时间格式错误");
        }
    }

    /**
     * 预检单行导入数据
     */
    private ImportRowResult validateImportRow(String tenantId, ImportCustomerRow row, int rowNum,
                                              Set<String> seenNameKeys)
    {
        List<String> errors = new ArrayList<>();
        String successMessage = "预检通过";

        String name = row.getName() == null ? "" : row.getName().trim();
        if (name.isEmpty())
        {
            errors.add("客户名称不能为空");
        }
        else
        {
            String nameKey = normalizeNameKey(name);
            if (seenNameKeys.contains(nameKey))
            {
                errors.add("文件内客户名称重复");
            }
            else
            {
                seenNameKeys.add(nameKey);
                if (customerMapper.selectByActiveNameKey(tenantId, nameKey) != null)
                {
                    successMessage = "预检通过，将补充已有客户的导入资料";
                }
            }
        }

        String nextFollowUpText = trimToNull(row.getNextFollowUpAt());
        if (nextFollowUpText != null && parseDate(nextFollowUpText) == null)
        {
            errors.add("下次跟进时间格式错误（支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）");
        }

        String importance = normalizeImportance(row.getImportance());
        if (importance != null && !IMPORTANCE_VALUES.contains(importance))
        {
            errors.add("重要程度仅支持：一般/重要/非常重要");
        }

        if (errors.isEmpty())
        {
            return new ImportRowResult(rowNum, name, true, successMessage);
        }
        return new ImportRowResult(rowNum, name, false, String.join("；", errors));
    }

    /**
     * 由导入行构建客户对象（主负责人默认为操作人）
     */
    private CrmCustomer buildCustomerFromRow(ImportCustomerRow row, Long operatorId, String operatorName)
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setName(row.getName().trim());
        // 历史钉钉表只有一个“地址”字段，而客户表的省、市、详细地址为 NOT NULL。
        // 未知的结构化地址保持为空字符串，原始地址完整保存在详细地址中。
        customer.setAddressProvince(emptyIfNull(trimToNull(row.getAddressProvince())));
        customer.setAddressCity(emptyIfNull(trimToNull(row.getAddressCity())));
        customer.setAddressDistrict(trimToNull(row.getAddressDistrict()));
        customer.setAddressDetail(emptyIfNull(firstNonBlank(row.getAddressDetail(), row.getLegacyAddress())));
        customer.setTags(normalizeTags(row.getTags()));
        customer.setFollowUpIntensity(firstNonBlank(row.getFollowUpIntensity(), row.getLegacyLevel()));
        customer.setSourceFollowUpStatus(trimToNull(row.getSourceFollowUpStatus()));
        customer.setCustomerGroup(trimToNull(row.getCustomerGroup()));
        customer.setSourceCustomerStatus(trimToNull(row.getSourceCustomerStatus()));
        customer.setLifecycleStage(normalizeLifecycleStage(row.getSourceFollowUpStatus()));
        String importance = normalizeImportance(row.getImportance());
        customer.setImportance(importance == null ? "一般" : importance);
        customer.setSource(emptyIfNull(trimToNull(row.getSource())));
        customer.setReferredCustomerName(trimToNull(row.getReferredCustomerName()));
        customer.setSourceOther(trimToNull(row.getSourceOther()));
        customer.setIndustry(emptyIfNull(firstNonBlank(row.getIndustry(), row.getLegacyIndustry())));
        customer.setIndustryOther(trimToNull(row.getIndustryOther()));
        customer.setSourceCreatorName(trimToNull(row.getSourceCreatorName()));
        customer.setSourceOwnerName(trimToNull(row.getSourceOwnerName()));
        customer.setSourceCollaboratorNames(trimToNull(row.getSourceCollaboratorNames()));
        customer.setCreateTime(row.getSourceCreateTime());
        customer.setUpdateTime(row.getSourceUpdateTime());
        customer.setDroppedProtectionAt(row.getDroppedProtectionAt());
        customer.setRemark(trimToNull(row.getRemark()));
        Date nextFollowUpAt = parseDate(row.getNextFollowUpAt());
        // 历史客户表没有“下次跟进时间”列。使用导入时刻保证正常客户约束，
        // 并让这些待重新规划的客户立即出现在跟进列表中，而不是虚构未来计划。
        customer.setNextFollowUpAt(nextFollowUpAt == null ? new Date() : nextFollowUpAt);
        customer.setPrimaryOwnerId(operatorId);
        customer.setPrimaryOwnerName(operatorName);
        return customer;
    }

    private int[] confirmCustomerRows(String tenantId, List<ImportCustomerRow> rows,
                                      List<ImportRowResult> results,
                                      Long operatorId, String operatorName)
    {
        int success = 0;
        int failed = 0;
        for (int i = 0; i < rows.size(); i++)
        {
            ImportRowResult result = results.get(i);
            if (!Boolean.TRUE.equals(result.getValid()))
            {
                result.setResult("SKIPPED");
                continue;
            }
            try
            {
                CrmCustomer customer = buildCustomerFromRow(rows.get(i), operatorId, operatorName);
                CrmCustomer existing = customerMapper.selectByActiveNameKey(
                        tenantId, normalizeNameKey(customer.getName()));
                CrmCustomer saved;
                if (existing == null)
                {
                    saved = customerService.create(customer);
                }
                else
                {
                    int updated = customerMapper.updateImportedMetadata(
                            tenantId, existing.getCustomerId(), customer);
                    if (updated == 0)
                    {
                        throw new IllegalStateException("已有客户资料回填失败");
                    }
                    saved = customerMapper.selectByCustomerId(tenantId, existing.getCustomerId());
                }
                result.setResult("SUCCESS");
                result.setCustomerId(saved.getCustomerId());
                success++;
            }
            catch (Exception e)
            {
                result.setResult("FAILED");
                result.setMessage(e.getMessage());
                failed++;
            }
        }
        return new int[]{success, failed};
    }

    private int[] confirmContactRows(String tenantId, List<ImportContactRow> rows,
                                     List<ImportRowResult> results, String operatorName)
    {
        Map<String, CrmCustomer> customers = loadCustomerMap(tenantId);
        int success = 0;
        int failed = 0;
        for (int i = 0; i < rows.size(); i++)
        {
            ImportRowResult result = results.get(i);
            if (!Boolean.TRUE.equals(result.getValid()))
            {
                result.setResult("SKIPPED");
                continue;
            }
            ImportContactRow row = rows.get(i);
            try
            {
                CrmCustomer customer = customers.get(normalizeNameKey(row.getCustomerName()));
                if (customer == null)
                {
                    throw new IllegalArgumentException("关联客户不存在：" + row.getCustomerName());
                }
                CrmContact existing = contactMapper.selectBySourceDataId(tenantId, row.getSourceDataId());
                if (existing != null)
                {
                    result.setResult("SUCCESS");
                    result.setCustomerId(existing.getCustomerId());
                    result.setMessage("源记录已存在，已跳过重复写入");
                    success++;
                    continue;
                }

                CrmContact contact = buildContactFromRow(row, tenantId, customer.getCustomerId(), operatorName);
                if (contact.getPhoneNumber() != null)
                {
                    existing = contactMapper.selectByCustomerAndPhone(
                            tenantId, customer.getCustomerId(), contact.getPhoneNumber());
                }
                if (existing != null)
                {
                    contactMapper.bindSourceDataId(
                            tenantId, existing.getContactId(), row.getSourceDataId(), operatorName);
                    result.setMessage("已关联该客户下相同手机号的联系人");
                }
                else
                {
                    contactMapper.insert(contact);
                }
                result.setResult("SUCCESS");
                result.setCustomerId(customer.getCustomerId());
                success++;
            }
            catch (Exception e)
            {
                result.setResult("FAILED");
                result.setMessage(e.getMessage());
                failed++;
            }
        }
        return new int[]{success, failed};
    }

    private CrmContact buildContactFromRow(ImportContactRow row, String tenantId,
                                           Long customerId, String operatorName)
    {
        String phone = normalizeImportedPhone(row.getPhone());
        String creatorName = firstNonBlank(row.getSourceCreatorName(), operatorName);
        Date createTime = parseDate(row.getSourceCreateTime());
        Date updateTime = parseDate(row.getSourceUpdateTime());

        CrmContact contact = new CrmContact();
        contact.setContactId(idGenerator.nextId());
        contact.setSourceDataId(trimToNull(row.getSourceDataId()));
        contact.setTenantId(tenantId);
        contact.setCustomerId(customerId);
        contact.setName(row.getName().trim());
        contact.setPhoneType(phone == null ? "其他" : "手机");
        contact.setCountryCode("+86");
        contact.setPhoneNumber(phone);
        contact.setPhoneMasked(maskPhone(phone));
        contact.setEmail(trimToNull(row.getEmail()));
        contact.setEmailMasked(maskEmail(row.getEmail()));
        contact.setWechatId(trimToNull(row.getWechatId()));
        contact.setWechatMasked(maskWechat(row.getWechatId()));
        contact.setResponsibility(trimToNull(row.getResponsibility()));
        contact.setTitle(trimToNull(row.getTitle()));
        contact.setIsDecisionMaker(isYes(row.getDecisionMaker()));
        contact.setRemark(trimToNull(row.getRemark()));
        contact.setStatus("有效");
        contact.setSourceOwnerNames(trimToNull(row.getSourceOwnerNames()));
        contact.setSourceCollaboratorNames(trimToNull(row.getSourceCollaboratorNames()));
        contact.setVersion(0);
        contact.setDelFlag("0");
        contact.setCreateBy(creatorName);
        contact.setCreateTime(createTime);
        contact.setUpdateBy(creatorName);
        contact.setUpdateTime(updateTime == null ? createTime : updateTime);
        return contact;
    }

    private int[] confirmFollowUpRows(String tenantId, List<ImportFollowUpRow> rows,
                                      List<ImportRowResult> results,
                                      Long operatorId, String operatorName)
    {
        Map<String, CrmCustomer> customers = loadCustomerMap(tenantId);
        int success = 0;
        int failed = 0;
        for (int i = 0; i < rows.size(); i++)
        {
            ImportRowResult result = results.get(i);
            if (!Boolean.TRUE.equals(result.getValid()))
            {
                result.setResult("SKIPPED");
                continue;
            }
            ImportFollowUpRow row = rows.get(i);
            try
            {
                CrmCustomer customer = customers.get(normalizeNameKey(row.getCustomerName()));
                if (customer == null)
                {
                    throw new IllegalArgumentException("关联客户不存在：" + row.getCustomerName());
                }
                CrmFollowUp existing = followUpMapper.selectBySourceDataId(tenantId, row.getSourceDataId());
                if (existing != null)
                {
                    result.setResult("SUCCESS");
                    result.setCustomerId(existing.getCustomerId());
                    result.setMessage("源记录已存在，已跳过重复写入");
                    success++;
                    continue;
                }

                CrmFollowUp followUp = buildFollowUpFromRow(
                        row, tenantId, customer.getCustomerId(), operatorId, operatorName);
                followUpMapper.insert(followUp);
                linkImportedFollowUpContacts(tenantId, customer.getCustomerId(),
                        followUp.getFollowUpId(), row.getContactNames());
                result.setResult("SUCCESS");
                result.setCustomerId(customer.getCustomerId());
                success++;
            }
            catch (Exception e)
            {
                result.setResult("FAILED");
                result.setMessage(e.getMessage());
                failed++;
            }
        }
        return new int[]{success, failed};
    }

    private CrmFollowUp buildFollowUpFromRow(ImportFollowUpRow row, String tenantId,
                                             Long customerId, Long operatorId, String operatorName)
    {
        Date followUpAt = parseDate(row.getSourceCreateTime());
        Date updateTime = parseDate(row.getSourceUpdateTime());
        if (followUpAt == null)
        {
            followUpAt = updateTime == null ? new Date() : updateTime;
        }
        String creatorName = firstNonBlank(row.getSourceCreatorName(), operatorName);

        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setFollowUpId(idGenerator.nextId());
        followUp.setSourceDataId(trimToNull(row.getSourceDataId()));
        followUp.setTenantId(tenantId);
        followUp.setCustomerId(customerId);
        followUp.setMethod(normalizeFollowUpMethod(row.getMethod()));
        followUp.setFollowUpAt(followUpAt);
        followUp.setContent(row.getContent().trim());
        followUp.setHasNewSigningProject(isYes(row.getHasNewSigningProject()));
        followUp.setIsCorrected(false);
        followUp.setIsVoided(false);
        followUp.setCreatedBy(operatorId);
        followUp.setCreatedByName(creatorName);
        followUp.setImmutableAt(updateTime == null ? followUpAt : updateTime);
        followUp.setSourceContactNames(trimToNull(row.getContactNames()));
        followUp.setSourceAttachmentRefs(trimToNull(row.getSourceAttachmentRefs()));
        followUp.setSourceIsKeyCustomer(isYes(row.getSourceIsKeyCustomer()));
        followUp.setSourceCreatorDept(trimToNull(row.getSourceCreatorDept()));
        followUp.setSourceApprovalTitle(trimToNull(row.getSourceApprovalTitle()));
        followUp.setSourceOwnerNames(trimToNull(row.getSourceOwnerNames()));
        followUp.setSourceCollaboratorNames(trimToNull(row.getSourceCollaboratorNames()));
        followUp.setVersion(0);
        followUp.setDelFlag("0");
        followUp.setCreateBy(creatorName);
        followUp.setCreateTime(followUpAt);
        followUp.setUpdateBy(creatorName);
        followUp.setUpdateTime(updateTime == null ? followUpAt : updateTime);
        return followUp;
    }

    private void linkImportedFollowUpContacts(String tenantId, Long customerId,
                                              Long followUpId, String contactNames)
    {
        String value = trimToNull(contactNames);
        if (value == null)
        {
            return;
        }
        List<CrmFollowUpContact> links = new ArrayList<>();
        Set<Long> contactIds = new HashSet<>();
        for (String item : value.split("、"))
        {
            String name = trimToNull(item);
            if (name == null)
            {
                continue;
            }
            CrmContact contact = contactMapper.selectByCustomerAndName(tenantId, customerId, name);
            if (contact != null && contactIds.add(contact.getContactId()))
            {
                CrmFollowUpContact link = new CrmFollowUpContact();
                link.setId(idGenerator.nextId());
                link.setTenantId(tenantId);
                link.setFollowUpId(followUpId);
                link.setContactId(contact.getContactId());
                links.add(link);
            }
        }
        if (!links.isEmpty())
        {
            followUpContactMapper.batchInsert(links);
        }
    }

    /**
     * 读取导入源文件数据行
     */
    private List<?> readSourceRows(String tenantId, CrmDataJob job, DataImportType importType)
    {
        File file = resolveStorageFile(job.getStorageKey());
        if (!file.exists())
        {
            throw new IllegalStateException("导入源文件不存在，请重新上传");
        }
        try (InputStream is = new FileInputStream(file))
        {
            return readImportRows(is, importType);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("导入源文件读取失败：" + e.getMessage());
        }
    }

    private List<?> readImportRows(InputStream input, DataImportType importType) throws Exception
    {
        switch (importType)
        {
            case CONTACT:
                return readContactRows(input);
            case FOLLOW_UP:
                return readFollowUpRows(input);
            case CUSTOMER:
            default:
                List<ImportCustomerRow> rows = new ExcelUtil<>(ImportCustomerRow.class).importExcel(input);
                return rows == null ? new ArrayList<>() : rows;
        }
    }

    private List<ImportContactRow> readContactRows(InputStream input) throws Exception
    {
        try (Workbook workbook = WorkbookFactory.create(input))
        {
            Sheet sheet = requireLegacySheet(workbook, "联系人");
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            List<ImportContactRow> rows = new ArrayList<>();
            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++)
            {
                Row source = sheet.getRow(rowIndex);
                if (source == null || isBlankRow(source, formatter, 1, 4))
                {
                    continue;
                }
                ImportContactRow row = new ImportContactRow();
                row.setSourceDataId(cellText(source, 1, formatter));
                row.setCustomerName(firstNonBlank(cellText(source, 3, formatter), cellText(source, 2, formatter)));
                row.setName(cellText(source, 4, formatter));
                row.setTitle(cellText(source, 5, formatter));
                row.setResponsibility(cellText(source, 6, formatter));
                row.setDecisionMaker(cellText(source, 7, formatter));
                row.setPhone(cellText(source, 8, formatter));
                row.setWechatId(cellText(source, 9, formatter));
                row.setEmail(cellText(source, 10, formatter));
                row.setRemark(cellText(source, 11, formatter));
                row.setSourceCreateTime(cellText(source, 12, formatter));
                row.setSourceCreatorName(cellText(source, 13, formatter));
                row.setSourceUpdateTime(cellText(source, 16, formatter));
                row.setSourceOwnerNames(cellText(source, 17, formatter));
                row.setSourceCollaboratorNames(cellText(source, 18, formatter));
                rows.add(row);
            }
            return rows;
        }
    }

    private List<ImportFollowUpRow> readFollowUpRows(InputStream input) throws Exception
    {
        try (Workbook workbook = WorkbookFactory.create(input))
        {
            Sheet sheet = requireLegacySheet(workbook, "跟进记录");
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            List<ImportFollowUpRow> rows = new ArrayList<>();
            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++)
            {
                Row source = sheet.getRow(rowIndex);
                if (source == null || isBlankRow(source, formatter, 1, 17))
                {
                    continue;
                }
                ImportFollowUpRow row = new ImportFollowUpRow();
                row.setSourceDataId(cellText(source, 1, formatter));
                row.setCustomerName(firstNonBlank(cellText(source, 3, formatter), cellText(source, 2, formatter)));
                row.setHasNewSigningProject(cellText(source, 4, formatter));
                row.setContactNames(joinDistinct(
                        firstNonBlank(cellText(source, 6, formatter), cellText(source, 5, formatter)),
                        firstNonBlank(cellText(source, 9, formatter), cellText(source, 8, formatter)),
                        firstNonBlank(cellText(source, 12, formatter), cellText(source, 11, formatter))));
                row.setMethod(cellText(source, 14, formatter));
                row.setSourceAttachmentRefs(cellText(source, 15, formatter));
                row.setSourceIsKeyCustomer(cellText(source, 16, formatter));
                row.setContent(cellText(source, 17, formatter));
                row.setSourceCreateTime(cellText(source, 18, formatter));
                row.setSourceCreatorName(cellText(source, 19, formatter));
                row.setSourceCreatorDept(cellText(source, 20, formatter));
                row.setSourceApprovalTitle(cellText(source, 21, formatter));
                row.setSourceUpdateTime(cellText(source, 22, formatter));
                row.setSourceOwnerNames(cellText(source, 23, formatter));
                row.setSourceCollaboratorNames(cellText(source, 24, formatter));
                rows.add(row);
            }
            return rows;
        }
    }

    private Sheet requireLegacySheet(Workbook workbook, String expectedType)
    {
        if (workbook.getNumberOfSheets() == 0)
        {
            throw new IllegalArgumentException("Excel 中没有工作表");
        }
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        String firstHeader = cellText(sheet.getRow(0), 1, formatter);
        String secondHeader = cellText(sheet.getRow(1), 1, formatter);
        if (!"数据id".equals(firstHeader) || !"数据id".equals(secondHeader))
        {
            throw new IllegalArgumentException("不是受支持的 CRM" + expectedType + "双层表头文件");
        }
        return sheet;
    }

    private boolean isBlankRow(Row row, DataFormatter formatter, int... indexes)
    {
        for (int index : indexes)
        {
            if (trimToNull(cellText(row, index, formatter)) != null)
            {
                return false;
            }
        }
        return true;
    }

    private String cellText(Row row, int index, DataFormatter formatter)
    {
        if (row == null)
        {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null)
        {
            return null;
        }
        return trimToNull(formatter.formatCellValue(cell));
    }

    /**
     * 保存上传的导入源文件
     */
    private String saveUploadFile(String tenantId, Long jobId, MultipartFile file)
    {
        String relativeKey = tenantId + "/" + jobId + "_source.xlsx";
        File target = resolveStorageFile(relativeKey);
        try
        {
            Files.createDirectories(target.toPath().getParent());
            try (InputStream input = file.getInputStream())
            {
                /*
                 * MultipartFile#transferTo 对相对路径的解释由 Servlet 容器决定。
                 * Tomcat 会把它解析到自己的临时工作目录，和上面创建的业务目录不一致。
                 * 显式流复制到绝对规范路径，确保本地、容器和测试环境行为一致。
                 */
                Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e)
        {
            log.error("导入文件保存失败: {}", e.getMessage(), e);
            throw new IllegalStateException("导入文件保存失败");
        }
        return relativeKey;
    }

    /**
     * 生成导出 Excel 文件（POI 原生写入，敏感字段按权限脱敏）
     */
    private String writeExportFile(String tenantId, Long jobId, List<CrmCustomer> customers, boolean isAdmin)
            throws Exception
    {
        String relativeKey = tenantId + "/" + jobId + ".xlsx";
        File target = resolveStorageFile(relativeKey);
        Files.createDirectories(target.toPath().getParent());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(target))
        {
            Sheet sheet = wb.createSheet("客户数据");
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++)
            {
                header.createCell(i).setCellValue(EXPORT_HEADERS[i]);
            }

            int rowIdx = 0;
            for (CrmCustomer c : customers)
            {
                rowIdx++;
                Row row = sheet.createRow(rowIdx);
                int col = 0;
                setCellValue(row, col++, c.getCustomerCode());
                setCellValue(row, col++, c.getName());
                setCellValue(row, col++, c.getOperatingStatus());
                setCellValue(row, col++, c.getLifecycleStage());
                setCellValue(row, col++, c.getImportance());
                setCellValue(row, col++, c.getSource());
                setCellValue(row, col++, c.getIndustry());
                setCellValue(row, col++, c.getAddressProvince());
                setCellValue(row, col++, c.getAddressCity());
                setCellValue(row, col++, c.getAddressDistrict());
                setCellValue(row, col++, c.getAddressDetail());
                setCellValue(row, col++, c.getPrimaryOwnerName());
                setDateCell(row, col++, c.getNextFollowUpAt(), dateStyle);
                setDateCell(row, col++, c.getLastEffectiveFollowUpAt(), dateStyle);
                setCellValue(row, col++, c.getFollowUpStatus());

                // 主要联系人（敏感字段按权限脱敏）
                CrmContact primaryContact = findPrimaryContact(tenantId, c.getCustomerId());
                if (primaryContact != null)
                {
                    setCellValue(row, col++, primaryContact.getName());
                    String phone = isAdmin ? primaryContact.getPhoneNumber() : primaryContact.getPhoneMasked();
                    setCellValue(row, col, phone);
                }
            }

            wb.write(fos);
        }
        return relativeKey;
    }

    /**
     * 查询客户第一个有效联系人
     */
    private CrmContact findPrimaryContact(String tenantId, Long customerId)
    {
        List<CrmContact> contacts = contactMapper.selectByCustomer(tenantId, customerId);
        if (contacts == null || contacts.isEmpty())
        {
            return null;
        }
        for (CrmContact contact : contacts)
        {
            if ("有效".equals(contact.getStatus()))
            {
                return contact;
            }
        }
        return contacts.get(0);
    }

    private void setCellValue(Row row, int col, String value)
    {
        row.createCell(col).setCellValue(value == null ? "" : value);
    }

    private void setDateCell(Row row, int col, Date value, CellStyle style)
    {
        Cell cell = row.createCell(col);
        if (value != null)
        {
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }

    private CrmDataJob requireJob(String tenantId, Long jobId, DataJobType expectedType)
    {
        CrmDataJob job = dataJobMapper.selectByJobId(tenantId, jobId);
        if (job == null)
        {
            throw new IllegalArgumentException("作业不存在：" + jobId);
        }
        if (expectedType != null && !expectedType.name().equals(job.getJobType()))
        {
            throw new IllegalArgumentException("作业类型不匹配");
        }
        return job;
    }

    /**
     * 可见性校验：管理员可见全部，其他用户仅可见本人作业
     */
    private void checkJobVisible(CrmDataJob job)
    {
        Long operatorId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(operatorId))
        {
            return;
        }
        if (!operatorId.equals(job.getOperatorId()))
        {
            throw new IllegalArgumentException("无权访问该作业");
        }
    }

    private File resolveStorageFile(String relativeKey)
    {
        Path storageRoot = Paths.get(properties.getStoragePath()).toAbsolutePath().normalize();
        Path target = storageRoot.resolve(relativeKey).normalize();
        if (!target.startsWith(storageRoot))
        {
            throw new IllegalArgumentException("非法的数据作业文件路径");
        }
        return target.toFile();
    }

    private String buildExportFileName(String operatorName)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "客户导出_" + operatorName + "_" + sdf.format(new Date()) + ".xlsx";
    }

    private String normalizeNameKey(String name)
    {
        if (name == null)
        {
            return "";
        }
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    private String trimToNull(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String emptyIfNull(String value)
    {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String primary, String fallback)
    {
        String value = trimToNull(primary);
        return value == null ? trimToNull(fallback) : value;
    }

    private String normalizeImportance(String importance)
    {
        String value = trimToNull(importance);
        if ("很重要".equals(value))
        {
            return "非常重要";
        }
        return value;
    }

    private String normalizeLifecycleStage(String sourceStatus)
    {
        String value = trimToNull(sourceStatus);
        if ("成交".equals(value))
        {
            return LifecycleStage.CLOSED_WON.getValue();
        }
        for (LifecycleStage stage : LifecycleStage.values())
        {
            if (stage.getValue().equals(value))
            {
                return value;
            }
        }
        return null;
    }

    private String normalizeTags(String tags)
    {
        String value = trimToNull(tags);
        if (value == null)
        {
            return null;
        }
        if (value.startsWith("[") && value.endsWith("]"))
        {
            try
            {
                return JSON.toJSONString(JSON.parseArray(value));
            }
            catch (Exception ignored)
            {
                // 非法 JSON 按普通分隔文本处理
            }
        }
        List<String> normalized = new ArrayList<>();
        for (String item : value.split("[,，;；、]"))
        {
            String tag = trimToNull(item);
            if (tag != null)
            {
                normalized.add(tag);
            }
        }
        return normalized.isEmpty() ? null : JSON.toJSONString(normalized);
    }

    /**
     * 宽松解析日期：支持 yyyy-MM-dd HH:mm:ss / yyyy-MM-dd
     */
    private Date parseDate(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        String trimmed = value.trim();
        String[] patterns = trimmed.length() <= 10
                ? new String[]{"yyyy-MM-dd", "yyyy/MM/dd"}
                : new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm"};
        for (String pattern : patterns)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(trimmed);
            }
            catch (Exception ignored)
            {
                // 尝试下一个格式
            }
        }
        return null;
    }

    private Map<String, CrmCustomer> loadCustomerMap(String tenantId)
    {
        Map<String, CrmCustomer> customers = new HashMap<>();
        List<CrmCustomer> rows = customerMapper.selectAll(tenantId);
        if (rows != null)
        {
            for (CrmCustomer customer : rows)
            {
                customers.put(normalizeNameKey(customer.getName()), customer);
            }
        }
        return customers;
    }

    private <T> List<T> castRows(List<?> rows, Class<T> type)
    {
        List<T> cast = new ArrayList<>(rows.size());
        for (Object row : rows)
        {
            cast.add(type.cast(row));
        }
        return cast;
    }

    private String normalizeImportedPhone(String phone)
    {
        String value = trimToNull(phone);
        if (value == null)
        {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }

    private String maskPhone(String phone)
    {
        String value = trimToNull(phone);
        if (value == null || value.length() <= 7)
        {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    private String maskEmail(String email)
    {
        String value = trimToNull(email);
        if (value == null || !value.contains("@"))
        {
            return value;
        }
        int atIndex = value.indexOf('@');
        return atIndex <= 1 ? value : value.charAt(0) + "***" + value.substring(atIndex);
    }

    private String maskWechat(String wechatId)
    {
        String value = trimToNull(wechatId);
        if (value == null || value.length() <= 4)
        {
            return value;
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    private boolean isYes(String value)
    {
        String normalized = trimToNull(value);
        return "是".equals(normalized) || "true".equalsIgnoreCase(normalized)
                || "1".equals(normalized) || "yes".equalsIgnoreCase(normalized);
    }

    private String normalizeFollowUpMethod(String value)
    {
        String method = trimToNull(value);
        if (method == null)
        {
            return "其他";
        }
        if (method.startsWith("电话"))
        {
            return "电话";
        }
        if (method.startsWith("微信"))
        {
            return "微信";
        }
        if (method.startsWith("面谈"))
        {
            return "面谈";
        }
        if (method.startsWith("邮件"))
        {
            return "邮件";
        }
        return "其他";
    }

    private String joinDistinct(String... values)
    {
        Set<String> distinct = new LinkedHashSet<>();
        for (String value : values)
        {
            String normalized = trimToNull(value);
            if (normalized != null)
            {
                distinct.add(normalized);
            }
        }
        return distinct.isEmpty() ? null : String.join("、", distinct);
    }

    private void recordAudit(String tenantId, Long jobId, Long operatorId, String operatorName,
                             String action, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("DATA_JOB");
        event.setEntityType(action.startsWith("IMPORT") ? "IMPORT" : "EXPORT");
        event.setEntityId(String.valueOf(jobId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }
}
