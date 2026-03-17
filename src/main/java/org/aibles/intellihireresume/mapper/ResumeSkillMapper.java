package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ResumeSkillResponse;
import org.aibles.intellihireresume.entity.ResumeSkill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ResumeSkillMapper {

    public ResumeSkill toEntity(String resumeId, ResumeSkillRequest request) {
        if (request == null) {
            return null;
        }
        ResumeSkill skill = new ResumeSkill();
        skill.setId(UUID.randomUUID().toString());
        skill.setResumeId(resumeId);
        skill.setSkillId(request.getSkillId());
        skill.setProficiencyLevel(request.getProficiencyLevel());
        skill.setYearsExperience(request.getYearsExperience());
        skill.setConfidenceScore(request.getConfidenceScore());
        skill.setEvidenceText(request.getEvidenceText());
        skill.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        return skill;
    }

    public void updateEntity(ResumeSkill skill, ResumeSkillRequest request) {
        skill.setProficiencyLevel(request.getProficiencyLevel());
        skill.setYearsExperience(request.getYearsExperience());
        skill.setConfidenceScore(request.getConfidenceScore());
        skill.setEvidenceText(request.getEvidenceText());
        skill.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
    }

    public ResumeSkillResponse toResponse(ResumeSkill entity) {
        if (entity == null) {
            return null;
        }
        return ResumeSkillResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .skillId(entity.getSkillId())
                .proficiencyLevel(entity.getProficiencyLevel())
                .yearsExperience(entity.getYearsExperience())
                .confidenceScore(entity.getConfidenceScore())
                .evidenceText(entity.getEvidenceText())
                .isPrimary(entity.getIsPrimary())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ResumeSkillResponse> toResponseList(List<ResumeSkill> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
