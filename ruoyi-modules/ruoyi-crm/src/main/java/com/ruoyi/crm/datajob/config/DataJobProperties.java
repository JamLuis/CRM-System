package com.ruoyi.crm.datajob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据作业（导入导出）配置属性
 *
 * @author ruoyi-crm
 */
@Component
@ConfigurationProperties(prefix = "crm.datajob")
public class DataJobProperties
{
    /**
     * 导入导出文件本地存储目录（相对或绝对路径）
     */
    private String storagePath = "./crm-datajob-files";

    /**
     * 导出文件保留时长（小时），超过后下载过期
     */
    private int exportRetentionHours = 24;

    /**
     * 单次导入最大行数
     */
    private int importMaxRows = 10000;

    public String getStoragePath()
    {
        return storagePath;
    }

    public void setStoragePath(String storagePath)
    {
        this.storagePath = storagePath;
    }

    public int getExportRetentionHours()
    {
        return exportRetentionHours;
    }

    public void setExportRetentionHours(int exportRetentionHours)
    {
        this.exportRetentionHours = exportRetentionHours;
    }

    public int getImportMaxRows()
    {
        return importMaxRows;
    }

    public void setImportMaxRows(int importMaxRows)
    {
        this.importMaxRows = importMaxRows;
    }
}
