package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeReviewResponse;

public interface ResumeReviewService {

    ResumeReviewResponse getByResumeId(String resumeId);

    ResumeReviewResponse generate(String resumeId);
}
