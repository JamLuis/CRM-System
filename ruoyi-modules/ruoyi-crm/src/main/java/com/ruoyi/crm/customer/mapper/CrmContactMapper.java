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

    /**
     * 插入联系人
     */
    int insert(CrmContact contact);

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
