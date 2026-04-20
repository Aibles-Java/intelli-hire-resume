package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.dto.UpdateResumeSkillProfileRequest;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeSkill;
import org.aibles.intellihireresume.entity.ResumeSkillProfile;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.entity.enums.SeniorityLevel;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeSkillProfileMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.repository.ResumeSkillProfileRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeSkillProfileServiceImplTest {

    @Mock
    private ResumeSkillProfileRepository profileRepository;

    @Mock
    private ResumeSkillRepository resumeSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeSkillProfileMapper profileMapper = new ResumeSkillProfileMapper();

    private ResumeSkillProfileServiceImpl profileService;

    private String testResumeId;
    private Resume testResume;
    private ResumeSkillProfile testProfile;
    private ResumeSkill testResumeSkill;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        profileService = new ResumeSkillProfileServiceImpl(
                profileRepository, resumeSkillRepository, skillRepository, resumeRepository, profileMapper);

        testResumeId = "resume-123";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId("user-456")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        testProfile = new ResumeSkillProfile();
        testProfile.setId("profile-001");
        testProfile.setResumeId(testResumeId);
        testProfile.setYearsEstimated(new BigDecimal("3.5"));
        testProfile.setSeniority(SeniorityLevel.MID);
        testProfile.setSummary("Experienced backend developer");
        testProfile.setGeneratedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testProfile.setCreatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testProfile.setUpdatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));

        testSkill = new Skill();
        testSkill.setId("skill-001");
        testSkill.setName("Java");

        testResumeSkill = new ResumeSkill();
        testResumeSkill.setId("resume-skill-001");
        testResumeSkill.setResumeId(testResumeId);
        testResumeSkill.setSkillId("skill-001");
        testResumeSkill.setYearsExperience(new BigDecimal("3.5"));
        testResumeSkill.setConfidenceScore(new BigDecimal("0.90"));
        testResumeSkill.setIsPrimary(true);
    }

    // GET TESTS

    @Test
    void getByResumeId_ShouldReturnProfile_WhenExists() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testProfile));

        ResumeSkillProfileResponse result = profileService.getByResumeId(testResumeId);

        assertThat(result).isNotNull();
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getSeniority()).isEqualTo("MID");
        verify(profileRepository).findByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldThrowNotFoundException_WhenProfileNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getByResumeId(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROFILE_001.getCode());
    }

    @Test
    void getByResumeId_ShouldThrowNotFoundException_WhenResumeNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getByResumeId(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(profileRepository, never()).findByResumeId(any());
    }

    // GENERATE TESTS

    @Test
    void generate_ShouldCreateProfile_WhenNoExistingProfile() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findByResumeIdOrderByConfidenceScoreDesc(testResumeId))
                .thenReturn(List.of(testResumeSkill));
        when(skillRepository.findAllById(anyList())).thenReturn(List.of(testSkill));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(ResumeSkillProfile.class))).thenReturn(testProfile);

        ResumeSkillProfileResponse result = profileService.generate(testResumeId);

        assertThat(result).isNotNull();
        verify(profileRepository).save(any(ResumeSkillProfile.class));
    }

    @Test
    void generate_ShouldUpdateExistingProfile_WhenProfileExists() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findByResumeIdOrderByConfidenceScoreDesc(testResumeId))
                .thenReturn(List.of(testResumeSkill));
        when(skillRepository.findAllById(anyList())).thenReturn(List.of(testSkill));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(ResumeSkillProfile.class))).thenReturn(testProfile);

        ResumeSkillProfileResponse result = profileService.generate(testResumeId);

        assertThat(result).isNotNull();
        verify(profileRepository).save(testProfile);
    }

    @Test
    void generate_ShouldSetJuniorSeniority_WhenYearsLessThan2() {
        testResumeSkill.setYearsExperience(new BigDecimal("1.0"));

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findByResumeIdOrderByConfidenceScoreDesc(testResumeId))
                .thenReturn(List.of(testResumeSkill));
        when(skillRepository.findAllById(anyList())).thenReturn(List.of(testSkill));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        ResumeSkillProfile savedProfile = new ResumeSkillProfile();
        savedProfile.setResumeId(testResumeId);
        savedProfile.setSeniority(SeniorityLevel.JUNIOR);
        savedProfile.setYearsEstimated(new BigDecimal("1.0"));
        when(profileRepository.save(any(ResumeSkillProfile.class))).thenReturn(savedProfile);

        ResumeSkillProfileResponse result = profileService.generate(testResumeId);

        assertThat(result.getSeniority()).isEqualTo("JUNIOR");
    }

    // UPDATE TESTS

    @Test
    void update_ShouldUpdateProfile_WhenValid() {
        UpdateResumeSkillProfileRequest request = UpdateResumeSkillProfileRequest.builder()
                .summary("Updated summary")
                .signals(Map.of("key", "value"))
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(ResumeSkillProfile.class))).thenReturn(testProfile);

        ResumeSkillProfileResponse result = profileService.update(testResumeId, request);

        assertThat(result).isNotNull();
        verify(profileRepository).save(testProfile);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenProfileNotFound() {
        UpdateResumeSkillProfileRequest request = UpdateResumeSkillProfileRequest.builder()
                .summary("Updated summary")
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(profileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.update(testResumeId, request))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROFILE_001.getCode());

        verify(profileRepository, never()).save(any());
    }
}
