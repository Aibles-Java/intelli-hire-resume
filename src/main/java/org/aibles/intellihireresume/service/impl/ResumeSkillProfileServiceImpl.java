package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.dto.UpdateResumeSkillProfileRequest;
import org.aibles.intellihireresume.entity.ResumeSkill;
import org.aibles.intellihireresume.entity.ResumeSkillProfile;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.entity.enums.SeniorityLevel;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeSkillProfileMapper;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.repository.ResumeSkillProfileRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.ResumeSkillProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeSkillProfileServiceImpl implements ResumeSkillProfileService {

    private final ResumeSkillProfileRepository profileRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final SkillRepository skillRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeSkillProfileMapper profileMapper;

    @Override
    @Transactional(readOnly = true)
    public ResumeSkillProfileResponse getByResumeId(String resumeId) {
        log.info("Getting skill profile for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        ResumeSkillProfile profile = profileRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_001));
        return profileMapper.toResponse(profile);
    }

    @Override
    public ResumeSkillProfileResponse generate(String resumeId) {
        log.info("Generating skill profile for resume ID: {}", resumeId);
        validateResumeExists(resumeId);

        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeIdOrderByConfidenceScoreDesc(resumeId);

        // Batch-load all skills to avoid N+1 queries
        List<String> skillIds = resumeSkills.stream().map(ResumeSkill::getSkillId).toList();
        Map<String, Skill> skillMap = skillRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        // Build topSkills: skill name → confidence score (insertion-ordered by confidence DESC)
        Map<String, Object> topSkills = new LinkedHashMap<>();
        for (ResumeSkill rs : resumeSkills) {
            Skill skill = skillMap.get(rs.getSkillId());
            if (skill != null) {
                topSkills.put(skill.getName(), rs.getConfidenceScore() != null ? rs.getConfidenceScore() : BigDecimal.ZERO);
            }
        }

        // yearsEstimated = max yearsExperience of primary skills; fallback to all skills
        BigDecimal yearsEstimated = resumeSkills.stream()
                .filter(rs -> Boolean.TRUE.equals(rs.getIsPrimary()) && rs.getYearsExperience() != null)
                .map(ResumeSkill::getYearsExperience)
                .max(Comparator.naturalOrder())
                .orElseGet(() -> resumeSkills.stream()
                        .filter(rs -> rs.getYearsExperience() != null)
                        .map(ResumeSkill::getYearsExperience)
                        .max(Comparator.naturalOrder())
                        .orElse(BigDecimal.ZERO));

        SeniorityLevel seniority = SeniorityLevel.fromYearsOfExperience(yearsEstimated);

        // Upsert profile
        ResumeSkillProfile profile = profileRepository.findByResumeId(resumeId)
                .orElseGet(() -> {
                    ResumeSkillProfile p = new ResumeSkillProfile();
                    p.setResumeId(resumeId);
                    p.setGeneratedAt(LocalDateTime.now());
                    return p;
                });

        profile.setTopSkills(topSkills);
        profile.setYearsEstimated(yearsEstimated);
        profile.setSeniority(seniority);

        ResumeSkillProfile saved = profileRepository.save(profile);
        log.info("Skill profile generated for resume ID: {}", resumeId);
        return profileMapper.toResponse(saved);
    }

    @Override
    public ResumeSkillProfileResponse update(String resumeId, UpdateResumeSkillProfileRequest request) {
        log.info("Updating skill profile for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        ResumeSkillProfile profile = profileRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROFILE_001));
        profile.setSummary(request.getSummary());
        if (request.getSignals() != null) {
            profile.setSignals(request.getSignals());
        }
        ResumeSkillProfile saved = profileRepository.save(profile);
        log.info("Skill profile updated for resume ID: {}", resumeId);
        return profileMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void upsertSummary(String resumeId, String summary) {
        log.info("Upserting summary for resume ID: {}", resumeId);
        ResumeSkillProfile profile = profileRepository.findByResumeId(resumeId)
            .orElseGet(() -> {
                ResumeSkillProfile p = new ResumeSkillProfile();
                p.setResumeId(resumeId);
                return p;
            });
        profile.setSummary(summary);
        profileRepository.save(profile);
    }

    private void validateResumeExists(String resumeId) {
        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }
    }
}
