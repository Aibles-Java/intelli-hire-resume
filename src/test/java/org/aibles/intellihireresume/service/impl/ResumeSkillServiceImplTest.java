package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ResumeSkillResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeSkill;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeSkillMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeSkillServiceImplTest {

    @Mock
    private ResumeSkillRepository resumeSkillRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private SkillRepository skillRepository;

    private ResumeSkillMapper resumeSkillMapper = new ResumeSkillMapper();

    private ResumeSkillServiceImpl resumeSkillService;

    private String testResumeId;
    private String testSkillId;
    private String testResumeSkillId;
    private Resume testResume;
    private ResumeSkill testResumeSkill;
    private ResumeSkillRequest testRequest;

    @BeforeEach
    void setUp() {
        resumeSkillService = new ResumeSkillServiceImpl(resumeSkillRepository, resumeRepository, skillRepository, resumeSkillMapper);

        testResumeId = "resume-123";
        testSkillId = "skill-001";
        testResumeSkillId = "resume-skill-789";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId("user-456")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        testResumeSkill = new ResumeSkill();
        testResumeSkill.setId(testResumeSkillId);
        testResumeSkill.setResumeId(testResumeId);
        testResumeSkill.setSkillId(testSkillId);
        testResumeSkill.setProficiencyLevel("ADVANCED");
        testResumeSkill.setYearsExperience(new BigDecimal("3.5"));
        testResumeSkill.setConfidenceScore(new BigDecimal("0.90"));
        testResumeSkill.setIsPrimary(true);
        testResumeSkill.setCreatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testResumeSkill.setUpdatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));

        testRequest = ResumeSkillRequest.builder()
                .skillId(testSkillId)
                .proficiencyLevel("ADVANCED")
                .yearsExperience(new BigDecimal("3.5"))
                .confidenceScore(new BigDecimal("0.90"))
                .isPrimary(true)
                .build();
    }

    // LIST TESTS

    @Test
    void list_ShouldReturnList_WhenResumeExists() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findByResumeId(testResumeId)).thenReturn(List.of(testResumeSkill));

        List<ResumeSkillResponse> result = resumeSkillService.list(testResumeId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillId()).isEqualTo(testSkillId);
        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeSkillRepository).findByResumeId(testResumeId);
    }

    @Test
    void list_ShouldThrowNotFoundException_WhenResumeNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeSkillService.list(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeSkillRepository, never()).findByResumeId(any());
    }

    // CREATE TESTS

    @Test
    void create_ShouldCreate_WhenValid() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(skillRepository.existsById(testSkillId)).thenReturn(true);
        when(resumeSkillRepository.existsByResumeIdAndSkillId(testResumeId, testSkillId)).thenReturn(false);
        when(resumeSkillRepository.save(any(ResumeSkill.class))).thenReturn(testResumeSkill);

        ResumeSkillResponse result = resumeSkillService.create(testResumeId, testRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSkillId()).isEqualTo(testSkillId);
        assertThat(result.getIsPrimary()).isTrue();
        verify(resumeSkillRepository).save(any(ResumeSkill.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenResumeNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeSkillService.create(testResumeId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeSkillRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenSkillNotInCatalog() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(skillRepository.existsById(testSkillId)).thenReturn(false);

        assertThatThrownBy(() -> resumeSkillService.create(testResumeId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SKILL_001.getCode());

        verify(resumeSkillRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenSkillAlreadyAdded() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(skillRepository.existsById(testSkillId)).thenReturn(true);
        when(resumeSkillRepository.existsByResumeIdAndSkillId(testResumeId, testSkillId)).thenReturn(true);

        assertThatThrownBy(() -> resumeSkillService.create(testResumeId, testRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESUME_SKILL_002.getCode());

        verify(resumeSkillRepository, never()).save(any());
    }

    // UPDATE TESTS

    @Test
    void update_ShouldUpdate_WhenValid() {
        ResumeSkillRequest updateRequest = ResumeSkillRequest.builder()
                .skillId(testSkillId)
                .proficiencyLevel("EXPERT")
                .yearsExperience(new BigDecimal("5.0"))
                .isPrimary(true)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findById(testResumeSkillId)).thenReturn(Optional.of(testResumeSkill));
        when(resumeSkillRepository.save(any(ResumeSkill.class))).thenReturn(testResumeSkill);

        ResumeSkillResponse result = resumeSkillService.update(testResumeId, testResumeSkillId, updateRequest);

        assertThat(result).isNotNull();
        verify(resumeSkillRepository).save(testResumeSkill);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenSkillNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findById(testResumeSkillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeSkillService.update(testResumeId, testResumeSkillId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESUME_SKILL_001.getCode());

        verify(resumeSkillRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenSkillDoesNotBelongToResume() {
        testResumeSkill.setResumeId("other-resume");

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findById(testResumeSkillId)).thenReturn(Optional.of(testResumeSkill));

        assertThatThrownBy(() -> resumeSkillService.update(testResumeId, testResumeSkillId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESUME_SKILL_001.getCode());

        verify(resumeSkillRepository, never()).save(any());
    }

    // DELETE TESTS

    @Test
    void delete_ShouldDelete_WhenValid() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findById(testResumeSkillId)).thenReturn(Optional.of(testResumeSkill));

        resumeSkillService.delete(testResumeId, testResumeSkillId);

        verify(resumeSkillRepository).delete(testResumeSkill);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenSkillNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findById(testResumeSkillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeSkillService.delete(testResumeId, testResumeSkillId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESUME_SKILL_001.getCode());

        verify(resumeSkillRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenSkillDoesNotBelongToResume() {
        testResumeSkill.setResumeId("other-resume");

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeSkillRepository.findById(testResumeSkillId)).thenReturn(Optional.of(testResumeSkill));

        assertThatThrownBy(() -> resumeSkillService.delete(testResumeId, testResumeSkillId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESUME_SKILL_001.getCode());

        verify(resumeSkillRepository, never()).delete(any());
    }
}
