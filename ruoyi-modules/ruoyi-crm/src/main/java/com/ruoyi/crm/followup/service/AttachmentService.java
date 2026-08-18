package com.ruoyi.crm.followup.service;

import com.ruoyi.crm.followup.domain.CrmAttachment;

import java.util.List;

/**
 * 附件服务接口
 *
 * @author ruoyi-crm
 */
public interface AttachmentService
{
    /**
     * 创建上传预签名记录（文件上传前调用）
     *
     * @param attachment 附件元数据（ownerType, ownerId, fileName, contentType, sizeBytes, storageKey, checksum）
     * @return 保存后的附件对象（含 attachmentId，状态为 PENDING_SCAN）
     */
    CrmAttachment createUpload(CrmAttachment attachment);

    /**
     * 确认上传完成（客户端上传到对象存储后回调）
     *
     * @param attachmentId 附件ID
     * @return 更新后的附件对象
     */
    CrmAttachment confirmUpload(Long attachmentId);

    /**
     * 获取下载URL（校验权限和状态）
     *
     * @param attachmentId 附件ID
     * @return 附件对象（含 storageKey 用于生成下载URL）
     */
    CrmAttachment getDownloadUrl(Long attachmentId);

    /**
     * 按业务对象查询附件列表
     *
     * @param ownerType 业务对象类型
     * @param ownerId   业务对象ID
     * @return 附件列表
     */
    List<CrmAttachment> listByOwner(String ownerType, Long ownerId);

    /**
     * 按业务对象和状态查询附件（校验是否有可用图片）
     *
     * @param ownerType 业务对象类型
     * @param ownerId   业务对象ID
     * @param status    状态
     * @return 附件列表
     */
    List<CrmAttachment> listByOwnerAndStatus(String ownerType, Long ownerId, String status);
}
