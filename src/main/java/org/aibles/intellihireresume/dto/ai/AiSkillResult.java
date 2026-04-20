package org.aibles.intellihireresume.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSkillResult(
    String name,
    Double confidenceScore,
    Double yearsExperience,
    Boolean isPrimary,
    String evidenceText
) {}
