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
public class ResumeContactResponse {

    private String id;
    private String resumeId;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String linkedinUrl;
    private String otherInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
