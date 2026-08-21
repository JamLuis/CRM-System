package com.ruoyi.crm.datajob.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.datajob.domain.CrmDataJob;
import com.ruoyi.crm.datajob.service.DataJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 数据作业接口（导入导出）
 * <p>
 * 导入：上传预检 → 确认执行，逐行反馈；导出：异步生成，短期下载。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/data-jobs")
public class DataJobController
{
    @Autowired
    private DataJobService dataJobService;

    /**
     * 上传导入文件并预检
     *
     * @param file Excel 文件
     * @return 预检作业（含逐行结果）
     */
    @PostMapping("/imports")
    public R<CrmDataJob> uploadImport(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "importType", defaultValue = "CUSTOMER") String importType)
    {
        return R.ok(dataJobService.uploadImport(file, importType));
    }

    /**
     * 确认执行导入
     *
     * @param jobId 作业 ID
     * @return 执行完成的作业
     */
    @PostMapping("/imports/{jobId}/confirm")
    public R<CrmDataJob> confirmImport(@PathVariable Long jobId)
    {
        return R.ok(dataJobService.confirmImport(jobId));
    }

    /**
     * 提交导出作业（异步）
     *
     * @param query 导出查询条件（与页面筛选一致）
     * @return 已提交的作业
     */
    @PostMapping("/exports")
    public R<CrmDataJob> submitExport(@RequestBody(required = false) CrmCustomer query)
    {
        return R.ok(dataJobService.submitExport(query == null ? new CrmCustomer() : query));
    }

    /**
     * 查询作业列表
     *
     * @param jobType 作业类型（可选：IMPORT / EXPORT）
     * @return 作业列表
     */
    @GetMapping
    public R<List<CrmDataJob>> list(@RequestParam(required = false) String jobType)
    {
        return R.ok(dataJobService.listJobs(jobType));
    }

    /**
     * 查询作业详情
     *
     * @param jobId 作业 ID
     * @return 作业详情
     */
    @GetMapping("/{jobId}")
    public R<CrmDataJob> detail(@PathVariable Long jobId)
    {
        return R.ok(dataJobService.detail(jobId));
    }

    /**
     * 下载导出文件
     *
     * @param jobId    作业 ID
     * @param response HTTP 响应
     */
    @GetMapping("/exports/{jobId}/download")
    public void downloadExport(@PathVariable Long jobId, HttpServletResponse response)
    {
        dataJobService.downloadExport(jobId, response);
    }
}
