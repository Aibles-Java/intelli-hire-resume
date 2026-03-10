package org.aibles.intellihireresume.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeEducationRequest;
import org.aibles.intellihireresume.dto.ResumeExperienceRequest;
import org.aibles.intellihireresume.dto.ResumeSkillRequest;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.repository.ResumeFileRepository;
import org.aibles.intellihireresume.repository.ResumeParseJobRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.aibles.intellihireresume.repository.ResumeSkillRepository;
import org.aibles.intellihireresume.repository.SkillRepository;
import org.aibles.intellihireresume.service.FileStorageService;
import org.aibles.intellihireresume.service.ResumeContactService;
import org.aibles.intellihireresume.service.ResumeEducationService;
import org.aibles.intellihireresume.service.ResumeExperienceService;
import org.aibles.intellihireresume.service.ResumeSkillProfileService;
import org.aibles.intellihireresume.service.ResumeSkillService;
import org.aibles.intellihireresume.service.ResumeWorkerService;
import org.aibles.intellihireresume.service.TextExtractionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeWorkerServiceImpl implements ResumeWorkerService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("[+]?[\\d][\\d\\s\\-]{6,13}[\\d]");
    private static final Pattern LINKEDIN_PATTERN =
            Pattern.compile("(https?://)?(www\\.)?linkedin\\.com/[\\w/\\-]+");
    private static final Pattern YEAR_PATTERN =
            Pattern.compile("\\b(19|20)\\d{2}\\b");

    private static final List<String> EXPERIENCE_HEADERS =
            List.of("EXPERIENCE", "WORK EXPERIENCE", "WORK HISTORY", "EMPLOYMENT HISTORY", "PROFESSIONAL EXPERIENCE");
    private static final List<String> EDUCATION_HEADERS =
            List.of("EDUCATION", "ACADEMIC BACKGROUND", "QUALIFICATIONS", "ACADEMIC QUALIFICATIONS");
    private static final List<String> SECTION_DELIMITERS =
            List.of("EDUCATION", "EXPERIENCE", "WORK", "SKILLS", "PROJECTS", "CERTIFICATIONS",
                    "AWARDS", "PUBLICATIONS", "LANGUAGES", "INTERESTS", "REFERENCES", "SUMMARY", "OBJECTIVE");

    private final ResumeRepository resumeRepository;
    private final ResumeParseJobRepository resumeParseJobRepository;
    private final ResumeFileRepository resumeFileRepository;
    private final SkillRepository skillRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final FileStorageService fileStorageService;
    private final TextExtractionService textExtractionService;
    private final ResumeContactService resumeContactService;
    private final ResumeExperienceService resumeExperienceService;
    private final ResumeEducationService resumeEducationService;
    private final ResumeSkillService resumeSkillService;
    private final ResumeSkillProfileService resumeSkillProfileService;

    @Async("workerPool")
    @Override
    public void processJob(String resumeId) {
        log.info("Worker started for resumeId={}", resumeId);

        ResumeParseJob job = resumeParseJobRepository.findByResumeId(resumeId).orElse(null);
        if (job == null) {
            log.warn("No parse job found for resumeId={}, skipping", resumeId);
            return;
        }

        Resume resume = resumeRepository.findByIdActive(resumeId).orElse(null);
        if (resume == null) {
            log.warn("Resume not found for ID={}, marking job as failed", resumeId);
            failJob(job, "Resume not found");
            return;
        }

        // Mark job as RUNNING
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        resumeParseJobRepository.save(job);

        // Mark resume as PARSING
        resume.setStatus(ResumeStatus.PARSING);
        resumeRepository.save(resume);

        try {
            // Step 1: Get file info
            ResumeFile file = resumeFileRepository.findByResumeId(resumeId)
                    .orElseThrow(() -> new RuntimeException("No file found for resumeId=" + resumeId));

            // Step 2: Download file bytes from MinIO
            log.info("Downloading file from MinIO: bucket={}, key={}", file.getObjectBucket(), file.getObjectKey());
            byte[] fileBytes;
            try (InputStream inputStream = fileStorageService.download(file.getObjectBucket(), file.getObjectKey())) {
                fileBytes = inputStream.readAllBytes();
            }

            // Step 3: Extract raw text
            String rawText;
            if (file.getFileType() == FileType.PDF) {
                rawText = textExtractionService.extractFromPdf(fileBytes);
            } else {
                rawText = textExtractionService.extractFromDocx(fileBytes);
            }

            // Step 4: Save raw text to resume
            resume.setRawText(rawText);
            resumeRepository.save(resume);
            job.setProgress(30);
            resumeParseJobRepository.save(job);

            // Step 5: Parse contact info
            try {
                parseAndSaveContact(resumeId, rawText);
            } catch (Exception e) {
                log.warn("Contact parsing failed for resumeId={}: {}", resumeId, e.getMessage());
            }

            job.setProgress(50);
            resumeParseJobRepository.save(job);

            // Step 6: Parse experiences
            try {
                parseAndSaveExperiences(resumeId, rawText);
            } catch (Exception e) {
                log.warn("Experience parsing failed for resumeId={}: {}", resumeId, e.getMessage());
            }

            // Step 7: Parse educations
            try {
                parseAndSaveEducations(resumeId, rawText);
            } catch (Exception e) {
                log.warn("Education parsing failed for resumeId={}: {}", resumeId, e.getMessage());
            }

            job.setProgress(70);
            resumeParseJobRepository.save(job);

            // Step 8: Extract and save skills
            try {
                extractAndSaveSkills(resumeId, rawText);
            } catch (Exception e) {
                log.warn("Skill extraction failed for resumeId={}: {}", resumeId, e.getMessage());
            }

            job.setProgress(90);
            resumeParseJobRepository.save(job);

            // Step 9: Generate skill profile
            try {
                resumeSkillProfileService.generate(resumeId);
            } catch (Exception e) {
                log.warn("Skill profile generation failed for resumeId={}: {}", resumeId, e.getMessage());
            }

            // Step 10: Mark as completed
            job.setStatus(JobStatus.SUCCEEDED);
            job.setFinishedAt(LocalDateTime.now());
            job.setProgress(100);
            resumeParseJobRepository.save(job);

            resume.setStatus(ResumeStatus.COMPLETED);
            resumeRepository.save(resume);

            log.info("Worker completed successfully for resumeId={}", resumeId);

        } catch (Exception e) {
            log.error("Worker failed for resumeId={}: {}", resumeId, e.getMessage(), e);
            failJob(job, e.getMessage());
            resume.setStatus(ResumeStatus.FAILED);
            resumeRepository.save(resume);
        }
    }

    private void failJob(ResumeParseJob job, String errorMessage) {
        job.setStatus(JobStatus.FAILED);
        job.setFinishedAt(LocalDateTime.now());
        job.setErrorMessage(errorMessage != null ? truncate(errorMessage, 1000) : "Unknown error");
        job.setRetryCount(job.getRetryCount() + 1);
        resumeParseJobRepository.save(job);
    }

    // ---- Contact Parsing ----

    private void parseAndSaveContact(String resumeId, String rawText) {
        String email = extractFirst(EMAIL_PATTERN, rawText);
        String phone = extractFirst(PHONE_PATTERN, rawText);
        String linkedinUrl = extractFirst(LINKEDIN_PATTERN, rawText);
        String fullName = extractFullName(rawText);

        if (email == null && phone == null && linkedinUrl == null && fullName == null) {
            log.debug("No contact info found for resumeId={}", resumeId);
            return;
        }

        ResumeContactRequest request = ResumeContactRequest.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone != null ? normalizePhone(phone) : null)
                .linkedinUrl(linkedinUrl)
                .build();

        resumeContactService.createOrUpdate(resumeId, request);
        log.info("Contact saved for resumeId={}", resumeId);
    }

    private String extractFullName(String rawText) {
        String[] lines = rawText.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            // Skip lines that look like section headers or contact info
            if (line.length() > 60) continue;
            if (line.contains("@")) continue;
            if (line.matches(".*\\d{4}.*")) continue;
            if (line.toUpperCase().equals(line) && line.length() > 3) continue; // all-caps header
            return truncate(line, 255);
        }
        return null;
    }

    private String normalizePhone(String phone) {
        // Keep only digits, spaces, dashes, plus
        String normalized = phone.replaceAll("[^+\\d\\s\\-]", "").trim();
        return normalized.length() <= 50 ? normalized : normalized.substring(0, 50);
    }

    // ---- Experience Parsing ----

    private void parseAndSaveExperiences(String resumeId, String rawText) {
        String section = extractSection(rawText, EXPERIENCE_HEADERS);
        if (section == null || section.isBlank()) return;

        List<ExperienceEntry> entries = parseExperienceEntries(section);
        int saved = 0;
        for (ExperienceEntry entry : entries) {
            if (saved >= 5) break; // limit max entries per parse run
            try {
                ResumeExperienceRequest request = ResumeExperienceRequest.builder()
                        .company(entry.company)
                        .title(entry.title)
                        .startDate(entry.startDate)
                        .endDate(entry.endDate)
                        .isCurrent(entry.endDate == null && entry.startDate != null)
                        .build();
                resumeExperienceService.create(resumeId, request);
                saved++;
            } catch (Exception e) {
                log.debug("Skipping experience entry: {}", e.getMessage());
            }
        }
        log.info("Saved {} experience entries for resumeId={}", saved, resumeId);
    }

    private List<ExperienceEntry> parseExperienceEntries(String section) {
        List<ExperienceEntry> entries = new ArrayList<>();
        String[] lines = section.split("\\n");

        String company = null;
        String title = null;
        LocalDate startDate = null;
        LocalDate endDate = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (company != null && title != null) {
                    entries.add(new ExperienceEntry(company, title, startDate, endDate));
                    company = null;
                    title = null;
                    startDate = null;
                    endDate = null;
                }
                continue;
            }

            // Try to extract year info
            Matcher yearMatcher = YEAR_PATTERN.matcher(line);
            if (yearMatcher.find()) {
                int year = Integer.parseInt(yearMatcher.group());
                if (startDate == null) {
                    startDate = LocalDate.of(year, 1, 1);
                } else if (endDate == null) {
                    endDate = LocalDate.of(year, 12, 31);
                }
                // Line with year is likely a date line, not company/title
                if (line.toLowerCase().contains("present") || line.toLowerCase().contains("current")) {
                    endDate = null; // isCurrent
                }
                continue;
            }

            if (company == null) {
                company = truncate(line, 255);
            } else if (title == null) {
                title = truncate(line, 255);
            }
        }

        // Save last entry
        if (company != null && title != null) {
            entries.add(new ExperienceEntry(company, title, startDate, endDate));
        }

        return entries;
    }

    private static class ExperienceEntry {
        final String company;
        final String title;
        final LocalDate startDate;
        final LocalDate endDate;

        ExperienceEntry(String company, String title, LocalDate startDate, LocalDate endDate) {
            this.company = company;
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    // ---- Education Parsing ----

    private void parseAndSaveEducations(String resumeId, String rawText) {
        String section = extractSection(rawText, EDUCATION_HEADERS);
        if (section == null || section.isBlank()) return;

        List<EducationEntry> entries = parseEducationEntries(section);
        int saved = 0;
        for (EducationEntry entry : entries) {
            if (saved >= 5) break;
            try {
                ResumeEducationRequest request = ResumeEducationRequest.builder()
                        .school(entry.school)
                        .degree(entry.degree)
                        .startYear(entry.startYear)
                        .endYear(entry.endYear)
                        .build();
                resumeEducationService.create(resumeId, request);
                saved++;
            } catch (Exception e) {
                log.debug("Skipping education entry: {}", e.getMessage());
            }
        }
        log.info("Saved {} education entries for resumeId={}", saved, resumeId);
    }

    private List<EducationEntry> parseEducationEntries(String section) {
        List<EducationEntry> entries = new ArrayList<>();
        String[] lines = section.split("\\n");

        String school = null;
        String degree = null;
        Integer startYear = null;
        Integer endYear = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                if (school != null) {
                    entries.add(new EducationEntry(school, degree, startYear, endYear));
                    school = null;
                    degree = null;
                    startYear = null;
                    endYear = null;
                }
                continue;
            }

            // Extract years
            Matcher yearMatcher = YEAR_PATTERN.matcher(line);
            List<Integer> yearsFound = new ArrayList<>();
            while (yearMatcher.find()) {
                yearsFound.add(Integer.parseInt(yearMatcher.group()));
            }

            if (!yearsFound.isEmpty()) {
                int currentYear = LocalDate.now().getYear();
                for (int year : yearsFound) {
                    if (year >= 1900 && year <= currentYear) {
                        if (startYear == null) {
                            startYear = year;
                        } else if (endYear == null && year > startYear) {
                            endYear = year;
                        }
                    }
                }
                continue;
            }

            // Detect degree keywords
            String lineLower = line.toLowerCase();
            if (lineLower.contains("bachelor") || lineLower.contains("master") || lineLower.contains("phd")
                    || lineLower.contains("doctor") || lineLower.contains("diploma")
                    || lineLower.contains("degree") || lineLower.contains("b.sc") || lineLower.contains("m.sc")) {
                if (degree == null) {
                    degree = truncate(line, 255);
                }
                continue;
            }

            if (school == null) {
                school = truncate(line, 255);
            } else if (degree == null) {
                degree = truncate(line, 255);
            }
        }

        if (school != null) {
            entries.add(new EducationEntry(school, degree, startYear, endYear));
        }

        return entries;
    }

    private static class EducationEntry {
        final String school;
        final String degree;
        final Integer startYear;
        final Integer endYear;

        EducationEntry(String school, String degree, Integer startYear, Integer endYear) {
            this.school = school;
            this.degree = degree;
            this.startYear = startYear;
            this.endYear = endYear;
        }
    }

    // ---- Skill Extraction ----

    private void extractAndSaveSkills(String resumeId, String rawText) {
        List<Skill> catalogSkills = skillRepository.findByIsActiveTrue();
        String lowerText = rawText.toLowerCase();

        int matched = 0;
        for (Skill skill : catalogSkills) {
            boolean found = containsSkill(lowerText, skill.getName());

            if (!found && skill.getAliases() != null) {
                for (String alias : skill.getAliases()) {
                    if (containsSkill(lowerText, alias)) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                // Skip if already exists (reprocess case)
                if (resumeSkillRepository.existsByResumeIdAndSkillId(resumeId, skill.getId())) {
                    continue;
                }
                try {
                    ResumeSkillRequest request = ResumeSkillRequest.builder()
                            .skillId(skill.getId())
                            .confidenceScore(new BigDecimal("0.70"))
                            .isPrimary(false)
                            .build();
                    resumeSkillService.create(resumeId, request);
                    matched++;
                } catch (Exception e) {
                    log.debug("Skipping skill {}: {}", skill.getName(), e.getMessage());
                }
            }
        }
        log.info("Extracted {} skills for resumeId={}", matched, resumeId);
    }

    private boolean containsSkill(String lowerText, String skillName) {
        if (skillName == null || skillName.isBlank()) return false;
        // Use word-boundary matching to avoid false positives (e.g. "go" matching "google")
        String escaped = Pattern.quote(skillName.toLowerCase());
        Pattern p = Pattern.compile("(?<![a-zA-Z0-9])" + escaped + "(?![a-zA-Z0-9])");
        return p.matcher(lowerText).find();
    }

    // ---- Utility Methods ----

    private String extractSection(String text, List<String> headers) {
        String upperText = text.toUpperCase();
        for (String header : headers) {
            int idx = upperText.indexOf(header);
            if (idx < 0) continue;

            int sectionStart = idx + header.length();
            int sectionEnd = text.length();

            // Find where the next major section starts
            for (String delimiter : SECTION_DELIMITERS) {
                if (delimiter.equals(header)) continue;
                int nextIdx = upperText.indexOf(delimiter, sectionStart + 1);
                if (nextIdx > sectionStart && nextIdx < sectionEnd) {
                    sectionEnd = nextIdx;
                }
            }

            return text.substring(sectionStart, sectionEnd).trim();
        }
        return null;
    }

    private String extractFirst(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
