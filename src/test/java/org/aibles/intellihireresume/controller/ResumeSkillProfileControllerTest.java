package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.dto.UpdateResumeSkillProfileRequest;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeSkillProfileService;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeSkillProfileController.class)
@ActiveProfiles("test")
class ResumeSkillProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeSkillProfileService profileService;

    private String testResumeId;
    private String testUserId;
    private ResumeSkillProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";

        Map<String, Object> topSkills = new LinkedHashMap<>();
        topSkills.put("Java", 0.90);
        topSkills.put("Spring Boot", 0.85);

        profileResponse = ResumeSkillProfileResponse.builder()
                .id("profile-001")
                .resumeId(testResumeId)
                .topSkills(topSkills)
                .yearsEstimated(new BigDecimal("3.5"))
                .seniority("MID")
                .summary("Experienced backend developer")
                .generatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();
    }

    @Test
    void getByResumeId_ShouldReturn200_WhenProfileExists() throws Exception {
        when(profileService.getByResumeId(testResumeId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/skill-profile", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("profile-001"))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.seniority").value("MID"));

        verify(profileService).getByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldReturn404_WhenProfileNotFound() throws Exception {
        when(profileService.getByResumeId(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.PROFILE_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/skill-profile", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.PROFILE_001.getCode()));
    }

    @Test
    void getByResumeId_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{resumeId}/skill-profile", testResumeId))
                .andExpect(status().isBadRequest());

        verify(profileService, never()).getByResumeId(any());
    }

    @Test
    void generate_ShouldReturn200_WhenValid() throws Exception {
        when(profileService.generate(testResumeId)).thenReturn(profileResponse);

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/skill-profile/generate", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.seniority").value("MID"))
                .andExpect(jsonPath("$.data.years_estimated").value(3.5));

        verify(profileService).generate(testResumeId);
    }

    @Test
    void generate_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(profileService.generate(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(post("/api/v1/resumes/{resumeId}/skill-profile/generate", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));
    }

    @Test
    void update_ShouldReturn200_WhenValid() throws Exception {
        UpdateResumeSkillProfileRequest request = UpdateResumeSkillProfileRequest.builder()
                .summary("Updated summary")
                .signals(Map.of("key", "value"))
                .build();

        when(profileService.update(eq(testResumeId), any(UpdateResumeSkillProfileRequest.class)))
                .thenReturn(profileResponse);

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/skill-profile", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("profile-001"));

        verify(profileService).update(eq(testResumeId), any(UpdateResumeSkillProfileRequest.class));
    }
}
