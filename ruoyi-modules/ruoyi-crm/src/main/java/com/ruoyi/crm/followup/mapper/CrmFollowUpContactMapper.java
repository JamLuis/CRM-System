package com.ruoyi.crm.followup.mapper;

import com.ruoyi.crm.followup.domain.CrmFollowUpContact;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跟进—联系人关系 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmFollowUpContactMapper
{
    /**
     * 按跟进ID查询联系人列表
     */
    List<CrmFollowUpContact> selectByFollowUpId(@Param("tenantId") String tenantId,
                                                 @Param("followUpId") Long followUpId);

    /**
     * 批量插入
     */
    int batchInsert(@Param("list") List<CrmFollowUpContact> list);

    /**
     * 按跟进ID删除
     */
    int deleteByFollowUpId(@Param("tenantId") String tenantId,
                           @Param("followUpId") Long followUpId);
}
