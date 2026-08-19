package com.ruoyi.crm.followup.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.crm.followup.domain.CrmFollowUpStatusStrategy;
import com.ruoyi.crm.followup.service.FollowUpStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跟进状态（健康度）管理接口
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/health")
public class FollowUpStatusController
{
    @Autowired
    private FollowUpStatusService followUpStatusService;

    /**
     * 手动触发单个客户状态计算
     *
     * @param customerId 客户ID
     * @return 计算后的状态
     */
    @PostMapping("/calculate/{customerId}")
    public R<String> calculate(@PathVariable Long customerId)
    {
        return R.ok(followUpStatusService.calculate(customerId));
    }

    /**
     * 手动触发批量重算
     *
     * @return 重算的客户数量
     */
    @RequiresPermissions("crm:admin:strategy")
    @PostMapping("/recalculate-batch")
    public R<Integer> recalculateBatch()
    {
        return R.ok(followUpStatusService.recalculateBatch());
    }

    /**
     * 保存策略
     *
     * @param strategy 策略
     * @return 保存后的策略
     */
    @RequiresPermissions("crm:admin:strategy")
    @PostMapping("/strategies")
    public R<CrmFollowUpStatusStrategy> saveStrategy(@RequestBody CrmFollowUpStatusStrategy strategy)
    {
        return R.ok(followUpStatusService.saveStrategy(strategy));
    }

    /**
     * 查询当前生效策略
     *
     * @return 策略
     */
    @GetMapping("/strategies/active")
    public R<CrmFollowUpStatusStrategy> getActiveStrategy()
    {
        return R.ok(followUpStatusService.getActiveStrategy());
    }

    /**
     * 查询全部策略
     *
     * @return 策略列表
     */
    @GetMapping("/strategies")
    public R<List<CrmFollowUpStatusStrategy>> listStrategies()
    {
        return R.ok(followUpStatusService.listStrategies());
    }
}
