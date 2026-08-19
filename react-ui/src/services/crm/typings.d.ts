/* eslint-disable */
// CRM 领域类型定义 — 与后端 ruoyi-modules/ruoyi-crm 实体字段对齐
// 后端统一返回 RuoYi R<T> = { code: number; msg: string; data: T }

declare namespace API.Crm {
  /** RuoYi 统一响应体 */
  interface R<T = any> {
    code: number;
    msg: string;
    data?: T;
  }

  // ==================== 客户 ====================

  /** 客户实体（对应 CrmCustomer） */
  interface Customer {
    customerId?: number;
    customerCode?: string;
    name?: string;
    activeNameKey?: string;
    addressProvince?: string;
    addressCity?: string;
    addressDistrict?: string;
    addressStreet?: string;
    addressDetail?: string;
    /** 客户标签（JSON 数组字符串） */
    tags?: string;
    /** 生命周期阶段 */
    lifecycleStage?: string;
    /** 经营状态：正常/暂停跟进/已失效/已归档 */
    operatingStatus?: string;
    stageChangeReason?: string;
    statusChangeReason?: string;
    plannedResumeAt?: string;
    /** 重要程度：一般/重要/非常重要 */
    importance?: string;
    source?: string;
    industry?: string;
    remark?: string;
    primaryOwnerId?: number;
    primaryOwnerName?: string;
    collaboratorIds?: string;
    creatorDeptId?: number;
    ownerDeptId?: number;
    nextFollowUpAt?: string;
    lastEffectiveFollowUpAt?: string;
    archivedAt?: string;
    /** 跟进状态：NORMAL/INSUFFICIENT/SEVERE_INSUFFICIENT/NOT_ASSESSED */
    followUpStatus?: string;
    followUpStatusCalculatedAt?: string;
    /** 乐观锁版本号 */
    version?: number;
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: string;
  }

  interface CustomerListParams {
    name?: string;
    operatingStatus?: string;
    lifecycleStage?: string;
    importance?: string;
    source?: string;
    industry?: string;
  }

  // ==================== 联系人 ====================

  interface Contact {
    contactId?: number;
    customerId?: number;
    name?: string;
    /** 电话类型：手机/座机/其他 */
    phoneType?: string;
    countryCode?: string;
    phoneNumber?: string;
    /** 脱敏号码（展示用） */
    phoneMasked?: string;
    email?: string;
    emailMasked?: string;
    wechatId?: string;
    wechatMasked?: string;
    responsibility?: string;
    title?: string;
    isDecisionMaker?: boolean;
    remark?: string;
    /** 状态：有效/已停用 */
    status?: string;
    version?: number;
    createTime?: string;
  }

  // ==================== 成员与归属 ====================

  interface CustomerOwner {
    id?: number;
    customerId?: number;
    userId?: number;
    userName?: string;
    /** 角色类型：PRIMARY/COLLABORATOR */
    roleType?: string;
    status?: string;
  }

  interface OwnerChange {
    id?: number;
    customerId?: number;
    changeType?: string;
    previousPrimaryOwnerId?: number;
    previousPrimaryOwnerName?: string;
    targetPrimaryOwnerId?: number;
    targetPrimaryOwnerName?: string;
    addedCollaboratorIds?: string;
    removedCollaboratorIds?: string;
    keepPreviousAsCollaborator?: boolean;
    reason?: string;
    operatorId?: number;
    operatorName?: string;
    createTime?: string;
  }

  interface TransferRequest {
    targetOwnerId: number;
    targetOwnerName: string;
    targetOwnerDeptId?: number;
    keepPreviousAsCollaborator?: boolean;
    reason?: string;
  }

  interface CollaboratorRequest {
    userId: number;
    userName: string;
  }

  // ==================== 跟踪 ====================

  interface FollowUp {
    followUpId?: number;
    customerId?: number;
    /** 方式：电话/面谈/微信/邮件/其他 */
    method?: string;
    followUpAt?: string;
    content?: string;
    hasNewSigningProject?: boolean;
    outcome?: string;
    nextAction?: string;
    nextFollowUpAt?: string;
    noNextFollowUpReason?: string;
    correctionOfFollowUpId?: number;
    correctionReason?: string;
    isCorrected?: boolean;
    isVoided?: boolean;
    voidedReason?: string;
    createdBy?: number;
    createdByName?: string;
    immutableAt?: string;
    createTime?: string;
  }

