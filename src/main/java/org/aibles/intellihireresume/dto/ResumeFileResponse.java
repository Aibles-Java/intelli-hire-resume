package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.intellihireresume.entity.enums.FileType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeFileResponse {

    private String id;
    private String resumeId;
    private String originalName;
    private FileType fileType;
    private Long fileSizeBytes;
    private String objectBucket;
    private String objectKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
