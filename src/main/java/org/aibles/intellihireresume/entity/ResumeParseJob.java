package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_parse_jobs")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeParseJob extends BaseEntity {
    
    @Column(name = "resume_id", length = 36, nullable = false, unique = true)
    private String resumeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private JobStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50, nullable = false)
    private JobType jobType;
    
    @Column(name = "progress", nullable = false)
    private Integer progress = 0;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
}