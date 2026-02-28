package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ResumeFileMapper {

    public ResumeFileResponse toResponse(ResumeFile entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        return ResumeFileResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .originalName(entity.getOriginalName())
                .fileType(entity.getFileType())
                .fileSizeBytes(entity.getFileSizeBytes())
                .objectBucket(entity.getObjectBucket())
                .objectKey(entity.getObjectKey())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
