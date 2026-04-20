package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ParseJobMapperTest {

    private ParseJobMapper parseJobMapper;

    @BeforeEach
    void setUp() {
        parseJobMapper = new ParseJobMapper();
    }

    @Test
    void toResponse_ShouldReturnNull_WhenEntityIsNull() {
        ParseJobResponse result = parseJobMapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 22, 10, 0, 0);
        LocalDateTime started = LocalDateTime.of(2024, 1, 22, 10, 1, 0);
        LocalDateTime finished = LocalDateTime.of(2024, 1, 22, 10, 3, 0);

        ResumeParseJob job = new ResumeParseJob();
        job.setId("job-001");
        job.setResumeId("resume-001");
        job.setStatus(JobStatus.SUCCEEDED);
        job.setJobType(JobType.PARSE);
        job.setProgress(100);
        job.setRetryCount(0);
        job.setErrorMessage(null);
        job.setStartedAt(started);
        job.setFinishedAt(finished);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);

        ParseJobResponse result = parseJobMapper.toResponse(job);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("job-001");
        assertThat(result.getResumeId()).isEqualTo("resume-001");
        assertThat(result.getStatus()).isEqualTo(JobStatus.SUCCEEDED);
        assertThat(result.getJobType()).isEqualTo(JobType.PARSE);
        assertThat(result.getProgress()).isEqualTo(100);
        assertThat(result.getRetryCount()).isEqualTo(0);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getStartedAt()).isEqualTo(started);
        assertThat(result.getFinishedAt()).isEqualTo(finished);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_ShouldMapFailedJobWithErrorMessage() {
        ResumeParseJob job = new ResumeParseJob();
        job.setId("job-002");
        job.setResumeId("resume-002");
        job.setStatus(JobStatus.FAILED);
        job.setJobType(JobType.REPARSE);
        job.setProgress(30);
        job.setRetryCount(2);
        job.setErrorMessage("Failed to parse PDF: corrupted file");

        ParseJobResponse result = parseJobMapper.toResponse(job);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(result.getJobType()).isEqualTo(JobType.REPARSE);
        assertThat(result.getRetryCount()).isEqualTo(2);
        assertThat(result.getErrorMessage()).isEqualTo("Failed to parse PDF: corrupted file");
    }
}
