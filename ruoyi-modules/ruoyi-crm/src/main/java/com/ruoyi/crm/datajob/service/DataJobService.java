package com.ruoyi.crm.datajob.service;

import com.ruoyi.crm.datajob.domain.CrmDataJob;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * CRM 数据作业服务接口（导入导出）
 * <p>
 * 导入按"上传—校验预览—确认执行"运行并逐行反馈；
 * 导出继承数据权限和脱敏规则，异步生成且短期下载。
 *
 * @author ruoyi-crm
 */
public interface DataJobService
{
    /**
     * 上传导入文件并执行预检（不写业务数据）
     *
     * @param file Excel 文件
     * @param importType 导入对象（CUSTOMER/CONTACT/FOLLOW_UP）
     * @return 预检完成的作业（状态 VALIDATED，rowResults 为逐行结果）
     */
    CrmDataJob uploadImport(MultipartFile file, String importType);

    /**
     * 确认执行导入（仅执行预检通过的行）
     *
     * @param jobId 作业 ID
     * @return 执行完成的作业
     */
    CrmDataJob confirmImport(Long jobId);

    /**
     * 提交导出作业（异步执行，继承当前用户数据范围）
     *
     * @param query 导出查询条件（与页面筛选一致）
     * @return 已提交的作业（状态 PENDING）
     */
    CrmDataJob submitExport(com.ruoyi.crm.customer.domain.CrmCustomer query);

    /**
     * 查询作业列表
     *
     * @param jobType 作业类型（可选）
     * @return 作业列表
     */
    List<CrmDataJob> listJobs(String jobType);

    /**
     * 查询作业详情
     *
     * @param jobId 作业 ID
     * @return 作业
     */
    CrmDataJob detail(Long jobId);

    /**
     * 下载导出文件（校验权限、租户、过期）
     *
     * @param jobId    作业 ID
     * @param response HTTP 响应
     */
    void downloadExport(Long jobId, HttpServletResponse response);
}
