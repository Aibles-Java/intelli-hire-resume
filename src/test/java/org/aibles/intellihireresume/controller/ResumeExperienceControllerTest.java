package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeExperienceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeExperienceController.class)
@ActiveProfiles("test")
class ResumeExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeExperienceService experienceService;

    private String testResumeId;
    private String testUserId;
    private String testExperienceId;
    private ResumeExperienceResponse experienceResponse;
    private ResumeExperienceRequest validRequest;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";
        testExperienceId = "exp-789";

        experienceResponse = ResumeExperienceResponse.builder()
                .id(testExperienceId)
                .resumeId(testResumeId)
                .company("Acme Corp")
                .title("Software Engineer")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .description("Developed backend systems")
                .isCurrent(false)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        validRequest = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Software Engineer")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .description("Developed backend systems")
                .isCurrent(false)
                .build();
    }

    // LIST TESTS

    @Test
    void list_ShouldReturn200_WhenValidRequest() throws Exception {
        when(experienceService.list(testResumeId)).thenReturn(List.of(experienceResponse));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(testExperienceId))
                .andExpect(jsonPath("$.data[0].resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data[0].company").value("Acme Corp"));

        verify(experienceService).list(testResumeId);
    }

    @Test
    void list_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{resumeId}/experiences", testResumeId))
                .andExpect(status().isBadRequest());

        verify(experienceService, never()).list(any());
    }

    @Test
    void list_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(experienceService.list(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(experienceService).list(testResumeId);
    }

    // CREATE TESTS

    @Test
    void create_ShouldReturn201_WhenValidRequest() throws Exception {
        when(experienceService.create(eq(testResumeId), any(ResumeExperienceRequest.class)))
                .thenReturn(experienceResponse);

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testExperienceId))
                .andExpect(jsonPath("$.data.company").value("Acme Corp"))
                .andExpect(jsonPath("$.data.title").value("Software Engineer"));

        verify(experienceService).create(eq(testResumeId), any(ResumeExperienceRequest.class));
    }

    @Test
    void create_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(experienceService, never()).create(any(), any());
    }

    @Test
    void create_ShouldReturn400_WhenCompanyIsBlank() throws Exception {
        ResumeExperienceRequest invalidRequest = ResumeExperienceRequest.builder()
                .company("")
                .title("Engineer")
                .build();

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()));

        verify(experienceService, never()).create(any(), any());
    }

    @Test
    void create_ShouldReturn400_WhenInvalidDates() throws Exception {
        when(experienceService.create(eq(testResumeId), any(ResumeExperienceRequest.class)))
                .thenThrow(new BadRequestException(ErrorCode.EXP_002));

        ResumeExperienceRequest invalidRequest = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Engineer")
                .startDate(LocalDate.of(2023, 12, 31))
                .endDate(LocalDate.of(2022, 1, 1))
                .isCurrent(false)
                .build();

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EXP_002.getCode()));

        verify(experienceService).create(eq(testResumeId), any(ResumeExperienceRequest.class));
    }

    @Test
    void create_ShouldReturn400_WhenIsCurrentTrueAndEndDateNotNull() throws Exception {
        when(experienceService.create(eq(testResumeId), any(ResumeExperienceRequest.class)))
                .thenThrow(new BadRequestException(ErrorCode.EXP_002));

        ResumeExperienceRequest invalidRequest = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Engineer")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .isCurrent(true)
                .build();

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EXP_002.getCode()));

        verify(experienceService).create(eq(testResumeId), any(ResumeExperienceRequest.class));
    }

    @Test
    void create_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(experienceService.create(eq(testResumeId), any(ResumeExperienceRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/experiences", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(experienceService).create(eq(testResumeId), any(ResumeExperienceRequest.class));
    }

    // UPDATE TESTS

    @Test
    void update_ShouldReturn200_WhenValidRequest() throws Exception {
        ResumeExperienceResponse updatedResponse = ResumeExperienceResponse.builder()
                .id(testExperienceId)
                .resumeId(testResumeId)
                .company("New Corp")
                .title("Senior Engineer")
                .build();

        when(experienceService.update(eq(testResumeId), eq(testExperienceId), any(ResumeExperienceRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/experiences/{id}", testResumeId, testExperienceId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testExperienceId))
                .andExpect(jsonPath("$.data.company").value("New Corp"));

        verify(experienceService).update(eq(testResumeId), eq(testExperienceId), any(ResumeExperienceRequest.class));
    }

    @Test
    void update_ShouldReturn404_WhenExperienceNotFound() throws Exception {
        when(experienceService.update(eq(testResumeId), eq(testExperienceId), any(ResumeExperienceRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.EXP_001));

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/experiences/{id}", testResumeId, testExperienceId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EXP_001.getCode()));

        verify(experienceService).update(eq(testResumeId), eq(testExperienceId), any(ResumeExperienceRequest.class));
    }

    @Test
    void update_ShouldReturn404_WhenExpDoesNotBelongToResume() throws Exception {
        when(experienceService.update(eq(testResumeId), eq(testExperienceId), any(ResumeExperienceRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.EXP_003));

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/experiences/{id}", testResumeId, testExperienceId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EXP_003.getCode()));

        verify(experienceService).update(eq(testResumeId), eq(testExperienceId), any(ResumeExperienceRequest.class));
    }

    // DELETE TESTS

    @Test
    void delete_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(experienceService).delete(testResumeId, testExperienceId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/experiences/{id}", testResumeId, testExperienceId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(experienceService).delete(testResumeId, testExperienceId);
    }

    @Test
    void delete_ShouldReturn404_WhenExperienceNotFound() throws Exception {
        doThrow(new NotFoundException(ErrorCode.EXP_001))
                .when(experienceService).delete(testResumeId, testExperienceId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/experiences/{id}", testResumeId, testExperienceId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EXP_001.getCode()));

        verify(experienceService).delete(testResumeId, testExperienceId);
    }

    @Test
    void delete_ShouldReturn404_WhenExpDoesNotBelongToResume() throws Exception {
        doThrow(new NotFoundException(ErrorCode.EXP_003))
                .when(experienceService).delete(testResumeId, testExperienceId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/experiences/{id}", testResumeId, testExperienceId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.EXP_003.getCode()));

        verify(experienceService).delete(testResumeId, testExperienceId);
    }
}
