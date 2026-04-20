package org.aibles.intellihireresume.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceResponse;
import org.aibles.intellihireresume.service.ResumeExperienceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/experiences")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeExperienceController {

    private final ResumeExperienceService experienceService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResumeExperienceResponse>>> list(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Listing experiences for resume ID: {}", resumeId);
        List<ResumeExperienceResponse> response = experienceService.list(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ResumeExperienceResponse>> create(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @Valid @RequestBody ResumeExperienceRequest request) {
        log.info("Creating experience for resume ID: {}", resumeId);
        ResumeExperienceResponse response = experienceService.create(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResumeExperienceResponse>> update(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @PathVariable String id,
            @Valid @RequestBody ResumeExperienceRequest request) {
        log.info("Updating experience ID: {} for resume ID: {}", id, resumeId);
        ResumeExperienceResponse response = experienceService.update(resumeId, id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @PathVariable String id) {
        log.info("Deleting experience ID: {} for resume ID: {}", id, resumeId);
        experienceService.delete(resumeId, id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}