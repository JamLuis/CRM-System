import { request } from '@umijs/max';

/** 查询客户的联系人列表 */
export async function getContactsByCustomer(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Contact[]>>(
    `/api/crm/v1/contacts/by-customer/${customerId}`,
    { method: 'GET', ...(options || {}) },
  );
}

/** 查询联系人详情 */
export async function getContact(contactId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Contact>>(`/api/crm/v1/contacts/${contactId}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 创建联系人 */
export async function addContact(data: API.Crm.Contact, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Contact>>('/api/crm/v1/contacts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 编辑联系人（须携带 contactId 和 version） */
export async function updateContact(data: API.Crm.Contact, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Contact>>('/api/crm/v1/contacts', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 停用联系人 */
export async function deactivateContact(contactId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.Contact>>(`/api/crm/v1/contacts/${contactId}/deactivate`, {
    method: 'POST',
    ...(options || {}),
  });
}
