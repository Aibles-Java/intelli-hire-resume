package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.entity.ResumeSkillProfile;
import org.springframework.stereotype.Component;

@Component
public class ResumeSkillProfileMapper {

    public ResumeSkillProfileResponse toResponse(ResumeSkillProfile entity) {
        if (entity == null) {
            return null;
        }
        return ResumeSkillProfileResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .topSkills(entity.getTopSkills())
                .yearsEstimated(entity.getYearsEstimated())
                .seniority(entity.getSeniority() != null ? entity.getSeniority().name() : null)
                .summary(entity.getSummary())
                .signals(entity.getSignals())
                .generatedAt(entity.getGeneratedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
