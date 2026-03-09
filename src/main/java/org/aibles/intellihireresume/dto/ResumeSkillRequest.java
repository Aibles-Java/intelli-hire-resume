package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeSkillRequest {

    @NotBlank(message = "Skill ID must not be blank")
    private String skillId;

    @Size(max = 20, message = "Proficiency level must not exceed 20 characters")
    private String proficiencyLevel;

    @DecimalMin(value = "0.0", message = "Years experience must be non-negative")
    @DecimalMax(value = "50.0", message = "Years experience must not exceed 50")
    @Digits(integer = 2, fraction = 1)
    private BigDecimal yearsExperience;

    @DecimalMin(value = "0.0", message = "Confidence score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Confidence score must be between 0.0 and 1.0")
    @Digits(integer = 1, fraction = 2)
    private BigDecimal confidenceScore;

    @Size(max = 500, message = "Evidence text must not exceed 500 characters")
    private String evidenceText;

    @Builder.Default
    private Boolean isPrimary = false;
}
