package com.ruoyi.crm.followup.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.crm.followup.domain.CrmAttachment;
import com.ruoyi.crm.followup.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 附件管理接口
 *
 * @author ruoyi-crm
 */
@RestController
@RequestMapping("/api/crm/v1/attachments")
public class AttachmentController
{
    @Autowired
    private AttachmentService attachmentService;

    /**
     * 创建上传预签名记录
     *
     * @param attachment 附件元数据
     * @return 保存后的附件对象（含 attachmentId，状态为 PENDING_SCAN）
     */
    @PostMapping("/pre-sign")
    public R<CrmAttachment> createUpload(@RequestBody CrmAttachment attachment)
    {
        return R.ok(attachmentService.createUpload(attachment));
    }

    /**
     * 确认上传完成
     *
     * @param attachmentId 附件ID
     * @return 更新后的附件对象
     */
    @PostMapping("/{attachmentId}/confirm")
    public R<CrmAttachment> confirmUpload(@PathVariable Long attachmentId)
    {
        return R.ok(attachmentService.confirmUpload(attachmentId));
    }

    /**
     * 获取下载URL
     *
     * @param attachmentId 附件ID
     * @return 附件对象（含 storageKey）
     */
    @GetMapping("/{attachmentId}/download")
    public R<CrmAttachment> getDownloadUrl(@PathVariable Long attachmentId)
    {
        return R.ok(attachmentService.getDownloadUrl(attachmentId));
    }

    /**
     * 按业务对象查询附件列表
     *
     * @param ownerType 业务对象类型(FOLLOW_UP/CUSTOMER)
     * @param ownerId   业务对象ID
     * @return 附件列表
     */
    @GetMapping("/by-owner")
    public R<List<CrmAttachment>> listByOwner(@RequestParam String ownerType,
                                               @RequestParam Long ownerId)
    {
        return R.ok(attachmentService.listByOwner(ownerType, ownerId));
    }
}
