package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeFileResponse;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeFileMapperTest {

    private ResumeFileMapper fileMapper;

    @BeforeEach
    void setUp() {
        fileMapper = new ResumeFileMapper();
    }

    @Test
    void toResponse_ShouldReturnNull_WhenEntityIsNull() {
        ResumeFileResponse result = fileMapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 22, 10, 0, 0);
        ResumeFile file = new ResumeFile();
        file.setId("file-001");
        file.setResumeId("resume-001");
        file.setOriginalName("my_resume.pdf");
        file.setFileType(FileType.PDF);
        file.setFileSizeBytes(1024L);
        file.setObjectBucket("resumes");
        file.setObjectKey("resumes/user-001/resume-001.pdf");
        file.setCreatedAt(now);
        file.setUpdatedAt(now);

        ResumeFileResponse result = fileMapper.toResponse(file);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("file-001");
        assertThat(result.getResumeId()).isEqualTo("resume-001");
        assertThat(result.getOriginalName()).isEqualTo("my_resume.pdf");
        assertThat(result.getFileType()).isEqualTo(FileType.PDF);
        assertThat(result.getFileSizeBytes()).isEqualTo(1024L);
        assertThat(result.getObjectBucket()).isEqualTo("resumes");
        assertThat(result.getObjectKey()).isEqualTo("resumes/user-001/resume-001.pdf");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_ShouldMapDocxFileType() {
        ResumeFile file = new ResumeFile();
        file.setId("file-002");
        file.setResumeId("resume-002");
        file.setOriginalName("resume.docx");
        file.setFileType(FileType.DOCX);
        file.setFileSizeBytes(2048L);
        file.setObjectBucket("resumes");
        file.setObjectKey("resumes/user-001/resume-002.docx");

        ResumeFileResponse result = fileMapper.toResponse(file);

        assertThat(result).isNotNull();
        assertThat(result.getFileType()).isEqualTo(FileType.DOCX);
        assertThat(result.getOriginalName()).isEqualTo("resume.docx");
    }
}
