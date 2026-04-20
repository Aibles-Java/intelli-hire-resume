package org.aibles.intellihireresume.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.service.ResumeContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/contact")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeContactController {

    private final ResumeContactService contactService;

    @GetMapping
    public ResponseEntity<BaseResponse<ResumeContactResponse>> getByResumeId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("Getting contact for resume ID: {}", resumeId);
        ResumeContactResponse response = contactService.getByResumeId(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<ResumeContactResponse>> createOrUpdate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @Valid @RequestBody ResumeContactRequest request) {
        log.info("Creating or updating contact for resume ID: {}", resumeId);
        ResumeContactResponse response = contactService.createOrUpdate(resumeId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
