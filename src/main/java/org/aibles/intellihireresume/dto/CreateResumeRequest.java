package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateResumeRequest {
    
    @NotBlank(message = "User ID is required")
    @Size(max = 36, message = "User ID must not exceed 36 characters")
    private String userId;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
}