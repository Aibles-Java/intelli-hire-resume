package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;
import org.aibles.intellihireresume.entity.ResumeEducation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ResumeEducationMapper {

    public ResumeEducation toEntity(String resumeId, ResumeEducationRequest request) {
        if (request == null) {
            return null;
        }
        ResumeEducation education = new ResumeEducation();
        education.setId(UUID.randomUUID().toString());
        education.setResumeId(resumeId);
        education.setSchool(request.getSchool());
        education.setDegree(request.getDegree());
        education.setField(request.getField());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setDescription(request.getDescription());
        return education;
    }

    public void updateEntity(ResumeEducation education, ResumeEducationRequest request) {
        education.setSchool(request.getSchool());
        education.setDegree(request.getDegree());
        education.setField(request.getField());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setDescription(request.getDescription());
    }

    public ResumeEducationResponse toResponse(ResumeEducation entity) {
        if (entity == null) {
            return null;
        }
        return ResumeEducationResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .school(entity.getSchool())
                .degree(entity.getDegree())
                .field(entity.getField())
                .startYear(entity.getStartYear())
                .endYear(entity.getEndYear())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ResumeEducationResponse> toResponseList(List<ResumeEducation> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
