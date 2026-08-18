import { request } from '@umijs/max';

/** 查询客户的提醒计划列表 */
export async function getRemindersByCustomer(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.ReminderPlan[]>>(
    `/api/crm/v1/reminders/by-customer/${customerId}`,
    { method: 'GET', ...(options || {}) },
  );
}

/** 取消客户的提醒计划 */
export async function cancelRemindersByCustomer(
  customerId: number,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<number>>(`/api/crm/v1/reminders/by-customer/${customerId}/cancel`, {
    method: 'POST',
    ...(options || {}),
  });
}
