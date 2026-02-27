package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ParseJobRequest;
import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeParseJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeParseJobController.class)
@ActiveProfiles("test")
class ResumeParseJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeParseJobService resumeParseJobService;

    private ParseJobRequest createRequest;
    private ParseJobResponse jobResponse;
    private String testJobId;
    private String testResumeId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testJobId = "job-123";
        testResumeId = "resume-456";
        testUserId = "user-789";

        createRequest = ParseJobRequest.builder()
                .resumeId(testResumeId)
                .jobType(JobType.PARSE)
                .build();

        jobResponse = ParseJobResponse.builder()
                .id(testJobId)
                .resumeId(testResumeId)
                .status(JobStatus.QUEUED)
                .jobType(JobType.PARSE)
                .progress(0)
                .retryCount(0)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();
    }

    // CREATE TESTS

    @Test
    void createParseJob_ShouldReturn201_WhenValidRequest() throws Exception {
        when(resumeParseJobService.create(eq(testResumeId), eq(JobType.PARSE))).thenReturn(jobResponse);

        mockMvc.perform(post("/api/v1/parse-jobs")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testJobId))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.status").value("QUEUED"))
                .andExpect(jsonPath("$.data.job_type").value("PARSE"))
                .andExpect(jsonPath("$.data.progress").value(0));

        verify(resumeParseJobService).create(testResumeId, JobType.PARSE);
    }

    @Test
    void createParseJob_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(resumeParseJobService.create(eq(testResumeId), eq(JobType.PARSE)))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(post("/api/v1/parse-jobs")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("RES_001"));

        verify(resumeParseJobService).create(testResumeId, JobType.PARSE);
    }

    @Test
    void createParseJob_ShouldReturn400_WhenJobAlreadyRunning() throws Exception {
        when(resumeParseJobService.create(eq(testResumeId), eq(JobType.PARSE)))
                .thenThrow(new BadRequestException(ErrorCode.JOB_003));

        mockMvc.perform(post("/api/v1/parse-jobs")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("JOB_003"));

        verify(resumeParseJobService).create(testResumeId, JobType.PARSE);
    }

    @Test
    void createParseJob_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/parse-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeParseJobService, never()).create(any(), any());
    }

    // GET BY ID TESTS

    @Test
    void getParseJob_ShouldReturn200_WhenJobExists() throws Exception {
        when(resumeParseJobService.getById(testJobId)).thenReturn(jobResponse);

        mockMvc.perform(get("/api/v1/parse-jobs/{id}", testJobId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testJobId))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));

        verify(resumeParseJobService).getById(testJobId);
    }

    @Test
    void getParseJob_ShouldReturn404_WhenJobNotFound() throws Exception {
        when(resumeParseJobService.getById(testJobId))
                .thenThrow(new NotFoundException(ErrorCode.JOB_001));

        mockMvc.perform(get("/api/v1/parse-jobs/{id}", testJobId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("JOB_001"));

        verify(resumeParseJobService).getById(testJobId);
    }

    // CANCEL TESTS

    @Test
    void cancelParseJob_ShouldReturn200_WhenJobIsQueued() throws Exception {
        ParseJobResponse canceledResponse = ParseJobResponse.builder()
                .id(testJobId)
                .resumeId(testResumeId)
                .status(JobStatus.CANCELED)
                .jobType(JobType.PARSE)
                .progress(0)
                .retryCount(0)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 31, 0))
                .build();

        when(resumeParseJobService.cancel(testJobId)).thenReturn(canceledResponse);

        mockMvc.perform(put("/api/v1/parse-jobs/{id}/cancel", testJobId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testJobId))
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        verify(resumeParseJobService).cancel(testJobId);
    }

    @Test
    void cancelParseJob_ShouldReturn400_WhenJobIsSucceeded() throws Exception {
        when(resumeParseJobService.cancel(testJobId))
                .thenThrow(new BadRequestException(ErrorCode.JOB_002));

        mockMvc.perform(put("/api/v1/parse-jobs/{id}/cancel", testJobId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("JOB_002"));

        verify(resumeParseJobService).cancel(testJobId);
    }

    @Test
    void cancelParseJob_ShouldReturn404_WhenJobNotFound() throws Exception {
        when(resumeParseJobService.cancel(testJobId))
                .thenThrow(new NotFoundException(ErrorCode.JOB_001));

        mockMvc.perform(put("/api/v1/parse-jobs/{id}/cancel", testJobId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value("JOB_001"));

        verify(resumeParseJobService).cancel(testJobId);
    }
}
