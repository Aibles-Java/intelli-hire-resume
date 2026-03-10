package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ParseJobMapper;
import org.aibles.intellihireresume.repository.ResumeParseJobRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.ResumeParseJobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeParseJobServiceImpl implements ResumeParseJobService {

    private final ResumeParseJobRepository resumeParseJobRepository;
    private final ResumeRepository resumeRepository;
    private final ParseJobMapper parseJobMapper;

    @Override
    public ParseJobResponse create(String resumeId, JobType jobType) {
        log.info("Creating parse job for resume: {}, type: {}", resumeId, jobType);

        try {
            resumeRepository.findByIdActive(resumeId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.RES_001));

            Optional<ResumeParseJob> existingJob = resumeParseJobRepository.findByResumeId(resumeId);
            if (existingJob.isPresent() && existingJob.get().getStatus().canCancel()) {
                throw new BadRequestException(ErrorCode.JOB_003);
            }

            ResumeParseJob job = new ResumeParseJob();
            job.setResumeId(resumeId);
            job.setJobType(jobType);
            job.setStatus(JobStatus.QUEUED);
            job.setProgress(0);
            job.setRetryCount(0);

            ResumeParseJob savedJob = resumeParseJobRepository.save(job);
            log.info("Parse job created successfully with ID: {}", savedJob.getId());
            return parseJobMapper.toResponse(savedJob);
        } catch (Exception e) {
            log.error("Failed to create parse job for resume: {}, error: {}", resumeId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ParseJobResponse getById(String id) {
        log.info("Getting parse job by ID: {}", id);

        ResumeParseJob job = findById(id);
        return parseJobMapper.toResponse(job);
    }

    @Override
    public ParseJobResponse cancel(String id) {
        log.info("Canceling parse job with ID: {}", id);

        try {
            ResumeParseJob job = findById(id);

            if (!job.getStatus().canCancel()) {
                throw new BadRequestException(ErrorCode.JOB_002);
            }

            job.setStatus(JobStatus.CANCELED);
            ResumeParseJob savedJob = resumeParseJobRepository.save(job);

            log.info("Parse job canceled successfully: {}", id);
            return parseJobMapper.toResponse(savedJob);
        } catch (Exception e) {
            log.error("Failed to cancel parse job with ID: {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void updateStatus(String id, JobStatus status) {
        log.info("Updating parse job status: {} -> {}", id, status);

        ResumeParseJob job = findById(id);
        job.setStatus(status);
        resumeParseJobRepository.save(job);
    }

    @Override
    public ParseJobResponse createOrReset(String resumeId, JobType jobType) {
        log.info("Creating or resetting parse job for resume: {}, type: {}", resumeId, jobType);

        resumeRepository.findByIdActive(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RES_001));

        Optional<ResumeParseJob> existingJob = resumeParseJobRepository.findByResumeId(resumeId);

        ResumeParseJob job;
        if (existingJob.isPresent()) {
            job = existingJob.get();
            job.setJobType(jobType);
            job.setStatus(JobStatus.QUEUED);
            job.setProgress(0);
            job.setRetryCount(0);
            job.setErrorMessage(null);
            job.setStartedAt(null);
            job.setFinishedAt(null);
        } else {
            job = new ResumeParseJob();
            job.setResumeId(resumeId);
            job.setJobType(jobType);
            job.setStatus(JobStatus.QUEUED);
            job.setProgress(0);
            job.setRetryCount(0);
        }

        ResumeParseJob saved = resumeParseJobRepository.save(job);
        log.info("Parse job created/reset successfully with ID: {}", saved.getId());
        return parseJobMapper.toResponse(saved);
    }

    private ResumeParseJob findById(String id) {
        return resumeParseJobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_001));
    }
}
