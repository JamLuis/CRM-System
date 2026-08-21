import { request } from '@umijs/max';

// ==================== 数据作业（导入导出） ====================

/** 上传导入文件并预检 */
export async function uploadImport(
  file: File,
  importType: API.Crm.DataImportType = 'CUSTOMER',
  options?: Record<string, any>,
) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('importType', importType);
  return request<API.Crm.R<API.Crm.CrmDataJob>>('/api/crm/v1/data-jobs/imports', {
    method: 'POST',
    data: formData,
    ...(options || {}),
  });
}

/** 确认执行导入 */
export async function confirmImport(jobId: API.Crm.Id, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.CrmDataJob>>(
    `/api/crm/v1/data-jobs/imports/${jobId}/confirm`,
    {
      method: 'POST',
      ...(options || {}),
    },
  );
}

/** 提交导出作业（异步，继承当前页面筛选条件） */
export async function submitExport(
  data?: Partial<API.Crm.Customer>,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.CrmDataJob>>('/api/crm/v1/data-jobs/exports', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data: data || {},
    ...(options || {}),
  });
}

/** 查询作业列表 */
export async function listDataJobs(jobType?: API.Crm.DataJobType, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.CrmDataJob[]>>('/api/crm/v1/data-jobs', {
    method: 'GET',
    params: jobType ? { jobType } : {},
    ...(options || {}),
  });
}

/** 查询作业详情 */
export async function getDataJob(jobId: API.Crm.Id, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.CrmDataJob>>(`/api/crm/v1/data-jobs/${jobId}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 导出文件下载地址（用于 window.open / a 标签） */
export function getExportDownloadUrl(jobId: API.Crm.Id) {
  return `/api/crm/v1/data-jobs/exports/${jobId}/download`;
}
