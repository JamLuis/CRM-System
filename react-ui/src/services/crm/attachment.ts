import { request } from '@umijs/max';

/** 申请预签名上传 */
export async function preSignUpload(
  data: {
    ownerType: string;
    ownerId?: number | string;
    fileName: string;
    contentType: string;
    sizeBytes: number;
    checksum?: string;
  },
  options?: Record<string, any>,
) {
  return request<API.Crm.R<any>>('/api/crm/v1/attachments/pre-sign', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 确认上传完成 */
export async function confirmUpload(attachmentId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Attachment>>(
    `/api/crm/v1/attachments/${attachmentId}/confirm`,
    { method: 'POST', ...(options || {}) },
  );
}

/** 获取附件下载地址（返回 302 或下载 URL，按后端实现） */
export function getAttachmentDownloadUrl(attachmentId: number) {
  return `/api/crm/v1/attachments/${attachmentId}/download`;
}

/** 按业务对象查询附件列表 */
export async function getAttachmentsByOwner(
  params: { ownerType: string; ownerId: number },
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Attachment[]>>('/api/crm/v1/attachments/by-owner', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}
