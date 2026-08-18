import { request } from '@umijs/max';

// ==================== 人员搜索（钉钉/系统用户） ====================

/** 按关键字搜索系统用户 */
export async function searchUsers(keyword: string, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.SysUserItem[]>>('/api/crm/v1/users/search', {
    method: 'GET',
    params: { keyword },
    ...(options || {}),
  });
}

/** 查询部门下用户 */
export async function getUsersByDept(deptId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.SysUserItem[]>>('/api/crm/v1/users/dept', {
    method: 'GET',
    params: { deptId },
    ...(options || {}),
  });
}

/** 递归查询部门及子部门下用户 */
export async function getUsersByDeptRecursive(deptId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.SysUserItem[]>>('/api/crm/v1/users/dept/recursive', {
    method: 'GET',
    params: { deptId },
    ...(options || {}),
  });
}

/** 查询用户信息 */
export async function getUserById(userId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.SysUserItem>>('/api/crm/v1/users/info', {
    method: 'GET',
    params: { userId },
    ...(options || {}),
  });
}

/** 刷新钉钉用户信息（管理员） */
export async function refreshDingTalkUser(dingtalkUserId: string, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.SysUserItem>>('/api/crm/v1/users/refresh', {
    method: 'GET',
    params: { dingtalkUserId },
    ...(options || {}),
  });
}

// ==================== Outbox 死信 ====================

/** 查询死信列表 */
export async function getDeadLetters(options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.OutboxDeadLetter[]>>('/api/crm/v1/outbox/dead', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 重放死信 */
export async function replayDeadLetter(id: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.OutboxDeadLetter>>(`/api/crm/v1/outbox/dead/${id}/replay`, {
    method: 'PUT',
    ...(options || {}),
  });
}

// ==================== 钉钉免登 ====================

/** 钉钉免登码换取身份 */
export async function exchangeDingTalkAuth(authCode: string, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.DingTalkExchangeResult>>(
    '/api/crm/v1/dingtalk/auth/exchange',
    { method: 'POST', params: { authCode }, ...(options || {}) },
  );
}

/** 获取钉钉应用配置（corpId/agentId） */
export async function getDingTalkConfig(options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.DingTalkConfig>>('/api/crm/v1/dingtalk/config', {
    method: 'POST',
    ...(options || {}),
  });
}

// ==================== 客户动态（时间线） ====================

/** 查询客户动态时间线（只读） */
export async function getCustomerTimeline(customerId: number, options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.CustomerTimeline[]>>(
    `/api/crm/v1/customers/${customerId}/timeline`,
    { method: 'GET', ...(options || {}) },
  );
}
