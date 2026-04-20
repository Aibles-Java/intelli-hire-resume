package org.aibles.intellihireresume.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.aibles.intellihireresume.service.ResumeFileService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/files")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResumeFileController {

    private final ResumeFileService resumeFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ResumeFileResponse>> upload(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /api/v1/resumes/{}/files - userId: {}", resumeId, userId);
        ResumeFileResponse response = resumeFileService.upload(resumeId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ResumeFileResponse>> getMetadata(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("GET /api/v1/resumes/{}/files - userId: {}", resumeId, userId);
        ResumeFileResponse response = resumeFileService.getByResumeId(resumeId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("GET /api/v1/resumes/{}/files/download - userId: {}", resumeId, userId);

        ResumeFileResponse metadata = resumeFileService.getByResumeId(resumeId);
        byte[] fileBytes = resumeFileService.download(resumeId);

        String contentType = metadata.getFileType() != null
                ? metadata.getFileType().getMimeType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(metadata.getOriginalName() != null ? metadata.getOriginalName() : resumeId)
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(fileBytes);
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> delete(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String resumeId) {
        log.info("DELETE /api/v1/resumes/{}/files - userId: {}", resumeId, userId);
        resumeFileService.delete(resumeId);
        return ResponseEntity.ok(BaseResponse.success(null));
    }
}
