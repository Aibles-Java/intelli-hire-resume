package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.entity.ResumeContact;
import org.springframework.stereotype.Component;

@Component
public class ResumeContactMapper {

    public ResumeContact toEntity(String resumeId, ResumeContactRequest request) {
        if (request == null) {
            return null;
        }
        ResumeContact contact = new ResumeContact();
        contact.setResumeId(resumeId);
        contact.setFullName(request.getFullName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setLocation(request.getLocation());
        contact.setLinkedinUrl(request.getLinkedinUrl());
        contact.setOtherInfo(request.getOtherInfo());
        return contact;
    }

    public void updateEntity(ResumeContact contact, ResumeContactRequest request) {
        contact.setFullName(request.getFullName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setLocation(request.getLocation());
        contact.setLinkedinUrl(request.getLinkedinUrl());
        contact.setOtherInfo(request.getOtherInfo());
    }

    public ResumeContactResponse toResponse(ResumeContact entity) {
        if (entity == null) {
            return null;
        }
        return ResumeContactResponse.builder()
                .id(entity.getId())
                .resumeId(entity.getResumeId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .location(entity.getLocation())
                .linkedinUrl(entity.getLinkedinUrl())
                .otherInfo(entity.getOtherInfo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