  interface FollowUpCreateRequest {
    followUp: FollowUp;
    contactIds?: number[];
    attachmentIds?: number[];
  }

  interface FollowUpCorrectRequest {
    followUp: FollowUp;
    contactIds?: number[];
    attachmentIds?: number[];
    correctionReason: string;
  }

  interface FollowUpVoidRequest {
    voidedReason: string;
  }

  // ==================== 附件 ====================

  interface Attachment {
    attachmentId?: number;
    /** 业务对象类型：FOLLOW_UP/CUSTOMER */
    ownerType?: string;
    ownerId?: number;
    fileName?: string;
    contentType?: string;
    sizeBytes?: number;
    storageKey?: string;
    checksum?: string;
    uploadedBy?: number;
    uploadedByName?: string;
    /** 状态：PENDING_SCAN/SCANNING/AVAILABLE/REJECTED */
    status?: string;
    scanStartedAt?: string;
    scanCompletedAt?: string;
    scanErrorCode?: string;
    createTime?: string;
  }

  // ==================== 提醒计划 ====================

  interface ReminderPlan {
    planId?: number;
    customerId?: number;
    sourceFollowUpId?: number;
    planKey?: string;
    plannedFollowUpAt?: string;
    scheduledAt?: string;
    /** 状态：ACTIVE/CANCELLED/DELIVERED/EXPIRED */
    status?: string;
    createTime?: string;
  }

  // ==================== 提醒投递（我的待办） ====================

  interface ReminderDelivery {
    deliveryId?: number;
    planId?: number;
    customerId?: number;
    planKey?: string;
    plannedFollowUpAt?: string;
    scheduledAt?: string;
    recipientUserId?: number;
    recipientName?: string;
    /** 状态：PENDING/RETRYING/SENT/COMPLETED/CANCELLED/FAILED */
    status?: string;
    retryCount?: number;
    lastAttemptAt?: string;
    lastErrorCode?: string;
    completedAt?: string;
    /** 客户名称（联查展示字段） */
    customerName?: string;
    createTime?: string;
  }

  // ==================== 钉钉免登 ====================

  interface DingTalkLoginResult {
    /** MAPPED=已映射（已签发会话）；PENDING_ACTIVATION=待激活 */
    status?: string;
    dingtalkUserId?: string;
    unionId?: string;
    sysUserId?: number;
    access_token?: string;
    expires_in?: number;
  }

  // ==================== 健康度策略 ====================

  interface FollowUpStatusStrategy {
    strategyId?: number;
    /** 跟进不足阈值（天） */
    insufficientThreshold?: number;
    /** 严重不足阈值（天） */
    severeThreshold?: number;
    effectiveFrom?: string;
    /** 状态：ACTIVE/SUPERSEDED */
    status?: string;
    createTime?: string;
  }

  // ==================== 客户动态（时间线） ====================

  interface CustomerTimeline {
    id?: number;
    customerId?: number;
    eventType?: string;
    /** 事件数据（JSON 字符串） */
    eventData?: string;
    operatorId?: number;
    operatorName?: string;
    createTime?: string;
  }

  // ==================== 人员搜索 ====================

  interface SysUserItem {
    userId?: number;
    userName?: string;
    nickName?: string;
    phonenumber?: string;
    deptId?: number;
    dept?: { deptId?: number; deptName?: string };
    status?: string;
    dingtalkUserId?: string;
  }

  // ==================== Outbox 死信 ====================

  interface OutboxDeadLetter {
    id?: number;
    tenantId?: string;
    topic?: string;
    payload?: string;
    status?: string;
    retryCount?: number;
    version?: number;
    createTime?: string;
  }

  // ==================== 钉钉免登 ====================

  interface DingTalkExchangeResult {
    dingtalkUserId?: string;
    unionId?: string;
    sysUserId?: number;
    /** MAPPED / PENDING_ACTIVATION */
    status?: string;
  }

  interface DingTalkConfig {
    corpId?: string;
    agentId?: string;
  }
}
