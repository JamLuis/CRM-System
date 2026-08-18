package com.ruoyi.crm.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrmOwnerChange 实体测试
 */
@DisplayName("CrmOwnerChange 实体测试")
class CrmOwnerChangeTest
{
    @Test
    @DisplayName("CrmOwnerChange 正确设置和获取所有字段")
    void testAllFields()
    {
        CrmOwnerChange change = new CrmOwnerChange();
        change.setId(6001L);
        change.setCustomerId(1001L);
        change.setChangeType(OwnerChangeType.TRANSFER.name());
        change.setPreviousPrimaryOwnerId(200L);
        change.setPreviousPrimaryOwnerName("张三");
        change.setTargetPrimaryOwnerId(201L);
        change.setTargetPrimaryOwnerName("李四");
        change.setAddedCollaboratorIds("200");
        change.setRemovedCollaboratorIds(null);
        change.setKeepPreviousAsCollaborator(true);
        change.setReason("人员调整");
        change.setOperatorId(1L);
        change.setOperatorName("管理员");

        assertEquals(6001L, change.getId());
        assertEquals(1001L, change.getCustomerId());
        assertEquals("TRANSFER", change.getChangeType());
        assertEquals(200L, change.getPreviousPrimaryOwnerId());
        assertEquals("张三", change.getPreviousPrimaryOwnerName());
        assertEquals(201L, change.getTargetPrimaryOwnerId());
        assertEquals("李四", change.getTargetPrimaryOwnerName());
        assertEquals("200", change.getAddedCollaboratorIds());
        assertNull(change.getRemovedCollaboratorIds());
        assertTrue(change.getKeepPreviousAsCollaborator());
        assertEquals("人员调整", change.getReason());
        assertEquals(1L, change.getOperatorId());
        assertEquals("管理员", change.getOperatorName());
    }
}
