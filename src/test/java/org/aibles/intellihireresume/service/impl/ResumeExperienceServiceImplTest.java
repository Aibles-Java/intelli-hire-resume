package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeExperience;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeExperienceMapper;
import org.aibles.intellihireresume.repository.ResumeExperienceRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeExperienceServiceImplTest {

    @Mock
    private ResumeExperienceRepository experienceRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeExperienceMapper experienceMapper = new ResumeExperienceMapper();

    private ResumeExperienceServiceImpl experienceService;

    private String testResumeId;
    private String testUserId;
    private String testExperienceId;
    private Resume testResume;
    private ResumeExperience testExperience;
    private ResumeExperienceRequest testRequest;

    @BeforeEach
    void setUp() {
        experienceService = new ResumeExperienceServiceImpl(experienceRepository, resumeRepository, experienceMapper);

        testResumeId = "resume-123";
        testUserId = "user-456";
        testExperienceId = "exp-789";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        testExperience = new ResumeExperience();
        testExperience.setId(testExperienceId);
        testExperience.setResumeId(testResumeId);
        testExperience.setCompany("Acme Corp");
        testExperience.setTitle("Software Engineer");
        testExperience.setStartDate(LocalDate.of(2022, 1, 1));
        testExperience.setEndDate(LocalDate.of(2023, 12, 31));
        testExperience.setDescription("Developed backend systems");
        testExperience.setIsCurrent(false);
        testExperience.setCreatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testExperience.setUpdatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));

        testRequest = ResumeExperienceRequest.builder()
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
    void list_ShouldReturnList_WhenResumeExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findByResumeId(testResumeId)).thenReturn(List.of(testExperience));

        // When
        List<ResumeExperienceResponse> result = experienceService.list(testResumeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testExperienceId);
        assertThat(result.get(0).getCompany()).isEqualTo("Acme Corp");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(experienceRepository).findByResumeId(testResumeId);
    }

    @Test
    void list_ShouldThrowNotFoundException_WhenResumeNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> experienceService.list(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(experienceRepository, never()).findByResumeId(any());
    }

    // CREATE TESTS

    @Test
    void create_ShouldCreateExperience_WhenValidRequest() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.save(any(ResumeExperience.class))).thenReturn(testExperience);

        // When
        ResumeExperienceResponse result = experienceService.create(testResumeId, testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testExperienceId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getCompany()).isEqualTo("Acme Corp");
        assertThat(result.getTitle()).isEqualTo("Software Engineer");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(experienceRepository).save(any(ResumeExperience.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenResumeNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> experienceService.create(testResumeId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(experienceRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenStartDateAfterEndDate() {
        // Given
        ResumeExperienceRequest invalidRequest = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Engineer")
                .startDate(LocalDate.of(2023, 12, 31))
                .endDate(LocalDate.of(2022, 1, 1))
                .isCurrent(false)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));

        // When & Then
        assertThatThrownBy(() -> experienceService.create(testResumeId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXP_002.getCode());

        verify(experienceRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenIsCurrentTrueAndEndDateNotNull() {
        // Given
        ResumeExperienceRequest invalidRequest = ResumeExperienceRequest.builder()
                .company("Acme Corp")
                .title("Engineer")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .isCurrent(true)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));

        // When & Then
        assertThatThrownBy(() -> experienceService.create(testResumeId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXP_002.getCode());

        verify(experienceRepository, never()).save(any());
    }

    // UPDATE TESTS

    @Test
    void update_ShouldUpdateExperience_WhenValid() {
        // Given
        ResumeExperienceRequest updateRequest = ResumeExperienceRequest.builder()
                .company("New Corp")
                .title("Senior Engineer")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .isCurrent(false)
                .build();

        ResumeExperience updatedExperience = new ResumeExperience();
        updatedExperience.setId(testExperienceId);
        updatedExperience.setResumeId(testResumeId);
        updatedExperience.setCompany("New Corp");
        updatedExperience.setTitle("Senior Engineer");

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findById(testExperienceId)).thenReturn(Optional.of(testExperience));
        when(experienceRepository.save(any(ResumeExperience.class))).thenReturn(updatedExperience);

        // When
        ResumeExperienceResponse result = experienceService.update(testResumeId, testExperienceId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCompany()).isEqualTo("New Corp");
        assertThat(result.getTitle()).isEqualTo("Senior Engineer");

        verify(experienceRepository).save(testExperience);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenExperienceNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findById(testExperienceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> experienceService.update(testResumeId, testExperienceId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXP_001.getCode());

        verify(experienceRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenExpDoesNotBelongToResume() {
        // Given
        String otherResumeId = "other-resume-999";
        testExperience.setResumeId(otherResumeId);

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findById(testExperienceId)).thenReturn(Optional.of(testExperience));

        // When & Then
        assertThatThrownBy(() -> experienceService.update(testResumeId, testExperienceId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXP_003.getCode());

        verify(experienceRepository, never()).save(any());
    }

    // DELETE TESTS

    @Test
    void delete_ShouldDelete_WhenValid() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findById(testExperienceId)).thenReturn(Optional.of(testExperience));

        // When
        experienceService.delete(testResumeId, testExperienceId);

        // Then
        verify(resumeRepository).findByIdActive(testResumeId);
        verify(experienceRepository).findById(testExperienceId);
        verify(experienceRepository).delete(testExperience);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenExperienceNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findById(testExperienceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> experienceService.delete(testResumeId, testExperienceId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXP_001.getCode());

        verify(experienceRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenExpDoesNotBelongToResume() {
        // Given
        String otherResumeId = "other-resume-999";
        testExperience.setResumeId(otherResumeId);

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(experienceRepository.findById(testExperienceId)).thenReturn(Optional.of(testExperience));

        // When & Then
        assertThatThrownBy(() -> experienceService.delete(testResumeId, testExperienceId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXP_003.getCode());

        verify(experienceRepository, never()).delete(any());
    }
}
