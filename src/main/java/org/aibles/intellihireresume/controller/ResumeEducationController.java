package org.aibles.intellihireresume.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeEducationResponse;
import org.aibles.intellihireresume.service.ResumeEducationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/educations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeEducationController {

    private final ResumeEducationService educationService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResumeEducationResponse>>> list(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Listing educations for resume ID: {}", resumeId);
        List<ResumeEducationResponse> response = educationService.list(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ResumeEducationResponse>> create(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @Valid @RequestBody ResumeEducationRequest request) {
        log.info("Creating education for resume ID: {}", resumeId);
        ResumeEducationResponse response = educationService.create(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResumeEducationResponse>> update(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @PathVariable String id,
            @Valid @RequestBody ResumeEducationRequest request) {
        log.info("Updating education ID: {} for resume ID: {}", id, resumeId);
        ResumeEducationResponse response = educationService.update(resumeId, id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @PathVariable String id) {
        log.info("Deleting education ID: {} for resume ID: {}", id, resumeId);
        educationService.delete(resumeId, id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
