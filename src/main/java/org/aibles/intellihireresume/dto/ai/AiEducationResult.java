package org.aibles.intellihireresume.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiEducationResult(
    String school,
    String degree,
    String field,
    Integer startYear,
    Integer endYear,
    String description
) {}
