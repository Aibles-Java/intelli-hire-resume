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
                .userId(testUserId)
                .title("Software Engineer Resume")
                .build();

        resumeResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.PROCESSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();
    }

    @Test
    void createResume_ShouldReturnCreatedStatus_WhenValidRequest() throws Exception {
        // Given
        when(resumeService.create(any(CreateResumeRequest.class))).thenReturn(resumeResponse);

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeId))
                .andExpect(jsonPath("$.data.user_id").value(testUserId))
                .andExpect(jsonPath("$.data.title").value("Software Engineer Resume"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.is_active").value(true));

        verify(resumeService).create(any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenUserIdIsBlank() throws Exception {
        // Given
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .userId("")
                .title("Software Engineer Resume")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenUserIdExceedsMaxLength() throws Exception {
        // Given
        String longUserId = "a".repeat(37); // Exceeds 36 character limit
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .userId(longUserId)
                .title("Software Engineer Resume")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenTitleExceedsMaxLength() throws Exception {
        // Given
        String longTitle = "a".repeat(256); // Exceeds 255 character limit
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .userId(testUserId)
                .title(longTitle)
                .build();

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(CreateResumeRequest.class));
    }

    @Test
    void getResumeById_ShouldReturnResumeResponse_WhenResumeExists() throws Exception {
        // Given
        when(resumeService.getById(testResumeId)).thenReturn(resumeResponse);

        // When & Then
        mockMvc.perform(get("/v1/resumes/{id}", testResumeId))
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
        // Given
        when(resumeService.getById(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        // When & Then
        mockMvc.perform(get("/v1/resumes/{id}", testResumeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));

        verify(resumeService).getById(testResumeId);
    }

    @Test
    void getResumesByUserId_ShouldReturnListOfResumes_WhenResumesExist() throws Exception {
        // Given
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

        // When & Then
        mockMvc.perform(get("/v1/resumes")
                        .param("userId", testUserId))
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
    void updateResume_ShouldReturnUpdatedResume_WhenValidRequest() throws Exception {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .userId(testUserId)
                .title("Updated Resume Title")
                .build();

        ResumeResponse updatedResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Updated Resume Title")
                .status(ResumeStatus.PROCESSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/v1/resumes/{id}", testResumeId)
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
    void deleteResume_ShouldReturnSuccessResponse_WhenResumeExists() throws Exception {
        // Given
        doNothing().when(resumeService).delete(testResumeId);

        // When & Then
        mockMvc.perform(delete("/v1/resumes/{id}", testResumeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        verify(resumeService).delete(testResumeId);
    }

    @Test
    void triggerReprocess_ShouldReturnResumeResponse_WhenResumeExists() throws Exception {
        // Given
        ResumeResponse reprocessedResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.PROCESSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 30, 0))
                .build();

        when(resumeService.reprocess(testResumeId)).thenReturn(reprocessedResponse);

        // When & Then
        mockMvc.perform(post("/v1/resumes/{id}/reprocess", testResumeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeId))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        verify(resumeService).reprocess(testResumeId);
    }

    @Test
    void createResume_ShouldAcceptNullTitle() throws Exception {
        // Given
        CreateResumeRequest requestWithoutTitle = CreateResumeRequest.builder()
                .userId(testUserId)
                .title(null)
                .build();

        ResumeResponse responseWithoutTitle = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.PROCESSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();

        when(resumeService.create(any(CreateResumeRequest.class))).thenReturn(responseWithoutTitle);

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutTitle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").doesNotExist());

        verify(resumeService).create(any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .userId(testUserId)
                .title("Updated Title")
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        // When & Then
        mockMvc.perform(put("/v1/resumes/{id}", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume not found"));

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void deleteResume_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        // Given
        doThrow(new NotFoundException(ErrorCode.RES_001))
                .when(resumeService).delete(testResumeId);

        // When & Then
        mockMvc.perform(delete("/v1/resumes/{id}", testResumeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume not found"));

        verify(resumeService).delete(testResumeId);
    }

    @Test
    void triggerReprocess_ShouldReturnNotFound_WhenResumeNotExists() throws Exception {
        // Given
        when(resumeService.reprocess(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        // When & Then
        mockMvc.perform(post("/v1/resumes/{id}/reprocess", testResumeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume not found"));

        verify(resumeService).reprocess(testResumeId);
    }

    @Test
    void createResume_ShouldReturnConflict_WhenTitleAlreadyExists() throws Exception {
        // Given
        when(resumeService.create(any(CreateResumeRequest.class)))
                .thenThrow(new DuplicateException(ErrorCode.RES_004));

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume title already exists"));

        verify(resumeService).create(any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnConflict_WhenTitleAlreadyExists() throws Exception {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .userId(testUserId)
                .title("Existing Title")
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenThrow(new DuplicateException(ErrorCode.RES_004));

        // When & Then
        mockMvc.perform(put("/v1/resumes/{id}", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Resume title already exists"));

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void getResumesByUserId_ShouldReturnEmptyList_WhenNoResumesFound() throws Exception {
        // Given
        when(resumeService.getByUserId(testUserId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/v1/resumes")
                        .param("userId", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(resumeService).getByUserId(testUserId);
    }

    @Test
    void getResumesByUserId_ShouldReturnBadRequest_WhenUserIdIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/resumes"))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).getByUserId(any());
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenRequestBodyIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(CreateResumeRequest.class));
    }

    @Test
    void createResume_ShouldReturnBadRequest_WhenUserIdIsNull() throws Exception {
        // Given
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .userId(null)
                .title("Test Title")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).create(any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnBadRequest_WhenUserIdIsBlank() throws Exception {
        // Given
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .userId("")
                .title("Updated Title")
                .build();

        // When & Then
        mockMvc.perform(put("/v1/resumes/{id}", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).update(any(), any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldReturnBadRequest_WhenTitleExceedsMaxLength() throws Exception {
        // Given
        String longTitle = "a".repeat(256);
        CreateResumeRequest invalidRequest = CreateResumeRequest.builder()
                .userId(testUserId)
                .title(longTitle)
                .build();

        // When & Then
        mockMvc.perform(put("/v1/resumes/{id}", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(resumeService, never()).update(any(), any(CreateResumeRequest.class));
    }

    @Test
    void updateResume_ShouldAcceptNullTitle() throws Exception {
        // Given
        CreateResumeRequest requestWithNullTitle = CreateResumeRequest.builder()
                .userId(testUserId)
                .title(null)
                .build();

        ResumeResponse responseWithNullTitle = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.PROCESSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        when(resumeService.update(eq(testResumeId), any(CreateResumeRequest.class)))
                .thenReturn(responseWithNullTitle);

        // When & Then
        mockMvc.perform(put("/v1/resumes/{id}", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNullTitle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").doesNotExist());

        verify(resumeService).update(eq(testResumeId), any(CreateResumeRequest.class));
    }

    @Test
    void allEndpoints_ShouldHaveCORSEnabled() throws Exception {
        // Given
        when(resumeService.getById(testResumeId)).thenReturn(resumeResponse);

        // When & Then
        mockMvc.perform(get("/v1/resumes/{id}", testResumeId)
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}