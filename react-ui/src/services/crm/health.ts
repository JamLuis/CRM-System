import { request } from '@umijs/max';

/** 计算单个客户跟进健康度，返回状态码 */
export async function calculateFollowUpStatus(customerId: API.Crm.Id, options?: Record<string, any>) {
  return request<API.Crm.R<string>>(`/api/crm/v1/health/calculate/${customerId}`, {
    method: 'POST',
    ...(options || {}),
  });
}

/** 批量重算跟进健康度，返回处理数量 */
export async function recalculateFollowUpStatusBatch(options?: Record<string, any>) {
  return request<API.Crm.R<number>>('/api/crm/v1/health/recalculate-batch', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 保存健康度策略（生成新版本，旧版本 SUPERSEDED） */
export async function saveFollowUpStatusStrategy(
  data: API.Crm.FollowUpStatusStrategy,
  options?: Record<string, any>,
) {
  return request<API.Crm.R<API.Crm.FollowUpStatusStrategy>>('/api/crm/v1/health/strategies', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json;charset=UTF-8' },
    data,
    ...(options || {}),
  });
}

/** 查询当前生效策略 */
export async function getActiveStrategy(options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.FollowUpStatusStrategy>>(
    '/api/crm/v1/health/strategies/active',
    { method: 'GET', ...(options || {}) },
  );
}

/** 查询策略历史列表 */
export async function getStrategies(options?: Record<string, any>) {
  return request<API.Crm.R<API.Crm.FollowUpStatusStrategy[]>>('/api/crm/v1/health/strategies', {
    method: 'GET',
    ...(options || {}),
  });
}
