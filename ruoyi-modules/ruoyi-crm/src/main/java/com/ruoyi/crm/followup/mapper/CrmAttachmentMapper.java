package com.ruoyi.crm.followup.mapper;

import com.ruoyi.crm.followup.domain.CrmAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 附件 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmAttachmentMapper
{
    /**
     * 按附件ID查询
     */
    CrmAttachment selectByAttachmentId(@Param("tenantId") String tenantId,
                                       @Param("attachmentId") Long attachmentId);

    /**
     * 按所属业务对象查询附件列表
     */
    List<CrmAttachment> selectByOwner(@Param("tenantId") String tenantId,
                                       @Param("ownerType") String ownerType,
                                       @Param("ownerId") Long ownerId);

    /**
     * 按业务对象和状态查询（校验是否有可用图片）
     */
    List<CrmAttachment> selectByOwnerAndStatus(@Param("tenantId") String tenantId,
                                                @Param("ownerType") String ownerType,
                                                @Param("ownerId") Long ownerId,
                                                @Param("status") String status);

    /**
     * 插入附件
     */
    int insert(CrmAttachment attachment);

    /**
     * 更新状态
     */
    int updateStatus(@Param("tenantId") String tenantId,
                     @Param("attachmentId") Long attachmentId,
                     @Param("status") String status,
                     @Param("updateBy") String updateBy);

    /**
     * 更新扫描结果
     */
    int updateScanResult(@Param("tenantId") String tenantId,
                         @Param("attachmentId") Long attachmentId,
                         @Param("status") String status,
                         @Param("scanCompletedAt") Date scanCompletedAt,
                         @Param("scanErrorCode") String scanErrorCode);
}
