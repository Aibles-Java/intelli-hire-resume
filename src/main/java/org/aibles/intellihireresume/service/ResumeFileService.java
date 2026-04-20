package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeFileService {

    ResumeFileResponse upload(String resumeId, String userId, MultipartFile file);

    ResumeFileResponse getByResumeId(String resumeId);

    byte[] download(String resumeId);

    void delete(String resumeId);
}