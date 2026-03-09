package org.aibles.intellihireresume.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ResumeSkillResponse;
import org.aibles.intellihireresume.service.ResumeSkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/skills")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeSkillController {

    private final ResumeSkillService resumeSkillService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResumeSkillResponse>>> list(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Listing resume skills for resume ID: {}", resumeId);
        List<ResumeSkillResponse> response = resumeSkillService.list(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ResumeSkillResponse>> create(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @Valid @RequestBody ResumeSkillRequest request) {
        log.info("Creating resume skill for resume ID: {}", resumeId);
        ResumeSkillResponse response = resumeSkillService.create(resumeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResumeSkillResponse>> update(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @PathVariable String id,
            @Valid @RequestBody ResumeSkillRequest request) {
        log.info("Updating resume skill ID: {} for resume ID: {}", id, resumeId);
        ResumeSkillResponse response = resumeSkillService.update(resumeId, id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @PathVariable String id) {
        log.info("Deleting resume skill ID: {} for resume ID: {}", id, resumeId);
        resumeSkillService.delete(resumeId, id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
