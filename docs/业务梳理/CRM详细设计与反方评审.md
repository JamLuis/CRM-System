# CRM V1 详细设计与反方评审

> 开发范围严格以 [CRM系统框架设计稿_简化版.md](CRM系统框架设计稿_简化版.md) 为准。本文将该简化版落为可开发的功能、数据和接口基线。日期：2026-08-18。

## 1. 范围冻结

V1 是客户经营基础闭环：

`客户录入 → 负责人/协同人归属 → 联系人与跟踪 → 下次跟踪提醒 → 客户经营状态流转 → 审计追溯`

| V1 交付 | 明确不交付（后续版本） |
| --- | --- |
| 客户、联系人、主负责人/协同人、归属变更 | 商机、报价、立项、钉钉审批 |
| 跟踪记录、附件、提醒、跟踪健康度 | 合同、订单、出库、项目、验收、回款、开票 |
| 钉钉免登、组织同步、工作通知 | 客户群、外部联系人、营销自动化 |
| RuoYi 权限整合、审计、导入导出 | 销售漏斗和财务/履约报表 |

`需求说明.md`和完整设计稿中超出此范围的内容只作为二期备忘；V1 不创建预留空表、空接口或菜单。`业务实体.md` 是 V1 的字段和字典来源：本文只补足其中缺失的约束、表关系和实现规则，不另造一套业务字典。

## 2. 严格反方评审

| 编号 | 不足/冲突 | 直接开发的风险 | 冻结修复 |
| --- | --- | --- | --- |
| R1 | 简化稿与其他文档的范围不一致 | 商机、审批会在 V1 被误实现 | 第 1 章为唯一 V1 范围；二期重新立项评审 |
| R2 | 客户有主负责人、协同人字段，又定义 `OwnerAssignment`，但没有真源 | 无法保证一名主负责人；移交历史、权限都可能错 | `crm_customer_owner` 是归属真源；客户表仅冗余 `primary_owner_id` 供查询 |
| R3 | `OwnerChange` 只描述结果数组，未定义有效期、并发和唯一约束 | 两次移交可产生两个主负责人或丢成员 | 移交以客户版本+成员行锁原子执行；数据库约束当前主负责人唯一 |
| R4 | 角色描述没有服务端判定算法，且未来模块需要可扩展授权 | 前端藏按钮仍可越权；新增模块时会重建权限体系 | 固定顺序：租户 → RuoYi 菜单权限 → 资源动作数据范围 → 客户状态；角色与范围分离配置 |
| R5 | `OrganizationUser/RoleAssignment` 会与 RuoYi 系统表双写 | 离职、调岗和角色撤销出现两套答案 | `sys_user/sys_dept/sys_role` 为权限事实源；CRM 仅存钉钉身份映射与客户成员 |
| R6 | 名称重复没有规范化、强制约束和幂等规则 | 同名客户可重复、重试会多建 | 规范名称精确命中即拒绝创建；数据库唯一约束兜底，不提供重复创建审核 |
| R7 | 提醒任务没有区分计划与实际投递 | 改期或多协同人会重复发送/互相覆盖 | `reminder_plan` 管业务计划，`reminder_delivery` 管每接收人每版本的一次发送 |
| R8 | 电话/微信需要截图，但附件有扫描状态 | 恶意文件可关联，或扫描中随机提交失败 | 文件先隔离；只有 `AVAILABLE` 图片能提交电话/微信跟踪 |
| R9 | 不可变审计和客户动态没有事务、投影幂等定义 | 业务成功却无审计，重试产生重复动态 | 业务变更、审计、outbox 同事务；动态以 `audit_event_id` 唯一投影，可重建 |
| R10 | 钉钉仅写“同步/重试”，没有租户凭据、游标、入站与出站幂等 | 漏同步、重复消息、外部超时不一致 | 增加租户配置、同步游标、outbox、死信和幂等键；V1 不接审批回调 |
| R11 | 恢复暂停/失效/归档后没有下次跟踪计划规则 | 恢复后的提醒和健康度无法解释 | 恢复时下一次跟踪计划可选；未填则不建提醒，页面标记“待计划”，直到后续新增计划或跟踪 |
| R12 | 保留 5 年与“不物理删除”不区分 | 易被错误自动清理或无限膨胀 | V1 只承诺至少保留 5 年，不启用自动删除；清理另立合规需求 |

