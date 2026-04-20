package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.entity.ResumeContact;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeContactMapper;
import org.aibles.intellihireresume.repository.ResumeContactRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.service.ResumeContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeContactServiceImpl implements ResumeContactService {

    private final ResumeContactRepository contactRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeContactMapper contactMapper;

    @Override
    @Transactional(readOnly = true)
    public ResumeContactResponse getByResumeId(String resumeId) {
        log.info("Getting contact for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        return contactRepository.findByResumeId(resumeId)
                .map(contactMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CONTACT_001));
    }

    @Override
    public ResumeContactResponse createOrUpdate(String resumeId, ResumeContactRequest request) {
        log.info("Creating or updating contact for resume ID: {}", resumeId);
        validateResumeExists(resumeId);

        ResumeContact contact = contactRepository.findByResumeId(resumeId).orElse(null);

        if (contact == null) {
            log.info("No existing contact for resume ID: {}, creating new", resumeId);
            contact = contactMapper.toEntity(resumeId, request);
        } else {
            log.info("Existing contact found for resume ID: {}, updating", resumeId);
            contactMapper.updateEntity(contact, request);
        }

        ResumeContact saved = contactRepository.save(contact);
        log.info("Contact saved successfully for resume ID: {}", resumeId);
        return contactMapper.toResponse(saved);
    }

    private void validateResumeExists(String resumeId) {
        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }
    }
}
