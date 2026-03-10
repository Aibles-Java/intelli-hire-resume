package org.aibles.intellihireresume.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(httpClient)
                .build();
    }

    @Bean
    public MinioInitializer minioInitializer(MinioClient minioClient) {
        return new MinioInitializer(minioClient, bucketName);
    }

    @Slf4j
    public static class MinioInitializer {

        private final MinioClient minioClient;
        private final String bucketName;

        public MinioInitializer(MinioClient minioClient, String bucketName) {
            this.minioClient = minioClient;
            this.bucketName = bucketName;
        }

        @PostConstruct
        public void init() {
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("MinIO bucket created: {}", bucketName);
                } else {
                    log.info("MinIO bucket already exists: {}", bucketName);
                }
            } catch (Exception e) {
                log.warn("MinIO bucket initialization failed for bucket '{}'. File operations will be unavailable: {}", bucketName, e.getMessage());
            }
        }
    }
}
