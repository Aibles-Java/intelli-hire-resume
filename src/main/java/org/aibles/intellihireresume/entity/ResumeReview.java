package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "resume_reviews")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeReview extends BaseEntity {

    @Column(name = "resume_id", length = 36, nullable = false, unique = true)
    private String resumeId;

    @Column(name = "overall_score", nullable = false, precision = 3, scale = 1)
    private BigDecimal overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections", columnDefinition = "json", nullable = false)
    private Map<String, Object> sections;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strengths", columnDefinition = "json", nullable = false)
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weaknesses", columnDefinition = "json", nullable = false)
    private List<String> weaknesses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "priority_actions", columnDefinition = "json", nullable = false)
    private List<String> priorityActions;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
