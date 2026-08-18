// CRM 前端枚举常量 — 与后端 ruoyi-crm 枚举值对齐（后端使用中文字符串枚举）

/** 生命周期阶段 */
export const LIFECYCLE_STAGE_ENUM: Record<string, { text: string; status?: string }> = {
  新获取: { text: '新获取', status: 'Default' },
  待跟进: { text: '待跟进', status: 'Processing' },
  初步意向: { text: '初步意向', status: 'Warning' },
  商机客户: { text: '商机客户', status: 'Warning' },
  成交客户: { text: '成交客户', status: 'Success' },
};

/** 经营状态 */
export const OPERATING_STATUS_ENUM: Record<string, { text: string; status?: string }> = {
  正常: { text: '正常', status: 'Success' },
  暂停跟进: { text: '暂停跟进', status: 'Warning' },
  已失效: { text: '已失效', status: 'Error' },
  已归档: { text: '已归档', status: 'Default' },
};

/** 重要程度 */
export const IMPORTANCE_ENUM: Record<string, { text: string }> = {
  一般: { text: '一般' },
  重要: { text: '重要' },
  非常重要: { text: '非常重要' },
};

/** 跟进健康度状态 */
export const FOLLOW_UP_STATUS_ENUM: Record<string, { text: string; color: string }> = {
  NORMAL: { text: '跟进正常', color: 'green' },
  INSUFFICIENT: { text: '跟进不足', color: 'orange' },
  SEVERE_INSUFFICIENT: { text: '严重不足', color: 'red' },
  NOT_ASSESSED: { text: '未评估', color: 'default' },
};

/** 跟踪方式 */
export const FOLLOW_UP_METHOD_ENUM: Record<string, { text: string }> = {
  电话: { text: '电话' },
  面谈: { text: '面谈' },
  微信: { text: '微信' },
  邮件: { text: '邮件' },
  其他: { text: '其他' },
};

/** 联系人电话类型 */
export const PHONE_TYPE_ENUM: Record<string, { text: string }> = {
  手机: { text: '手机' },
  座机: { text: '座机' },
  其他: { text: '其他' },
};

/** 提醒计划状态 */
export const REMINDER_STATUS_ENUM: Record<string, { text: string; color: string }> = {
  ACTIVE: { text: '待执行', color: 'processing' },
  CANCELLED: { text: '已取消', color: 'default' },
  DELIVERED: { text: '已投递', color: 'success' },
  EXPIRED: { text: '已过期', color: 'warning' },
};

/** 成员角色类型 */
export const OWNER_ROLE_TYPE_ENUM: Record<string, { text: string; color: string }> = {
  PRIMARY: { text: '主负责人', color: 'blue' },
  COLLABORATOR: { text: '协同人', color: 'default' },
};

/** 负责人变更类型 */
export const OWNER_CHANGE_TYPE_ENUM: Record<string, { text: string }> = {
  TRANSFER: { text: '移交' },
  ADD_COLLABORATOR: { text: '新增协同人' },
  REMOVE_COLLABORATOR: { text: '移除协同人' },
};

/** 附件状态 */
export const ATTACHMENT_STATUS_ENUM: Record<string, { text: string; color: string }> = {
  PENDING_SCAN: { text: '待扫描', color: 'default' },
  SCANNING: { text: '扫描中', color: 'processing' },
  AVAILABLE: { text: '可用', color: 'success' },
  REJECTED: { text: '已拒绝', color: 'error' },
};

/** 客户动态事件类型 */
export const TIMELINE_EVENT_TYPE_ENUM: Record<string, { text: string }> = {
  CUSTOMER_CREATED: { text: '创建客户' },
  CUSTOMER_UPDATED: { text: '编辑客户' },
  STATUS_CHANGED: { text: '状态变更' },
  OWNER_TRANSFERRED: { text: '负责人移交' },
  COLLABORATOR_ADDED: { text: '新增协同人' },
  COLLABORATOR_REMOVED: { text: '移除协同人' },
  FOLLOW_UP_CREATED: { text: '新增跟踪' },
  FOLLOW_UP_CORRECTED: { text: '跟踪更正' },
  FOLLOW_UP_VOIDED: { text: '跟踪作废' },
  CONTACT_CREATED: { text: '新增联系人' },
  CONTACT_UPDATED: { text: '编辑联系人' },
  CONTACT_DEACTIVATED: { text: '停用联系人' },
};
