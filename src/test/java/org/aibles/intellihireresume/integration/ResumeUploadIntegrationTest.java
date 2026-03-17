package org.aibles.intellihireresume.integration;

import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.repository.ResumeFileRepository;
import org.aibles.intellihireresume.repository.ResumeParseJobRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.FileStorageService;
import org.aibles.intellihireresume.service.RedisJobQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cache.type=none",
                "management.health.redis.enabled=false",
                "management.health.diskspace.enabled=false"
        }
)
@AutoConfigureMockMvc
@Testcontainers
class ResumeUploadIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("resumes_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeFileRepository resumeFileRepository;

    @Autowired
    private ResumeParseJobRepository resumeParseJobRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private RedisJobQueueService redisJobQueueService;

    private Resume testResume;

    @BeforeEach
    void setUp() throws Exception {
        resumeFileRepository.deleteAll();
        resumeParseJobRepository.deleteAll();
        resumeRepository.deleteAll();

        testResume = new Resume();
        testResume.setUserId("user-integration-001");
        testResume.setTitle("Integration Test Resume");
        testResume.setStatus(ResumeStatus.UPLOADED);
        testResume.setIsActive(true);
        testResume = resumeRepository.save(testResume);

        when(fileStorageService.upload(anyString(), anyString(), any(InputStream.class),
                anyLong(), anyString()))
                .thenReturn(testResume.getId() + "/test-file.pdf");

        doNothing().when(redisJobQueueService).enqueue(anyString());
    }

    @Test
    void upload_ShouldReturn201_WhenValidPdfAndUserIdPresent() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test-resume.pdf",
                "application/pdf",
                new byte[1024]
        );

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResume.getId())
                        .file(pdfFile)
                        .header("X-User-Id", "user-integration-001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resume_id").value(testResume.getId()));
    }

    @Test
    void upload_ShouldReturn403_WhenXUserIdHeaderMissing() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test-resume.pdf",
                "application/pdf",
                new byte[1024]
        );

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResume.getId())
                        .file(pdfFile))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("COM_004"));
    }

    @Test
    void actuatorHealth_ShouldReturn200_WithoutXUserIdHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
