package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;

@Entity
@Table(name = "resumes")
@Data
@EqualsAndHashCode(callSuper = true)
public class Resume extends BaseEntity {
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "title", length = 255)
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ResumeStatus status;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
}