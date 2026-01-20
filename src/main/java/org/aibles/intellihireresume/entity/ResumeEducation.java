package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "resume_educations")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeEducation extends BaseEntity {
    
    @Column(name = "resume_id", length = 36, nullable = false)
    private String resumeId;
    
    @Column(name = "school", length = 255, nullable = false)
    private String school;
    
    @Column(name = "degree", length = 255)
    private String degree;
    
    @Column(name = "field", length = 255)
    private String field;
    
    @Column(name = "start_year")
    private Integer startYear;
    
    @Column(name = "end_year")
    private Integer endYear;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}