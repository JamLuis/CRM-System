import { request } from '@umijs/max';

/** 查询客户的跟进记录列表 */
export async function getFollowUpsByCustomer(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.FollowUp[]>>(
    `/api/crm/v1/follow-ups/by-customer/${customerId}`,
    { method: 'GET', ...(options || {}) },
  );
}

/** 查询跟进详情 */
export async function getFollowUp(followUpId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.FollowUp>>(`/api/crm/v1/follow-ups/${followUpId}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 新增跟进 */
export async function createFollowUp(
  data: API.Crm.FollowUpCreateRequest,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.FollowUp>>('/api/crm/v1/follow-ups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 更正跟进（原记录不可变，生成更正记录） */
export async function correctFollowUp(
  followUpId: number,
  data: API.Crm.FollowUpCorrectRequest,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.FollowUp>>(`/api/crm/v1/follow-ups/${followUpId}/correct`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 作废跟进 */
export async function voidFollowUp(
  followUpId: number,
  data: API.Crm.FollowUpVoidRequest,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.FollowUp>>(`/api/crm/v1/follow-ups/${followUpId}/void`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}
