package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.mapper.ResumeMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
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

    @Override
    public ResumeResponse create(CreateResumeRequest request) {
        log.info("Creating resume for user: {}", request.getUserId());
        
        try {
            validateUniqueness(request.getUserId(), request.getTitle());
            
            Resume resume = createEntityWithDefaults(request);
            Resume savedResume = resumeRepository.save(resume);
            
            log.info("Resume created successfully with ID: {}", savedResume.getId());
            return resumeMapper.toResponse(savedResume);
        } catch (Exception e) {
            log.error("Failed to create resume for user: {}, error: {}", request.getUserId(), e.getMessage(), e);
            throw e; // Re-throw để transaction rollback
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
            
            // Only validate uniqueness if title is changing
            if (request.getTitle() != null && !request.getTitle().equals(resume.getTitle())) {
                validateUniquenessForUpdate(resume.getUserId(), request.getTitle(), id);
            }
            
            // Update fields (removing redundant setId)
            if (request.getTitle() != null) {
                resume.setTitle(request.getTitle().trim());
            }
            
            Resume updatedResume = resumeRepository.save(resume);
            
            log.info("Resume updated successfully");
            return resumeMapper.toResponse(updatedResume);
        } catch (Exception e) {
            log.error("Failed to update resume with ID: {}, error: {}", id, e.getMessage(), e);
            throw e; // Re-throw để transaction rollback
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
        setStatusToProcessing(resume);
        Resume updatedResume = resumeRepository.save(resume);
        
        log.info("Resume reprocess triggered successfully");
        return resumeMapper.toResponse(updatedResume);
    }

    // Private helper methods for reusable business logic

    /**
     * Validates field uniqueness for create operation
     * Can be reused for any entity that needs uniqueness validation
     */
    private void validateUniqueness(String userId, String title) {
        if (title != null && !title.trim().isEmpty() &&
            resumeRepository.existsByUserIdAndTitleActive(userId, title.trim())) {
            throw new DuplicateException(ErrorCode.RES_004);
        }
    }
    
    /**
     * Validates field uniqueness for update operation
     * Excludes the current record from uniqueness check
     */
    private void validateUniquenessForUpdate(String userId, String title, String excludeId) {
        if (title != null && !title.trim().isEmpty() &&
            resumeRepository.existsByUserIdAndTitleActiveExcluding(userId, title.trim(), excludeId)) {
            throw new DuplicateException(ErrorCode.RES_004);
        }
    }

    /**
     * Creates entity from request with default values applied
     * Template pattern - can be adapted for other entities
     */
    private Resume createEntityWithDefaults(CreateResumeRequest request) {
        Resume entity = resumeMapper.toEntity(request);
        entity.setStatus(ResumeStatus.PROCESSING);
        entity.setIsActive(true);
        return entity;
    }

    /**
     * Finds an active entity by ID or throws not found exception
     * Generic pattern for entity lookup with active filter
     */
    private Resume findById(String id) {
        return resumeRepository.findByIdActive(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RES_001));
    }

    /**
     * Performs soft delete on an entity
     * Generic soft delete operation - can be used for any entity with isActive flag
     */
    private void softDelete(Resume entity) {
        entity.setIsActive(false);
        resumeRepository.save(entity);
    }

    /**
     * Sets entity status to PROCESSING
     * Generic status update operation
     */
    private void setStatusToProcessing(Resume entity) {
        entity.setStatus(ResumeStatus.PROCESSING);
    }
}