package org.aibles.intellihireresume.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.dto.UpdateResumeSkillProfileRequest;
import org.aibles.intellihireresume.service.ResumeSkillProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/skill-profile")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeSkillProfileController {

    private final ResumeSkillProfileService profileService;

    @GetMapping
    public ResponseEntity<BaseResponse<ResumeSkillProfileResponse>> getByResumeId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Getting skill profile for resume ID: {}", resumeId);
        ResumeSkillProfileResponse response = profileService.getByResumeId(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ResumeSkillProfileResponse>> generate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Generating skill profile for resume ID: {}", resumeId);
        ResumeSkillProfileResponse response = profileService.generate(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<ResumeSkillProfileResponse>> update(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @RequestBody UpdateResumeSkillProfileRequest request) {
        log.info("Updating skill profile for resume ID: {}", resumeId);
        ResumeSkillProfileResponse response = profileService.update(resumeId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
