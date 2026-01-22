package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.Resume;

import java.util.List;

public interface ResumeService {
    
    ResumeResponse create(CreateResumeRequest request);
    
    ResumeResponse getById(String id);
    
    List<ResumeResponse> getByUserId(String userId);
    
    ResumeResponse update(String id, CreateResumeRequest request);
    
    void delete(String id);
    
    ResumeResponse reprocess(String id);
}