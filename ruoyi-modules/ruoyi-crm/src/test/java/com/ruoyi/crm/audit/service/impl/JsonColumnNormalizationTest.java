package com.ruoyi.crm.audit.service.impl;

import com.ruoyi.crm.audit.domain.CrmAuditEvent;
import com.ruoyi.crm.audit.domain.CrmCustomerTimeline;
import com.ruoyi.crm.audit.mapper.CrmAuditEventMapper;
import com.ruoyi.crm.audit.mapper.CrmCustomerTimelineMapper;
import com.ruoyi.crm.common.id.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("审计 JSON 列规范化测试")
class JsonColumnNormalizationTest
{
    @Test
    @DisplayName("审计普通文本转换为合法 JSON，已有 JSON 保持不变")
    void testAuditEventNormalizesJsonColumns()
    {
        CrmAuditEventMapper mapper = mock(CrmAuditEventMapper.class);
        IdGenerator idGenerator = mock(IdGenerator.class);
        when(idGenerator.nextId()).thenReturn(1001L);

        AuditEventServiceImpl service = new AuditEventServiceImpl();
        ReflectionTestUtils.setField(service, "auditEventMapper", mapper);
        ReflectionTestUtils.setField(service, "idGenerator", idGenerator);

        CrmAuditEvent event = new CrmAuditEvent();
        event.setBeforeData("历史客户");
        event.setAfterData("{\"name\":\"新客户\"}");
        service.record(event);

        ArgumentCaptor<CrmAuditEvent> captor = ArgumentCaptor.forClass(CrmAuditEvent.class);
        verify(mapper).insert(captor.capture());
        assertEquals("\"历史客户\"", captor.getValue().getBeforeData());
        assertEquals("{\"name\":\"新客户\"}", captor.getValue().getAfterData());
        assertEquals(1001L, captor.getValue().getId());
    }

    @Test
    @DisplayName("时间线普通文本转换为合法 JSON")
    void testTimelineNormalizesJsonColumn()
    {
        CrmCustomerTimelineMapper mapper = mock(CrmCustomerTimelineMapper.class);
        IdGenerator idGenerator = mock(IdGenerator.class);
        when(idGenerator.nextId()).thenReturn(2001L);

        CustomerTimelineServiceImpl service = new CustomerTimelineServiceImpl();
        ReflectionTestUtils.setField(service, "timelineMapper", mapper);
        ReflectionTestUtils.setField(service, "idGenerator", idGenerator);

        CrmCustomerTimeline timeline = new CrmCustomerTimeline();
        timeline.setEventData("客户已创建");
        service.record(timeline);

        ArgumentCaptor<CrmCustomerTimeline> captor = ArgumentCaptor.forClass(CrmCustomerTimeline.class);
        verify(mapper).insert(captor.capture());
        assertEquals("\"客户已创建\"", captor.getValue().getEventData());
        assertEquals(2001L, captor.getValue().getId());
    }
}
