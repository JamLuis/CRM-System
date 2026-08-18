package com.ruoyi.crm.outbox.controller;

import com.ruoyi.crm.outbox.domain.CrmOutbox;
import com.ruoyi.crm.outbox.service.OutboxService;
import com.ruoyi.common.core.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Outbox 管理接口 — 死信查询与人工重放
 * <p>
 * 仅管理员可访问（权限控制由后续 M2 阶段补充）。
 */
@RestController
@RequestMapping("/api/crm/v1/outbox")
public class OutboxAdminController
{
    @Autowired
    private OutboxService outboxService;

    /**
     * 查询死信列表
     */
    @GetMapping("/dead")
    public R<List<CrmOutbox>> deadLetters()
    {
        // TODO: 从登录上下文获取 tenantId
        String tenantId = "default";
        return R.ok(outboxService.findDead(tenantId));
    }

    /**
     * 人工重放死信
     */
    @PutMapping("/dead/{id}/replay")
    public R<Void> replayDead(@PathVariable Long id)
    {
        // TODO: 从登录上下文获取 tenantId
        String tenantId = "default";
        // 先查询获取 version
        List<CrmOutbox> deadList = outboxService.findDead(tenantId);
        CrmOutbox target = deadList.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (target == null)
        {
            return R.fail("死信不存在或已不属于当前租户");
        }
        boolean ok = outboxService.resetDeadToPending(id, tenantId, target.getVersion());
        return ok ? R.ok() : R.fail("重放失败：消息可能已被其他实例处理");
    }
}
