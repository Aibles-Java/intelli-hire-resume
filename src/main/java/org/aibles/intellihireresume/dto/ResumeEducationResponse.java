package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeEducationResponse {

    private String id;
    private String resumeId;
    private String school;
    private String degree;
    private String field;
    private Integer startYear;
    private Integer endYear;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
