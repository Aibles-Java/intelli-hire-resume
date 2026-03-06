package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.CreateResumeRequest;
import org.aibles.intellihireresume.dto.ResumeResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeMapperTest {

    private ResumeMapper resumeMapper;

    @BeforeEach
    void setUp() {
        resumeMapper = new ResumeMapper();
    }

    // toEntity tests

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        Resume result = resumeMapper.toEntity(null);
        assertThat(result).isNull();
    }

    @Test
    void toEntity_ShouldMapTitleFromRequest() {
        CreateResumeRequest request = CreateResumeRequest.builder()
                .title("My Resume")
                .build();

        Resume result = resumeMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("My Resume");
    }

    @Test
    void toEntity_ShouldMapNullTitle_WhenRequestHasNullTitle() {
        CreateResumeRequest request = CreateResumeRequest.builder().build();

        Resume result = resumeMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isNull();
    }

    // toResponse tests

    @Test
    void toResponse_ShouldReturnNull_WhenResumeIsNull() {
        ResumeResponse result = resumeMapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 22, 10, 0, 0);
        Resume resume = Resume.builder()
                .id("resume-001")
                .userId("user-001")
                .title("Software Engineer CV")
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        resume.setCreatedBy("system");
        resume.setUpdatedBy("user-001");

        ResumeResponse result = resumeMapper.toResponse(resume);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("resume-001");
        assertThat(result.getUserId()).isEqualTo("user-001");
        assertThat(result.getTitle()).isEqualTo("Software Engineer CV");
        assertThat(result.getStatus()).isEqualTo(ResumeStatus.UPLOADED);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
        assertThat(result.getCreatedBy()).isEqualTo("system");
        assertThat(result.getUpdatedBy()).isEqualTo("user-001");
    }

    // toResponseList tests

    @Test
    void toResponseList_ShouldReturnEmptyList_WhenListIsEmpty() {
        List<ResumeResponse> result = resumeMapper.toResponseList(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    void toResponseList_ShouldMapAllResumesInList() {
        Resume resume1 = Resume.builder().id("r1").userId("u1").title("CV 1").status(ResumeStatus.UPLOADED).isActive(true).build();
        Resume resume2 = Resume.builder().id("r2").userId("u1").title("CV 2").status(ResumeStatus.COMPLETED).isActive(true).build();

        List<ResumeResponse> result = resumeMapper.toResponseList(List.of(resume1, resume2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("r1");
        assertThat(result.get(1).getId()).isEqualTo("r2");
        assertThat(result.get(0).getTitle()).isEqualTo("CV 1");
        assertThat(result.get(1).getTitle()).isEqualTo("CV 2");
    }
}
