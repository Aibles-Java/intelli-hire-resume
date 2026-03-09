package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ResumeSkillResponse;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeSkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeSkillController.class)
@ActiveProfiles("test")
class ResumeSkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeSkillService resumeSkillService;

    private String testResumeId;
    private String testUserId;
    private String testResumeSkillId;
    private ResumeSkillResponse skillResponse;
    private ResumeSkillRequest validRequest;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";
        testResumeSkillId = "resume-skill-789";

        skillResponse = ResumeSkillResponse.builder()
                .id(testResumeSkillId)
                .resumeId(testResumeId)
                .skillId("skill-001")
                .proficiencyLevel("ADVANCED")
                .yearsExperience(new BigDecimal("3.5"))
                .confidenceScore(new BigDecimal("0.90"))
                .isPrimary(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        validRequest = ResumeSkillRequest.builder()
                .skillId("skill-001")
                .proficiencyLevel("ADVANCED")
                .yearsExperience(new BigDecimal("3.5"))
                .confidenceScore(new BigDecimal("0.90"))
                .isPrimary(true)
                .build();
    }

    @Test
    void list_ShouldReturn200_WhenValidRequest() throws Exception {
        when(resumeSkillService.list(testResumeId)).thenReturn(List.of(skillResponse));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/skills", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(testResumeSkillId))
                .andExpect(jsonPath("$.data[0].skill_id").value("skill-001"));

        verify(resumeSkillService).list(testResumeId);
    }

    @Test
    void list_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{resumeId}/skills", testResumeId))
                .andExpect(status().isBadRequest());

        verify(resumeSkillService, never()).list(any());
    }

    @Test
    void create_ShouldReturn201_WhenValidRequest() throws Exception {
        when(resumeSkillService.create(eq(testResumeId), any(ResumeSkillRequest.class)))
                .thenReturn(skillResponse);

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/skills", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeSkillId))
                .andExpect(jsonPath("$.data.is_primary").value(true));

        verify(resumeSkillService).create(eq(testResumeId), any(ResumeSkillRequest.class));
    }

    @Test
    void create_ShouldReturn400_WhenSkillIdIsBlank() throws Exception {
        ResumeSkillRequest invalidRequest = ResumeSkillRequest.builder()
                .skillId("")
                .build();

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/skills", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()));

        verify(resumeSkillService, never()).create(any(), any());
    }

    @Test
    void create_ShouldReturn404_WhenSkillNotInCatalog() throws Exception {
        when(resumeSkillService.create(eq(testResumeId), any(ResumeSkillRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.SKILL_001));

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/skills", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.SKILL_001.getCode()));
    }

    @Test
    void create_ShouldReturn400_WhenSkillAlreadyAdded() throws Exception {
        when(resumeSkillService.create(eq(testResumeId), any(ResumeSkillRequest.class)))
                .thenThrow(new BadRequestException(ErrorCode.RESUME_SKILL_002));

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/skills", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RESUME_SKILL_002.getCode()));
    }

    @Test
    void update_ShouldReturn200_WhenValidRequest() throws Exception {
        when(resumeSkillService.update(eq(testResumeId), eq(testResumeSkillId), any(ResumeSkillRequest.class)))
                .thenReturn(skillResponse);

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/skills/{id}", testResumeId, testResumeSkillId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testResumeSkillId));

        verify(resumeSkillService).update(eq(testResumeId), eq(testResumeSkillId), any(ResumeSkillRequest.class));
    }

    @Test
    void delete_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(resumeSkillService).delete(testResumeId, testResumeSkillId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/skills/{id}", testResumeId, testResumeSkillId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(resumeSkillService).delete(testResumeId, testResumeSkillId);
    }

    @Test
    void delete_ShouldReturn404_WhenSkillNotFound() throws Exception {
        doThrow(new NotFoundException(ErrorCode.RESUME_SKILL_001))
                .when(resumeSkillService).delete(testResumeId, testResumeSkillId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/skills/{id}", testResumeId, testResumeSkillId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RESUME_SKILL_001.getCode()));
    }
}
