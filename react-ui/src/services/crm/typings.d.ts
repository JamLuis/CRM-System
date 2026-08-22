/* eslint-disable */
// CRM 领域类型定义 — 与后端 ruoyi-modules/ruoyi-crm 实体字段对齐
// 后端统一返回 RuoYi R<T> = { code: number; msg: string; data: T }

declare namespace API.Crm {
  /** 后端雪花 ID 超出 JavaScript 安全整数范围，统一按字符串传输。 */
  type Id = string | number;

  /** RuoYi 统一响应体 */
  interface R<T = any> {
    code: number;
    msg: string;
    data?: T;
  }

  // ==================== 客户 ====================

  /** 客户实体（对应 CrmCustomer） */
  interface Customer {
    customerId?: Id;
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
    /** 跟进力度（导入源字段） */
    followUpIntensity?: string;
    /** 导入源客户跟进状态 */
    sourceFollowUpStatus?: string;
    customerGroup?: string;
    /** 导入源客户状态 */
    sourceCustomerStatus?: string;
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
    referredCustomerName?: string;
    sourceOther?: string;
    industry?: string;
    industryOther?: string;
    sourceCreatorName?: string;
    sourceOwnerName?: string;
    sourceCollaboratorNames?: string;
    remark?: string;
    primaryOwnerId?: number;
    primaryOwnerName?: string;
    collaboratorIds?: string;
    creatorDeptId?: number;
    ownerDeptId?: number;
    nextFollowUpAt?: string;
    lastEffectiveFollowUpAt?: string;
    archivedAt?: string;
    droppedProtectionAt?: string;
    /** 跟进状态：NORMAL/INSUFFICIENT/SEVERE_INSUFFICIENT/NOT_ASSESSED */
    followUpStatus?: string;
    followUpStatusCalculatedAt?: string;
    followUpRecordCount?: number;
    collaboratorNames?: string;
    latestActivityAt?: string;
    latestVisitAt?: string;
    latestAssignmentAt?: string;
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
    contactId?: Id;
    sourceDataId?: string;
    customerId?: Id;
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
    sourceOwnerNames?: string;
    sourceCollaboratorNames?: string;
    version?: number;
    createTime?: string;
  }

  // ==================== 成员与归属 ====================

  interface CustomerOwner {
    id?: number;
    customerId?: Id;
    userId?: number;
    userName?: string;
    /** 角色类型：PRIMARY/COLLABORATOR */
    roleType?: string;
    status?: string;
  }

  interface OwnerChange {
    id?: number;
    customerId?: Id;
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
    followUpId?: Id;
    sourceDataId?: string;
    customerId?: Id;
    /** 方式：电话/面谈/微信/邮件/其他 */
    method?: string;
    followUpAt?: string;
    content?: string;
    hasNewSigningProject?: boolean;
    outcome?: string;
    nextAction?: string;
    nextFollowUpAt?: string;
    noNextFollowUpReason?: string;
    correctionOfFollowUpId?: Id;
    correctionReason?: string;
    isCorrected?: boolean;
    isVoided?: boolean;
    voidedReason?: string;
    createdBy?: number;
    createdByName?: string;
    immutableAt?: string;
    sourceContactNames?: string;
    sourceAttachmentRefs?: string;
    sourceIsKeyCustomer?: boolean;
    sourceCreatorDept?: string;
    sourceApprovalTitle?: string;
    sourceOwnerNames?: string;
    sourceCollaboratorNames?: string;
    createTime?: string;
  }

  interface FollowUpCreateRequest {
    followUp: FollowUp;
    contactIds?: Id[];
    attachmentIds?: Id[];
  }

  interface FollowUpCorrectRequest {
    followUp: FollowUp;
    contactIds?: Id[];
    attachmentIds?: Id[];
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
    uploadUrl?: string;
    downloadUrl?: string;
    createTime?: string;
  }

  // ==================== 提醒计划 ====================

  interface ReminderPlan {
    planId?: number;
    customerId?: Id;
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
    deliveryId?: Id;
    planId?: Id;
    customerId?: Id;
    planKey?: string;
    plannedFollowUpAt?: string;
    scheduledAt?: string;
    recipientUserId?: Id;
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
    customerId?: Id;
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

  interface DingTalkDirectoryUser {
    id?: number;
    dingtalkUserId: string;
    name?: string;
    mobile?: string;
    email?: string;
    title?: string;
    deptIds?: string;
    deptNames?: string;
    sysDeptId?: number;
    active?: boolean;
    lastSyncTime?: string;
    accessGranted?: boolean;
    sysUserId?: number;
    /** 逗号分隔的角色 ID，由列表聚合查询返回 */
    roleIds?: string;
    roleNames?: string;
    permissionCodes?: string;
  }

  interface CrmRoleOption {
    roleId: number;
    roleName: string;
    roleKey: string;
    roleSort?: number;
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
    /** 钉钉应用 AppKey，新版 requestAuthCode 用于绑定授权码所属应用 */
    clientId?: string;
    agentId?: string;
  }

  // ==================== 组织同步 ====================

  interface OrgSyncCursorInfo {
    source?: string;
    cursor?: string;
    lastSyncTime?: string;
    status?: string;
  }

  interface OrgSyncResult {
    deptCount?: number;
    userCount?: number;
    userUpdated?: number;
    userDeactivated?: number;
    success?: boolean;
    error?: string;
  }

  // ==================== 钉钉身份映射 ====================

  interface CrmDingtalkIdentity {
    id?: number;
    dingtalkUserId?: string;
    sysUserId?: number;
    unionId?: string;
    createTime?: string;
    updateTime?: string;
  }

  // ==================== 角色数据范围 ====================

  /** 范围类型：ALL / DEPT / SELF_CREATED_OR_MEMBER */
  interface CrmRoleScope {
    id?: number;
    roleId?: number;
    scopeType?: string;
    createTime?: string;
  }

  // ==================== 数据作业（导入导出） ====================

  /** 作业类型：IMPORT / EXPORT */
  type DataJobType = 'IMPORT' | 'EXPORT';

  /** 导入对象类型 */
  type DataImportType = 'CUSTOMER' | 'CONTACT' | 'FOLLOW_UP';

  /** 作业状态：PENDING / RUNNING / VALIDATED / SUCCESS / FAILED / EXPIRED */
  type DataJobStatus = 'PENDING' | 'RUNNING' | 'VALIDATED' | 'SUCCESS' | 'FAILED' | 'EXPIRED';

  /** 导入逐行结果 */
  interface ImportRowResult {
    rowNum?: number;
    name?: string;
    valid?: boolean;
    message?: string;
    /** SUCCESS / FAILED / SKIPPED */
    result?: string;
    customerId?: Id;
  }

  /** 数据作业（对应 CrmDataJob） */
  interface CrmDataJob {
    jobId?: Id;
    tenantId?: string;
    jobType?: DataJobType;
    importType?: DataImportType;
    status?: DataJobStatus;
    fileName?: string;
    storageKey?: string;
    queryCondition?: string;
    totalCount?: number;
    successCount?: number;
    failedCount?: number;
    rowResults?: string;
    expireTime?: string;
    operatorId?: number;
    operatorName?: string;
    errorMsg?: string;
    startTime?: string;
    finishTime?: string;
    createBy?: string;
    createTime?: string;
    updateTime?: string;
  }
}
