package org.aibles.intellihireresume.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiContactResult(
    String fullName,
    String email,
    String phone,
    String linkedinUrl,
    String location
) {}
