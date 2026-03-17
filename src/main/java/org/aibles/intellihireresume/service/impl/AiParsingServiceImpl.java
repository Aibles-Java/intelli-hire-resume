package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.config.AiProperties;
import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.dto.ai.AiContactResult;
import org.aibles.intellihireresume.dto.ai.AiEducationResult;
import org.aibles.intellihireresume.dto.ai.AiExperienceResult;
import org.aibles.intellihireresume.dto.ai.AiSkillResult;
import org.aibles.intellihireresume.dto.ai.ResumeParseResult;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.exception.AiAuthException;
import org.aibles.intellihireresume.exception.AiRateLimitException;
import org.aibles.intellihireresume.repository.ResumeContactRepository;
import org.aibles.intellihireresume.repository.ResumeEducationRepository;
import org.aibles.intellihireresume.repository.ResumeExperienceRepository;
import org.aibles.intellihireresume.repository.ResumeSkillProfileRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.AiParsingService;
import org.aibles.intellihireresume.service.ResumeContactService;
import org.aibles.intellihireresume.service.ResumeEducationService;
import org.aibles.intellihireresume.service.ResumeExperienceService;
import org.aibles.intellihireresume.service.ResumeSkillProfileService;
import org.aibles.intellihireresume.service.ResumeSkillService;
import org.aibles.intellihireresume.service.TextExtractionService;
import org.aibles.intellihireresume.service.ai.AiProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiParsingServiceImpl implements AiParsingService {

    private final AiProvider aiProvider;
    private final AiProperties aiProperties;
    private final TextExtractionService textExtractionService;
    private final SkillRepository skillRepository;
    private final ResumeContactService resumeContactService;
    private final ResumeExperienceService resumeExperienceService;
    private final ResumeEducationService resumeEducationService;
    private final ResumeSkillService resumeSkillService;
    private final ResumeSkillProfileService resumeSkillProfileService;
    private final ResumeContactRepository resumeContactRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeEducationRepository resumeEducationRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeSkillProfileRepository resumeSkillProfileRepository;

    @Override
    @Transactional
    public void parse(String rawText, String resumeId) {
        log.info("Starting AI parsing for resumeId={}", resumeId);

        // Load skill catalog once — used for AI prompt and name→ID lookup
        List<Skill> allSkills = skillRepository.findByIsActiveTrue();
        List<String> catalogNames = allSkills.stream().map(Skill::getName).toList();
        Map<String, Skill> catalogByName = allSkills.stream()
            .collect(Collectors.toMap(Skill::getName, s -> s));

        log.info("Skill catalog loaded: {} skills", catalogNames.size());

        ResumeParseResult result = parseWithRetry(rawText, catalogNames);
        persistResults(resumeId, result, catalogByName);

        log.info("AI parsing completed for resumeId={}", resumeId);
    }

    @Override
    @Transactional
    public void parseFromFile(byte[] fileBytes, String mimeType, String resumeId) {
        log.info("Starting AI file parsing for resumeId={}, mimeType={}", resumeId, mimeType);

        List<Skill> allSkills = skillRepository.findByIsActiveTrue();
        List<String> catalogNames = allSkills.stream().map(Skill::getName).toList();
        Map<String, Skill> catalogByName = allSkills.stream()
            .collect(Collectors.toMap(Skill::getName, s -> s));

        log.info("Skill catalog loaded: {} skills", catalogNames.size());

        ResumeParseResult result;
        if (aiProvider.supportsFileInput()) {
            log.info("Provider supports native file input — sending PDF bytes directly to AI");
            result = parseFromBytesWithRetry(fileBytes, mimeType, catalogNames);
        } else {
            log.info("Provider does not support file input — falling back to text extraction");
            String rawText = "application/pdf".equals(mimeType)
                ? textExtractionService.extractFromPdf(fileBytes)
                : textExtractionService.extractFromDocx(fileBytes);
            result = parseWithRetry(rawText, catalogNames);
        }

        persistResults(resumeId, result, catalogByName);
        log.info("AI file parsing completed for resumeId={}", resumeId);
    }

    private ResumeParseResult parseFromBytesWithRetry(byte[] fileBytes, String mimeType, List<String> catalogNames) {
        int maxRetries = aiProperties.getMaxRetries();
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return aiProvider.parseFromBytes(fileBytes, mimeType, catalogNames);
            } catch (AiAuthException e) {
                throw e;
            } catch (AiRateLimitException e) {
                lastException = e;
                log.warn("AI rate limited (429), attempt {}/{}, waiting {}s", attempt, maxRetries, e.getRetryAfterSeconds());
                if (attempt < maxRetries) sleepSeconds(e.getRetryAfterSeconds());
            } catch (Exception e) {
                lastException = e;
                log.warn("AI file parse attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) sleepSeconds((long) Math.pow(2, attempt));
            }
        }
        throw new RuntimeException("AI file parsing failed after " + maxRetries + " attempts", lastException);
    }

    private ResumeParseResult parseWithRetry(String rawText, List<String> catalogNames) {
        int maxRetries = aiProperties.getMaxRetries();
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return aiProvider.parse(rawText, catalogNames);
            } catch (AiAuthException e) {
                throw e; // 401 — no retry
            } catch (AiRateLimitException e) {
                lastException = e;
                log.warn("AI rate limited (429), attempt {}/{}, waiting {}s before retry",
                        attempt, maxRetries, e.getRetryAfterSeconds());
                if (attempt < maxRetries) {
                    sleepSeconds(e.getRetryAfterSeconds());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("AI parse attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
                if (attempt < maxRetries) {
                    sleepSeconds((long) Math.pow(2, attempt)); // 2s, 4s, 8s
                }
            }
        }
        throw new RuntimeException("AI parsing failed after " + maxRetries + " attempts", lastException);
    }

    @Transactional
    public void persistResults(String resumeId, ResumeParseResult result,
                               Map<String, Skill> catalogByName) {
        log.info("Persisting AI parse results for resumeId={}", resumeId);

        // 1. Delete existing child records (idempotency — clean re-parse)
        resumeContactRepository.deleteByResumeId(resumeId);
        resumeExperienceRepository.deleteByResumeId(resumeId);
        resumeEducationRepository.deleteByResumeId(resumeId);
        resumeSkillRepository.deleteByResumeId(resumeId);
        resumeSkillProfileRepository.deleteByResumeId(resumeId);

        // 2. Save contact info
        AiContactResult contact = result.contact();
        if (contact != null) {
            try {
                resumeContactService.createOrUpdate(resumeId, ResumeContactRequest.builder()
                    .fullName(contact.fullName())
                    .email(contact.email())
                    .phone(contact.phone())
                    .location(contact.location())
                    .linkedinUrl(contact.linkedinUrl())
                    .build());
            } catch (Exception e) {
                log.warn("Skipping contact (validation error): {}", e.getMessage());
            }
        }

        // 3. Save work experiences
        for (AiExperienceResult exp : result.experiences()) {
            if (exp.company() == null || exp.title() == null) continue;
            try {
                LocalDate startDate = exp.startYear() != null
                    ? LocalDate.of(exp.startYear(), exp.startMonth() != null ? exp.startMonth() : 1, 1)
                    : null;
                boolean isCurrent = Boolean.TRUE.equals(exp.isCurrent());
                LocalDate endDate = (!isCurrent && exp.endYear() != null)
                    ? LocalDate.of(exp.endYear(), exp.endMonth() != null ? exp.endMonth() : 12, 1)
                    : null;

                resumeExperienceService.create(resumeId, ResumeExperienceRequest.builder()
                    .company(exp.company())
                    .title(exp.title())
                    .startDate(startDate)
                    .endDate(endDate)
                    .isCurrent(isCurrent)
                    .description(exp.description())
                    .build());
            } catch (Exception e) {
                log.warn("Skipping experience '{}': {}", exp.title(), e.getMessage());
            }
        }

        // 4. Save educations
        for (AiEducationResult edu : result.educations()) {
            if (edu.school() == null) continue;
            try {
                resumeEducationService.create(resumeId, ResumeEducationRequest.builder()
                    .school(edu.school())
                    .degree(edu.degree())
                    .field(edu.field())
                    .startYear(edu.startYear())
                    .endYear(edu.endYear())
                    .description(edu.description())
                    .build());
            } catch (Exception e) {
                log.warn("Skipping education '{}': {}", edu.school(), e.getMessage());
            }
        }

        // 5. Save skills (catalog-matched only)
        for (AiSkillResult aiSkill : result.skills()) {
            Skill skill = catalogByName.get(aiSkill.name());
            if (skill == null) {
                log.warn("AI returned unknown skill '{}', skipping", aiSkill.name());
                continue;
            }
            try {
                BigDecimal confidence = (aiSkill.confidenceScore() != null
                    ? BigDecimal.valueOf(aiSkill.confidenceScore()) : BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
                BigDecimal years = aiSkill.yearsExperience() != null
                    ? BigDecimal.valueOf(aiSkill.yearsExperience()).setScale(1, RoundingMode.HALF_UP)
                    : null;
                String evidence = aiSkill.evidenceText() != null
                    ? aiSkill.evidenceText().substring(0, Math.min(aiSkill.evidenceText().length(), 500))
                    : null;

                resumeSkillService.create(resumeId, ResumeSkillRequest.builder()
                    .skillId(skill.getId())
                    .confidenceScore(confidence)
                    .yearsExperience(years)
                    .isPrimary(Boolean.TRUE.equals(aiSkill.isPrimary()))
                    .evidenceText(evidence)
                    .build());
            } catch (Exception e) {
                log.warn("Skipping skill '{}': {}", aiSkill.name(), e.getMessage());
            }
        }

        // 6. Persist AI-generated summary, then compute skill profile
        resumeSkillProfileService.upsertSummary(resumeId, result.summary());
        resumeSkillProfileService.generate(resumeId);

        log.info("All results persisted for resumeId={}", resumeId);
    }

    private void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry sleep interrupted", e);
        }
    }
}
