package org.aibles.intellihireresume.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.service.FileStorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;

    @Override
    public String upload(String bucket, String objectKey, InputStream inputStream, long size, String contentType) {
        log.info("Uploading file to MinIO: bucket={}, key={}", bucket, objectKey);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("File uploaded successfully to MinIO: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: bucket={}, key={}, error={}", bucket, objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to storage: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String bucket, String objectKey) {
        log.info("Downloading file from MinIO: bucket={}, key={}", bucket, objectKey);
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: bucket={}, key={}, error={}", bucket, objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to download file from storage: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        log.info("Deleting file from MinIO: bucket={}, key={}", bucket, objectKey);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            log.info("File deleted successfully from MinIO: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: bucket={}, key={}, error={}", bucket, objectKey, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from storage: " + e.getMessage(), e);
        }
    }
}
