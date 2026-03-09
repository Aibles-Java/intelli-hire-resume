package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;

import java.util.List;

public interface ResumeEducationService {

    List<ResumeEducationResponse> list(String resumeId);

    ResumeEducationResponse create(String resumeId, ResumeEducationRequest request);

    ResumeEducationResponse update(String resumeId, String id, ResumeEducationRequest request);

    void delete(String resumeId, String id);
}
