package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ResumeSkillResponse;
import org.aibles.intellihireresume.entity.ResumeSkill;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeSkillMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.ResumeSkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeSkillServiceImpl implements ResumeSkillService {

    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;
    private final ResumeSkillMapper resumeSkillMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ResumeSkillResponse> list(String resumeId) {
        log.info("Listing resume skills for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        return resumeSkillMapper.toResponseList(resumeSkillRepository.findByResumeId(resumeId));
    }

    @Override
    public ResumeSkillResponse create(String resumeId, ResumeSkillRequest request) {
        log.info("Creating resume skill for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        if (!skillRepository.existsById(request.getSkillId())) {
            throw new NotFoundException(ErrorCode.SKILL_001);
        }
        if (resumeSkillRepository.existsByResumeIdAndSkillId(resumeId, request.getSkillId())) {
            throw new BadRequestException(ErrorCode.RESUME_SKILL_002);
        }
        ResumeSkill entity = resumeSkillMapper.toEntity(resumeId, request);
        ResumeSkill saved = resumeSkillRepository.save(entity);
        log.info("Resume skill created with ID: {}", saved.getId());
        return resumeSkillMapper.toResponse(saved);
    }

    @Override
    public ResumeSkillResponse update(String resumeId, String id, ResumeSkillRequest request) {
        log.info("Updating resume skill ID: {} for resume ID: {}", id, resumeId);
        validateResumeExists(resumeId);
        ResumeSkill skill = resumeSkillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_SKILL_001));
        if (!resumeId.equals(skill.getResumeId())) {
            throw new NotFoundException(ErrorCode.RESUME_SKILL_001);
        }
        resumeSkillMapper.updateEntity(skill, request);
        ResumeSkill saved = resumeSkillRepository.save(skill);
        log.info("Resume skill updated with ID: {}", saved.getId());
        return resumeSkillMapper.toResponse(saved);
    }

    @Override
    public void delete(String resumeId, String id) {
        log.info("Deleting resume skill ID: {} for resume ID: {}", id, resumeId);
        validateResumeExists(resumeId);
        ResumeSkill skill = resumeSkillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_SKILL_001));
        if (!resumeId.equals(skill.getResumeId())) {
            throw new NotFoundException(ErrorCode.RESUME_SKILL_001);
        }
        resumeSkillRepository.delete(skill);
        log.info("Resume skill deleted with ID: {}", id);
    }

    private void validateResumeExists(String resumeId) {
        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }
    }
}
