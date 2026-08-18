package com.ruoyi.crm.customer.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.audit.service.CustomerTimelineService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.*;
import com.ruoyi.crm.customer.mapper.CrmContactMapper;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.customer.service.ContactService;
import com.ruoyi.crm.permission.PermissionCode;
import com.ruoyi.crm.permission.PermissionContext;
import com.ruoyi.crm.permission.PermissionService;
import com.ruoyi.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户联系人服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class ContactServiceImpl implements ContactService
{
    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    @Autowired
    private CrmContactMapper contactMapper;

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private CustomerTimelineService timelineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmContact create(CrmContact contact)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询客户
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, contact.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + contact.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CONTACT_WRITE);
        permissionService.check(ctx);

        // 手机号规范化 + 唯一性校验
        if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty())
        {
            String normalizedPhone = normalizePhone(contact.getCountryCode(), contact.getPhoneNumber());
            contact.setPhoneNumber(normalizedPhone);
            contact.setPhoneMasked(maskPhone(normalizedPhone));

            // 同客户手机号唯一
            CrmContact existing = contactMapper.selectByCustomerAndPhone(tenantId,
                    contact.getCustomerId(), normalizedPhone);
            if (existing != null)
            {
                throw new IllegalArgumentException("该客户下已存在相同手机号的联系人");
            }
        }

        // 邮箱脱敏
        if (contact.getEmail() != null && !contact.getEmail().isEmpty())
        {
            contact.setEmailMasked(maskEmail(contact.getEmail()));
        }

        // 微信号脱敏
        if (contact.getWechatId() != null && !contact.getWechatId().isEmpty())
        {
            contact.setWechatMasked(maskWechat(contact.getWechatId()));
        }

        // 设置默认值
        contact.setContactId(idGenerator.nextId());
        contact.setTenantId(tenantId);
        contact.setStatus("有效");
        contact.setVersion(0);
        contact.setDelFlag("0");
        contact.setCreateBy(operatorName);
        contact.setUpdateBy(operatorName);

        contactMapper.insert(contact);

        recordAudit(tenantId, contact.getContactId(), contact.getCustomerId(),
                operatorId, operatorName, "CREATE", null, contact.getName());
        recordTimeline(tenantId, contact.getCustomerId(), operatorId, operatorName,
                "CONTACT_CREATED", contact.getName());

        log.info("Contact created: tenantId={}, contactId={}, customerId={}, name={}, operator={}",
                tenantId, contact.getContactId(), contact.getCustomerId(), contact.getName(), operatorName);

        return contact;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmContact edit(CrmContact contact)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        // 查询现有联系人
        CrmContact existing = contactMapper.selectByContactId(tenantId, contact.getContactId());
        if (existing == null)
        {
            throw new IllegalArgumentException("联系人不存在：" + contact.getContactId());
        }

        // 查询客户用于权限校验
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, existing.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + existing.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CONTACT_WRITE);
        permissionService.check(ctx);

        // 手机号变更时重新校验唯一性
        if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty())
        {
            String normalizedPhone = normalizePhone(contact.getCountryCode(), contact.getPhoneNumber());
            if (!normalizedPhone.equals(existing.getPhoneNumber()))
            {
                CrmContact dup = contactMapper.selectByCustomerAndPhone(tenantId,
                        existing.getCustomerId(), normalizedPhone);
                if (dup != null && !dup.getContactId().equals(existing.getContactId()))
                {
                    throw new IllegalArgumentException("该客户下已存在相同手机号的联系人");
                }
            }
            contact.setPhoneNumber(normalizedPhone);
            contact.setPhoneMasked(maskPhone(normalizedPhone));
        }

        // 邮箱脱敏
        if (contact.getEmail() != null && !contact.getEmail().isEmpty())
        {
            contact.setEmailMasked(maskEmail(contact.getEmail()));
        }

        // 微信号脱敏
        if (contact.getWechatId() != null && !contact.getWechatId().isEmpty())
        {
            contact.setWechatMasked(maskWechat(contact.getWechatId()));
        }

        // 保留不可修改字段
        contact.setTenantId(tenantId);
        contact.setCustomerId(existing.getCustomerId());
        contact.setStatus(existing.getStatus());
        contact.setUpdateBy(operatorName);
        if (contact.getVersion() == null)
        {
            contact.setVersion(existing.getVersion());
        }

        int rows = contactMapper.update(contact);
        if (rows == 0)
        {
            throw new IllegalStateException("联系人已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, contact.getContactId(), existing.getCustomerId(),
                operatorId, operatorName, "UPDATE", existing.getName(), contact.getName());

        log.info("Contact updated: tenantId={}, contactId={}, operator={}",
                tenantId, contact.getContactId(), operatorName);

        return contactMapper.selectByContactId(tenantId, contact.getContactId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmContact deactivate(Long contactId)
    {
        String tenantId = TenantContext.getTenantId();
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = SecurityUtils.getUsername();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        CrmContact existing = contactMapper.selectByContactId(tenantId, contactId);
        if (existing == null)
        {
            throw new IllegalArgumentException("联系人不存在：" + contactId);
        }

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, existing.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + existing.getCustomerId());
        }

        // 权限校验
        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CONTACT_WRITE);
        permissionService.check(ctx);

        int rows = contactMapper.deactivate(tenantId, contactId, operatorName, existing.getVersion());
        if (rows == 0)
        {
            throw new IllegalStateException("联系人已被其他操作修改，请刷新后重试");
        }

        recordAudit(tenantId, contactId, existing.getCustomerId(),
                operatorId, operatorName, "DEACTIVATE", existing.getName(), "已停用");
        recordTimeline(tenantId, existing.getCustomerId(), operatorId, operatorName,
                "CONTACT_DEACTIVATED", existing.getName());

        log.info("Contact deactivated: tenantId={}, contactId={}, operator={}",
                tenantId, contactId, operatorName);

        return contactMapper.selectByContactId(tenantId, contactId);
    }

    @Override
    public CrmContact detail(Long contactId)
    {
        String tenantId = TenantContext.getTenantId();
        CrmContact contact = contactMapper.selectByContactId(tenantId, contactId);
        if (contact == null)
        {
            throw new IllegalArgumentException("联系人不存在：" + contactId);
        }

        // 权限校验
        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, contact.getCustomerId());
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + contact.getCustomerId());
        }

        Long operatorId = SecurityUtils.getUserId();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long operatorDeptId = loginUser != null && loginUser.getSysUser() != null
                ? loginUser.getSysUser().getDeptId() : null;
        boolean isAdmin = SecurityUtils.isAdmin(operatorId);

        PermissionContext ctx = buildPermissionContext(customer, operatorId, operatorDeptId, isAdmin,
                PermissionCode.CRM_CONTACT_READ);
        permissionService.check(ctx);

        return contact;
    }

    @Override
    public List<CrmContact> listByCustomer(Long customerId)
    {
        String tenantId = TenantContext.getTenantId();
        return contactMapper.selectByCustomer(tenantId, customerId);
    }

    // ==================== Private helpers ====================

    /**
     * 规范化手机号：去空格、去横线、补全国家码前缀
     */
    private String normalizePhone(String countryCode, String phoneNumber)
    {
        if (phoneNumber == null)
        {
            return null;
        }
        // 去除空格和横线
        String normalized = phoneNumber.replaceAll("[\\s-]", "");
        // 补全国家码
        if (countryCode != null && !countryCode.isEmpty())
        {
            String cc = countryCode.replaceAll("\\+", "");
            if (!normalized.startsWith(cc))
            {
                normalized = cc + normalized;
            }
        }
        return normalized;
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间用 **** 替代
     */
    private String maskPhone(String phoneNumber)
    {
        if (phoneNumber == null || phoneNumber.length() <= 7)
        {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * 邮箱脱敏：保留首字母和 @ 后域名
     */
    private String maskEmail(String email)
    {
        if (email == null || !email.contains("@"))
        {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1)
        {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * 微信号脱敏：保留前2位和后2位
     */
    private String maskWechat(String wechatId)
    {
        if (wechatId == null || wechatId.length() <= 4)
        {
            return wechatId;
        }
        return wechatId.substring(0, 2) + "***" + wechatId.substring(wechatId.length() - 2);
    }

    private PermissionContext buildPermissionContext(CrmCustomer customer, Long operatorId,
                                                    Long operatorDeptId, boolean isAdmin,
                                                    PermissionCode permissionCode)
    {
        PermissionContext ctx = new PermissionContext();
        ctx.setOperatorId(operatorId);
        ctx.setOperatorDeptId(operatorDeptId);
        ctx.setAdmin(isAdmin);
        ctx.setPermissionCode(permissionCode);
        ctx.setPrimaryOwnerId(customer.getPrimaryOwnerId());
        ctx.setCollaboratorIds(customer.getCollaboratorIds());
        ctx.setCreatorDeptId(customer.getCreatorDeptId());
        ctx.setOwnerDeptId(customer.getOwnerDeptId());
        ctx.setOperatingStatus(customer.getOperatingStatus());
        return ctx;
    }

    private void recordAudit(String tenantId, Long contactId, Long customerId,
                             Long operatorId, String operatorName,
                             String action, String beforeData, String afterData)
    {
        CrmAuditEvent event = new CrmAuditEvent();
        event.setTenantId(tenantId);
        event.setEventType("CONTACT");
        event.setEntityType("CONTACT");
        event.setEntityId(String.valueOf(contactId));
        event.setOperatorId(operatorId);
        event.setOperatorName(operatorName);
        event.setAction(action);
        event.setBeforeData(beforeData);
        event.setAfterData(afterData);
        auditEventService.record(event);
    }

    private void recordTimeline(String tenantId, Long customerId, Long operatorId,
                                String operatorName, String eventType, String eventData)
    {
        CrmCustomerTimeline timeline = new CrmCustomerTimeline();
        timeline.setTenantId(tenantId);
        timeline.setCustomerId(customerId);
        timeline.setEventType(eventType);
        timeline.setEventData(eventData);
        timeline.setOperatorId(operatorId);
        timeline.setOperatorName(operatorName);
        timelineService.record(timeline);
    }
}
