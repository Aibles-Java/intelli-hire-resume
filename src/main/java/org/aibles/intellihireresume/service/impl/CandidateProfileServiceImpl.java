package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.CandidateProfileResponse;
import org.aibles.intellihireresume.dto.CandidateSkillSummary;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.entity.ResumeSkill;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeContactMapper;
import org.aibles.intellihireresume.mapper.ResumeEducationMapper;
import org.aibles.intellihireresume.mapper.ResumeExperienceMapper;
import org.aibles.intellihireresume.mapper.ResumeSkillProfileMapper;
import org.aibles.intellihireresume.repository.ResumeContactRepository;
import org.aibles.intellihireresume.repository.ResumeEducationRepository;
import org.aibles.intellihireresume.repository.ResumeExperienceRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.repository.ResumeSkillProfileRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.CandidateProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateProfileServiceImpl implements CandidateProfileService {

    private final ResumeRepository resumeRepository;
    private final ResumeContactRepository contactRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final SkillRepository skillRepository;
    private final ResumeSkillProfileRepository skillProfileRepository;

    private final ResumeContactMapper contactMapper;
    private final ResumeExperienceMapper experienceMapper;
    private final ResumeEducationMapper educationMapper;
    private final ResumeSkillProfileMapper skillProfileMapper;

    @Override
    public CandidateProfileResponse getProfile(String resumeId) {
        log.info("Getting candidate profile for resume ID: {}", resumeId);

        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }

        ResumeContactResponse contact = contactRepository.findByResumeId(resumeId)
                .map(contactMapper::toResponse)
                .orElse(null);

        List<ResumeExperienceResponse> experiences = experienceMapper.toResponseList(
                experienceRepository.findByResumeId(resumeId));

        List<ResumeEducationResponse> educations = educationMapper.toResponseList(
                educationRepository.findByResumeId(resumeId));

        List<CandidateSkillSummary> skills = buildSkillSummaries(resumeId);

        ResumeSkillProfileResponse skillProfile = skillProfileRepository.findByResumeId(resumeId)
                .map(skillProfileMapper::toResponse)
                .orElse(null);

        log.info("Candidate profile assembled for resume ID: {}", resumeId);

        return CandidateProfileResponse.builder()
                .resumeId(resumeId)
                .contact(contact)
                .experiences(experiences)
                .educations(educations)
                .skills(skills)
                .skillProfile(skillProfile)
                .build();
    }

    private List<CandidateSkillSummary> buildSkillSummaries(String resumeId) {
        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resumeId);
        if (resumeSkills.isEmpty()) {
            return List.of();
        }

        Set<String> skillIds = resumeSkills.stream()
                .map(ResumeSkill::getSkillId)
                .collect(Collectors.toSet());

        Map<String, Skill> skillMap = skillRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, Function.identity()));

        return resumeSkills.stream()
                .map(rs -> {
                    Skill skill = skillMap.get(rs.getSkillId());
                    return CandidateSkillSummary.builder()
                            .id(rs.getId())
                            .skillId(rs.getSkillId())
                            .skillName(skill != null ? skill.getName() : null)
                            .skillCategory(skill != null ? skill.getCategory() : null)
                            .skillType(skill != null && skill.getType() != null ? skill.getType().name() : null)
                            .proficiencyLevel(rs.getProficiencyLevel())
                            .yearsExperience(rs.getYearsExperience())
                            .confidenceScore(rs.getConfidenceScore())
                            .isPrimary(rs.getIsPrimary())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
