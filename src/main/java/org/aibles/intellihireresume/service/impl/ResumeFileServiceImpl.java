package org.aibles.intellihireresume.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeFileMapper;
import org.aibles.intellihireresume.repository.ResumeFileRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.FileStorageService;
import org.aibles.intellihireresume.service.RedisJobQueueService;
import org.aibles.intellihireresume.service.ResumeFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeFileServiceImpl implements ResumeFileService {

    private final ResumeFileRepository resumeFileRepository;
    private final ResumeRepository resumeRepository;
    private final FileStorageService fileStorageService;
    private final RedisJobQueueService redisJobQueueService;
    private final ResumeFileMapper resumeFileMapper;
    private final MeterRegistry meterRegistry;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public ResumeFileResponse upload(String resumeId, String userId, MultipartFile file) {
        log.info("Uploading file for resume: {}, userId: {}", resumeId, userId);

        try {
            resumeRepository.findByIdActive(resumeId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.RES_001));

            resumeFileRepository.findByResumeId(resumeId).ifPresent(existing -> {
                throw new DuplicateException(ErrorCode.FILE_004);
            });

            FileType fileType = FileType.fromMimeType(file.getContentType());
            if (fileType == null) {
                throw new BadRequestException(ErrorCode.FILE_002);
            }

            if (!fileType.isValidSize(file.getSize())) {
                throw new BadRequestException(ErrorCode.FILE_003);
            }

            String objectKey = resumeId + "/" + UUID.randomUUID() + "." + fileType.getExtension();

            try (InputStream inputStream = file.getInputStream()) {
                fileStorageService.upload(bucketName, objectKey, inputStream, file.getSize(), file.getContentType());
            }

            ResumeFile resumeFile = new ResumeFile();
            resumeFile.setResumeId(resumeId);
            resumeFile.setOriginalName(file.getOriginalFilename());
            resumeFile.setFileType(fileType);
            resumeFile.setFileSizeBytes(file.getSize());
            resumeFile.setObjectBucket(bucketName);
            resumeFile.setObjectKey(objectKey);
            resumeFile.setCreatedBy(userId);

            ResumeFile savedFile = resumeFileRepository.save(resumeFile);
            meterRegistry.counter("file.upload.count", "fileType", fileType.name()).increment();

            // Enqueue after DB commit to avoid orphaned queue entries on rollback
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisJobQueueService.enqueue(resumeId);
                    }
                });
            } else {
                redisJobQueueService.enqueue(resumeId);
            }

            log.info("File uploaded successfully for resume: {}, fileId: {}", resumeId, savedFile.getId());
            return resumeFileMapper.toResponse(savedFile);
        } catch (Exception e) {
            log.error("Failed to upload file for resume: {}, error: {}", resumeId, e.getMessage(), e);
            throw (e instanceof RuntimeException re) ? re : new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeFileResponse getByResumeId(String resumeId) {
        log.info("Getting file metadata for resume: {}", resumeId);

        ResumeFile resumeFile = findByResumeId(resumeId);
        return resumeFileMapper.toResponse(resumeFile);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(String resumeId) {
        log.info("Downloading file for resume: {}", resumeId);

        ResumeFile resumeFile = findByResumeId(resumeId);

        try (InputStream inputStream = fileStorageService.download(resumeFile.getObjectBucket(), resumeFile.getObjectKey())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to download file for resume: {}, error: {}", resumeId, e.getMessage(), e);
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String resumeId) {
        log.info("Deleting file for resume: {}", resumeId);

        try {
            ResumeFile resumeFile = findByResumeId(resumeId);

            fileStorageService.delete(resumeFile.getObjectBucket(), resumeFile.getObjectKey());
            resumeFileRepository.delete(resumeFile);

            log.info("File deleted successfully for resume: {}", resumeId);
        } catch (Exception e) {
            log.error("Failed to delete file for resume: {}, error: {}", resumeId, e.getMessage(), e);
            throw (e instanceof RuntimeException re) ? re : new RuntimeException(e);
        }
    }

    private ResumeFile findByResumeId(String resumeId) {
        return resumeFileRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FILE_001));
    }
}
