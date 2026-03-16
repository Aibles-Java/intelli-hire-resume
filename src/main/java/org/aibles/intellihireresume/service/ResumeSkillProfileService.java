package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.dto.UpdateResumeSkillProfileRequest;

public interface ResumeSkillProfileService {

    ResumeSkillProfileResponse getByResumeId(String resumeId);

    ResumeSkillProfileResponse generate(String resumeId);

    ResumeSkillProfileResponse update(String resumeId, UpdateResumeSkillProfileRequest request);

    void upsertSummary(String resumeId, String summary);
}
