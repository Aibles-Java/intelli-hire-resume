package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.JobType;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.mapper.ResumeMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.RedisJobQueueService;
import org.aibles.intellihireresume.service.ResumeParseJobService;
import org.aibles.intellihireresume.service.ResumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final ResumeParseJobService resumeParseJobService;
    private final RedisJobQueueService redisJobQueueService;

    @Override
    public ResumeResponse create(String userId, CreateResumeRequest request) {
        log.info("Creating resume for user: {}", userId);

        try {
            validateUniqueness(userId, request.getTitle());

            Resume resume = createEntityWithDefaults(userId, request);
            Resume savedResume = resumeRepository.save(resume);

            log.info("Resume created successfully with ID: {}", savedResume.getId());
            return resumeMapper.toResponse(savedResume);
        } catch (Exception e) {
            log.error("Failed to create resume for user: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getById(String id) {
        log.info("Getting resume by ID: {}", id);

        Resume resume = findById(id);
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getByUserId(String userId) {
        log.info("Getting resumes for user: {}", userId);

        List<Resume> resumes = resumeRepository.findByUserIdActive(userId);
        return resumeMapper.toResponseList(resumes);
    }

    @Override
    public ResumeResponse update(String id, CreateResumeRequest request) {
        log.info("Updating resume with ID: {}", id);

        try {
            Resume resume = findById(id);

            if (request.getTitle() != null && !request.getTitle().equals(resume.getTitle())) {
                validateUniquenessForUpdate(resume.getUserId(), request.getTitle(), id);
            }

            if (request.getTitle() != null) {
                resume.setTitle(request.getTitle().trim());
            }

            Resume updatedResume = resumeRepository.save(resume);

            log.info("Resume updated successfully");
            return resumeMapper.toResponse(updatedResume);
        } catch (Exception e) {
            log.error("Failed to update resume with ID: {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void delete(String id) {
        log.info("Soft deleting resume with ID: {}", id);

        Resume resume = findById(id);
        softDelete(resume);

        log.info("Resume soft deleted successfully");
    }

    @Override
    public ResumeResponse reprocess(String id) {
        log.info("Triggering reprocess for resume ID: {}", id);

        Resume resume = findById(id);
        setStatusToParsing(resume);
        Resume updatedResume = resumeRepository.save(resume);

        resumeParseJobService.createOrReset(id, JobType.REPARSE);
        redisJobQueueService.enqueue(id);

        log.info("Resume reprocess triggered successfully");
        return resumeMapper.toResponse(updatedResume);
    }

    // Private helper methods

    private void validateUniqueness(String userId, String title) {
        if (title != null && !title.trim().isEmpty() &&
            resumeRepository.existsByUserIdAndTitleActive(userId, title.trim())) {
            throw new DuplicateException(ErrorCode.RES_004);
        }
    }

    private void validateUniquenessForUpdate(String userId, String title, String excludeId) {
        if (title != null && !title.trim().isEmpty() &&
            resumeRepository.existsByUserIdAndTitleActiveExcluding(userId, title.trim(), excludeId)) {
            throw new DuplicateException(ErrorCode.RES_004);
        }
    }

    private Resume createEntityWithDefaults(String userId, CreateResumeRequest request) {
        Resume entity = resumeMapper.toEntity(request);
        entity.setUserId(userId);
        entity.setStatus(ResumeStatus.UPLOADED);
        entity.setIsActive(true);
        return entity;
    }

    private Resume findById(String id) {
        return resumeRepository.findByIdActive(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RES_001));
    }

    private void softDelete(Resume entity) {
        entity.setIsActive(false);
        resumeRepository.save(entity);
    }

    private void setStatusToParsing(Resume entity) {
        entity.setStatus(ResumeStatus.PARSING);
    }
}
