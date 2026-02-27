package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ParseJobMapper;
import org.aibles.intellihireresume.repository.ResumeParseJobRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeParseJobServiceImplTest {

    @Mock
    private ResumeParseJobRepository resumeParseJobRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ParseJobMapper parseJobMapper = new ParseJobMapper();

    private ResumeParseJobServiceImpl resumeParseJobService;

    private String testJobId;
    private String testResumeId;
    private Resume testResume;
    private ResumeParseJob queuedJob;
    private ResumeParseJob runningJob;
    private ResumeParseJob succeededJob;

    @BeforeEach
    void setUp() {
        resumeParseJobService = new ResumeParseJobServiceImpl(
                resumeParseJobRepository, resumeRepository, parseJobMapper);

        testJobId = "job-123";
        testResumeId = "resume-456";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId("user-789")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        queuedJob = new ResumeParseJob();
        queuedJob.setId(testJobId);
        queuedJob.setResumeId(testResumeId);
        queuedJob.setStatus(JobStatus.QUEUED);
        queuedJob.setJobType(JobType.PARSE);
        queuedJob.setProgress(0);
        queuedJob.setRetryCount(0);

        runningJob = new ResumeParseJob();
        runningJob.setId(testJobId);
        runningJob.setResumeId(testResumeId);
        runningJob.setStatus(JobStatus.RUNNING);
        runningJob.setJobType(JobType.PARSE);
        runningJob.setProgress(50);
        runningJob.setRetryCount(0);

        succeededJob = new ResumeParseJob();
        succeededJob.setId(testJobId);
        succeededJob.setResumeId(testResumeId);
        succeededJob.setStatus(JobStatus.SUCCEEDED);
        succeededJob.setJobType(JobType.PARSE);
        succeededJob.setProgress(100);
        succeededJob.setRetryCount(0);
    }

    // CREATE TESTS

    @Test
    void create_ShouldReturnQueuedJob_WhenValidRequest() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeParseJobRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(resumeParseJobRepository.save(any(ResumeParseJob.class))).thenReturn(queuedJob);

        // When
        ParseJobResponse result = resumeParseJobService.create(testResumeId, JobType.PARSE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testJobId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(result.getJobType()).isEqualTo(JobType.PARSE);

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeParseJobRepository).findByResumeId(testResumeId);
        verify(resumeParseJobRepository).save(argThat(job ->
                job.getStatus() == JobStatus.QUEUED &&
                job.getResumeId().equals(testResumeId) &&
                job.getProgress() == 0 &&
                job.getRetryCount() == 0
        ));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenResumeNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeParseJobService.create(testResumeId, JobType.PARSE))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeParseJobRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenJobAlreadyRunning() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeParseJobRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(runningJob));

        // When & Then
        assertThatThrownBy(() -> resumeParseJobService.create(testResumeId, JobType.PARSE))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_003.getCode());

        verify(resumeParseJobRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBadRequestException_WhenJobAlreadyQueued() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeParseJobRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(queuedJob));

        // When & Then
        assertThatThrownBy(() -> resumeParseJobService.create(testResumeId, JobType.PARSE))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_003.getCode());

        verify(resumeParseJobRepository, never()).save(any());
    }

    // GET BY ID TESTS

    @Test
    void getById_ShouldReturnJob_WhenJobExists() {
        // Given
        when(resumeParseJobRepository.findById(testJobId)).thenReturn(Optional.of(queuedJob));

        // When
        ParseJobResponse result = resumeParseJobService.getById(testJobId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testJobId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getStatus()).isEqualTo(JobStatus.QUEUED);

        verify(resumeParseJobRepository).findById(testJobId);
    }

    @Test
    void getById_ShouldThrowNotFoundException_WhenJobNotFound() {
        // Given
        when(resumeParseJobRepository.findById(testJobId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeParseJobService.getById(testJobId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_001.getCode());

        verify(resumeParseJobRepository).findById(testJobId);
    }

    // CANCEL TESTS

    @Test
    void cancel_ShouldCancelJob_WhenJobIsQueued() {
        // Given
        ResumeParseJob canceledJob = new ResumeParseJob();
        canceledJob.setId(testJobId);
        canceledJob.setResumeId(testResumeId);
        canceledJob.setStatus(JobStatus.CANCELED);
        canceledJob.setJobType(JobType.PARSE);
        canceledJob.setProgress(0);
        canceledJob.setRetryCount(0);

        when(resumeParseJobRepository.findById(testJobId)).thenReturn(Optional.of(queuedJob));
        when(resumeParseJobRepository.save(any(ResumeParseJob.class))).thenReturn(canceledJob);

        // When
        ParseJobResponse result = resumeParseJobService.cancel(testJobId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(JobStatus.CANCELED);

        verify(resumeParseJobRepository).findById(testJobId);
        verify(resumeParseJobRepository).save(argThat(job -> job.getStatus() == JobStatus.CANCELED));
    }

    @Test
    void cancel_ShouldCancelJob_WhenJobIsRunning() {
        // Given
        ResumeParseJob canceledJob = new ResumeParseJob();
        canceledJob.setId(testJobId);
        canceledJob.setResumeId(testResumeId);
        canceledJob.setStatus(JobStatus.CANCELED);
        canceledJob.setJobType(JobType.PARSE);
        canceledJob.setProgress(50);
        canceledJob.setRetryCount(0);

        when(resumeParseJobRepository.findById(testJobId)).thenReturn(Optional.of(runningJob));
        when(resumeParseJobRepository.save(any(ResumeParseJob.class))).thenReturn(canceledJob);

        // When
        ParseJobResponse result = resumeParseJobService.cancel(testJobId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(JobStatus.CANCELED);

        verify(resumeParseJobRepository).save(argThat(job -> job.getStatus() == JobStatus.CANCELED));
    }

    @Test
    void cancel_ShouldThrowBadRequestException_WhenJobIsSucceeded() {
        // Given
        when(resumeParseJobRepository.findById(testJobId)).thenReturn(Optional.of(succeededJob));

        // When & Then
        assertThatThrownBy(() -> resumeParseJobService.cancel(testJobId))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_002.getCode());

        verify(resumeParseJobRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldThrowNotFoundException_WhenJobNotFound() {
        // Given
        when(resumeParseJobRepository.findById(testJobId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeParseJobService.cancel(testJobId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOB_001.getCode());

        verify(resumeParseJobRepository, never()).save(any());
    }
}
