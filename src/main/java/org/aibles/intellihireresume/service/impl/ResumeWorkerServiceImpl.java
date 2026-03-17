package org.aibles.intellihireresume.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.repository.ResumeFileRepository;
import org.aibles.intellihireresume.repository.ResumeParseJobRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.AiParsingService;
import org.aibles.intellihireresume.service.FileStorageService;
import org.aibles.intellihireresume.service.ResumeWorkerService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeWorkerServiceImpl implements ResumeWorkerService {

    private final ResumeRepository resumeRepository;
    private final ResumeParseJobRepository resumeParseJobRepository;
    private final ResumeFileRepository resumeFileRepository;
    private final FileStorageService fileStorageService;
    private final AiParsingService aiParsingService;
    private final MeterRegistry meterRegistry;

    @Async("workerPool")
    @Override
    public void processJob(String resumeId) {
        log.info("Worker started for resumeId={}", resumeId);

        ResumeParseJob job = resumeParseJobRepository.findByResumeId(resumeId).orElse(null);
        if (job == null) {
            log.warn("No parse job found for resumeId={}, skipping", resumeId);
            return;
        }

        Resume resume = resumeRepository.findByIdActive(resumeId).orElse(null);
        if (resume == null) {
            log.warn("Resume not found for ID={}, marking job as failed", resumeId);
            failJob(job, "Resume not found");
            return;
        }

        // Mark job as RUNNING
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        resumeParseJobRepository.save(job);

        // Mark resume as PARSING
        resume.setStatus(ResumeStatus.PARSING);
        resumeRepository.save(resume);

        Timer.Sample timerSample = Timer.start(meterRegistry);
        String outcome = "success";

        try {
            // Step 1: Get file info
            ResumeFile file = resumeFileRepository.findByResumeId(resumeId)
                    .orElseThrow(() -> new RuntimeException("No file found for resumeId=" + resumeId));

            // Step 2: Download file bytes from MinIO
            log.info("Downloading file from MinIO: bucket={}, key={}", file.getObjectBucket(), file.getObjectKey());
            byte[] fileBytes;
            try (InputStream inputStream = fileStorageService.download(file.getObjectBucket(), file.getObjectKey())) {
                fileBytes = inputStream.readAllBytes();
            }

            // Step 3: Save progress, then AI parsing — send file bytes directly (Gemini reads PDF natively)
            job.setProgress(30);
            resumeParseJobRepository.save(job);

            // Step 4: AI parsing — contact, experience, education, skills, skill profile
            job.setProgress(50);
            resumeParseJobRepository.save(job);

            aiParsingService.parseFromFile(fileBytes, file.getFileType().getMimeType(), resumeId);

            job.setProgress(90);
            resumeParseJobRepository.save(job);

            // Step 6: Mark as completed
            job.setStatus(JobStatus.SUCCEEDED);
            job.setFinishedAt(LocalDateTime.now());
            job.setProgress(100);
            resumeParseJobRepository.save(job);

            resume.setStatus(ResumeStatus.COMPLETED);
            resumeRepository.save(resume);

            log.info("Worker completed successfully for resumeId={}", resumeId);

        } catch (Exception e) {
            log.error("Worker failed for resumeId={}: {}", resumeId, e.getMessage(), e);
            outcome = "failure";
            failJob(job, e.getMessage());
            resume.setStatus(ResumeStatus.FAILED);
            resumeRepository.save(resume);
        } finally {
            timerSample.stop(Timer.builder("resume.parse.duration")
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }

    private void failJob(ResumeParseJob job, String errorMessage) {
        job.setStatus(JobStatus.FAILED);
        job.setFinishedAt(LocalDateTime.now());
        String msg = errorMessage != null ? errorMessage : "Unknown error";
        job.setErrorMessage(msg.length() <= 1000 ? msg : msg.substring(0, 1000));
        job.setRetryCount(job.getRetryCount() + 1);
        resumeParseJobRepository.save(job);
    }
}
