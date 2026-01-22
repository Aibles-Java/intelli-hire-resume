package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_skills")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeSkill extends BaseEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "resume_id", length = 36, nullable = false)
    private String resumeId;
    
    @Column(name = "skill_id", length = 36, nullable = false)
    private String skillId;
    
    @Column(name = "proficiency_level", length = 20)
    private String proficiencyLevel;
    
    @Column(name = "years_experience", precision = 4, scale = 1)
    private BigDecimal yearsExperience;
    
    @Column(name = "is_estimated", nullable = false)
    private Boolean isEstimated = false;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "evidence_text", length = 500)
    private String evidenceText;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
    
    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;
}