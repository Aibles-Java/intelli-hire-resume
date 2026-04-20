package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.entity.ResumeExperience;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeExperienceMapper;
import org.aibles.intellihireresume.repository.ResumeExperienceRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.ResumeExperienceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeExperienceServiceImpl implements ResumeExperienceService {

    private final ResumeExperienceRepository experienceRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeExperienceMapper experienceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ResumeExperienceResponse> list(String resumeId) {
        log.info("Listing experiences for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        List<ResumeExperience> experiences = experienceRepository.findByResumeId(resumeId);
        return experienceMapper.toResponseList(experiences);
    }

    @Override
    public ResumeExperienceResponse create(String resumeId, ResumeExperienceRequest request) {
        log.info("Creating experience for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        validateDates(request);
        ResumeExperience experience = experienceMapper.toEntity(resumeId, request);
        ResumeExperience saved = experienceRepository.save(experience);
        log.info("Experience created successfully with ID: {}", saved.getId());
        return experienceMapper.toResponse(saved);
    }

    @Override
    public ResumeExperienceResponse update(String resumeId, String id, ResumeExperienceRequest request) {
        log.info("Updating experience ID: {} for resume ID: {}", id, resumeId);
        validateResumeExists(resumeId);
        ResumeExperience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EXP_001));
        if (!resumeId.equals(experience.getResumeId())) {
            throw new NotFoundException(ErrorCode.EXP_003);
        }
        validateDates(request);
        experienceMapper.updateEntity(experience, request);
        ResumeExperience saved = experienceRepository.save(experience);
        log.info("Experience updated successfully with ID: {}", saved.getId());
        return experienceMapper.toResponse(saved);
    }

    @Override
    public void delete(String resumeId, String id) {
        log.info("Deleting experience ID: {} for resume ID: {}", id, resumeId);
        validateResumeExists(resumeId);
        ResumeExperience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EXP_001));
        if (!resumeId.equals(experience.getResumeId())) {
            throw new NotFoundException(ErrorCode.EXP_003);
        }
        experienceRepository.delete(experience);
        log.info("Experience deleted successfully with ID: {}", id);
    }

    private void validateResumeExists(String resumeId) {
        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }
    }

    private void validateDates(ResumeExperienceRequest request) {
        Boolean isCurrent = request.getIsCurrent();
        if (Boolean.TRUE.equals(isCurrent) && request.getEndDate() != null) {
            throw new BadRequestException(ErrorCode.EXP_002);
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException(ErrorCode.EXP_002);
        }
    }
}
