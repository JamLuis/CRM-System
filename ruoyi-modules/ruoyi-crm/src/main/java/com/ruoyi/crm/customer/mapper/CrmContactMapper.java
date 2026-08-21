package com.ruoyi.crm.customer.mapper;

import com.ruoyi.crm.customer.domain.CrmContact;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户联系人 Mapper 接口
 *
 * @author ruoyi-crm
 */
public interface CrmContactMapper
{
    /**
     * 按联系人ID查询
     */
    CrmContact selectByContactId(@Param("tenantId") String tenantId, @Param("contactId") Long contactId);

    /** 按外部来源数据 ID 查询（幂等导入） */
    CrmContact selectBySourceDataId(@Param("tenantId") String tenantId,
                                    @Param("sourceDataId") String sourceDataId);

    /**
     * 按客户查询联系人列表
     */
    List<CrmContact> selectByCustomer(@Param("tenantId") String tenantId, @Param("customerId") Long customerId);

    /**
     * 按客户和规范化手机号查询（唯一性校验）
     */
    CrmContact selectByCustomerAndPhone(@Param("tenantId") String tenantId,
                                        @Param("customerId") Long customerId,
                                        @Param("phoneNumber") String phoneNumber);

    /** 按客户和姓名查询联系人（导入跟进记录时建立关联） */
    CrmContact selectByCustomerAndName(@Param("tenantId") String tenantId,
                                       @Param("customerId") Long customerId,
                                       @Param("name") String name);

    /**
     * 插入联系人
     */
    int insert(CrmContact contact);

    /** 为已有同手机号联系人补充外部来源 ID。 */
    int bindSourceDataId(@Param("tenantId") String tenantId,
                         @Param("contactId") Long contactId,
                         @Param("sourceDataId") String sourceDataId,
                         @Param("updateBy") String updateBy);

    /**
     * 更新联系人（乐观锁）
     */
    int update(CrmContact contact);

    /**
     * 停用联系人（乐观锁）
     */
    int deactivate(@Param("tenantId") String tenantId,
                   @Param("contactId") Long contactId,
                   @Param("updateBy") String updateBy,
                   @Param("version") Integer version);
}
