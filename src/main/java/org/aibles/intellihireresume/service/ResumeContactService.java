package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;

public interface ResumeContactService {

    ResumeContactResponse getByResumeId(String resumeId);

    ResumeContactResponse createOrUpdate(String resumeId, ResumeContactRequest request);
}
