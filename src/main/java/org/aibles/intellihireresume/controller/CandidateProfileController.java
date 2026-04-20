package org.aibles.intellihireresume.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.CandidateProfileResponse;
import org.aibles.intellihireresume.service.CandidateProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/profile")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @GetMapping
    public ResponseEntity<BaseResponse<CandidateProfileResponse>> getProfile(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Getting candidate profile for resume ID: {}", resumeId);
        CandidateProfileResponse response = candidateProfileService.getProfile(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
