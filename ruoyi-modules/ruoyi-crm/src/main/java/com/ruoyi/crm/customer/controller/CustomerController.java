package com.ruoyi.crm.customer.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理接口
 * <p>
 * 提供客户创建、查询、编辑、列表等 REST API。
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/customers")
public class CustomerController
{
    @Autowired
    private CustomerService customerService;

    /**
     * 创建客户
     *
     * @param customer 客户信息
     * @return 创建后的客户
     */
    @PostMapping
    public R<CrmCustomer> create(@RequestBody CrmCustomer customer)
    {
        return R.ok(customerService.create(customer));
    }

    /**
     * 查询客户详情
     *
     * @param customerId 客户 ID
     * @return 客户信息
     */
    @GetMapping("/{customerId}")
    public R<CrmCustomer> detail(@PathVariable Long customerId)
    {
        return R.ok(customerService.detail(customerId));
    }

    /**
     * 编辑客户核心字段
     *
     * @param customer 客户信息（须携带 customerId 和 version）
     * @return 更新后的客户
     */
    @PutMapping
    public R<CrmCustomer> edit(@RequestBody CrmCustomer customer)
    {
        return R.ok(customerService.edit(customer));
    }

    /**
     * 查询客户列表（按当前用户数据范围）
     *
     * @param name            客户名称（模糊）
     * @param operatingStatus 经营状态
     * @param lifecycleStage  生命周期阶段
     * @param importance      重要程度
     * @param source          客户来源
     * @param industry        行业
     * @return 客户列表
     */
    @GetMapping
    public R<List<CrmCustomer>> list(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "operatingStatus", required = false) String operatingStatus,
            @RequestParam(value = "lifecycleStage", required = false) String lifecycleStage,
            @RequestParam(value = "importance", required = false) String importance,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "industry", required = false) String industry)
    {
        CrmCustomer query = new CrmCustomer();
        query.setName(name);
        query.setOperatingStatus(operatingStatus);
        query.setLifecycleStage(lifecycleStage);
        query.setImportance(importance);
        query.setSource(source);
        query.setIndustry(industry);
        return R.ok(customerService.list(query));
    }

    /**
     * 查询全部客户（仅管理员）
     *
     * @param name            客户名称（模糊）
     * @param operatingStatus 经营状态
     * @param lifecycleStage  生命周期阶段
     * @return 客户列表
     */
    @GetMapping("/all")
    public R<List<CrmCustomer>> listAll(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "operatingStatus", required = false) String operatingStatus,
            @RequestParam(value = "lifecycleStage", required = false) String lifecycleStage)
    {
        CrmCustomer query = new CrmCustomer();
        query.setName(name);
        query.setOperatingStatus(operatingStatus);
        query.setLifecycleStage(lifecycleStage);
        return R.ok(customerService.listAll(query));
    }
}
