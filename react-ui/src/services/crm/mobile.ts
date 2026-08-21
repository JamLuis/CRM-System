import { request } from '@umijs/max';

// ==================== 钉钉 H5 免登 ====================

/** 钉钉免登登录（白名单接口，无需 token）：authCode 换取 CRM 会话 */
export async function dingtalkLogin(authCode: string, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.DingTalkLoginResult>>('/api/crm/v1/dingtalk/auth/login', {
    method: 'POST',
    params: { authCode },
    ...(options || {}),
  });
}

/** 获取钉钉微应用公开配置（corpId/clientId/agentId） */
export async function getDingtalkConfig(options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.DingTalkConfig>>('/api/crm/v1/dingtalk/config', {
    method: 'POST',
    ...(options || {}),
  });
}

// ==================== 我的待办 ====================

/** 查询我的待办列表 */
export async function getMyTodos(options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.ReminderDelivery[]>>('/api/crm/v1/reminders/my-todos', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 完成我的一条待办 */
export async function completeMyTodo(deliveryId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.ReminderDelivery>>(
    `/api/crm/v1/reminders/my-todos/${deliveryId}/complete`,
    { method: 'POST', ...(options || {}) },
  );
}
