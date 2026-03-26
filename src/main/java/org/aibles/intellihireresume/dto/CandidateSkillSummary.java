package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class CandidateSkillSummary {

    private String id;
    private String skillId;
    private String skillName;
    private String skillCategory;
    private String skillType;
    private String proficiencyLevel;
    private BigDecimal yearsExperience;
    private BigDecimal confidenceScore;
    private Boolean isPrimary;
}
