package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_files")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeFile extends BaseEntity {
    
    @Column(name = "resume_id", length = 36, nullable = false, unique = true)
    private String resumeId;
    
    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 10, nullable = false)
    private FileType fileType;
    
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;
    
    @Column(name = "object_bucket", length = 100, nullable = false)
    private String objectBucket;
    
    @Column(name = "object_key", nullable = false, columnDefinition = "TEXT")
    private String objectKey;
    
    @Column(name = "object_etag", length = 100)
    private String objectEtag;
    
    
}