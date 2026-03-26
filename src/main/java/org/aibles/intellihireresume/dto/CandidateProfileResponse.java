package org.aibles.intellihireresume.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CandidateProfileResponse {

    private String resumeId;
    private ResumeContactResponse contact;
    private List<ResumeExperienceResponse> experiences;
    private List<ResumeEducationResponse> educations;
    private List<CandidateSkillSummary> skills;
    private ResumeSkillProfileResponse skillProfile;
}
