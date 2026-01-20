package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aibles.intellihireresume.entity.enums.SeniorityLevel;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "resume_skill_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeSkillProfile extends BaseEntity {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "resume_id", length = 36, nullable = false, unique = true)
    private String resumeId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "top_skills", columnDefinition = "json")
    private Map<String, Object> topSkills;
    
    @Column(name = "years_estimated", precision = 5, scale = 2)
    private BigDecimal yearsEstimated;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", length = 20)
    private SeniorityLevel seniority;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signals", columnDefinition = "json")
    private Map<String, Object> signals;
    
    @CreatedDate
    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;
    
    @Column(name = "generated_by", length = 36, updatable = false)
    private String generatedBy;
    
}