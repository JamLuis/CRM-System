package com.ruoyi.crm.datajob.service.impl;

import com.ruoyi.crm.datajob.config.DataJobProperties;
import com.ruoyi.crm.customer.domain.CrmCustomer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据作业本地文件存储测试。
 */
@DisplayName("数据作业文件存储测试")
class DataJobServiceImplStorageTest
{
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("存储路径始终解析为绝对规范路径")
    void testResolveStorageFileReturnsAbsoluteNormalizedPath()
    {
        DataJobServiceImpl service = createService("./crm-datajob-files");

        File resolved = ReflectionTestUtils.invokeMethod(
                service, "resolveStorageFile", "default/100_source.xlsx");

        assertTrue(resolved.isAbsolute());
        assertTrue(resolved.toPath().normalize().endsWith("crm-datajob-files/default/100_source.xlsx"));
    }

    @Test
    @DisplayName("上传文件通过流复制保存到业务存储目录")
    void testSaveUploadFileCopiesContentToStorageDirectory() throws Exception
    {
        DataJobServiceImpl service = createService(tempDir.toString());
        byte[] content = "valid-xlsx-content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile upload = new MockMultipartFile(
                "file", "客户.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);

        String storageKey = ReflectionTestUtils.invokeMethod(
                service, "saveUploadFile", "default", 100L, upload);

        Path savedFile = tempDir.resolve(storageKey);
        assertTrue(Files.isRegularFile(savedFile));
        assertArrayEquals(content, Files.readAllBytes(savedFile));
    }

    @Test
    @DisplayName("历史客户行映射旧表头、重要程度和默认跟进时间")
    void testBuildCustomerFromLegacyRow()
    {
        DataJobServiceImpl service = createService(tempDir.toString());
        ImportCustomerRow row = new ImportCustomerRow();
        row.setName("历史客户");
        row.setLegacyAddress("江苏省南京市测试路 1 号");
        row.setLegacyIndustry("交通运输");
        row.setImportance("很重要");
        row.setLegacyLevel("核心");
        row.setSourceFollowUpStatus("成交");
        row.setSourceCustomerStatus("成交客户");
        row.setSourceOwnerName("历史负责人");
        row.setSourceCollaboratorNames("协同甲,协同乙");
        Date sourceTime = new Date(1700000000000L);
        row.setSourceCreateTime(sourceTime);
        row.setSourceUpdateTime(sourceTime);

        CrmCustomer customer = ReflectionTestUtils.invokeMethod(
                service, "buildCustomerFromRow", row, 1L, "admin");

        assertEquals("江苏省南京市测试路 1 号", customer.getAddressDetail());
        assertEquals("", customer.getAddressProvince());
        assertEquals("", customer.getAddressCity());
        assertEquals("交通运输", customer.getIndustry());
        assertEquals("", customer.getSource());
        assertEquals("非常重要", customer.getImportance());
        assertEquals("核心", customer.getFollowUpIntensity());
        assertEquals("成交", customer.getSourceFollowUpStatus());
        assertEquals("成交客户", customer.getLifecycleStage());
        assertEquals("成交客户", customer.getSourceCustomerStatus());
        assertEquals("历史负责人", customer.getSourceOwnerName());
        assertEquals("协同甲,协同乙", customer.getSourceCollaboratorNames());
        assertEquals(sourceTime, customer.getCreateTime());
        assertEquals(sourceTime, customer.getUpdateTime());
        assertNotNull(customer.getNextFollowUpAt());
        assertEquals(1L, customer.getPrimaryOwnerId());
    }

    @Test
    @DisplayName("历史客户行缺失非空列时使用可入库默认值")
    void testBuildCustomerFromLegacyRowUsesRequiredDefaults()
    {
        DataJobServiceImpl service = createService(tempDir.toString());
        ImportCustomerRow row = new ImportCustomerRow();
        row.setName("仅有名称的历史客户");

        CrmCustomer customer = ReflectionTestUtils.invokeMethod(
                service, "buildCustomerFromRow", row, 1L, "admin");

        assertEquals("", customer.getAddressProvince());
        assertEquals("", customer.getAddressCity());
        assertEquals("", customer.getAddressDetail());
        assertEquals("一般", customer.getImportance());
        assertEquals("", customer.getSource());
        assertEquals("", customer.getIndustry());
        assertNotNull(customer.getNextFollowUpAt());
    }

    private DataJobServiceImpl createService(String storagePath)
    {
        DataJobProperties properties = new DataJobProperties();
        properties.setStoragePath(storagePath);

        DataJobServiceImpl service = new DataJobServiceImpl();
        ReflectionTestUtils.setField(service, "properties", properties);
        return service;
    }
}
