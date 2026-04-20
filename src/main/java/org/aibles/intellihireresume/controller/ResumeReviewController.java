package org.aibles.intellihireresume.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeReviewResponse;
import org.aibles.intellihireresume.service.ResumeReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/review")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeReviewController {

    private final ResumeReviewService reviewService;

    @GetMapping
    public ResponseEntity<BaseResponse<ResumeReviewResponse>> getReview(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("GET /api/v1/resumes/{}/review", resumeId);
        return ResponseEntity.ok(BaseResponse.success(reviewService.getByResumeId(resumeId)));
    }

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ResumeReviewResponse>> generate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("POST /api/v1/resumes/{}/review/generate", resumeId);
        return ResponseEntity.ok(BaseResponse.success(reviewService.generate(resumeId)));
    }
}
