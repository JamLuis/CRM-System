package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import com.ruoyi.crm.customer.mapper.CrmCustomerMapper;
import com.ruoyi.crm.followup.domain.CrmFollowUp;
import com.ruoyi.crm.followup.domain.CrmFollowUpStatusStrategy;
import com.ruoyi.crm.followup.domain.FollowUpStatus;
import com.ruoyi.crm.followup.mapper.CrmFollowUpMapper;
import com.ruoyi.crm.followup.mapper.CrmFollowUpStatusStrategyMapper;
import com.ruoyi.crm.followup.service.FollowUpStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 跟进状态（健康度）服务实现
 *
 * @author ruoyi-crm
 */
@Service
public class FollowUpStatusServiceImpl implements FollowUpStatusService
{
    private static final Logger log = LoggerFactory.getLogger(FollowUpStatusServiceImpl.class);

    @Autowired
    private CrmFollowUpStatusStrategyMapper strategyMapper;

    @Autowired
    private CrmCustomerMapper customerMapper;

    @Autowired
    private CrmFollowUpMapper followUpMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String calculate(Long customerId)
    {
        String tenantId = TenantContext.getTenantId();
        String operatorName = "system";

        CrmCustomer customer = customerMapper.selectByCustomerId(tenantId, customerId);
        if (customer == null)
        {
            throw new IllegalArgumentException("客户不存在：" + customerId);
        }

        // 暂停/失效/归档客户：不考核，保留上次计算结果
        String operatingStatus = customer.getOperatingStatus();
        if ("暂停跟进".equals(operatingStatus) || "已失效".equals(operatingStatus) || "已归档".equals(operatingStatus))
        {
            log.info("Customer {} is {}, skip calculation", customerId, operatingStatus);
            return customer.getFollowUpStatus() != null
                    ? customer.getFollowUpStatus()
                    : FollowUpStatus.NOT_ASSESSED.name();
        }

        // 获取策略
        CrmFollowUpStatusStrategy strategy = strategyMapper.selectActive(tenantId);
        if (strategy == null)
        {
            // 无策略默认值
            return FollowUpStatus.NOT_ASSESSED.name();
        }

        // 计算最近有效跟进时间
        Date lastFollowUpAt = customer.getLastEffectiveFollowUpAt();
        if (lastFollowUpAt == null)
        {
            // 新客户使用创建时间
            lastFollowUpAt = customer.getCreateTime() != null ? customer.getCreateTime() : new Date();
        }

        // 计算天数差
        int daysSinceLastFollowUp = daysBetween(lastFollowUpAt, new Date());

        // 判定状态
        String newStatus;
        if (daysSinceLastFollowUp >= strategy.getSevereThreshold())
        {
            newStatus = FollowUpStatus.SEVERE_INSUFFICIENT.name();
        }
        else if (daysSinceLastFollowUp >= strategy.getInsufficientThreshold())
        {
            newStatus = FollowUpStatus.INSUFFICIENT.name();
        }
        else
        {
            newStatus = FollowUpStatus.NORMAL.name();
        }

        // 更新客户跟进状态
        String oldStatus = customer.getFollowUpStatus();
        customerMapper.updateFollowUpStatus(tenantId, customerId, newStatus, operatorName,
                customer.getVersion());

        // 状态变更通知
        if (oldStatus != null && !oldStatus.equals(newStatus))
        {
            notifyStatusChange(customer, oldStatus, newStatus);
        }

        log.info("Follow-up status calculated: tenantId={}, customerId={}, oldStatus={}, newStatus={}, days={}",
                tenantId, customerId, oldStatus, newStatus, daysSinceLastFollowUp);

        return newStatus;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recalculateBatch()
    {
        String tenantId = TenantContext.getTenantId();
        List<CrmCustomer> normalCustomers = customerMapper.selectIdsByFollowUpStatus(
                tenantId, FollowUpStatus.NORMAL.name());

        int count = 0;
        for (CrmCustomer customer : normalCustomers)
        {
            try
            {
                calculate(customer.getCustomerId());
                count++;
            }
            catch (Exception e)
            {
                log.error("Failed to recalculate status for customer {}: {}",
                        customer.getCustomerId(), e.getMessage());
            }
        }

        log.info("Batch recalculation completed: tenantId={}, recalculated={}", tenantId, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmFollowUpStatusStrategy saveStrategy(CrmFollowUpStatusStrategy strategy)
    {
        String tenantId = TenantContext.getTenantId();
        String operatorName = SecurityUtils.getUsername();

        // 校验阈值
        if (strategy.getInsufficientThreshold() == null || strategy.getInsufficientThreshold() < 1)
        {
            throw new IllegalArgumentException("不足阈值天数必须 >= 1");
        }
        if (strategy.getSevereThreshold() == null
                || strategy.getSevereThreshold() <= strategy.getInsufficientThreshold())
        {
            throw new IllegalArgumentException("严重不足阈值天数必须 > 不足阈值");
        }
        if (strategy.getInsufficientThreshold() > 365 || strategy.getSevereThreshold() > 365)
        {
            throw new IllegalArgumentException("阈值天数不能超过 365");
        }

        // 创建新策略
        strategy.setStrategyId(idGenerator.nextId());
        strategy.setTenantId(tenantId);
        strategy.setEffectiveFrom(new Date());
        strategy.setStatus("ACTIVE");
        strategy.setVersion(0);
        strategy.setDelFlag("0");
        strategy.setCreateBy(operatorName);
        strategy.setUpdateBy(operatorName);

        strategyMapper.insert(strategy);

        // 停用旧策略
        strategyMapper.deactivateOld(tenantId, strategy.getStrategyId(), operatorName);

        // 触发全量重算
        recalculateBatch();

        log.info("Strategy saved: tenantId={}, strategyId={}, insufficient={}, severe={}",
                tenantId, strategy.getStrategyId(),
                strategy.getInsufficientThreshold(), strategy.getSevereThreshold());

        return strategy;
    }

    @Override
    public CrmFollowUpStatusStrategy getActiveStrategy()
    {
        String tenantId = TenantContext.getTenantId();
        return strategyMapper.selectActive(tenantId);
    }

    @Override
    public List<CrmFollowUpStatusStrategy> listStrategies()
    {
        String tenantId = TenantContext.getTenantId();
        return strategyMapper.selectAll(tenantId);
    }

    // ==================== Private helpers ====================

    /**
     * 计算两个日期之间的天数差
     */
    private int daysBetween(Date from, Date to)
    {
        long diff = to.getTime() - from.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * 状态变更通知
     * <p>
     * normal→insufficient: 通知主负责人
     * insufficient→severe: 通知主负责人 + 销售经理
     */
    private void notifyStatusChange(CrmCustomer customer, String oldStatus, String newStatus)
    {
        if (FollowUpStatus.NORMAL.name().equals(oldStatus)
                && FollowUpStatus.INSUFFICIENT.name().equals(newStatus))
        {
            log.info("Status changed NORMAL→INSUFFICIENT, notify primary owner: customerId={}, ownerId={}",
                    customer.getCustomerId(), customer.getPrimaryOwnerId());
        }
        else if (FollowUpStatus.INSUFFICIENT.name().equals(oldStatus)
                && FollowUpStatus.SEVERE_INSUFFICIENT.name().equals(newStatus))
        {
            log.info("Status changed INSUFFICIENT→SEVERE, notify primary owner + sales manager: customerId={}",
                    customer.getCustomerId());
        }
    }
}
