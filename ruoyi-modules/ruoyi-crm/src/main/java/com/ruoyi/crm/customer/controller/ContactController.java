package com.ruoyi.crm.customer.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.customer.domain.CrmContact;
import com.ruoyi.crm.customer.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户联系人管理接口
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/contacts")
public class ContactController
{
    @Autowired
    private ContactService contactService;

    /**
     * 创建联系人
     *
     * @param contact 联系人信息
     * @return 创建后的联系人
     */
    @PostMapping
    public R<CrmContact> create(@RequestBody CrmContact contact)
    {
        return R.ok(contactService.create(contact));
    }

    /**
     * 编辑联系人
     *
     * @param contact 联系人信息（须携带 contactId 和 version）
     * @return 更新后的联系人
     */
    @PutMapping
    public R<CrmContact> edit(@RequestBody CrmContact contact)
    {
        return R.ok(contactService.edit(contact));
    }

    /**
     * 停用联系人
     *
     * @param contactId 联系人 ID
     * @return 更新后的联系人
     */
    @PostMapping("/{contactId}/deactivate")
    public R<CrmContact> deactivate(@PathVariable Long contactId)
    {
        return R.ok(contactService.deactivate(contactId));
    }

    /**
     * 查询联系人详情
     *
     * @param contactId 联系人 ID
     * @return 联系人信息
     */
    @GetMapping("/{contactId}")
    public R<CrmContact> detail(@PathVariable Long contactId)
    {
        return R.ok(contactService.detail(contactId));
    }

    /**
     * 查询客户联系人列表
     *
     * @param customerId 客户 ID
     * @return 联系人列表
     */
    @GetMapping("/by-customer/{customerId}")
    public R<List<CrmContact>> listByCustomer(@PathVariable Long customerId)
    {
        return R.ok(contactService.listByCustomer(customerId));
    }
}
