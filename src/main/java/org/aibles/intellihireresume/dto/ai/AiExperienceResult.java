package org.aibles.intellihireresume.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiExperienceResult(
    String company,
    String title,
    Integer startYear,
    Integer startMonth,
    Integer endYear,
    Integer endMonth,
    Boolean isCurrent,
    String description
) {}
