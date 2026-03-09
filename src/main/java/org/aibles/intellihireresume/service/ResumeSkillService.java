package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ResumeSkillResponse;

import java.util.List;

public interface ResumeSkillService {

    List<ResumeSkillResponse> list(String resumeId);

    ResumeSkillResponse create(String resumeId, ResumeSkillRequest request);

    ResumeSkillResponse update(String resumeId, String id, ResumeSkillRequest request);

    void delete(String resumeId, String id);
}
