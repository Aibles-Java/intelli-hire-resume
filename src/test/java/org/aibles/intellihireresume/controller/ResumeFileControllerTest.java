package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.aibles.intellihireresume.exception.BadRequestException;
import org.aibles.intellihireresume.exception.DuplicateException;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeFileController.class)
class ResumeFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeFileService resumeFileService;

    private String testResumeId;
    private String testUserId;
    private String testFileId;
    private ResumeFileResponse fileResponse;
    private MockMultipartFile mockPdfFile;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";
        testFileId = "file-789";

        fileResponse = ResumeFileResponse.builder()
                .id(testFileId)
                .resumeId(testResumeId)
                .originalName("resume.pdf")
                .fileType(FileType.PDF)
                .fileSizeBytes(1024L)
                .objectBucket("resume-files")
                .objectKey(testResumeId + "/uuid.pdf")
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        mockPdfFile = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "fake-pdf-content".getBytes()
        );
    }

    // UPLOAD TESTS

    @Test
    void upload_ShouldReturn201_WhenValidPdfUploaded() throws Exception {
        when(resumeFileService.upload(eq(testResumeId), eq(testUserId), any())).thenReturn(fileResponse);

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .file(mockPdfFile)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testFileId))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.original_name").value("resume.pdf"))
                .andExpect(jsonPath("$.data.file_type").value("PDF"));

        verify(resumeFileService).upload(eq(testResumeId), eq(testUserId), any());
    }

    @Test
    void upload_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .file(mockPdfFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isForbidden());

        verify(resumeFileService, never()).upload(any(), any(), any());
    }

    @Test
    void upload_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(resumeFileService.upload(eq(testResumeId), eq(testUserId), any()))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .file(mockPdfFile)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(resumeFileService).upload(eq(testResumeId), eq(testUserId), any());
    }

    @Test
    void upload_ShouldReturn409_WhenFileAlreadyExists() throws Exception {
        when(resumeFileService.upload(eq(testResumeId), eq(testUserId), any()))
                .thenThrow(new DuplicateException(ErrorCode.FILE_004));

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .file(mockPdfFile)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.FILE_004.getCode()));

        verify(resumeFileService).upload(eq(testResumeId), eq(testUserId), any());
    }

    @Test
    void upload_ShouldReturn400_WhenInvalidFileType() throws Exception {
        when(resumeFileService.upload(eq(testResumeId), eq(testUserId), any()))
                .thenThrow(new BadRequestException(ErrorCode.FILE_002));

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .file(mockPdfFile)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.FILE_002.getCode()));
    }

    @Test
    void upload_ShouldReturn400_WhenFileSizeExceeded() throws Exception {
        when(resumeFileService.upload(eq(testResumeId), eq(testUserId), any()))
                .thenThrow(new BadRequestException(ErrorCode.FILE_003));

        mockMvc.perform(multipart("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .file(mockPdfFile)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.FILE_003.getCode()));
    }

    // GET METADATA TESTS

    @Test
    void getMetadata_ShouldReturn200_WhenFileExists() throws Exception {
        when(resumeFileService.getByResumeId(testResumeId)).thenReturn(fileResponse);

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testFileId))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.file_size_bytes").value(1024));

        verify(resumeFileService).getByResumeId(testResumeId);
    }

    @Test
    void getMetadata_ShouldReturn404_WhenFileNotFound() throws Exception {
        when(resumeFileService.getByResumeId(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.FILE_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.FILE_001.getCode()));
    }

    // DOWNLOAD TESTS

    @Test
    void download_ShouldReturn200WithBytes_WhenFileExists() throws Exception {
        byte[] fileContent = "fake-pdf-content".getBytes();
        when(resumeFileService.getByResumeId(testResumeId)).thenReturn(fileResponse);
        when(resumeFileService.download(testResumeId)).thenReturn(fileContent);

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/files/download", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("resume.pdf")))
                .andExpect(content().bytes(fileContent));

        verify(resumeFileService).getByResumeId(testResumeId);
        verify(resumeFileService).download(testResumeId);
    }

    @Test
    void download_ShouldReturn404_WhenFileNotFound() throws Exception {
        when(resumeFileService.getByResumeId(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.FILE_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/files/download", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.FILE_001.getCode()));
    }

    // DELETE TESTS

    @Test
    void delete_ShouldReturn200_WhenFileDeleted() throws Exception {
        doNothing().when(resumeFileService).delete(testResumeId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(resumeFileService).delete(testResumeId);
    }

    @Test
    void delete_ShouldReturn404_WhenFileNotFound() throws Exception {
        doThrow(new NotFoundException(ErrorCode.FILE_001)).when(resumeFileService).delete(testResumeId);

        mockMvc.perform(delete("/api/v1/resumes/{resumeId}/files", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.FILE_001.getCode()));
    }
}
