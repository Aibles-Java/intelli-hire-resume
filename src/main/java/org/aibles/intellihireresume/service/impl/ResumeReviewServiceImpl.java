package org.aibles.intellihireresume.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeReviewResponse;
import org.aibles.intellihireresume.dto.SectionScoreDto;
import org.aibles.intellihireresume.entity.*;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.repository.*;
import org.aibles.intellihireresume.service.ResumeReviewService;
import org.aibles.intellihireresume.service.ai.AiProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ResumeReviewServiceImpl implements ResumeReviewService {

    private final ResumeReviewRepository reviewRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeFileRepository fileRepository;
    private final ResumeContactRepository contactRepository;
    private final ResumeExperienceRepository experienceRepository;
    private final ResumeEducationRepository educationRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final SkillRepository skillRepository;
    private final ResumeSkillProfileRepository profileRepository;
    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ResumeReviewResponse getByResumeId(String resumeId) {
        log.info("Getting CV review for resume ID: {}", resumeId);
        validateResumeExists(resumeId);
        ResumeReview review = reviewRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_001));
        return toResponse(review);
    }

    @Override
    public ResumeReviewResponse generate(String resumeId) {
        log.info("Generating CV review for resume ID: {}", resumeId);
        validateResumeExists(resumeId);

        fileRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, ErrorCode.REVIEW_002.getMessage()));

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(resumeId);

        String rawJson;
        try {
            rawJson = aiProvider.complete(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("AI call failed for resume {}: {}", resumeId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI review generation failed: " + e.getMessage());
        }

        ResumeReview review = parseAndUpsert(resumeId, rawJson);
        log.info("CV review generated for resume ID: {}", resumeId);
        return toResponse(review);
    }

    private void validateResumeExists(String resumeId) {
        if (resumeRepository.findByIdActive(resumeId).isEmpty()) {
            throw new NotFoundException(ErrorCode.RES_001);
        }
    }

    private String buildSystemPrompt() {
        return "You are a professional CV reviewer. " +
               "Return ONLY valid JSON matching the exact structure requested. " +
               "No markdown, no code fences, no explanation outside the JSON.";
    }

    private String buildUserPrompt(String resumeId) {
        StringBuilder sb = new StringBuilder();

        contactRepository.findByResumeId(resumeId).ifPresent(c -> {
            sb.append("CONTACT:\n");
            sb.append("Name: ").append(nvl(c.getFullName())).append("\n");
            sb.append("Email: ").append(nvl(c.getEmail())).append("\n");
            sb.append("Phone: ").append(nvl(c.getPhone())).append("\n");
            sb.append("Location: ").append(nvl(c.getLocation())).append("\n\n");
        });

        List<ResumeExperience> experiences = experienceRepository.findByResumeId(resumeId);
        if (!experiences.isEmpty()) {
            sb.append("EXPERIENCE:\n");
            for (int i = 0; i < experiences.size(); i++) {
                ResumeExperience exp = experiences.get(i);
                String start = exp.getStartDate() != null ? exp.getStartDate().toString() : "?";
                String end = Boolean.TRUE.equals(exp.getIsCurrent()) ? "Present"
                        : (exp.getEndDate() != null ? exp.getEndDate().toString() : "?");
                sb.append(i + 1).append(". ").append(nvl(exp.getTitle()))
                        .append(" at ").append(nvl(exp.getCompany()))
                        .append(" (").append(start).append(" - ").append(end).append(")\n");
                if (exp.getDescription() != null && !exp.getDescription().isBlank()) {
                    sb.append("   ").append(exp.getDescription()).append("\n");
                }
            }
            sb.append("\n");
        }

        List<ResumeEducation> educations = educationRepository.findByResumeId(resumeId);
        if (!educations.isEmpty()) {
            sb.append("EDUCATION:\n");
            for (int i = 0; i < educations.size(); i++) {
                ResumeEducation edu = educations.get(i);
                sb.append(i + 1).append(". ").append(nvl(edu.getDegree()))
                        .append(" in ").append(nvl(edu.getField()))
                        .append(", ").append(nvl(edu.getSchool()));
                if (edu.getStartYear() != null || edu.getEndYear() != null) {
                    sb.append(" (").append(nvl(String.valueOf(edu.getStartYear())))
                            .append(" - ").append(nvl(String.valueOf(edu.getEndYear()))).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resumeId);
        if (!resumeSkills.isEmpty()) {
            List<String> skillIds = resumeSkills.stream().map(ResumeSkill::getSkillId).toList();
            Map<String, Skill> skillMap = skillRepository.findAllById(skillIds).stream()
                    .collect(Collectors.toMap(Skill::getId, s -> s));
            sb.append("SKILLS:\n");
            for (ResumeSkill rs : resumeSkills) {
                Skill skill = skillMap.get(rs.getSkillId());
                if (skill != null) {
                    sb.append("- ").append(skill.getName());
                    if (rs.getYearsExperience() != null) {
                        sb.append(" (").append(rs.getYearsExperience()).append(" yrs)");
                    }
                    if (Boolean.TRUE.equals(rs.getIsPrimary())) {
                        sb.append(" [PRIMARY]");
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        profileRepository.findByResumeId(resumeId).ifPresent(p -> {
            sb.append("SKILL PROFILE:\n");
            if (p.getSeniority() != null) sb.append("Seniority: ").append(p.getSeniority()).append("\n");
            if (p.getYearsEstimated() != null) sb.append("Years estimated: ").append(p.getYearsEstimated()).append("\n");
            if (p.getSummary() != null && !p.getSummary().isBlank()) sb.append("Summary: ").append(p.getSummary()).append("\n");
            sb.append("\n");
        });

        sb.append("""
Rate this CV on a scale of 0.0-10.0 (one decimal place) for each section:
- contact: completeness and quality of contact information
- experience: relevance, depth, and presentation of work experience
- education: educational background and qualifications
- skills: skill set breadth, depth, and relevance
- presentation: overall clarity, structure, and professionalism

Return ONLY this JSON (no markdown, no extra text):
{
  "overall_score": 7.5,
  "sections": {
    "contact":      {"score": 8.0, "feedback": "..."},
    "experience":   {"score": 7.0, "feedback": "..."},
    "education":    {"score": 9.0, "feedback": "..."},
    "skills":       {"score": 6.5, "feedback": "..."},
    "presentation": {"score": 7.5, "feedback": "..."}
  },
  "strengths":        ["...", "..."],
  "weaknesses":       ["...", "..."],
  "priority_actions": ["...", "...", "..."]
}
""");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private ResumeReview parseAndUpsert(String resumeId, String rawJson) {
        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(rawJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse AI JSON for resume {}: {}", resumeId, rawJson);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    ErrorCode.REVIEW_003.getMessage());
        }

        if (!parsed.containsKey("overall_score") || !parsed.containsKey("sections")) {
            log.error("AI response missing required fields for resume {}: {}", resumeId, rawJson);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    ErrorCode.REVIEW_003.getMessage());
        }

        BigDecimal overallScore;
        try {
            overallScore = new BigDecimal(parsed.get("overall_score").toString());
        } catch (Exception e) {
            log.error("Invalid overall_score in AI response for resume {}", resumeId);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, ErrorCode.REVIEW_003.getMessage());
        }

        Map<String, Object> sections;
        List<String> strengths;
        List<String> weaknesses;
        List<String> priorityActions;
        try {
            sections = (Map<String, Object>) parsed.get("sections");
            strengths = parsed.get("strengths") instanceof List ? (List<String>) parsed.get("strengths") : List.of();
            weaknesses = parsed.get("weaknesses") instanceof List ? (List<String>) parsed.get("weaknesses") : List.of();
            priorityActions = parsed.get("priority_actions") instanceof List ? (List<String>) parsed.get("priority_actions") : List.of();
        } catch (ClassCastException e) {
            log.error("Unexpected AI response structure for resume {}", resumeId);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, ErrorCode.REVIEW_003.getMessage());
        }

        ResumeReview review = reviewRepository.findByResumeId(resumeId)
                .orElseGet(() -> {
                    ResumeReview r = new ResumeReview();
                    r.setResumeId(resumeId);
                    return r;
                });

        review.setOverallScore(overallScore);
        review.setSections(sections);
        review.setStrengths(strengths);
        review.setWeaknesses(weaknesses);
        review.setPriorityActions(priorityActions);
        review.setGeneratedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    @SuppressWarnings("unchecked")
    private ResumeReviewResponse toResponse(ResumeReview review) {
        Map<String, SectionScoreDto> sections = new LinkedHashMap<>();
        if (review.getSections() != null) {
            review.getSections().forEach((key, value) -> {
                Map<String, Object> sectionMap = (Map<String, Object>) value;
                BigDecimal score;
                try {
                    Object scoreVal = sectionMap.get("score");
                    score = scoreVal != null ? new BigDecimal(scoreVal.toString()) : BigDecimal.ZERO;
                } catch (NumberFormatException e) {
                    score = BigDecimal.ZERO;
                }
                String feedback = sectionMap.get("feedback") instanceof String ? (String) sectionMap.get("feedback") : "";
                sections.put(key, SectionScoreDto.builder().score(score).feedback(feedback).build());
            });
        }

        return ResumeReviewResponse.builder()
                .id(review.getId())
                .resumeId(review.getResumeId())
                .overallScore(review.getOverallScore())
                .sections(sections)
                .strengths(review.getStrengths())
                .weaknesses(review.getWeaknesses())
                .priorityActions(review.getPriorityActions())
                .generatedAt(review.getGeneratedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private String nvl(String value) {
        return value != null ? value : "N/A";
    }
}
