package com.ruoyi.crm.followup.service.impl;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.crm.audit.service.AuditEventService;
import com.ruoyi.crm.common.id.IdGenerator;
import com.ruoyi.crm.common.tenant.TenantContext;
import com.ruoyi.crm.followup.domain.AttachmentStatus;
import com.ruoyi.crm.followup.domain.CrmAttachment;
import com.ruoyi.crm.followup.mapper.CrmAttachmentMapper;
import com.ruoyi.system.api.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 附件服务测试
 */
@DisplayName("附件服务测试")
class AttachmentServiceImplTest
{
    private CrmAttachmentMapper attachmentMapper;
    private IdGenerator idGenerator;
    private AuditEventService auditEventService;
    private AttachmentServiceImpl attachmentService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setup() throws Exception
    {
        attachmentMapper = Mockito.mock(CrmAttachmentMapper.class);
        idGenerator = Mockito.mock(IdGenerator.class);
        auditEventService = Mockito.mock(AuditEventService.class);

        when(idGenerator.nextId()).thenReturn(5001L);

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(1L);
        loginUser.setUsername("admin");
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

        attachmentService = new AttachmentServiceImpl();
        setField(attachmentService, "attachmentMapper", attachmentMapper);
        setField(attachmentService, "idGenerator", idGenerator);
        setField(attachmentService, "auditEventService", auditEventService);
    }

    @AfterEach
    void tearDown()
    {
        securityUtilsMock.close();
        TenantContext.clear();
    }

    @Test
    @DisplayName("创建上传 - 设置 PENDING_SCAN 状态")
    void testCreateUploadSetsPendingScanStatus()
    {
        TenantContext.setTenantId("test-tenant");

        CrmAttachment attachment = new CrmAttachment();
        attachment.setOwnerType("FOLLOW_UP");
        attachment.setOwnerId(2001L);
        attachment.setFileName("photo.jpg");
        attachment.setContentType("image/jpeg");
        attachment.setSizeBytes(102400L);
        attachment.setStorageKey("uploads/2024/photo.jpg");

        when(attachmentMapper.insert(any(CrmAttachment.class))).thenReturn(1);

        CrmAttachment result = attachmentService.createUpload(attachment);

        assertEquals(5001L, result.getAttachmentId());
        assertEquals("test-tenant", result.getTenantId());
        assertEquals(AttachmentStatus.PENDING_SCAN.name(), result.getStatus());
        assertEquals(1L, result.getUploadedBy());
        assertEquals("admin", result.getUploadedByName());
        assertEquals(Integer.valueOf(0), result.getVersion());

        verify(attachmentMapper).insert(any(CrmAttachment.class));
        verify(auditEventService).record(any());
    }

    @Test
    @DisplayName("确认上传 - 状态从 PENDING_SCAN 变为 AVAILABLE")
    void testConfirmUploadTransitionsToAvailable()
    {
        TenantContext.setTenantId("test-tenant");

        CrmAttachment existing = new CrmAttachment();
        existing.setAttachmentId(5001L);
        existing.setStatus(AttachmentStatus.PENDING_SCAN.name());

        CrmAttachment confirmed = new CrmAttachment();
        confirmed.setAttachmentId(5001L);
        confirmed.setStatus(AttachmentStatus.AVAILABLE.name());

        when(attachmentMapper.selectByAttachmentId("test-tenant", 5001L))
                .thenReturn(existing, confirmed);
        when(attachmentMapper.updateScanResult(eq("test-tenant"), eq(5001L),
                eq(AttachmentStatus.AVAILABLE.name()), any(), isNull())).thenReturn(1);

        CrmAttachment result = attachmentService.confirmUpload(5001L);

        assertEquals(AttachmentStatus.AVAILABLE.name(), result.getStatus());
        verify(attachmentMapper).updateScanResult(eq("test-tenant"), eq(5001L),
                eq(AttachmentStatus.AVAILABLE.name()), any(), isNull());
    }

    @Test
    @DisplayName("确认上传 - 附件不存在时抛出异常")
    void testConfirmUploadNotFound()
    {
        TenantContext.setTenantId("test-tenant");
        when(attachmentMapper.selectByAttachmentId("test-tenant", 9999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> attachmentService.confirmUpload(9999L));
    }

    @Test
    @DisplayName("确认上传 - 非 PENDING_SCAN 状态时抛出异常")
    void testConfirmUploadWrongStatus()
    {
        TenantContext.setTenantId("test-tenant");

        CrmAttachment existing = new CrmAttachment();
        existing.setAttachmentId(5001L);
        existing.setStatus(AttachmentStatus.AVAILABLE.name());

        when(attachmentMapper.selectByAttachmentId("test-tenant", 5001L)).thenReturn(existing);

        assertThrows(IllegalStateException.class, () -> attachmentService.confirmUpload(5001L));
    }

    @Test
    @DisplayName("获取下载URL - 非 AVAILABLE 状态时抛出异常")
    void testGetDownloadUrlWrongStatus()
    {
        TenantContext.setTenantId("test-tenant");

        CrmAttachment existing = new CrmAttachment();
        existing.setAttachmentId(5001L);
        existing.setStatus(AttachmentStatus.PENDING_SCAN.name());

        when(attachmentMapper.selectByAttachmentId("test-tenant", 5001L)).thenReturn(existing);

        assertThrows(IllegalStateException.class, () -> attachmentService.getDownloadUrl(5001L));
    }

    @Test
    @DisplayName("按业务对象查询附件列表")
    void testListByOwner()
    {
        TenantContext.setTenantId("test-tenant");

        CrmAttachment a1 = new CrmAttachment();
        a1.setAttachmentId(5001L);
        CrmAttachment a2 = new CrmAttachment();
        a2.setAttachmentId(5002L);

        when(attachmentMapper.selectByOwner("test-tenant", "FOLLOW_UP", 2001L))
                .thenReturn(Arrays.asList(a1, a2));

        List<CrmAttachment> result = attachmentService.listByOwner("FOLLOW_UP", 2001L);

        assertEquals(2, result.size());
        verify(attachmentMapper).selectByOwner("test-tenant", "FOLLOW_UP", 2001L);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception
    {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
