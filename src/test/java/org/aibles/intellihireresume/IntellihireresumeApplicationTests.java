package org.aibles.intellihireresume;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.aibles.intellihireresume.service.FileStorageService;
import org.aibles.intellihireresume.service.RedisJobQueueService;

@SpringBootTest(properties = {
        "spring.cache.type=none",
        "management.health.redis.enabled=false",
        "management.health.diskspace.enabled=false"
})
@Testcontainers
class IntellihireresumeApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("resumes_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private RedisJobQueueService redisJobQueueService;

    @Test
    void contextLoads() {
    }

}
