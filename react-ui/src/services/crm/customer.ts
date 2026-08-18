import { request } from '@umijs/max';

// ==================== 客户 ====================

/** 查询客户列表（按当前用户数据范围） */
export async function getCustomerList(
  params?: API.Crm.CustomerListParams,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer[]>>('/api/crm/v1/customers', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 查询全部客户（仅管理员） */
export async function getAllCustomers(
  params?: API.Crm.CustomerListParams,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer[]>>('/api/crm/v1/customers/all', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 查询客户详情 */
export async function getCustomer(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Customer>>(`/api/crm/v1/customers/${customerId}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 创建客户 */
export async function addCustomer(
  data: API.Crm.Customer,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>('/api/crm/v1/customers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 编辑客户核心字段（须携带 customerId 和 version） */
export async function updateCustomer(
  data: API.Crm.Customer,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>('/api/crm/v1/customers', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

// ==================== 客户经营状态 ====================

/** 暂停跟进 */
export async function pauseCustomer(
  customerId: number,
  reason?: string,
  plannedResumeAt?: string,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>(`/api/crm/v1/customers/${customerId}/status/pause`, {
    method: 'POST',
    params: { reason, plannedResumeAt },
    ...(options || {}),
  });
}

/** 恢复跟进 */
export async function resumeCustomer(
  customerId: number,
  reason?: string,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>(`/api/crm/v1/customers/${customerId}/status/resume`, {
    method: 'POST',
    params: { reason },
    ...(options || {}),
  });
}

/** 设为已失效 */
export async function invalidateCustomer(
  customerId: number,
  reason?: string,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>(
    `/api/crm/v1/customers/${customerId}/status/invalidate`,
    { method: 'POST', params: { reason }, ...(options || {}) },
  );
}

/** 归档客户 */
export async function archiveCustomer(
  customerId: number,
  reason?: string,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>(
    `/api/crm/v1/customers/${customerId}/status/archive`,
    { method: 'POST', params: { reason }, ...(options || {}) },
  );
}

/** 恢复归档客户（仅管理员） */
export async function restoreArchivedCustomer(
  customerId: number,
  reason?: string,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>(
    `/api/crm/v1/customers/${customerId}/status/restore-archive`,
    { method: 'POST', params: { reason }, ...(options || {}) },
  );
}

/** 恢复失效客户（销售主管或管理员） */
export async function restoreInvalidCustomer(
  customerId: number,
  reason?: string,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.Customer>>(
    `/api/crm/v1/customers/${customerId}/status/restore-invalid`,
    { method: 'POST', params: { reason }, ...(options || {}) },
  );
}

// ==================== 客户成员与移交 ====================

/** 查询客户成员列表 */
export async function getCustomerOwners(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.CustomerOwner[]>>(
    `/api/crm/v1/customers/${customerId}/owners`,
    { method: 'GET', ...(options || {}) },
  );
}

/** 移交主负责人 */
export async function transferOwner(
  customerId: number,
  data: API.Crm.TransferRequest,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.OwnerChange>>(
    `/api/crm/v1/customers/${customerId}/owners/transfer`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json;charset=UTF-8' },
      data,
      ...(options || {}),
    },
  );
}

/** 新增协同人 */
export async function addCollaborator(
  customerId: number,
  data: API.Crm.CollaboratorRequest,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.OwnerChange>>(
    `/api/crm/v1/customers/${customerId}/owners/collaborators`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json;charset=UTF-8' },
      data,
      ...(options || {}),
    },
  );
}

/** 移除协同人 */
export async function removeCollaborator(
  customerId: number,
  collaboratorId: number,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.OwnerChange>>(
    `/api/crm/v1/customers/${customerId}/owners/collaborators/${collaboratorId}`,
    { method: 'DELETE', ...(options || {}) },
  );
}

/** 查询负责人变更历史 */
export async function getOwnerChangeHistory(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.OwnerChange[]>>(
    `/api/crm/v1/customers/${customerId}/owners/changes`,
    { method: 'GET', ...(options || {}) },
  );
}
