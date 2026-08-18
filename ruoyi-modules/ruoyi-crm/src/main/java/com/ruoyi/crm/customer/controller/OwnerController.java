package com.ruoyi.crm.customer.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.customer.domain.CrmCustomerOwner;
import com.ruoyi.crm.customer.domain.CrmOwnerChange;
import com.ruoyi.crm.customer.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户成员维护与移交接口
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/customers/{customerId}/owners")
public class OwnerController
{
    @Autowired
    private OwnerService ownerService;

    /**
     * 移交主负责人
     *
     * @param customerId 客户 ID
     * @param body       请求体
     * @return 变更记录
     */
    @PostMapping("/transfer")
    public R<CrmOwnerChange> transfer(@PathVariable Long customerId, @RequestBody TransferRequest body)
    {
        return R.ok(ownerService.transfer(customerId, body.getTargetOwnerId(),
                body.getTargetOwnerName(), body.getTargetOwnerDeptId(),
                body.isKeepPreviousAsCollaborator(), body.getReason()));
    }

    /**
     * 新增协同人
     *
     * @param customerId 客户 ID
     * @param body       请求体
     * @return 变更记录
     */
    @PostMapping("/collaborators")
    public R<CrmOwnerChange> addCollaborator(@PathVariable Long customerId,
                                             @RequestBody CollaboratorRequest body)
    {
        return R.ok(ownerService.addCollaborator(customerId,
                body.getUserId(), body.getUserName()));
    }

    /**
     * 移除协同人
     *
     * @param customerId     客户 ID
     * @param collaboratorId 协同人用户 ID
     * @return 变更记录
     */
    @DeleteMapping("/collaborators/{collaboratorId}")
    public R<CrmOwnerChange> removeCollaborator(@PathVariable Long customerId,
                                               @PathVariable Long collaboratorId)
    {
        return R.ok(ownerService.removeCollaborator(customerId, collaboratorId));
    }

    /**
     * 查询客户成员列表
     *
     * @param customerId 客户 ID
     * @return 成员列表
     */
    @GetMapping
    public R<List<CrmCustomerOwner>> listMembers(@PathVariable Long customerId)
    {
        return R.ok(ownerService.listMembers(customerId));
    }

    /**
     * 查询负责人变更历史
     *
     * @param customerId 客户 ID
     * @return 变更记录列表
     */
    @GetMapping("/changes")
    public R<List<CrmOwnerChange>> listChangeHistory(@PathVariable Long customerId)
    {
        return R.ok(ownerService.listChangeHistory(customerId));
    }

    // ==================== 请求体内部类 ====================

    public static class TransferRequest
    {
        private Long targetOwnerId;
        private String targetOwnerName;
        private Long targetOwnerDeptId;
        private boolean keepPreviousAsCollaborator;
        private String reason;

        public Long getTargetOwnerId()
        {
            return targetOwnerId;
        }

        public void setTargetOwnerId(Long targetOwnerId)
        {
            this.targetOwnerId = targetOwnerId;
        }

        public String getTargetOwnerName()
        {
            return targetOwnerName;
        }

        public void setTargetOwnerName(String targetOwnerName)
        {
            this.targetOwnerName = targetOwnerName;
        }

        public Long getTargetOwnerDeptId()
        {
            return targetOwnerDeptId;
        }

        public void setTargetOwnerDeptId(Long targetOwnerDeptId)
        {
            this.targetOwnerDeptId = targetOwnerDeptId;
        }

        public boolean isKeepPreviousAsCollaborator()
        {
            return keepPreviousAsCollaborator;
        }

        public void setKeepPreviousAsCollaborator(boolean keepPreviousAsCollaborator)
        {
            this.keepPreviousAsCollaborator = keepPreviousAsCollaborator;
        }

        public String getReason()
        {
            return reason;
        }

        public void setReason(String reason)
        {
            this.reason = reason;
        }
    }

    public static class CollaboratorRequest
    {
        private Long userId;
        private String userName;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public String getUserName()
        {
            return userName;
        }

        public void setUserName(String userName)
        {
            this.userName = userName;
        }
    }
}
