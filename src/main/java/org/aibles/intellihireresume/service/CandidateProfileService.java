package org.aibles.intellihireresume.service;

import org.aibles.intellihireresume.dto.CandidateProfileResponse;

public interface CandidateProfileService {

    CandidateProfileResponse getProfile(String resumeId);
}
