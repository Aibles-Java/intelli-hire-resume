package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.entity.ResumeExperience;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResumeExperienceMapper {

    public ResumeExperience toEntity(String resumeId, ResumeExperienceRequest request) {
        if (request == null) {
            return null;
        }
        ResumeExperience experience = new ResumeExperience();
        experience.setResumeId(resumeId);
        experience.setCompany(request.getCompany());
        experience.setTitle(request.getTitle());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        experience.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
        return experience;
    }

    public void updateEntity(ResumeExperience experience, ResumeExperienceRequest request) {
        experience.setCompany(request.getCompany());
        experience.setTitle(request.getTitle());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        experience.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
    }

    public ResumeExperienceResponse toResponse(ResumeExperience entity) {
        if (entity == null) {
            return null;
        }
        return ResumeExperienceResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .company(entity.getCompany())
                .title(entity.getTitle())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .description(entity.getDescription())
                .isCurrent(entity.getIsCurrent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ResumeExperienceResponse> toResponseList(List<ResumeExperience> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
