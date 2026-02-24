package org.aibles.intellihireresume.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @CreatedBy
    @Column(name = "created_by", length = 36, nullable = false, updatable = false)
    private String createdBy;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    public void initDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}