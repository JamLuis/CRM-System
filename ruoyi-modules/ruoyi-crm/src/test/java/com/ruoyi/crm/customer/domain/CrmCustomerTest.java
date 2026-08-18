package com.ruoyi.crm.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrmCustomer 实体测试
 */
@DisplayName("CrmCustomer 实体测试")
class CrmCustomerTest
{
    @Test
    @DisplayName("CrmCustomer 正确设置和获取所有字段")
    void testAllFields()
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerId(1001L);
        customer.setCustomerCode("C00001001");
        customer.setName("测试客户公司");
        customer.setActiveNameKey("测试客户公司");
        customer.setAddressProvince("广东省");
        customer.setAddressCity("深圳市");
        customer.setAddressDistrict("南山区");
        customer.setAddressStreet("科技园");
        customer.setAddressDetail("A栋101");
        customer.setTags("[\"VIP\"]");
        customer.setLifecycleStage(LifecycleStage.NEW.getValue());
        customer.setOperatingStatus(OperatingStatus.NORMAL.getValue());
        customer.setImportance("重要");
        customer.setSource("线上推广");
        customer.setIndustry("互联网");
        customer.setRemark("测试备注");
        customer.setPrimaryOwnerId(200L);
        customer.setPrimaryOwnerName("张三");
        customer.setCollaboratorIds("201,202");
        customer.setCreatorDeptId(10L);
        customer.setOwnerDeptId(10L);
        Date now = new Date();
        customer.setNextFollowUpAt(now);
        customer.setLastEffectiveFollowUpAt(now);
        customer.setArchivedAt(null);
        customer.setVersion(0);
        customer.setDelFlag("0");

        assertEquals(1001L, customer.getCustomerId());
        assertEquals("C00001001", customer.getCustomerCode());
        assertEquals("测试客户公司", customer.getName());
        assertEquals("测试客户公司", customer.getActiveNameKey());
        assertEquals("广东省", customer.getAddressProvince());
        assertEquals("深圳市", customer.getAddressCity());
        assertEquals("南山区", customer.getAddressDistrict());
        assertEquals("科技园", customer.getAddressStreet());
        assertEquals("A栋101", customer.getAddressDetail());
        assertEquals("[\"VIP\"]", customer.getTags());
        assertEquals("新获取", customer.getLifecycleStage());
        assertEquals("正常", customer.getOperatingStatus());
        assertEquals("重要", customer.getImportance());
        assertEquals("线上推广", customer.getSource());
        assertEquals("互联网", customer.getIndustry());
        assertEquals("测试备注", customer.getRemark());
        assertEquals(200L, customer.getPrimaryOwnerId());
        assertEquals("张三", customer.getPrimaryOwnerName());
        assertEquals("201,202", customer.getCollaboratorIds());
        assertEquals(10L, customer.getCreatorDeptId());
        assertEquals(10L, customer.getOwnerDeptId());
        assertEquals(now, customer.getNextFollowUpAt());
        assertEquals(now, customer.getLastEffectiveFollowUpAt());
        assertNull(customer.getArchivedAt());
        assertEquals(Integer.valueOf(0), customer.getVersion());
        assertEquals("0", customer.getDelFlag());
    }

    @Test
    @DisplayName("CrmCustomer 继承 CrmBaseEntity 的 tenantId 字段")
    void testInheritsTenantId()
    {
        CrmCustomer customer = new CrmCustomer();
        customer.setTenantId("test-tenant");
        assertEquals("test-tenant", customer.getTenantId());
    }
}
