package org.aibles.intellihireresume.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResumeParseResult(
    AiContactResult contact,
    String summary,
    List<AiExperienceResult> experiences,
    List<AiEducationResult> educations,
    List<AiSkillResult> skills
) {
    public List<AiExperienceResult> experiences() {
        return experiences != null ? experiences : List.of();
    }

    public List<AiEducationResult> educations() {
        return educations != null ? educations : List.of();
    }

    public List<AiSkillResult> skills() {
        return skills != null ? skills : List.of();
    }
}
