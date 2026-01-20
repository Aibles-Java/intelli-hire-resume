package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_experiences")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeExperience extends BaseEntity {
    
    @Column(name = "resume_id", length = 36, nullable = false)
    private String resumeId;
    
    @Column(name = "company", length = 255, nullable = false)
    private String company;
    
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;
}