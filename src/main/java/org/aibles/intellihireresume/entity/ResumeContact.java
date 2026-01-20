package org.aibles.intellihireresume.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "resume_contacts")
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeContact extends BaseEntity {
    
    @Column(name = "resume_id", length = 36, unique = true)
    private String resumeId;
    
    @Column(name = "full_name", length = 255)
    private String fullName;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;
    
    @Column(name = "other_info", columnDefinition = "TEXT")
    private String otherInfo;
}