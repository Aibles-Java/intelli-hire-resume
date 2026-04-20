package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.service.ResumeContactService;
import org.aibles.intellihireresume.service.ResumeEducationService;
import org.aibles.intellihireresume.service.ResumeExperienceService;
import org.aibles.intellihireresume.service.ResumeSkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wraps individual AI parse save operations in REQUIRES_NEW transactions so that
 * a validation failure in one item rolls back only that item's transaction and does
 * not poison the outer transaction in AiParsingServiceImpl.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPersistenceHelper {

    private final ResumeContactService resumeContactService;
    private final ResumeExperienceService resumeExperienceService;
    private final ResumeEducationService resumeEducationService;
    private final ResumeSkillService resumeSkillService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveContact(String resumeId, ResumeContactRequest request) {
        resumeContactService.createOrUpdate(resumeId, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveExperience(String resumeId, ResumeExperienceRequest request) {
        resumeExperienceService.create(resumeId, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveEducation(String resumeId, ResumeEducationRequest request) {
        resumeEducationService.create(resumeId, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveSkill(String resumeId, ResumeSkillRequest request) {
        resumeSkillService.create(resumeId, request);
    }
}
