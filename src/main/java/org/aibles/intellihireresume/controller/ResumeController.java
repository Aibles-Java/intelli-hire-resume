package org.aibles.intellihireresume.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/resumes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeController {
    
    private final ResumeService resumeService;
    
    @PostMapping
    public ResponseEntity<BaseResponse<ResumeResponse>> create(
            @Valid @RequestBody CreateResumeRequest request) {
        log.info("Creating resume for user: {}", request.getUserId());
        
        ResumeResponse response = resumeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ResumeResponse>> getById(
            @PathVariable String id) {
        log.info("Getting resume by ID: {}", id);
        
        ResumeResponse response = resumeService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @GetMapping
    public ResponseEntity<BaseResponse<List<ResumeResponse>>> getByUserId(
            @RequestParam String userId) {
        log.info("Getting resumes for user: {}", userId);
        
        List<ResumeResponse> responses = resumeService.getByUserId(userId);
        return ResponseEntity.ok(BaseResponse.success(responses));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<ResumeResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateResumeRequest request) {
        log.info("Updating resume with ID: {}", id);
        
        ResumeResponse response = resumeService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable String id) {
        log.info("Deleting resume with ID: {}", id);
        
        resumeService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
    
    @PostMapping("/{id}/reprocess")
    public ResponseEntity<BaseResponse<ResumeResponse>> reprocess(
            @PathVariable String id) {
        log.info("Triggering reprocess for resume ID: {}", id);
        
        ResumeResponse response = resumeService.reprocess(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}