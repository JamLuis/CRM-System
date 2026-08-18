package com.ruoyi.crm.followup.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 附件状态枚举测试
 */
@DisplayName("附件状态枚举测试")
class AttachmentStatusTest
{
    @Test
    @DisplayName("fromString 正确解析已知值")
    void testFromStringKnown()
    {
        assertEquals(AttachmentStatus.PENDING_SCAN, AttachmentStatus.fromString("PENDING_SCAN"));
        assertEquals(AttachmentStatus.AVAILABLE, AttachmentStatus.fromString("AVAILABLE"));
        assertEquals(AttachmentStatus.QUARANTINED, AttachmentStatus.fromString("QUARANTINED"));
        assertEquals(AttachmentStatus.DELETED, AttachmentStatus.fromString("DELETED"));
    }

    @Test
    @DisplayName("fromString 对未知值抛出异常")
    void testFromStringUnknown()
    {
        assertThrows(IllegalArgumentException.class, () -> AttachmentStatus.fromString("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> AttachmentStatus.fromString(null));
    }
}
