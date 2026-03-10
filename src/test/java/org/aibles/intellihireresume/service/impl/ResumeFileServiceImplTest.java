package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeFileMapper;
import org.aibles.intellihireresume.repository.ResumeFileRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.FileStorageService;
import org.aibles.intellihireresume.service.RedisJobQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeFileServiceImplTest {

    @Mock
    private ResumeFileRepository resumeFileRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private RedisJobQueueService redisJobQueueService;

    private ResumeFileMapper resumeFileMapper = new ResumeFileMapper();

    private ResumeFileServiceImpl resumeFileService;

    private String testResumeId;
    private String testUserId;
    private String testFileId;
    private String testBucketName;
    private Resume testResume;
    private ResumeFile testResumeFile;

    @BeforeEach
    void setUp() {
        resumeFileService = new ResumeFileServiceImpl(
                resumeFileRepository,
                resumeRepository,
                fileStorageService,
                redisJobQueueService,
                resumeFileMapper,
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry()
        );
        ReflectionTestUtils.setField(resumeFileService, "bucketName", "resume-files");

        testResumeId = "resume-123";
        testUserId = "user-456";
        testFileId = "file-789";
        testBucketName = "resume-files";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        testResumeFile = new ResumeFile();
        testResumeFile.setId(testFileId);
        testResumeFile.setResumeId(testResumeId);
        testResumeFile.setOriginalName("resume.pdf");
        testResumeFile.setFileType(FileType.PDF);
        testResumeFile.setFileSizeBytes(1024L);
        testResumeFile.setObjectBucket(testBucketName);
        testResumeFile.setObjectKey(testResumeId + "/some-uuid.pdf");
        testResumeFile.setCreatedBy(testUserId);
        testResumeFile.setCreatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testResumeFile.setUpdatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
    }

    // UPLOAD TESTS

    @Test
    void upload_ShouldReturnFileResponse_WhenValidPdfUploaded() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("resume.pdf");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[1024]));

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(fileStorageService.upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn(testResumeId + "/some-uuid.pdf");
        when(resumeFileRepository.save(any(ResumeFile.class))).thenReturn(testResumeFile);

        // When
        ResumeFileResponse result = resumeFileService.upload(testResumeId, testUserId, mockFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testFileId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getFileType()).isEqualTo(FileType.PDF);

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(resumeFileRepository).findByResumeId(testResumeId);
        verify(fileStorageService).upload(eq(testBucketName), anyString(), any(InputStream.class), eq(1024L), eq("application/pdf"));
        verify(resumeFileRepository).save(any(ResumeFile.class));
        verify(redisJobQueueService).enqueue(testResumeId);
    }

    @Test
    void upload_ShouldThrowNotFoundException_WhenResumeNotFound() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.upload(testResumeId, testUserId, mockFile))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(resumeFileRepository, never()).save(any());
        verify(redisJobQueueService, never()).enqueue(any());
    }

    @Test
    void upload_ShouldThrowDuplicateException_WhenFileAlreadyExists() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testResumeFile));

        // When & Then
        assertThatThrownBy(() -> resumeFileService.upload(testResumeId, testUserId, mockFile))
                .isInstanceOf(DuplicateException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_004.getCode());

        verify(resumeFileRepository, never()).save(any());
        verify(redisJobQueueService, never()).enqueue(any());
    }

    @Test
    void upload_ShouldThrowBadRequestException_WhenInvalidFileType() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.upload(testResumeId, testUserId, mockFile))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_002.getCode());

        verify(resumeFileRepository, never()).save(any());
        verify(redisJobQueueService, never()).enqueue(any());
    }

    @Test
    void upload_ShouldThrowBadRequestException_WhenFileSizeExceeds5MB() {
        // Given
        long sixMB = 6L * 1024 * 1024;
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(sixMB);
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.upload(testResumeId, testUserId, mockFile))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_003.getCode());

        verify(resumeFileRepository, never()).save(any());
        verify(redisJobQueueService, never()).enqueue(any());
    }

    // GET BY RESUME ID TESTS

    @Test
    void getByResumeId_ShouldReturnFileResponse_WhenFileExists() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testResumeFile));

        // When
        ResumeFileResponse result = resumeFileService.getByResumeId(testResumeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testFileId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getOriginalName()).isEqualTo("resume.pdf");

        verify(resumeFileRepository).findByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldThrowNotFoundException_WhenFileNotFound() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.getByResumeId(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_001.getCode());

        verify(resumeFileRepository).findByResumeId(testResumeId);
    }

    // DOWNLOAD TESTS

    @Test
    void download_ShouldReturnBytes_WhenFileExists() {
        // Given
        byte[] fileContent = "fake-pdf-content".getBytes();
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testResumeFile));
        when(fileStorageService.download(testBucketName, testResumeFile.getObjectKey()))
                .thenReturn(new ByteArrayInputStream(fileContent));

        // When
        byte[] result = resumeFileService.download(testResumeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(fileContent);

        verify(resumeFileRepository).findByResumeId(testResumeId);
        verify(fileStorageService).download(testBucketName, testResumeFile.getObjectKey());
    }

    @Test
    void download_ShouldThrowNotFoundException_WhenFileNotFound() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.download(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_001.getCode());

        verify(fileStorageService, never()).download(any(), any());
    }

    // DELETE TESTS

    @Test
    void delete_ShouldDeleteFileFromStorageAndDb_WhenFileExists() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testResumeFile));
        doNothing().when(fileStorageService).delete(testBucketName, testResumeFile.getObjectKey());
        doNothing().when(resumeFileRepository).delete(testResumeFile);

        // When
        resumeFileService.delete(testResumeId);

        // Then
        verify(resumeFileRepository).findByResumeId(testResumeId);
        verify(fileStorageService).delete(testBucketName, testResumeFile.getObjectKey());
        verify(resumeFileRepository).delete(testResumeFile);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenFileNotFound() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.delete(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_001.getCode());

        verify(fileStorageService, never()).delete(any(), any());
        verify(resumeFileRepository, never()).delete(any(ResumeFile.class));
    }

    @Test
    void upload_ShouldReturnFileResponse_WhenValidDocxUploaded() throws IOException {
        // Given
        ResumeFile docxFile = new ResumeFile();
        docxFile.setId(testFileId);
        docxFile.setResumeId(testResumeId);
        docxFile.setOriginalName("resume.docx");
        docxFile.setFileType(FileType.DOCX);
        docxFile.setFileSizeBytes(2048L);
        docxFile.setObjectBucket(testBucketName);
        docxFile.setObjectKey(testResumeId + "/some-uuid.docx");
        docxFile.setCreatedBy(testUserId);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        when(mockFile.getSize()).thenReturn(2048L);
        when(mockFile.getOriginalFilename()).thenReturn("resume.docx");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[2048]));

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(fileStorageService.upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn(testResumeId + "/some-uuid.docx");
        when(resumeFileRepository.save(any(ResumeFile.class))).thenReturn(docxFile);

        // When
        ResumeFileResponse result = resumeFileService.upload(testResumeId, testUserId, mockFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileType()).isEqualTo(FileType.DOCX);
        verify(redisJobQueueService).enqueue(testResumeId);
    }

    @Test
    void upload_ShouldThrowBadRequestException_WhenFileSizeIsZero() {
        // Given — isValidSize(0) returns false
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(0L);

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.upload(testResumeId, testUserId, mockFile))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_003.getCode());

        verify(resumeFileRepository, never()).save(any());
        verify(redisJobQueueService, never()).enqueue(any());
    }

    @Test
    void upload_ShouldThrowBadRequestException_WhenMimeTypeIsNull() {
        // Given — fromMimeType(null) returns null → FILE_002
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(null);

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.upload(testResumeId, testUserId, mockFile))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_002.getCode());

        verify(resumeFileRepository, never()).save(any());
        verify(redisJobQueueService, never()).enqueue(any());
    }

    @Test
    void download_ShouldThrowRuntimeException_WhenStorageThrowsException() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testResumeFile));
        when(fileStorageService.download(testBucketName, testResumeFile.getObjectKey()))
                .thenThrow(new RuntimeException("Storage connection failed"));

        // When & Then
        assertThatThrownBy(() -> resumeFileService.download(testResumeId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to download file");
    }

    @Test
    void delete_ShouldThrowRuntimeException_WhenStorageDeleteFails() {
        // Given
        when(resumeFileRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testResumeFile));
        doThrow(new RuntimeException("Storage error")).when(fileStorageService)
                .delete(testBucketName, testResumeFile.getObjectKey());

        // When & Then
        assertThatThrownBy(() -> resumeFileService.delete(testResumeId))
                .isInstanceOf(RuntimeException.class);

        verify(resumeFileRepository, never()).delete(any(ResumeFile.class));
    }
}
