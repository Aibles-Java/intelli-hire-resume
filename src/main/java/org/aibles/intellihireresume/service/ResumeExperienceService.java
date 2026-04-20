package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;

import java.util.List;

public interface ResumeExperienceService {

    List<ResumeExperienceResponse> list(String resumeId);

    ResumeExperienceResponse create(String resumeId, ResumeExperienceRequest request);

    ResumeExperienceResponse update(String resumeId, String id, ResumeExperienceRequest request);

    void delete(String resumeId, String id);
}