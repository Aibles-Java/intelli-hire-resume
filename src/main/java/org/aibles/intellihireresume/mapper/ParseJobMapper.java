package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ParseJobMapper {

    public ParseJobResponse toResponse(ResumeParseJob entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        return ParseJobResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .status(entity.getStatus())
                .jobType(entity.getJobType())
                .progress(entity.getProgress())
                .retryCount(entity.getRetryCount())
                .errorMessage(entity.getErrorMessage())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
