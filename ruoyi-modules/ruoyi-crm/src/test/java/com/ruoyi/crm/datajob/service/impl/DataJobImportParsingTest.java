package com.ruoyi.crm.datajob.service.impl;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("联系人与跟进记录导入解析测试")
class DataJobImportParsingTest
{
    @Test
    @DisplayName("联系人双层表头被跳过并按列映射")
    void parsesContactWorkbookWithTwoHeaderRows() throws Exception
    {
        try (XSSFWorkbook workbook = baseWorkbook())
        {
            Row row = workbook.getSheetAt(0).createRow(2);
            row.createCell(1).setCellValue("contact-source-1");
            row.createCell(3).setCellValue("测试客户");
            row.createCell(4).setCellValue("联系人甲");
            row.createCell(5).setCellValue("技术负责人");
            row.createCell(7).setCellValue("是");
            row.createCell(8).setCellValue("+86-13800000000");
            row.createCell(12).setCellValue("2026-08-20 10:00:00");
            row.createCell(13).setCellValue("历史创建人");

            List<ImportContactRow> rows = ReflectionTestUtils.invokeMethod(
                    new DataJobServiceImpl(), "readContactRows",
                    new ByteArrayInputStream(toBytes(workbook)));

            assertEquals(1, rows.size());
            assertEquals("contact-source-1", rows.get(0).getSourceDataId());
            assertEquals("测试客户", rows.get(0).getCustomerName());
            assertEquals("联系人甲", rows.get(0).getName());
            assertEquals("+86-13800000000", rows.get(0).getPhone());
        }
    }

    @Test
    @DisplayName("跟进记录双层表头、联系人和历史字段正确映射")
    void parsesFollowUpWorkbookWithTwoHeaderRows() throws Exception
    {
        try (XSSFWorkbook workbook = baseWorkbook())
        {
            Row row = workbook.getSheetAt(0).createRow(2);
            row.createCell(1).setCellValue("follow-source-1");
            row.createCell(3).setCellValue("测试客户");
            row.createCell(4).setCellValue("是");
            row.createCell(6).setCellValue("联系人甲");
            row.createCell(9).setCellValue("联系人乙");
            row.createCell(14).setCellValue("微信（附聊天截图）");
            row.createCell(15).setCellValue("原附件引用");
            row.createCell(17).setCellValue("历史跟进内容");
            row.createCell(18).setCellValue("2026-08-20 11:00:00");
            row.createCell(19).setCellValue("历史创建人");

            DataJobServiceImpl service = new DataJobServiceImpl();
            List<ImportFollowUpRow> rows = ReflectionTestUtils.invokeMethod(
                    service, "readFollowUpRows", new ByteArrayInputStream(toBytes(workbook)));

            assertEquals(1, rows.size());
            assertEquals("follow-source-1", rows.get(0).getSourceDataId());
            assertEquals("联系人甲、联系人乙", rows.get(0).getContactNames());
            assertEquals("历史跟进内容", rows.get(0).getContent());
            assertEquals("微信", ReflectionTestUtils.invokeMethod(
                    service, "normalizeFollowUpMethod", rows.get(0).getMethod()));
        }
    }

    private XSSFWorkbook baseWorkbook()
    {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Row first = workbook.createSheet("数据").createRow(0);
        first.createCell(1).setCellValue("数据id");
        Row second = workbook.getSheetAt(0).createRow(1);
        second.createCell(1).setCellValue("数据id");
        return workbook;
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        workbook.write(output);
        return output.toByteArray();
    }
}
