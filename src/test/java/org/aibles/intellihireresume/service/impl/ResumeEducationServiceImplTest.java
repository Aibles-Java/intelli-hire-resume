package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeEducation;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeEducationMapper;
import org.aibles.intellihireresume.repository.ResumeEducationRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeEducationServiceImplTest {

    @Mock
    private ResumeEducationRepository educationRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeEducationMapper educationMapper = new ResumeEducationMapper();

    private ResumeEducationServiceImpl educationService;

    private String testResumeId;
    private String testUserId;
    private String testEducationId;
    private Resume testResume;
    private ResumeEducation testEducation;
    private ResumeEducationRequest testRequest;

    @BeforeEach
    void setUp() {
        educationService = new ResumeEducationServiceImpl(educationRepository, resumeRepository, educationMapper);

        testResumeId = "resume-123";
        testUserId = "user-456";
        testEducationId = "edu-789";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        testEducation = new ResumeEducation();
        testEducation.setId(testEducationId);
        testEducation.setResumeId(testResumeId);
        testEducation.setSchool("Hanoi University of Technology");
        testEducation.setDegree("Bachelor");
        testEducation.setField("Computer Science");
        testEducation.setStartYear(2018);
        testEducation.setEndYear(2022);
        testEducation.setDescription("Graduated with honors");
        testEducation.setCreatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testEducation.setUpdatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));

        testRequest = ResumeEducationRequest.builder()
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
    void list_ShouldReturnList_WhenResumeExists() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findByResumeId(testResumeId)).thenReturn(List.of(testEducation));

        List<ResumeEducationResponse> result = educationService.list(testResumeId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testEducationId);
        assertThat(result.get(0).getSchool()).isEqualTo("Hanoi University of Technology");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(educationRepository).findByResumeId(testResumeId);
    }

    @Test
    void list_ShouldThrowNotFoundException_WhenResumeNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.list(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(educationRepository, never()).findByResumeId(any());
    }

    // CREATE TESTS

    @Test
    void create_ShouldCreateEducation_WhenValidRequest() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.save(any(ResumeEducation.class))).thenReturn(testEducation);

        ResumeEducationResponse result = educationService.create(testResumeId, testRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testEducationId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getSchool()).isEqualTo("Hanoi University of Technology");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(educationRepository).save(any(ResumeEducation.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenResumeNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.create(testResumeId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(educationRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenStartYearAfterEndYear() {
        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .startYear(2022)
                .endYear(2018)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));

        assertThatThrownBy(() -> educationService.create(testResumeId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_002.getCode());

        verify(educationRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenYearBefore1900() {
        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .startYear(1800)
                .endYear(2022)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));

        assertThatThrownBy(() -> educationService.create(testResumeId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_002.getCode());

        verify(educationRepository, never()).save(any());
    }

    // UPDATE TESTS

    @Test
    void update_ShouldUpdateEducation_WhenValid() {
        ResumeEducationRequest updateRequest = ResumeEducationRequest.builder()
                .school("Vietnam National University")
                .degree("Master")
                .field("Software Engineering")
                .startYear(2022)
                .endYear(2024)
                .build();

        ResumeEducation updatedEducation = new ResumeEducation();
        updatedEducation.setId(testEducationId);
        updatedEducation.setResumeId(testResumeId);
        updatedEducation.setSchool("Vietnam National University");
        updatedEducation.setDegree("Master");

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.of(testEducation));
        when(educationRepository.save(any(ResumeEducation.class))).thenReturn(updatedEducation);

        ResumeEducationResponse result = educationService.update(testResumeId, testEducationId, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSchool()).isEqualTo("Vietnam National University");
        assertThat(result.getDegree()).isEqualTo("Master");

        verify(educationRepository).save(testEducation);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenEducationNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.update(testResumeId, testEducationId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_001.getCode());

        verify(educationRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenEduDoesNotBelongToResume() {
        String otherResumeId = "other-resume-999";
        testEducation.setResumeId(otherResumeId);

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.of(testEducation));

        assertThatThrownBy(() -> educationService.update(testResumeId, testEducationId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_003.getCode());

        verify(educationRepository, never()).save(any());
    }

    // DELETE TESTS

    @Test
    void delete_ShouldDelete_WhenValid() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.of(testEducation));

        educationService.delete(testResumeId, testEducationId);

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(educationRepository).findById(testEducationId);
        verify(educationRepository).delete(testEducation);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenEducationNotFound() {
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.delete(testResumeId, testEducationId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_001.getCode());

        verify(educationRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenEduDoesNotBelongToResume() {
        String otherResumeId = "other-resume-999";
        testEducation.setResumeId(otherResumeId);

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.of(testEducation));

        assertThatThrownBy(() -> educationService.delete(testResumeId, testEducationId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_003.getCode());

        verify(educationRepository, never()).delete(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenEndYearExceedsCurrentYear() {
        // Given — endYear in the future
        int futureYear = java.time.Year.now().getValue() + 5;
        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .startYear(2020)
                .endYear(futureYear)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));

        // When & Then
        assertThatThrownBy(() -> educationService.create(testResumeId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_002.getCode());

        verify(educationRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenStartYearExceedsCurrentYear() {
        // Given — startYear in the future
        int futureYear = java.time.Year.now().getValue() + 1;
        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .startYear(futureYear)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));

        // When & Then
        assertThatThrownBy(() -> educationService.create(testResumeId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_002.getCode());

        verify(educationRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowBadRequestException_WhenStartYearAfterEndYear() {
        // Given
        ResumeEducationRequest invalidRequest = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .startYear(2022)
                .endYear(2018)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.findById(testEducationId)).thenReturn(Optional.of(testEducation));

        // When & Then
        assertThatThrownBy(() -> educationService.update(testResumeId, testEducationId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EDU_002.getCode());

        verify(educationRepository, never()).save(any());
    }

    @Test
    void create_ShouldCreate_WhenYearsAreNull() {
        // Given — null years are valid (optional fields)
        ResumeEducationRequest requestNullYears = ResumeEducationRequest.builder()
                .school("Hanoi University of Technology")
                .degree("Bachelor")
                .startYear(null)
                .endYear(null)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(educationRepository.save(any(ResumeEducation.class))).thenReturn(testEducation);

        // When
        ResumeEducationResponse result = educationService.create(testResumeId, requestNullYears);

        // Then
        assertThat(result).isNotNull();
        verify(educationRepository).save(any(ResumeEducation.class));
    }
}
