package com.ruoyi.crm.tenant.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.permission.ScopeType;
import com.ruoyi.crm.tenant.domain.CrmRoleScope;
import com.ruoyi.crm.tenant.mapper.CrmRoleScopeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 角色数据范围授权管理接口（管理员）
 * <p>
 * 管理 crm_role_scope 中角色的数据范围（ALL / DEPT / SELF_CREATED_OR_MEMBER），
 * 管理员可在后台调整角色数据范围，无需数据库手工操作。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/role-scopes")
public class RoleScopeController
{
    private static final Logger log = LoggerFactory.getLogger(RoleScopeController.class);

    @Autowired
    private CrmRoleScopeMapper roleScopeMapper;

    @Autowired
    private IdGenerator idGenerator;

    /**
     * 查询全部角色数据范围
     */
    @RequiresPermissions("crm:admin:grant")
    @GetMapping
    public R<List<CrmRoleScope>> listRoleScopes()
    {
        String tenantId = TenantContext.getTenantId();
        return R.ok(roleScopeMapper.selectAll(tenantId));
    }

    /**
     * 查询指定角色的数据范围
     */
    @RequiresPermissions("crm:admin:grant")
    @GetMapping("/by-role")
    public R<CrmRoleScope> getByRoleId(@org.springframework.web.bind.annotation.RequestParam("roleId") Long roleId)
    {
        String tenantId = TenantContext.getTenantId();
        CrmRoleScope scope = roleScopeMapper.selectByRoleId(tenantId, roleId);
        return scope != null ? R.ok(scope) : R.fail("该角色尚未配置数据范围");
    }

    /**
     * 创建或更新角色数据范围
     * <p>
     * 请求体：{ "roleId": 13, "scopeType": "ALL" }
     * scopeType 取值：ALL / DEPT / SELF_CREATED_OR_MEMBER
     */
    @RequiresPermissions("crm:admin:grant")
    @PostMapping("/save")
    public R<Void> saveRoleScope(@RequestBody Map<String, Object> body)
    {
        Object roleIdObj = body.get("roleId");
        String scopeTypeStr = (String) body.get("scopeType");

        if (roleIdObj == null)
        {
            return R.fail("roleId 不能为空");
        }
        Long roleId = Long.valueOf(String.valueOf(roleIdObj));
        ScopeType scopeType = ScopeType.fromString(scopeTypeStr);

        String tenantId = TenantContext.getTenantId();
        CrmRoleScope existing = roleScopeMapper.selectByRoleId(tenantId, roleId);
        if (existing != null)
        {
            existing.setScopeType(scopeType.name());
            roleScopeMapper.update(existing);
        }
        else
        {
            CrmRoleScope scope = new CrmRoleScope();
            scope.setId(idGenerator.nextId());
            scope.setTenantId(tenantId);
            scope.setRoleId(roleId);
            scope.setScopeType(scopeType.name());
            roleScopeMapper.insert(scope);
        }
        log.info("Admin saved role scope: roleId={}, scopeType={}", roleId, scopeType.name());
        return R.ok();
    }
}
