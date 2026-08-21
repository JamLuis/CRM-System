package com.ruoyi.crm.followup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crm.file.minio")
public class MinioStorageProperties
{
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket = "crm-attachments";
    private int uploadUrlExpireSeconds = 900;
    private int downloadUrlExpireSeconds = 3600;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public int getUploadUrlExpireSeconds() { return uploadUrlExpireSeconds; }
    public void setUploadUrlExpireSeconds(int uploadUrlExpireSeconds) { this.uploadUrlExpireSeconds = uploadUrlExpireSeconds; }
    public int getDownloadUrlExpireSeconds() { return downloadUrlExpireSeconds; }
    public void setDownloadUrlExpireSeconds(int downloadUrlExpireSeconds) { this.downloadUrlExpireSeconds = downloadUrlExpireSeconds; }
}
