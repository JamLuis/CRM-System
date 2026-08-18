package com.ruoyi.crm.permission.impl;

import com.ruoyi.common.security.auth.AuthUtil;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionDeniedException;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.crm.permission.ScopeType;
import com.ruoyi.crm.tenant.domain.CrmRoleScope;
import com.ruoyi.crm.tenant.mapper.CrmRoleScopeMapper;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 权限服务实现
 * <p>
 * 权限判断顺序：
 * <ol>
 *   <li>认证有效 → 操作人已登录（SecurityUtils.getLoginUser）</li>
 *   <li>角色具备操作权限 → AuthUtil.hasPermi(permissionCode)</li>
 *   <li>数据范围命中 → 根据 ScopeType 判断</li>
 *   <li>对象经营状态允许该操作 → 检查 operatingStatus</li>
 * </ol>
 *
 * @author ruoyi-crm
 */
@Service
public class PermissionServiceImpl implements PermissionService
{
    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    /** 经营状态：正常 */
    private static final String STATUS_NORMAL = "正常";
    /** 经营状态：暂停跟进 */
    private static final String STATUS_PAUSED = "暂停跟进";
    /** 经营状态：已失效 */
    private static final String STATUS_EXPIRED = "已失效";
    /** 经营状态：已归档 */
    private static final String STATUS_ARCHIVED = "已归档";

    @Autowired
    private CrmRoleScopeMapper roleScopeMapper;

    @Override
    public boolean can(PermissionContext context)
    {
        if (context == null || context.getPermissionCode() == null)
        {
            return false;
        }

        // 1. 管理员直接放行
        if (context.isAdmin() || SecurityUtils.isAdmin(context.getOperatorId()))
        {
            return checkOperatingStatus(context);
        }

        // 2. 角色具备操作权限
        if (!AuthUtil.hasPermi(context.getPermissionCode().getCode()))
        {
            log.debug("Permission denied: operator={} lacks permission={}",
                    context.getOperatorId(), context.getPermissionCode().getCode());
            return false;
        }

        // 3. 数据范围命中
        if (!checkDataScope(context))
        {
            log.debug("Permission denied: operator={} out of data scope for customer owner={}",
                    context.getOperatorId(), context.getPrimaryOwnerId());
            return false;
        }

        // 4. 对象经营状态允许该操作
        return checkOperatingStatus(context);
    }

    @Override
    public void check(PermissionContext context) throws PermissionDeniedException
    {
        if (!can(context))
        {
            throw new PermissionDeniedException("权限不足或对象状态不允许该操作");
        }
    }

    @Override
    public ScopeType getScopeType(String tenantId, Long userId)
    {
        // 管理员返回 ALL
        if (SecurityUtils.isAdmin(userId))
        {
            return ScopeType.ALL;
        }

        // 从 LoginUser 获取角色
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null && loginUser.getSysUser() != null && loginUser.getSysUser().getRoles() != null)
        {
            for (com.ruoyi.system.api.domain.SysRole role : loginUser.getSysUser().getRoles())
            {
                CrmRoleScope scope = roleScopeMapper.selectByRoleId(tenantId, role.getRoleId());
                if (scope != null)
                {
                    return ScopeType.fromString(scope.getScopeType());
                }
            }
        }

        // 默认最小权限
        return ScopeType.SELF_CREATED_OR_MEMBER;
    }

    /**
     * 检查数据范围
     */
    private boolean checkDataScope(PermissionContext context)
    {
        // 获取当前用户的数据范围
        ScopeType scopeType = getScopeType(null, context.getOperatorId());

        switch (scopeType)
        {
            case ALL:
                // 全部客户
                return true;

            case DEPT:
                // 本部门及下级部门
                // 检查操作人部门是否与客户主负责人部门相同
                // 实际实现中需要递归检查子部门，这里简化为同部门检查
                return context.isSameDept();

            case SELF_CREATED_OR_MEMBER:
            default:
                // 本人主负责或协同
                return context.isPrimaryOwner() || context.isCollaborator();
        }
    }

    /**
     * 检查对象经营状态是否允许该操作
     * <p>
     * 已暂停、已失效和已归档客户不得新增跟进、联系人、商机或立项申请。
     * 仅允许查看和导出。
     */
    private boolean checkOperatingStatus(PermissionContext context)
    {
        String status = context.getOperatingStatus();
        if (status == null || STATUS_NORMAL.equals(status))
        {
            return true;
        }

        // 非正常状态下，只允许读取类操作
        switch (context.getPermissionCode())
        {
            case CRM_CUSTOMER_READ:
            case CRM_CONTACT_READ:
            case CRM_FOLLOWUP_READ:
            case CRM_OPPORTUNITY_READ:
            case CRM_AUDIT_QUERY:
            case CRM_CUSTOMER_EXPORT:
                return true;

            // 恢复操作允许在暂停/失效状态下执行（由上层业务逻辑控制具体权限）
            case CRM_CUSTOMER_STATUS:
                return true;

            default:
                // 写操作在非正常状态下被拒绝
                log.debug("Permission denied: operating status={} blocks write operation {}",
                        status, context.getPermissionCode().getCode());
                return false;
        }
    }
}
