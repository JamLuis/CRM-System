package com.ruoyi.crm.permission;

/**
 * CRM 权限服务接口
 * <p>
 * 权限判断顺序为：
 * <ol>
 *   <li>认证有效 → 操作人已登录</li>
 *   <li>角色具备操作权限 → RuoYi @RequiresPermissions 或 AuthUtil.hasPermi</li>
 *   <li>数据范围命中 → SELF_CREATED_OR_MEMBER / DEPT / ALL</li>
 *   <li>对象经营状态允许该操作 → 正常/暂停/失效/归档</li>
 * </ol>
 * 任何一步不满足时，接口返回无权限或状态不允许。
 *
 * @author ruoyi-crm
 */
public interface PermissionService
{
    /**
     * 检查权限（含数据范围 + 经营状态校验）
     *
     * @param context 权限上下文
     * @return true=允许，false=拒绝
     */
    boolean can(PermissionContext context);

    /**
     * 检查权限，不满足时抛出异常
     *
     * @param context 权限上下文
     * @throws PermissionDeniedException 权限不足
     */
    void check(PermissionContext context) throws PermissionDeniedException;

    /**
     * 查询当前用户的数据范围类型
     *
     * @param tenantId 租户 ID
     * @param userId   用户 ID
     * @return 数据范围类型
     */
    ScopeType getScopeType(String tenantId, Long userId);
}
