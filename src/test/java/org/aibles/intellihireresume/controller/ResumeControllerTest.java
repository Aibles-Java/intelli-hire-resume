package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeController.class)
@ActiveProfiles("test")
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeService resumeService;

    private CreateResumeRequest createRequest;
    private ResumeResponse resumeResponse;
    private String testResumeId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";

        createRequest = CreateResumeRequest.builder()
                .title("Software Engineer Resume")
                .build();

        resumeResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();
    }

    @Test
    void createResume_ShouldReturnCreatedStatus_WhenValidRequest() throws Exception {
        when(resumeService.create(eq(testUserId), any(CreateResumeRequest.class))).thenReturn(resumeResponse);

        mockMvc.perform(post("/api/v1/resumes")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeId))
                .andExpect(jsonPath("$.data.user_id").value(testUserId))
                .andExpect(jsonPath("$.data.title").value("Software Engineer Resume"))
                .andExpect(jsonPath("$.data.status").value("UPLOADED"))
                .andExpect(jsonPath("$.data.is_active").value(true));

        verify(resumeService).create(eq(testUserId), any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(), any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenTitleExceedsMaxLength() throws Exception {
        String longTitle = "a".repeat(256);
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .title(longTitle)
                .build();

        mockMvc.perform(post("/api/v1/resumes")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(), any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldAcceptNullTitle() throws Exception {
        CreateResumeRequest requestWithoutTitle = CreateResumeRequest.builder()
                .title(null)
                .build();

        ResumeResponse responseWithoutTitle = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();

        when(resumeService.create(eq(testUserId), any(CreateResumeRequest.class))).thenReturn(responseWithoutTitle);

        mockMvc.perform(post("/api/v1/resumes")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutTitle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").doesNotExist());

        verify(resumeService).create(eq(testUserId), any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnConflict_WhenTitleAlreadyExists() throws Exception {
        when(resumeService.create(eq(testUserId), any(CreateResumeRequest.class)))
                .thenThrow(new DuplicateException(ErrorCode.RES_004));

        mockMvc.perform(post("/api/v1/resumes")
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume title already exists"));

        verify(resumeService).create(eq(testUserId), any(CreateResumeRequest.class));
    }

    @Test
    void getResumeById_ShouldReturnResumeResponse_WhenResumeExists() throws Exception {
        when(resumeService.getById(testResumeId)).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeId))
                .andExpect(jsonPath("$.data.user_id").value(testUserId))
                .andExpect(jsonPath("$.data.title").value("Software Engineer Resume"));

        verify(resumeService).getById(testResumeId);
    }

    @Test
    void getResumeById_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        when(resumeService.getById(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(get("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));

        verify(resumeService).getById(testResumeId);
    }

    @Test
    void getResumesByUserId_ShouldReturnListOfResumes_WhenResumesExist() throws Exception {
        ResumeResponse secondResume = ResumeResponse.builder()
                .id("resume-789")
                .userId(testUserId)
                .title("Another Resume")
                .status(ResumeStatus.COMPLETED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        List<ResumeResponse> resumeList = Arrays.asList(resumeResponse, secondResume);
        when(resumeService.getByUserId(testUserId)).thenReturn(resumeList);

        mockMvc.perform(get("/api/v1/resumes")
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(testResumeId))
                .andExpect(jsonPath("$.data[1].id").value("resume-789"));

        verify(resumeService).getByUserId(testUserId);
    }

    @Test
    void getResumesByUserId_ShouldReturnEmptyList_WhenNoResumesFound() throws Exception {
        when(resumeService.getByUserId(testUserId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resumes")
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(resumeService).getByUserId(testUserId);
    }

    @Test
    void getResumesByUserId_ShouldReturnBadRequest_WhenXUserIdHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/resumes"))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).getByUserId(any());
    }

    @Test
    void updateResume_ShouldReturnUpdatedResume_WhenValidRequest() throws Exception {
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("Updated Resume Title")
                .build();

        ResumeResponse updatedResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Updated Resume Title")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeId))
                .andExpect(jsonPath("$.data.title").value("Updated Resume Title"));

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("Updated Title")
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(put("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume not found"));

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnConflict_WhenTitleAlreadyExists() throws Exception {
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("Existing Title")
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenThrow(new DuplicateException(ErrorCode.RES_004));

        mockMvc.perform(put("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume title already exists"));

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnBadRequest_WhenTitleExceedsMaxLength() throws Exception {
        String longTitle = "a".repeat(256);
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .title(longTitle)
                .build();

        mockMvc.perform(put("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).update(any(), any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldAcceptNullTitle() throws Exception {
        CreateResumeRequest requestWithNullTitle = CreateResumeRequest.builder()
                .title(null)
                .build();

        ResumeResponse responseWithNullTitle = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenReturn(responseWithNullTitle);

        mockMvc.perform(put("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNullTitle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").doesNotExist());

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void deleteResume_ShouldReturnSuccessResponse_WhenResumeExists() throws Exception {
        doNothing().when(resumeService).delete(testResumeId);

        mockMvc.perform(delete("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        verify(resumeService).delete(testResumeId);
    }

    @Test
    void deleteResume_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        doThrow(new NotFoundException(ErrorCode.RES_001))
                .when(resumeService).delete(testResumeId);

        mockMvc.perform(delete("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume not found"));

        verify(resumeService).delete(testResumeId);
    }

    @Test
    void triggerReprocess_ShouldReturnResumeResponse_WhenResumeExists() throws Exception {
        ResumeResponse reprocessedResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.PARSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 30, 0))
                .build();

        when(resumeService.reprocess(testResumeId)).thenReturn(reprocessedResponse);

        mockMvc.perform(post("/api/v1/resumes/{id}/reprocess", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeId))
                .andExpect(jsonPath("$.data.status").value("PARSING"));

        verify(resumeService).reprocess(testResumeId);
    }

    @Test
    void triggerReprocess_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        when(resumeService.reprocess(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(post("/api/v1/resumes/{id}/reprocess", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume not found"));

        verify(resumeService).reprocess(testResumeId);
    }

    @Test
    void allEndpoints_ShouldHaveCORSEnabled() throws Exception {
        when(resumeService.getById(testResumeId)).thenReturn(resumeResponse);

        mockMvc.perform(get("/api/v1/resumes/{id}", testResumeId)
                        .header("X-User-Id", testUserId)
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
