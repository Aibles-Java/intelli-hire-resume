package org.aibles.intellihireresume.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ParseJobRequest;
import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.service.ResumeParseJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/parse-jobs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeParseJobController {

    private final ResumeParseJobService resumeParseJobService;

    @PostMapping
    public ResponseEntity<BaseResponse<ParseJobResponse>> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ParseJobRequest request) {
        log.info("POST /api/v1/parse-jobs - userId: {}, resumeId: {}", userId, request.getResumeId());
        ParseJobResponse response = resumeParseJobService.create(request.getResumeId(), request.getJobType());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ParseJobResponse>> getById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        log.info("GET /api/v1/parse-jobs/{} - userId: {}", id, userId);
        ParseJobResponse response = resumeParseJobService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResponse<ParseJobResponse>> cancel(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        log.info("PUT /api/v1/parse-jobs/{}/cancel - userId: {}", id, userId);
        ParseJobResponse response = resumeParseJobService.cancel(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
