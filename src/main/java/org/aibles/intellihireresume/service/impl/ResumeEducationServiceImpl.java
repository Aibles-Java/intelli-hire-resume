package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;
import org.aibles.intellihireresume.entity.ResumeEducation;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeEducationMapper;
import org.aibles.intellihireresume.repository.ResumeEducationRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.ResumeEducationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeEducationServiceImpl implements ResumeEducationService {

    private final ResumeEducationRepository educationRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeEducationMapper educationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ResumeEducationResponse> list(String resumeId) {
        log.info("Listing educations for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        List<ResumeEducation> educations = educationRepository.findByResumeId(resumeId);
        return educationMapper.toResponseList(educations);
    }

    @Override
    public ResumeEducationResponse create(String resumeId, ResumeEducationRequest request) {
        log.info("Creating education for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        validateYears(request);
        ResumeEducation education = educationMapper.toEntity(resumeId, request);
        ResumeEducation saved = educationRepository.save(education);
        log.info("Education created successfully with ID: {}", saved.getId());
        return educationMapper.toResponse(saved);
    }

    @Override
    public ResumeEducationResponse update(String resumeId, String id, ResumeEducationRequest request) {
        log.info("Updating education ID: {} for resume ID: {}", id, resumeId);
        validateResumeExists(resumeId);
        ResumeEducation education = educationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EDU_001));
        if (!resumeId.equals(education.getResumeId())) {
            throw new NotFoundException(ErrorCode.EDU_003);
        }
        validateYears(request);
        educationMapper.updateEntity(education, request);
        ResumeEducation saved = educationRepository.save(education);
        log.info("Education updated successfully with ID: {}", saved.getId());
        return educationMapper.toResponse(saved);
    }

    @Override
    public void delete(String resumeId, String id) {
        log.info("Deleting education ID: {} for resume ID: {}", id, resumeId);
        validateResumeExists(resumeId);
        ResumeEducation education = educationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EDU_001));
        if (!resumeId.equals(education.getResumeId())) {
            throw new NotFoundException(ErrorCode.EDU_003);
        }
        educationRepository.delete(education);
        log.info("Education deleted successfully with ID: {}", id);
    }

    private void validateResumeExists(String resumeId) {
        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }
    }

    private void validateYears(ResumeEducationRequest request) {
        int currentYear = Year.now().getValue();
        Integer startYear = request.getStartYear();
        Integer endYear = request.getEndYear();

        if (startYear != null && (startYear < 1900 || startYear > currentYear)) {
            throw new BadRequestException(ErrorCode.EDU_002);
        }
        if (endYear != null && (endYear < 1900 || endYear > currentYear)) {
            throw new BadRequestException(ErrorCode.EDU_002);
        }
        if (startYear != null && endYear != null && startYear > endYear) {
            throw new BadRequestException(ErrorCode.EDU_002);
        }
    }
}
