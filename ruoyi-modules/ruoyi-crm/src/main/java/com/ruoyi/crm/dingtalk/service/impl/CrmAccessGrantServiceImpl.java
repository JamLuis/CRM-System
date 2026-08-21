package com.ruoyi.crm.dingtalk.service.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.crm.dingtalk.service.CrmAccessGrantService;
import com.ruoyi.crm.dingtalk.service.DingTalkIdentityExchangeService;
import com.ruoyi.crm.tenant.domain.CrmDingtalkDirectoryUser;
import com.ruoyi.crm.tenant.domain.CrmDingtalkIdentity;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkDirectoryUserMapper;
import com.ruoyi.crm.tenant.mapper.CrmDingtalkIdentityMapper;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.domain.SysRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CrmAccessGrantServiceImpl implements CrmAccessGrantService
{
    private static final String SOURCE_INNER = SecurityConstants.INNER;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private CrmDingtalkDirectoryUserMapper directoryUserMapper;

    @Autowired
    private CrmDingtalkIdentityMapper identityMapper;

    @Autowired
    private DingTalkIdentityExchangeService identityExchangeService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Override
    public List<CrmDingtalkDirectoryUser> listDirectoryUsers(String tenantId, String keyword, String accessStatus)
    {
        return directoryUserMapper.selectAuthorizationList(
                tenantId,
                keyword == null ? null : keyword.trim(),
                accessStatus == null ? null : accessStatus.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long grant(String tenantId, String dingtalkUserId, List<Long> roleIds)
    {
        if (roleIds == null || roleIds.isEmpty())
        {
            throw new ServiceException("至少选择一个 CRM 角色");
        }
        CrmDingtalkDirectoryUser directoryUser =
                directoryUserMapper.selectByDingtalkUserId(tenantId, dingtalkUserId);
        if (directoryUser == null)
        {
            throw new ServiceException("人员不在已同步的企业通讯录中，请先执行组织同步");
        }
        if (Boolean.FALSE.equals(directoryUser.getActive()))
        {
            throw new ServiceException("离职或停用人员不能分配 CRM 访问权");
        }

        Set<Long> assignableRoleIds = getAssignableRoleIds();
        Set<Long> requestedRoleIds = roleIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (requestedRoleIds.isEmpty() || !assignableRoleIds.containsAll(requestedRoleIds))
        {
            throw new ServiceException("只能分配已配置 crm:access 的 CRM 角色");
        }

        CrmDingtalkIdentity identity =
                identityMapper.selectByDingtalkUserId(tenantId, dingtalkUserId);
        Long sysUserId = identity == null ? null : identity.getSysUserId();

        if (sysUserId == null && directoryUser.getMobile() != null && !directoryUser.getMobile().isEmpty())
        {
            R<SysUser> existingByPhone =
                    remoteUserService.innerGetUserByPhone(directoryUser.getMobile(), SOURCE_INNER);
            if (existingByPhone != null && R.isSuccess(existingByPhone) && existingByPhone.getData() != null)
            {
                sysUserId = existingByPhone.getData().getUserId();
            }
        }

        if (SysUser.isAdmin(sysUserId))
        {
            throw new ServiceException("不能将超级管理员账号绑定为钉钉 CRM 身份");
        }

        Long[] assignedRoleIds = requestedRoleIds.toArray(new Long[0]);
        if (sysUserId == null)
        {
            SysUser sysUser = toSystemUser(directoryUser);
            sysUser.setRoleIds(assignedRoleIds);
            sysUser.setPassword(randomPassword());
            R<Long> addResult = remoteUserService.innerAddUser(sysUser, SOURCE_INNER);
            if (addResult == null || !R.isSuccess(addResult) || addResult.getData() == null)
            {
                throw new ServiceException(addResult == null ? "创建系统用户失败" : addResult.getMsg());
            }
            sysUserId = addResult.getData();
        }
        else
        {
            SysUser sysUser = toSystemUser(directoryUser);
            sysUser.setUserId(sysUserId);
            R<Boolean> editResult = remoteUserService.innerEditUser(sysUser, SOURCE_INNER);
            if (editResult == null || !R.isSuccess(editResult))
            {
                throw new ServiceException(editResult == null ? "更新系统用户失败" : editResult.getMsg());
            }
            Long[] mergedRoleIds = mergeWithExistingNonCrmRoles(sysUserId, requestedRoleIds, assignableRoleIds);
            R<Boolean> roleResult = remoteUserService.innerAuthRoles(sysUserId, mergedRoleIds, SOURCE_INNER);
            if (roleResult == null || !R.isSuccess(roleResult))
            {
                throw new ServiceException(roleResult == null ? "分配角色失败" : roleResult.getMsg());
            }
        }

        identityExchangeService.mapIdentity(
                tenantId, dingtalkUserId, sysUserId, directoryUser.getUnionId());
        return sysUserId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(String tenantId, String dingtalkUserId)
    {
        CrmDingtalkIdentity identity =
                identityMapper.selectByDingtalkUserId(tenantId, dingtalkUserId);
        if (identity == null)
        {
            return;
        }
        if (SysUser.isAdmin(identity.getSysUserId()))
        {
            throw new ServiceException("超级管理员账号不能通过 CRM 授权页撤销角色");
        }
        Long[] retainedRoleIds = mergeWithExistingNonCrmRoles(
                identity.getSysUserId(), java.util.Collections.emptySet(), getAssignableRoleIds());
        R<Boolean> roleResult =
                remoteUserService.innerAuthRoles(identity.getSysUserId(), retainedRoleIds, SOURCE_INNER);
        if (roleResult == null || !R.isSuccess(roleResult))
        {
            throw new ServiceException(roleResult == null ? "清除角色失败" : roleResult.getMsg());
        }
        identityMapper.deleteByDingtalkUserId(tenantId, dingtalkUserId);
    }

    @Override
    public List<SysRole> listAssignableRoles()
    {
        return directoryUserMapper.selectAssignableRoles();
    }

    private SysUser toSystemUser(CrmDingtalkDirectoryUser directoryUser)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserName(buildUserName(directoryUser.getDingtalkUserId()));
        sysUser.setNickName(directoryUser.getName());
        sysUser.setPhonenumber(directoryUser.getMobile());
        sysUser.setEmail(directoryUser.getEmail());
        sysUser.setDeptId(directoryUser.getSysDeptId());
        sysUser.setStatus("0");
        sysUser.setRemark("钉钉免登专用账号；CRM权限由访问授权页维护");
        return sysUser;
    }

    private String buildUserName(String dingtalkUserId)
    {
        UUID stableId = UUID.nameUUIDFromBytes(dingtalkUserId.getBytes(StandardCharsets.UTF_8));
        return "dt_" + stableId.toString().replace("-", "").substring(0, 24);
    }

    private String randomPassword()
    {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Set<Long> getAssignableRoleIds()
    {
        return directoryUserMapper.selectAssignableRoles().stream()
                .map(SysRole::getRoleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Long[] mergeWithExistingNonCrmRoles(Long sysUserId,
                                                  Collection<Long> requestedCrmRoleIds,
                                                  Set<Long> assignableCrmRoleIds)
    {
        R<SysUser> userResult = remoteUserService.innerGetUserById(sysUserId, SOURCE_INNER);
        if (userResult == null || !R.isSuccess(userResult) || userResult.getData() == null)
        {
            throw new ServiceException(userResult == null ? "读取系统用户角色失败" : userResult.getMsg());
        }
        Set<Long> merged = new LinkedHashSet<>();
        List<SysRole> existingRoles = userResult.getData().getRoles();
        if (existingRoles != null)
        {
            existingRoles.stream()
                    .map(SysRole::getRoleId)
                    .filter(java.util.Objects::nonNull)
                    .filter(roleId -> !assignableCrmRoleIds.contains(roleId))
                    .forEach(merged::add);
        }
        merged.addAll(requestedCrmRoleIds);
        return merged.toArray(new Long[0]);
    }
}