补充决策：客户可先创建但首次有效跟踪前必须有联系人；已提交跟踪不能直接改，采用“作废并重提”；提醒 `SENT` 只表示通知成功，不代表业务跟踪已完成；健康度除正常/不足/严重不足外增加 `NOT_ASSESSED`，用于恢复后未填计划等不纳入考核的状态。

## 3. 基于 RuoYi React 的技术方案

使用用户指定的 [whiteshader/ruoyi-react](https://gitee.com/whiteshader/ruoyi-react) 作为脚手架；上线项目必须锁定经过评审的 tag/commit，禁止直接跟随 `master`。仓库发行信息显示 v3.2 使用 React 19 与 Ant Design 6，v3.1 已升级 JDK 17、Spring Boot 3；创建工程时应按选定 tag 进行依赖、许可证和漏洞扫描并提交锁文件。 [发行版](https://gitee.com/whiteshader/ruoyi-react/releases)

| 层 | 方案 | 边界 |
| --- | --- | --- |
| 前端 | 复用 RuoYi React 的路由、菜单、权限、表格与表单；新增 `src/pages/crm`、`src/services/crm` | H5 与 PC 复用领域 API；前端不能裁决权限 |
| 后端 | Spring Boot 3/Java 17 模块化单体：`crm-core`、`crm-web`、`crm-job`、`crm-dingtalk-adapter` | controller 不直接调用钉钉 SDK |
| 数据 | MySQL 8 做事务关系数据；Redis 做缓存、短锁和幂等辅助；私有对象存储放附件 | 协同人、联系人、权限关系不能存 JSON/数组 |
| 异步 | RuoYi 调度运行批任务；`crm_outbox` 可靠投递消息、动态投影和同步任务 | 不用扫表补偿代替事务 |

RuoYi 的用户、部门、角色、菜单、数据权限仍由 `sys_*` 表负责；CRM 只增加对象级授权和领域审计，不能维护第二套用户与角色事实来源。

## 4. 功能与权限设计

### 4.1 页面与权限

| 菜单 | PC | 钉钉 H5 | 权限码示例 |
| --- | --- | --- | --- |
| 客户工作台 | 列表、筛选、新建、导出 | 我的客户、搜索、详情 | `crm:customer:list/create/export` |
| 客户 360 | 概览、联系人、成员、跟踪、提醒、动态 | 概览、联系人、快速跟踪、动态 | `crm:customer:query/update` |
| 跟踪待办 | 健康度列表、部门督办、提醒异常 | 我的待办、改期、完成、快速跟踪 | `crm:followup:create` |
| 归属管理 | 分配、移交、协同人 | 主负责人仅维护协同人 | `crm:customer:assign`, `crm:owner:manage` |
| 后台 | 钉钉选人、系统访问/客户管理授权、同步、策略、标签、作业、审计 | 不提供 | `crm:access`, `crm:admin:grant`, `crm:config:*`, `crm:audit:query` |

权限分为三层，后台管理员从钉钉部门/姓名搜索已同步人员后，向 RuoYi 用户授予角色和范围；搜索只是选择体验，最终权限以本地同步身份和角色为准。

| 层 | 规则 | 为后续模块预留 |
| --- | --- | --- |
| 系统访问 | `crm:access` 决定是否可进入 CRM 菜单；无此权限即使是钉钉在职人员也不能进入 | 新模块增加自己的菜单和访问码，不修改用户身份模型 |
| 功能动作 | `crm:customer:create/update/assign`、`crm:contact:*`、`crm:followup:*` 等由 RuoYi `sys_menu + sys_role + sys_user_role` 授予 | 权限码统一为 `{module}:{resource}:{action}`；新增模块可独立加资源和动作 |
| 数据范围 | 角色通过 `crm_role_scope` 配置 `SELF_CREATED_OR_MEMBER`、`DEPARTMENT` 或 `ALL` | 范围记录含 `module_code/resource_code/action_code`，后续模块可复用，不与角色表双写 |

销售人员的客户可见范围固定为：`customer.created_by = 当前用户` **或** 当前有效 `crm_customer_owner` 中存在该用户（主负责人或协同人）。因此协助销售只能看被协助分配的客户及自己创建的客户，看不到任何其他客户；主负责销售只能看自己创建或自己当前负责的客户。二者均可在有写动作权限且客户为正常时填写联系人、跟踪和附件；仅主负责人可修改客户核心字段、协同人和提醒计划。拥有 `crm:customer:assign` 的后台授予人员可按其数据范围分配/移交客户；管理员使用 `ALL` 范围，可查看和管理所有客户。

服务端统一函数 `can(user, action, customer)` 按“租户 → 系统访问/菜单动作 → 资源动作数据范围 → 客户状态”判定。前端只根据结果隐藏入口，不能代替校验。

### 4.2 客户与归属

1. 新建必填：名称、地址（省/市/详细地址）、来源、行业、重要程度、初始跟踪时间；创建人自动成为主负责人。
2. 名称按 Unicode 归一、去首尾空格、折叠连续空白后精确查重。命中未归档客户即拒绝创建，返回 `CRM_CUSTOMER_NAME_EXISTS`；前端只提示“客户已存在”，不向无权限用户暴露负责人或详情。数据库以 `(tenant_id, active_name_key)` 唯一约束作为最终兜底。
3. 经营状态：`ACTIVE → PAUSED/INVALID/ARCHIVED`；`PAUSED/INVALID/ARCHIVED → ACTIVE`。暂停由主负责人申请；失效、归档由主管/管理员；归档恢复只限管理员；每次必填原因。
4. 非 `ACTIVE` 客户不得新增联系人、跟踪或编辑主数据，未发送提醒须取消；只可查看、导出和规定的恢复操作。恢复时下一次跟踪计划为可选项；不填写则不建提醒，客户显示“待计划/不考核”。
5. 每客户恰一名有效主负责人、零至多名协同人；同一用户不能同时为当前主负责人和协同人。移交原子更新成员、冗余主负责人、提醒接收人和审计。

### 4.3 联系人、跟踪、提醒

1. 联系人只归属一个客户；同客户的标准化手机号唯一。被跟踪引用后只能停用，不能删除。
2. 跟踪必填：客户、至少一位有效联系人、方式（电话/面谈/微信）、实际时间、内容。不得晚于当前时间；默认只允许补录近 30 日，超期仅主管/管理员可做且填写原因。
3. 电话/微信必须关联至少一张扫描可用的图片附件；面谈可选。提交后正文、时间只读；错误使用“作废并重提”，旧记录不参与最后有效跟踪计算。
4. 正常客户创建或提交跟踪必须有下一次跟踪时间；系统取消旧计划、创建新计划。暂停、失效、归档时取消计划，不允许新跟踪。
5. 计划默认在计划前一日 09:00 CST 提醒。主负责人必收，可另选协同人；按 5 分钟、30 分钟、2 小时重试，最多三次，最终失败告警主负责人和直属销售主管。
6. 健康度按最后有效跟踪日期的 CST 自然日计算：`NORMAL/ATTENTION/CRITICAL`；新客户以创建日为基准，每夜 00:05 和有效跟踪后重算。策略约束：`1 <= attention < critical <= 365`，每租户任一时点仅一条生效策略。

### 4.4 审计与异步作业

新建、更新、状态流转、归属变更、联系人停用、跟踪、计划、发送/重试/取消、导入、导出、文件预览/下载都产生领域审计。客户动态是审计的可读投影，用户不能直接编辑。导入按“上传—校验预览—确认执行”异步运行并逐行反馈；导出继承数据权限和脱敏规则，异步生成且短期下载。

## 5. 数据库设计

### 5.1 约定

所有 CRM 表包含 `id, tenant_id, created_at, created_by, updated_at, updated_by, version`；时间 UTC 存储、CST 展示；租户从会话上下文注入，客户端不得传有效 `tenant_id`；主键统一雪花 `bigint`（实施时冻结）。状态使用英文代码，字典提供中文。

### 5.2 表清单

| 表 | 用途与关键字段 | 约束/索引 |
| --- | --- | --- |
| `crm_tenant` | `tenant_id, corp_id, name, status` | `UNIQUE(corp_id)` |
| `crm_dingtalk_identity` | `tenant_id,user_id,dingtalk_user_id,employment_status,synced_at` | `UNIQUE(tenant_id,dingtalk_user_id)`；映射 RuoYi 用户 |
| `crm_org_sync_cursor` | `tenant_id,sync_type,cursor,last_success_at,status,error_summary` | 租户+同步类型唯一 |
| `crm_customer` | 编码、名称/规范名、地址、来源、行业、标签、重要度、经营状态、冗余主负责人、创建/负责人部门、最后有效跟踪、健康度、策略版本 | `UNIQUE(tenant_id,active_name_key)`；按负责人/状态/健康度索引 |
| `crm_customer_owner` | `customer_id,user_id,owner_role(PRIMARY/COLLABORATOR),effective_from,to,status,assigned_by` | 当前主负责人唯一；按用户+状态索引 |
| `crm_owner_change` | 客户、变更类型、前后负责人快照、是否保留协同、原因、操作人、请求 ID | 按客户+生效时间索引 |
| `crm_role_scope` | `role_id,module_code,resource_code,action_code,scope_type(SELF_CREATED_OR_MEMBER/DEPARTMENT/ALL)` | 角色仍引用 RuoYi `sys_role`；联合唯一，支持未来模块扩展 |
| `crm_followup_policy` | `attention_after_days,critical_after_days,effective_at,status,published_by` | 每租户任一时点一条生效策略 |
| `crm_contact` | 客户、姓名、加密电话、`phone_hash`、邮箱、微信、职位、决策人、状态 | `UNIQUE(tenant_id,customer_id,phone_hash)` |
| `crm_follow_up` | 客户、方式、实际时间、内容、结果、下一步、`record_status(EFFECTIVE/VOIDED)`、被替换记录、原因 | 按客户+实际时间倒序索引 |
| `crm_follow_up_contact` | `follow_up_id,contact_id` | 联合唯一；服务层校验同客户、有效 |
| `crm_reminder_plan` | 客户、源跟踪、计划时间、版本、`ACTIVE/COMPLETED/CANCELLED`、取消原因 | 每客户至多一个活动计划 |
| `crm_reminder_delivery` | 计划、接收人、调度时间、`PENDING/RETRYING/SENT/FAILED/CANCELLED`、重试、外部消息 ID | `UNIQUE(plan_id,recipient_user_id,plan_version)` |
| `crm_attachment` | 文件名、MIME、大小、私有 storage key、checksum、扫描状态 | 只发短期受权 URL |
| `crm_attachment_link` | 附件、对象类型（跟踪/客户）、对象 ID、用途 | 联合唯一；不能绕过对象权限 |
| `crm_audit_event` | 实体、动作、操作人快照、原因、前后脱敏快照、请求 ID、来源、发生时间 | 仅 INSERT/SELECT |
| `crm_customer_timeline` | 客户、审计事件、标题、摘要、发生时间 | `UNIQUE(audit_event_id)` |
| `crm_outbox` | 聚合、事件、载荷、幂等键、投递状态、重试、死信原因 | `UNIQUE(tenant_id,idempotency_key)` |
| `crm_idempotency_key` | 用户、键、请求哈希、响应引用、过期时间 | `UNIQUE(tenant_id,actor_id,idempotency_key)` |
| `crm_import_job/import_row/export_job` | 文件哈希、行结果、筛选快照、状态、下载过期时间 | 仅异步执行 |

`sys_user/sys_dept/sys_role/sys_user_role/sys_role_dept` 不复制建表。所有子表以 `(tenant_id, customer_id)` 校验归属；不得用裸 ID 跨租户查询。审计、跟踪、成员历史、提醒投递均不物理删除。

## 6. 接口设计

统一前缀 `/api/crm/v1`，JSON；读接口用 `cursor,limit<=100`，写接口要求 `Idempotency-Key`，更新/命令要求 `version` 或 `If-Match`。错误体：`{code,message,requestId,details}`。

| 能力 | 接口 | 要点 |
| --- | --- | --- |
| 客户 | `GET/POST /customers`、`GET/PATCH /customers/{id}` | POST 同时有初始计划；PATCH 不可直接改状态/负责人 |
| 查重 | `POST /customers/duplicate-check` | 仅供前端预检；创建接口和数据库唯一约束才是最终防线 |
| 状态/成员 | `POST /customers/{id}:pause|resume|invalidate|archive`、`POST /customers/{id}:transfer`、`POST/DELETE /customers/{id}/collaborators/{userId}` | 原因、版本必填，服务端验证状态机 |
| 联系人 | `GET/POST /customers/{id}/contacts`、`PATCH /contacts/{id}`、`POST /contacts/{id}:disable` | 电话在服务端标准化、加密、生成哈希 |
| 跟踪 | `GET/POST /customers/{id}/follow-ups`、`POST /follow-ups/{id}:void-and-replace` | 传有效联系人和 `AVAILABLE` 附件 ID |
| 提醒 | `GET /me/reminders`、`PATCH /reminder-plans/{id}`、`POST /reminder-plans/{id}:complete` | 改期创建新版本；投递记录只读 |
| 附件 | `POST /attachments:prepare-upload`、`POST /attachments/{id}:complete-upload`、`GET /attachments/{id}:download-url` | 私有直传、hash/扫描验证、短期签名下载 |
| 动态/审计 | `GET /customers/{id}/timeline`、`GET /audit-events` | 审计查询独立权限并脱敏 |
| 配置/作业 | `GET/POST /followup-policies`、`POST /imports`、`GET /import-jobs/{id}`、`POST /exports` | 发布前预览影响；导出异步 |
| 钉钉事件 | `POST /integrations/dingtalk/events` | 仅组织事件，验签后持久化并异步处理 |

移交请求：

```json
{
  "targetOwnerId": "1840021",
  "keepPreviousAsCollaborator": true,
  "reason": "区域调整",
  "version": 7
}
```

非法流转返回 `CRM_STATE_TRANSITION_FORBIDDEN`，版本冲突返回 `CRM_VERSION_CONFLICT`，同一幂等键重试返回首次成功结果而不重复创建记录或消息。

## 7. 钉钉对接（V1）

V1 只接入登录、组织架构和工作通知，不接钉钉审批。钉钉内 H5 从微应用取得一次性免登授权码，后端换取身份、映射当前租户 RuoYi 用户后签发 CRM 会话；授权码不能被前端作为业务 token 保存。`requestAuthCode` 面向微应用并要求 `clientId/corpId`。 [钉钉 requestAuthCode](https://open.dingtalk.com/tools/explorer/jsapi?id=11723)

| 场景 | 设计 | 异常与安全 |
| --- | --- | --- |
| H5/PC 登录 | H5 免登；PC 使用钉钉支持的 OAuth/扫码入口，统一进入后端 exchange | 授权码一次消费；未映射身份仅待激活，绝不自动授予角色 |
| 组织同步 | 组织事件增量同步 + 每夜全量对账，更新 RuoYi 用户/部门与钉钉身份映射 | 同步游标、成功快照和告警；离职只禁新分配，不删历史 |
| 选人 | H5 可调用钉钉组织选择器，PC 使用系统选择器 | 后端再次校验在职、租户、角色；`complexChoose` 的 UI 选择结果不是授权依据。 [complexChoose](https://open.dingtalk.com/tools/explorer/jsapi?id=10309) |
| 提醒通知 | 到期 delivery 写入 outbox，由适配器发送工作通知并回写外部消息 ID | 计划+接收人+版本幂等；三次退避后死信、告警、可人工重放 |

每个租户单独配置 `corp_id、client_id、agent_id、密钥引用、通知模板版本`；密钥不写日志、审计快照或前端。上线前逐租户验证免登、组织同步、人员查询、消息发送和回调验签。

## 8. 安全、验收与实施顺序

### 8.1 安全与可靠性

- 手机、邮箱、微信加密保存，查询/导出按角色脱敏；检索手机号使用不可逆哈希。
- 文件进入私有隔离区，限制 MIME/大小、扫描、校验 checksum，下载预览仅给短期签名并审计。
- `crm_audit_event` 的数据库账号没有 UPDATE/DELETE 权限；审计快照脱敏，动态可由审计重建。
- 外部消息、动态投影、同步均经 outbox；失败可重试、可见死信、可人工重放，重放仍有幂等保护。

### 8.2 必须验收

1. 同名未归档客户在预检、创建接口和数据库唯一约束三层均被拒绝，任何人不能创建第二个客户。
2. 并发移交仅一个请求成功；任意时刻恰一名当前主负责人。
3. 协同人无法改核心字段、负责人和状态，但可对正常客户新增联系人和跟踪。
4. 电话跟踪的附件未扫描可用时被拒绝；可用图片关联后可以提交。
5. 改期后旧未发送投递被取消，新版本只向设定接收人各发一次。
6. 重复写请求、重复消息投递均不重复创建客户、跟踪、动态或钉钉消息。
7. 客户暂停、失效、归档后不可新增联系人/跟踪且未发送提醒取消；恢复时计划可选，未填时不发提醒且标记待计划。
8. 租户 A 无法通过 ID、列表、导出或附件 URL 访问租户 B 数据。

### 8.3 建设批次

1. **迭代 0**：锁定 RuoYi tag，做依赖安全扫描，建立租户上下文、RuoYi 权限映射、数据库迁移、审计/outbox 基座。
2. **迭代 1**：钉钉登录/组织同步、后台钉钉选人与角色/范围授权、客户、硬性查重、成员/移交、联系人和对象权限。
3. **迭代 2**：跟踪、附件扫描、计划/通知、健康度策略、H5 快速跟踪。
4. **迭代 3**：导入导出、配置、审计查询、压测、故障演练、安全测试、UAT。

二期若引入商机或立项，必须单独评审状态机、数据模型和钉钉审批可靠性；不得在 V1 客户表上临时加字段绕过权限、审计和事件边界。

### 9. 开发配置

1. APP ID：9659de38-a1ca-4024-898e-01a14a618bfa
2. AgentId：4872143404
3. Client ID (原 AppKey 和 SuiteKey)：dingspla8sbhscihsikj
4. Client Secret (原 AppSecret 和 SuiteSecret)： juTchSncKGeergA90po2L4az4rIkkr6JV_EjK3kl1w05CQCb5lDQwMwv7Zk3PEb_
5. Corp ID：ding37becc0f8438e7c5
6. API Token：e5d6986b8f753a3586a785e9cfc19bcc