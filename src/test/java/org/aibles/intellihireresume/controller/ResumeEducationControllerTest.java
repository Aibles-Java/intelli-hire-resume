package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeEducationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeEducationController.class)
class ResumeEducationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeEducationService educationService;

    private String testResumeId;
    private String testUserId;
    private String testEducationId;
    private ResumeEducationResponse educationResponse;
    private ResumeEducationRequest validRequest;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";
        testEducationId = "edu-789";

        educationResponse = ResumeEducationResponse.builder()
                .id(testEducationId)
                .resumeId(testResumeId)
                .school("Hanoi University of Technology")
                .degree("Bachelor")
                .field("Computer Science")
                .startYear(2018)
                .endYear(2022)
                .description("Graduated with honors")
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        validRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .degree("Bachelor")
                .field("Computer Science")
                .startYear(2018)
                .endYear(2022)
                .description("Graduated with honors")
                .build();
    }

    // LIST TESTS

    @Test
    void list_ShouldReturn200_WhenValidRequest() throws Exception {
        when(educationService.list(testResumeId)).thenReturn(List.of(educationResponse));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(testEducationId))
                .andExpect(jsonPath("$.data[0].resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data[0].school").value("Hanoi University of Technology"));

        verify(educationService).list(testResumeId);
    }

    @Test
    void list_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{resumeId}/educations", testResumeId))
                .andExpect(status().isForbidden());

        verify(educationService, never()).list(any());
    }

    @Test
    void list_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(educationService.list(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(educationService).list(testResumeId);
    }

    // CREATE TESTS

    @Test
    void create_ShouldReturn201_WhenValidRequest() throws Exception {
        when(educationService.create(eq(testResumeId), any(ResumeEducationRequest.class)))
                .thenReturn(educationResponse);

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testEducationId))
                .andExpect(jsonPath("$.data.school").value("Hanoi University of Technology"))
                .andExpect(jsonPath("$.data.degree").value("Bachelor"));

        verify(educationService).create(eq(testResumeId), any(ResumeEducationRequest.class));
    }

    @Test
    void create_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(educationService, never()).create(any(), any());
    }

    @Test
    void create_ShouldReturn400_WhenSchoolIsBlank() throws Exception {
        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("")
                .degree("Bachelor")
                .build();

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()));

        verify(educationService, never()).create(any(), any());
    }

    @Test
    void create_ShouldReturn400_WhenInvalidYears() throws Exception {
        when(educationService.create(eq(testResumeId), any(ResumeEducationRequest.class)))
                .thenThrow(new BadRequestException(ErrorCode.EDU_002));

        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .startYear(2022)
                .endYear(2018)
                .build();

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EDU_002.getCode()));

        verify(educationService).create(eq(testResumeId), any(ResumeEducationRequest.class));
    }

    @Test
    void create_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(educationService.create(eq(testResumeId), any(ResumeEducationRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/educations", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(educationService).create(eq(testResumeId), any(ResumeEducationRequest.class));
    }

    // UPDATE TESTS

    @Test
    void update_ShouldReturn200_WhenValidRequest() throws Exception {
        ResumeEducationResponse updatedResponse = ResumeEducationResponse.builder()
                .id(testEducationId)
                .resumeId(testResumeId)
                .school("Vietnam National University")
                .degree("Master")
                .build();

        when(educationService.update(eq(testResumeId), eq(testEducationId), any(ResumeEducationRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/educations/{id}", testResumeId, testEducationId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testEducationId))
                .andExpect(jsonPath("$.data.school").value("Vietnam National University"));

        verify(educationService).update(eq(testResumeId), eq(testEducationId), any(ResumeEducationRequest.class));
    }

    @Test
    void update_ShouldReturn404_WhenEducationNotFound() throws Exception {
        when(educationService.update(eq(testResumeId), eq(testEducationId), any(ResumeEducationRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.EDU_001));

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/educations/{id}", testResumeId, testEducationId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EDU_001.getCode()));

        verify(educationService).update(eq(testResumeId), eq(testEducationId), any(ResumeEducationRequest.class));
    }

    @Test
    void update_ShouldReturn404_WhenEduDoesNotBelongToResume() throws Exception {
        when(educationService.update(eq(testResumeId), eq(testEducationId), any(ResumeEducationRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.EDU_003));

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/educations/{id}", testResumeId, testEducationId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EDU_003.getCode()));

        verify(educationService).update(eq(testResumeId), eq(testEducationId), any(ResumeEducationRequest.class));
    }

    // DELETE TESTS

    @Test
    void delete_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(educationService).delete(testResumeId, testEducationId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/educations/{id}", testResumeId, testEducationId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(educationService).delete(testResumeId, testEducationId);
    }

    @Test
    void delete_ShouldReturn404_WhenEducationNotFound() throws Exception {
        doThrow(new NotFoundException(ErrorCode.EDU_001))
                .when(educationService).delete(testResumeId, testEducationId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/educations/{id}", testResumeId, testEducationId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EDU_001.getCode()));

        verify(educationService).delete(testResumeId, testEducationId);
    }

    @Test
    void delete_ShouldReturn404_WhenEduDoesNotBelongToResume() throws Exception {
        doThrow(new NotFoundException(ErrorCode.EDU_003))
                .when(educationService).delete(testResumeId, testEducationId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/educations/{id}", testResumeId, testEducationId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EDU_003.getCode()));

        verify(educationService).delete(testResumeId, testEducationId);
    }
}
