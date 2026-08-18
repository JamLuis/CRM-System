package com.ruoyi.crm.followup.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.followup.domain.CrmFollowUp;
import com.ruoyi.crm.followup.service.FollowUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跟进记录管理接口
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/follow-ups")
public class FollowUpController
{
    @Autowired
    private FollowUpService followUpService;

    /**
     * 创建跟进记录
     *
     * @param request 跟进记录创建请求
     * @return 创建后的跟进记录
     */
    @PostMapping
    public R<CrmFollowUp> create(@RequestBody FollowUpCreateRequest request)
    {
        return R.ok(followUpService.create(request.getFollowUp(),
                request.getContactIds(), request.getAttachmentIds()));
    }

    /**
     * 更正跟进记录
     *
     * @param followUpId 原跟进记录ID
     * @param request    更正请求
     * @return 更正后的新跟进记录
     */
    @PostMapping("/{followUpId}/correct")
    public R<CrmFollowUp> correct(@PathVariable Long followUpId,
                                   @RequestBody FollowUpCorrectRequest request)
    {
        return R.ok(followUpService.correct(followUpId, request.getFollowUp(),
                request.getContactIds(), request.getAttachmentIds(),
                request.getCorrectionReason()));
    }

    /**
     * 作废跟进记录
     *
     * @param followUpId 跟进记录ID
     * @param request    作废请求
     * @return 作废后的跟进记录
     */
    @PostMapping("/{followUpId}/void")
    public R<CrmFollowUp> voidFollowUp(@PathVariable Long followUpId,
                                        @RequestBody FollowUpVoidRequest request)
    {
        return R.ok(followUpService.void_(followUpId, request.getVoidedReason()));
    }

    /**
     * 查询跟进记录详情
     *
     * @param followUpId 跟进记录ID
     * @return 跟进记录
     */
    @GetMapping("/{followUpId}")
    public R<CrmFollowUp> detail(@PathVariable Long followUpId)
    {
        return R.ok(followUpService.detail(followUpId));
    }

    /**
     * 按客户查询跟进记录列表
     *
     * @param customerId 客户ID
     * @return 跟进记录列表
     */
    @GetMapping("/by-customer/{customerId}")
    public R<List<CrmFollowUp>> listByCustomer(@PathVariable Long customerId)
    {
        return R.ok(followUpService.listByCustomer(customerId));
    }

    // ==================== Request DTOs ====================

    public static class FollowUpCreateRequest
    {
        private CrmFollowUp followUp;
        private List<Long> contactIds;
        private List<Long> attachmentIds;

        public CrmFollowUp getFollowUp()
        {
            return followUp;
        }

        public void setFollowUp(CrmFollowUp followUp)
        {
            this.followUp = followUp;
        }

        public List<Long> getContactIds()
        {
            return contactIds;
        }

        public void setContactIds(List<Long> contactIds)
        {
            this.contactIds = contactIds;
        }

        public List<Long> getAttachmentIds()
        {
            return attachmentIds;
        }

        public void setAttachmentIds(List<Long> attachmentIds)
        {
            this.attachmentIds = attachmentIds;
        }
    }

    public static class FollowUpCorrectRequest
    {
        private CrmFollowUp followUp;
        private List<Long> contactIds;
        private List<Long> attachmentIds;
        private String correctionReason;

        public CrmFollowUp getFollowUp()
        {
            return followUp;
        }

        public void setFollowUp(CrmFollowUp followUp)
        {
            this.followUp = followUp;
        }

        public List<Long> getContactIds()
        {
            return contactIds;
        }

        public void setContactIds(List<Long> contactIds)
        {
            this.contactIds = contactIds;
        }

        public List<Long> getAttachmentIds()
        {
            return attachmentIds;
        }

        public void setAttachmentIds(List<Long> attachmentIds)
        {
            this.attachmentIds = attachmentIds;
        }

        public String getCorrectionReason()
        {
            return correctionReason;
        }

        public void setCorrectionReason(String correctionReason)
        {
            this.correctionReason = correctionReason;
        }
    }

    public static class FollowUpVoidRequest
    {
        private String voidedReason;

        public String getVoidedReason()
        {
            return voidedReason;
        }

        public void setVoidedReason(String voidedReason)
        {
            this.voidedReason = voidedReason;
        }
    }
}
