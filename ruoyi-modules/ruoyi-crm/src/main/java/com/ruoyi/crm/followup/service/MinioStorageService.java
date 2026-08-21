package com.ruoyi.crm.followup.service;

import com.ruoyi.crm.followup.config.MinioStorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService
{
    private final MinioStorageProperties properties;
    private final MinioClient client;

    @Autowired
    public MinioStorageService(MinioStorageProperties properties)
    {
        this.properties = properties;
        if (isBlank(properties.getEndpoint())
                || isBlank(properties.getAccessKey())
                || isBlank(properties.getSecretKey()))
        {
            throw new IllegalStateException("MinIO 配置不完整");
        }
        this.client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    public String createUploadUrl(String storageKey)
    {
        try
        {
            ensureBucket();
            return client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(properties.getBucket())
                            .object(storageKey)
                            .expiry(properties.getUploadUrlExpireSeconds(), TimeUnit.SECONDS)
                            .build());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("生成 MinIO 上传地址失败", e);
        }
    }

    public String createDownloadUrl(String storageKey)
    {
        try
        {
            return client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(storageKey)
                            .expiry(properties.getDownloadUrlExpireSeconds(), TimeUnit.SECONDS)
                            .build());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("生成 MinIO 下载地址失败", e);
        }
    }

    public StatObjectResponse stat(String storageKey)
    {
        try
        {
            return client.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(storageKey)
                    .build());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("MinIO 文件不存在或不可访问", e);
        }
    }

    private synchronized void ensureBucket() throws Exception
    {
        boolean exists = client.bucketExists(
                BucketExistsArgs.builder().bucket(properties.getBucket()).build());
        if (!exists)
        {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
        }
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
