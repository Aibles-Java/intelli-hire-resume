package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeMapper resumeMapper = new ResumeMapper();

    private ResumeServiceImpl resumeService;

    private CreateResumeRequest createRequest;
    private Resume resume;
    private ResumeResponse resumeResponse;
    private String testUserId;
    private String testResumeId;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeServiceImpl(resumeRepository, resumeMapper);

        testUserId = "user-123";
        testResumeId = "resume-456";

        createRequest = CreateResumeRequest.builder()
                .title("Software Engineer Resume")
                .build();

        resume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .createdBy("system")
                .updatedBy("system")
                .build();

        resumeResponse = ResumeResponse.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .createdBy("system")
                .updatedBy("system")
                .build();
    }

    // CREATE TESTS
    @Test
    void create_ShouldCreateResume_WhenValidRequest() {
        // Given
        when(resumeRepository.existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume"))
                .thenReturn(false);
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        // When
        ResumeResponse result = resumeService.create(testUserId, createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testResumeId);
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getTitle()).isEqualTo("Software Engineer Resume");
        assertThat(result.getStatus()).isEqualTo(ResumeStatus.UPLOADED);
        assertThat(result.getIsActive()).isTrue();

        verify(resumeRepository).existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume");
        verify(resumeRepository).save(any(Resume.class));

        verify(resumeRepository).save(argThat(savedResume ->
            savedResume.getStatus() == ResumeStatus.UPLOADED &&
            savedResume.getIsActive().equals(true) &&
            savedResume.getUserId().equals(testUserId)
        ));
    }

    @Test
    void create_ShouldCreateResume_WhenTitleIsNull() {
        // Given
        CreateResumeRequest requestWithNullTitle = CreateResumeRequest.builder()
                .title(null)
                .build();

        Resume resumeWithNullTitle = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();

        when(resumeRepository.save(any(Resume.class))).thenReturn(resumeWithNullTitle);

        // When
        ResumeResponse result = resumeService.create(testUserId, requestWithNullTitle);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isNull();
        verify(resumeRepository, never()).existsByUserIdAndTitleActive(any(), any());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void create_ShouldThrowDuplicateException_WhenTitleAlreadyExists() {
        // Given
        when(resumeRepository.existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> resumeService.create(testUserId, createRequest))
                .isInstanceOf(DuplicateException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_004.getCode());

        verify(resumeRepository).existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume");
        verify(resumeRepository, never()).save(any());
    }

    // GET BY ID TESTS
    @Test
    void getById_ShouldReturnResume_WhenResumeExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));

        // When
        ResumeResponse result = resumeService.getById(testResumeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testResumeId);
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getTitle()).isEqualTo("Software Engineer Resume");

        verify(resumeRepository).findByIdActive(testResumeId);
    }

    @Test
    void getById_ShouldThrowNotFoundException_WhenResumeNotExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeService.getById(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
    }

    // GET BY USER ID TESTS
    @Test
    void getByUserId_ShouldReturnResumeList_WhenResumesExist() {
        // Given
        Resume resume2 = Resume.builder()
                .id("resume-789")
                .userId(testUserId)
                .title("Another Resume")
                .status(ResumeStatus.COMPLETED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        List<Resume> resumes = Arrays.asList(resume, resume2);
        when(resumeRepository.findByUserIdActive(testUserId)).thenReturn(resumes);

        // When
        List<ResumeResponse> result = resumeService.getByUserId(testUserId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(testResumeId);
        assertThat(result.get(1).getId()).isEqualTo("resume-789");
        assertThat(result).allMatch(response -> response.getUserId().equals(testUserId));
        assertThat(result).allMatch(response -> response.getIsActive().equals(true));

        verify(resumeRepository).findByUserIdActive(testUserId);
    }

    @Test
    void getByUserId_ShouldReturnEmptyList_WhenNoResumesExist() {
        // Given
        when(resumeRepository.findByUserIdActive(testUserId)).thenReturn(Collections.emptyList());

        // When
        List<ResumeResponse> result = resumeService.getByUserId(testUserId);

        // Then
        assertThat(result).isEmpty();
        verify(resumeRepository).findByUserIdActive(testUserId);
    }

    // UPDATE TESTS
    @Test
    void update_ShouldUpdateResume_WhenValidRequest() {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("Updated Resume Title")
                .build();

        Resume updatedResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Updated Resume Title")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 0, 0))
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(updatedResume);

        // When
        ResumeResponse result = resumeService.update(testResumeId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testResumeId);
        assertThat(result.getTitle()).isEqualTo("Updated Resume Title");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenResumeNotExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeService.update(testResumeId, createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowDuplicateException_WhenTitleAlreadyExists() {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("Existing Title")
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));
        when(resumeRepository.existsByUserIdAndTitleActiveExcluding(testUserId, "Existing Title", testResumeId))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> resumeService.update(testResumeId, updateRequest))
                .isInstanceOf(DuplicateException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_004.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository).existsByUserIdAndTitleActiveExcluding(testUserId, "Existing Title", testResumeId);
        verify(resumeRepository, never()).save(any());
    }

    @Test
    void update_ShouldAllowNullTitle() {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title(null)
                .build();

        Resume updatedResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title(null)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(updatedResume);

        // When
        ResumeResponse result = resumeService.update(testResumeId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isNull();
        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository, never()).existsByUserIdAndTitleActiveExcluding(any(), any(), any());
        verify(resumeRepository).save(any(Resume.class));
    }

    // DELETE TESTS
    @Test
    void delete_ShouldSoftDeleteResume_WhenResumeExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));

        // When
        resumeService.delete(testResumeId);

        // Then
        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository).save(argThat(savedResume ->
            savedResume.getIsActive().equals(false)
        ));
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenResumeNotExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeService.delete(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository, never()).save(any());
    }

    // REPROCESS TESTS
    @Test
    void reprocess_ShouldSetStatusToParsing_WhenResumeExists() {
        // Given
        Resume failedResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.FAILED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .build();

        Resume reprocessedResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("Software Engineer Resume")
                .status(ResumeStatus.PARSING)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 14, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 15, 30, 0))
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(failedResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(reprocessedResume);

        // When
        ResumeResponse result = resumeService.reprocess(testResumeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testResumeId);
        assertThat(result.getStatus()).isEqualTo(ResumeStatus.PARSING);

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository).save(argThat(savedResume ->
            savedResume.getStatus() == ResumeStatus.PARSING
        ));
    }

    @Test
    void reprocess_ShouldThrowNotFoundException_WhenResumeNotExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeService.reprocess(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeRepository, never()).save(any());
    }

    // EDGE CASE TESTS
    @Test
    void create_ShouldSkipUniquenessCheck_WhenTitleIsEmpty() {
        // Given
        CreateResumeRequest requestWithEmptyTitle = CreateResumeRequest.builder()
                .title("")
                .build();

        Resume resumeWithEmptyTitle = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .title("")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .build();

        when(resumeRepository.save(any(Resume.class))).thenReturn(resumeWithEmptyTitle);

        // When
        ResumeResponse result = resumeService.create(testUserId, requestWithEmptyTitle);

        // Then
        assertThat(result).isNotNull();
        // Empty string is trimmed and isEmpty → uniqueness check is skipped
        verify(resumeRepository, never()).existsByUserIdAndTitleActive(any(), any());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void update_ShouldSkipUniquenessCheck_WhenTitleIsEmpty() {
        // Given
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("")
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        // When
        ResumeResponse result = resumeService.update(testResumeId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        // Empty string → uniqueness check is skipped
        verify(resumeRepository, never()).existsByUserIdAndTitleActiveExcluding(any(), any(), any());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void create_ShouldTrimTitle_WhenTitleHasWhitespace() {
        // Given — "  My Resume  " should be trimmed to "My Resume" before uniqueness check
        CreateResumeRequest requestWithSpaces = CreateResumeRequest.builder()
                .title("  Software Engineer Resume  ")
                .build();

        when(resumeRepository.existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume"))
                .thenReturn(false);
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        // When
        ResumeResponse result = resumeService.create(testUserId, requestWithSpaces);

        // Then
        assertThat(result).isNotNull();
        verify(resumeRepository).existsByUserIdAndTitleActive(testUserId, "Software Engineer Resume");
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void update_ShouldNotCallUniquenessCheck_WhenTitleIsUnchanged() {
        // Given — new title equals current title → no uniqueness check required
        CreateResumeRequest updateRequest = CreateResumeRequest.builder()
                .title("Software Engineer Resume") // same as resume.getTitle()
                .build();

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(resume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

        // When
        ResumeResponse result = resumeService.update(testResumeId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(resumeRepository, never()).existsByUserIdAndTitleActiveExcluding(any(), any(), any());
        verify(resumeRepository).save(any(Resume.class));
    }
}
