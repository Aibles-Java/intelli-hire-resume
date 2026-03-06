package org.aibles.intellihireresume.entity;

import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    @Test
    void initDefaults_ShouldSetUUID_WhenIdIsNull() {
        Resume resume = new Resume();
        assertThat(resume.getId()).isNull();

        resume.initDefaults();

        assertThat(resume.getId()).isNotNull();
        assertThat(resume.getId()).hasSize(36); // UUID format
    }

    @Test
    void initDefaults_ShouldNotOverrideId_WhenIdIsAlreadySet() {
        Resume resume = Resume.builder()
                .id("existing-id-123")
                .userId("user-1")
                .status(ResumeStatus.UPLOADED)
                .build();

        resume.initDefaults();

        assertThat(resume.getId()).isEqualTo("existing-id-123");
    }

    @Test
    void initDefaults_ShouldSetCreatedByToSystem_WhenCreatedByIsNull() {
        Resume resume = new Resume();
        assertThat(resume.getCreatedBy()).isNull();

        resume.initDefaults();

        assertThat(resume.getCreatedBy()).isEqualTo("system");
    }

    @Test
    void initDefaults_ShouldNotOverrideCreatedBy_WhenCreatedByIsAlreadySet() {
        Resume resume = Resume.builder()
                .id("resume-1")
                .userId("user-1")
                .status(ResumeStatus.UPLOADED)
                .build();
        resume.setCreatedBy("admin");

        resume.initDefaults();

        assertThat(resume.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    void initDefaults_ShouldSetBothIdAndCreatedBy_WhenBothAreNull() {
        ResumeContact contact = new ResumeContact();

        contact.initDefaults();

        assertThat(contact.getId()).isNotNull();
        assertThat(contact.getCreatedBy()).isEqualTo("system");
    }
}
