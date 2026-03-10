package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeSkillProfileResponse;
import org.aibles.intellihireresume.dto.ParseJobResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeFile;
import org.aibles.intellihireresume.entity.ResumeParseJob;
import org.aibles.intellihireresume.entity.Skill;
import org.aibles.intellihireresume.entity.enums.FileType;
import org.aibles.intellihireresume.entity.enums.JobStatus;
import org.aibles.intellihireresume.entity.enums.JobType;
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
import org.aibles.intellihireresume.service.TextExtractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeWorkerServiceImplTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private ResumeParseJobRepository resumeParseJobRepository;
    @Mock private ResumeFileRepository resumeFileRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private ResumeSkillRepository resumeSkillRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private TextExtractionService textExtractionService;
    @Mock private ResumeContactService resumeContactService;
    @Mock private ResumeExperienceService resumeExperienceService;
    @Mock private ResumeEducationService resumeEducationService;
    @Mock private ResumeSkillService resumeSkillService;
    @Mock private ResumeSkillProfileService resumeSkillProfileService;

    private ResumeWorkerServiceImpl workerService;

    private static final String RESUME_ID = "resume-001";
    private static final String RAW_TEXT = "John Doe\njohn.doe@example.com\n+84 123 456 789\n"
            + "EXPERIENCE\nGoogle\nSoftware Engineer\n2020 - 2023\n\n"
            + "EDUCATION\nHanoi University of Technology\nBachelor of Computer Science\n2016 2020\n\n"
            + "SKILLS\nJava Spring Boot Python Docker";

    private Resume testResume;
    private ResumeParseJob testJob;
    private ResumeFile testFile;

    @BeforeEach
    void setUp() {
        workerService = new ResumeWorkerServiceImpl(
                resumeRepository, resumeParseJobRepository, resumeFileRepository,
                skillRepository, resumeSkillRepository, fileStorageService,
                textExtractionService, resumeContactService, resumeExperienceService,
                resumeEducationService, resumeSkillService, resumeSkillProfileService,
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry()
        );

        testResume = new Resume();
        testResume.setId(RESUME_ID);
        testResume.setUserId("user-001");
        testResume.setStatus(ResumeStatus.UPLOADED);
        testResume.setIsActive(true);

        testJob = new ResumeParseJob();
        testJob.setId("job-001");
        testJob.setResumeId(RESUME_ID);
        testJob.setStatus(JobStatus.QUEUED);
        testJob.setJobType(JobType.PARSE);
        testJob.setProgress(0);
        testJob.setRetryCount(0);

        testFile = new ResumeFile();
        testFile.setId("file-001");
        testFile.setResumeId(RESUME_ID);
        testFile.setFileType(FileType.PDF);
        testFile.setObjectBucket("resume-bucket");
        testFile.setObjectKey("resumes/resume-001.pdf");
    }

    @Test
    void processJob_ShouldCompleteSuccessfully_WhenPdfFile() throws Exception {
        byte[] pdfBytes = "fake pdf content".getBytes();

        when(resumeParseJobRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testJob));
        when(resumeRepository.findByIdActive(RESUME_ID)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testFile));
        when(fileStorageService.download(anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream(pdfBytes));
        when(textExtractionService.extractFromPdf(any())).thenReturn(RAW_TEXT);
        when(resumeRepository.save(any())).thenReturn(testResume);
        when(resumeParseJobRepository.save(any())).thenReturn(testJob);
        when(skillRepository.findByIsActiveTrue()).thenReturn(List.of());
        when(resumeSkillProfileService.generate(RESUME_ID)).thenReturn(new ResumeSkillProfileResponse());

        workerService.processJob(RESUME_ID);

        // Verify RUNNING state set
        ArgumentCaptor<ResumeParseJob> jobCaptor = ArgumentCaptor.forClass(ResumeParseJob.class);
        verify(resumeParseJobRepository, atLeast(2)).save(jobCaptor.capture());

        // Verify SUCCEEDED at end
        List<ResumeParseJob> savedJobs = jobCaptor.getAllValues();
        assertThat(savedJobs).anyMatch(j -> j.getStatus() == JobStatus.SUCCEEDED);

        // Verify resume set to COMPLETED
        ArgumentCaptor<Resume> resumeCaptor = ArgumentCaptor.forClass(Resume.class);
        verify(resumeRepository, atLeast(2)).save(resumeCaptor.capture());
        assertThat(resumeCaptor.getAllValues()).anyMatch(r -> r.getStatus() == ResumeStatus.COMPLETED);
    }

    @Test
    void processJob_ShouldCallExtractFromDocx_WhenDocxFile() throws Exception {
        testFile.setFileType(FileType.DOCX);
        byte[] docxBytes = "fake docx content".getBytes();

        when(resumeParseJobRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testJob));
        when(resumeRepository.findByIdActive(RESUME_ID)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testFile));
        when(fileStorageService.download(anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream(docxBytes));
        when(textExtractionService.extractFromDocx(any())).thenReturn(RAW_TEXT);
        when(resumeRepository.save(any())).thenReturn(testResume);
        when(resumeParseJobRepository.save(any())).thenReturn(testJob);
        when(skillRepository.findByIsActiveTrue()).thenReturn(List.of());
        when(resumeSkillProfileService.generate(RESUME_ID)).thenReturn(new ResumeSkillProfileResponse());

        workerService.processJob(RESUME_ID);

        verify(textExtractionService).extractFromDocx(any());
        verify(textExtractionService, never()).extractFromPdf(any());
    }

    @Test
    void processJob_ShouldSetJobFailed_WhenExtractionThrows() throws Exception {
        byte[] pdfBytes = "bad content".getBytes();

        when(resumeParseJobRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testJob));
        when(resumeRepository.findByIdActive(RESUME_ID)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testFile));
        when(fileStorageService.download(anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream(pdfBytes));
        when(textExtractionService.extractFromPdf(any()))
                .thenThrow(new RuntimeException("Corrupt PDF"));
        when(resumeRepository.save(any())).thenReturn(testResume);
        when(resumeParseJobRepository.save(any())).thenReturn(testJob);

        workerService.processJob(RESUME_ID);

        ArgumentCaptor<ResumeParseJob> jobCaptor = ArgumentCaptor.forClass(ResumeParseJob.class);
        verify(resumeParseJobRepository, atLeast(2)).save(jobCaptor.capture());

        List<ResumeParseJob> savedJobs = jobCaptor.getAllValues();
        assertThat(savedJobs).anyMatch(j -> j.getStatus() == JobStatus.FAILED);
        assertThat(savedJobs).anyMatch(j -> j.getStatus() == JobStatus.FAILED
                && j.getErrorMessage() != null
                && j.getRetryCount() > 0);

        ArgumentCaptor<Resume> resumeCaptor = ArgumentCaptor.forClass(Resume.class);
        verify(resumeRepository, atLeast(2)).save(resumeCaptor.capture());
        assertThat(resumeCaptor.getAllValues()).anyMatch(r -> r.getStatus() == ResumeStatus.FAILED);
    }

    @Test
    void processJob_ShouldSkip_WhenNoParseJobFound() {
        when(resumeParseJobRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.empty());

        workerService.processJob(RESUME_ID);

        verify(resumeRepository, never()).findByIdActive(any());
        verify(fileStorageService, never()).download(any(), any());
    }

    @Test
    void processJob_ShouldFailJob_WhenNoResumeFileFound() {
        when(resumeParseJobRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testJob));
        when(resumeRepository.findByIdActive(RESUME_ID)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.empty());
        when(resumeRepository.save(any())).thenReturn(testResume);
        when(resumeParseJobRepository.save(any())).thenReturn(testJob);

        workerService.processJob(RESUME_ID);

        ArgumentCaptor<ResumeParseJob> jobCaptor = ArgumentCaptor.forClass(ResumeParseJob.class);
        verify(resumeParseJobRepository, atLeast(1)).save(jobCaptor.capture());
        assertThat(jobCaptor.getAllValues()).anyMatch(j -> j.getStatus() == JobStatus.FAILED);
    }

    @Test
    void processJob_ShouldMatchSkills_WhenSkillsFoundInText() throws Exception {
        byte[] pdfBytes = "java content".getBytes();
        String textWithSkills = "Java Spring Boot developer with Python experience";

        Skill javaSkill = new Skill();
        javaSkill.setId("skill-java");
        javaSkill.setName("Java");
        javaSkill.setIsActive(true);

        when(resumeParseJobRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testJob));
        when(resumeRepository.findByIdActive(RESUME_ID)).thenReturn(Optional.of(testResume));
        when(resumeFileRepository.findByResumeId(RESUME_ID)).thenReturn(Optional.of(testFile));
        when(fileStorageService.download(anyString(), anyString()))
                .thenReturn(new ByteArrayInputStream(pdfBytes));
        when(textExtractionService.extractFromPdf(any())).thenReturn(textWithSkills);
        when(resumeRepository.save(any())).thenReturn(testResume);
        when(resumeParseJobRepository.save(any())).thenReturn(testJob);
        when(skillRepository.findByIsActiveTrue()).thenReturn(List.of(javaSkill));
        when(resumeSkillRepository.existsByResumeIdAndSkillId(RESUME_ID, "skill-java")).thenReturn(false);
        when(resumeSkillProfileService.generate(RESUME_ID)).thenReturn(new ResumeSkillProfileResponse());

        workerService.processJob(RESUME_ID);

        verify(resumeSkillService).create(eq(RESUME_ID), argThat(req -> "skill-java".equals(req.getSkillId())));
    }
}
