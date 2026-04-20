package org.aibles.intellihireresume.mapper;

import java.util.Objects;
import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pure mapper for Resume entity and DTOs conversion
 * Contains only data transformation logic, no business logic
 */
@Component
public class ResumeMapper {

    /**
     * Converts CreateResumeRequest to Resume entity
     * Only maps the provided data, no default values or business logic
     */
    public Resume toEntity(CreateResumeRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }

        return Resume.builder()
                .title(request.getTitle())
                .build();
    }

    /**
     * Converts Resume entity to ResumeResponse DTO
     */
    public ResumeResponse toResponse(Resume resume) {
        if (Objects.isNull(resume)) {
            return null;
        }

        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUserId())
                .title(resume.getTitle())
                .status(resume.getStatus())
                .isActive(resume.getIsActive())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .createdBy(resume.getCreatedBy())
                .updatedBy(resume.getUpdatedBy())
                .build();
    }

    /**
     * Converts list of Resume entities to list of ResumeResponse DTOs
     */
    public List<ResumeResponse> toResponseList(List<Resume> resumes) {
        if (resumes.isEmpty()) {
            return Collections.emptyList();
        }

        return resumes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}