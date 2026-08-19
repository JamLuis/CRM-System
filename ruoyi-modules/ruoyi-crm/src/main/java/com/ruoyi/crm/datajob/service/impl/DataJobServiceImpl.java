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
import com.ruoyi.crm.customer.mapper.CrmContactMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.service.CustomerService;
import com.ruoyi.crm.datajob.config.DataJobProperties;
import com.ruoyi.crm.datajob.domain.CrmDataJob;
import com.ruoyi.crm.datajob.domain.DataJobStatus;
import com.ruoyi.crm.datajob.domain.DataJobType;
import com.ruoyi.crm.datajob.domain.ImportRowResult;
import com.ruoyi.crm.datajob.mapper.CrmDataJobMapper;
import com.ruoyi.crm.datajob.service.DataJobService;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.crm.permission.ScopeType;
import com.ruoyi.system.api.model.LoginUser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    public CrmDataJob uploadImport(MultipartFile file)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        checkPermission(PermissionCode.CRM_CUSTOMER_IMPORT);

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
        List<ImportCustomerRow> rows;
        try (InputStream is = file.getInputStream())
        {
            rows = new ExcelUtil<>(ImportCustomerRow.class).importExcel(is);
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
        Set<String> seenNameKeys = new HashSet<>();
        List<ImportRowResult> results = new ArrayList<>();
        int rowNum = 0;
        for (ImportCustomerRow row : rows)
        {
            rowNum++;
            results.add(validateImportRow(tenantId, row, rowNum, seenNameKeys));
        }

        // 3. 保存源文件与作业记录
        Long jobId = idGenerator.nextId();
        String storageKey = saveUploadFile(tenantId, jobId, file);

        CrmDataJob job = new CrmDataJob();
        job.setJobId(jobId);
        job.setTenantId(tenantId);
        job.setJobType(DataJobType.IMPORT.name());
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

        log.info("Import pre-checked: tenantId={}, jobId={}, rows={}, operator={}",
                tenantId, jobId, rows.size(), operatorName);
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

        List<ImportRowResult> results = JSON.parseArray(job.getRowResults(), ImportRowResult.class);
        List<ImportCustomerRow> rows = readSourceRows(tenantId, job);
        if (rows.size() != results.size())
        {
            throw new IllegalStateException("源文件行数与预检结果不一致，请重新上传");
        }

        // 标记开始执行
        job.setStatus(DataJobStatus.RUNNING.name());
        job.setStartTime(new Date());
        job.setUpdateBy(operatorName);
        dataJobMapper.update(job);

        int success = 0;
        int failed = 0;
        for (int i = 0; i < rows.size(); i++)
        {
            ImportRowResult result = results.get(i);
            if (result.getValid() == null || !result.getValid())
            {
                // 预检未通过的行跳过
                result.setResult("SKIPPED");
                continue;
            }
            try
            {
                CrmCustomer customer = buildCustomerFromRow(rows.get(i), operatorId, operatorName);
                CrmCustomer created = customerService.create(customer);
                result.setResult("SUCCESS");
                result.setCustomerId(created.getCustomerId());
                success++;
            }
            catch (Exception e)
            {
                result.setResult("FAILED");
                result.setMessage(e.getMessage());
                failed++;
            }
        }

        job.setStatus(DataJobStatus.SUCCESS.name());
        job.setSuccessCount(success);
        job.setFailedCount(failed);
        job.setRowResults(JSON.toJSONString(results));
        job.setFinishTime(new Date());
        job.setUpdateBy(operatorName);
        dataJobMapper.update(job);

        // 审计：导入执行
        recordAudit(tenantId, jobId, operatorId, operatorName, "IMPORT",
                "{\"total\":" + rows.size() + ",\"success\":" + success + ",\"failed\":" + failed + "}");

        log.info("Import confirmed: tenantId={}, jobId={}, success={}, failed={}, operator={}",
                tenantId, jobId, success, failed, operatorName);
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

    /**
     * 预检单行导入数据
     */
    private ImportRowResult validateImportRow(String tenantId, ImportCustomerRow row, int rowNum,
                                              Set<String> seenNameKeys)
    {
        List<String> errors = new ArrayList<>();

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
                    errors.add("客户名称已存在（重名）");
                }
            }
        }

        Date nextFollowUpAt = parseDate(row.getNextFollowUpAt());
        if (nextFollowUpAt == null)
        {
            errors.add("下次跟进时间为空或格式错误（支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）");
        }

        if (row.getImportance() != null && !row.getImportance().trim().isEmpty()
                && !IMPORTANCE_VALUES.contains(row.getImportance().trim()))
        {
            errors.add("重要程度仅支持：一般/重要/非常重要");
        }

        if (errors.isEmpty())
        {
            return new ImportRowResult(rowNum, name, true, "预检通过");
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
        customer.setAddressProvince(trimToNull(row.getAddressProvince()));
        customer.setAddressCity(trimToNull(row.getAddressCity()));
        customer.setAddressDistrict(trimToNull(row.getAddressDistrict()));
        customer.setAddressDetail(trimToNull(row.getAddressDetail()));
        customer.setImportance(trimToNull(row.getImportance()));
        customer.setSource(trimToNull(row.getSource()));
        customer.setIndustry(trimToNull(row.getIndustry()));
        customer.setRemark(trimToNull(row.getRemark()));
        customer.setNextFollowUpAt(parseDate(row.getNextFollowUpAt()));
        customer.setPrimaryOwnerId(operatorId);
        customer.setPrimaryOwnerName(operatorName);
        return customer;
    }

    /**
     * 读取导入源文件数据行
     */
    private List<ImportCustomerRow> readSourceRows(String tenantId, CrmDataJob job)
    {
        File file = resolveStorageFile(job.getStorageKey());
        if (!file.exists())
        {
            throw new IllegalStateException("导入源文件不存在，请重新上传");
        }
        try (InputStream is = new FileInputStream(file))
        {
            List<ImportCustomerRow> rows = new ExcelUtil<>(ImportCustomerRow.class).importExcel(is);
            return rows == null ? new ArrayList<>() : rows;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("导入源文件读取失败：" + e.getMessage());
        }
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
            Files.createDirectories(Paths.get(properties.getStoragePath(), tenantId));
            file.transferTo(target);
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
        Files.createDirectories(Paths.get(properties.getStoragePath(), tenantId));

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
        return Paths.get(properties.getStoragePath(), relativeKey).toFile();
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

    private void recordAudit(String tenantId, Long jobId, Long operatorId, String operatorName,
                             String action, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("DATA_JOB");
        event.setEntityType("IMPORT".equals(action) || "EXPORT".equals(action) ? action : "EXPORT");
        event.setEntityId(String.valueOf(jobId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }
}
