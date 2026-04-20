package org.aibles.intellihireresume.mapper;

import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.entity.ResumeContact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeContactMapperTest {

    private ResumeContactMapper contactMapper;

    @BeforeEach
    void setUp() {
        contactMapper = new ResumeContactMapper();
    }

    // toEntity tests

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        ResumeContact result = contactMapper.toEntity("resume-1", null);
        assertThat(result).isNull();
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        ResumeContactRequest request = ResumeContactRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .location("Hanoi, Vietnam")
                .linkedinUrl("https://linkedin.com/in/johndoe")
                .otherInfo("Some info")
                .build();

        ResumeContact result = contactMapper.toEntity("resume-1", request);

        assertThat(result).isNotNull();
        assertThat(result.getResumeId()).isEqualTo("resume-1");
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        assertThat(result.getLocation()).isEqualTo("Hanoi, Vietnam");
        assertThat(result.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/johndoe");
        assertThat(result.getOtherInfo()).isEqualTo("Some info");
    }

    @Test
    void toEntity_ShouldMapNullFields_WhenRequestHasNullFields() {
        ResumeContactRequest request = ResumeContactRequest.builder()
                .fullName("John Doe")
                .build();

        ResumeContact result = contactMapper.toEntity("resume-1", request);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getLocation()).isNull();
        assertThat(result.getLinkedinUrl()).isNull();
        assertThat(result.getOtherInfo()).isNull();
    }

    // updateEntity tests

    @Test
    void updateEntity_ShouldUpdateAllFields() {
        ResumeContact contact = new ResumeContact();
        contact.setFullName("Old Name");
        contact.setEmail("old@example.com");

        ResumeContactRequest request = ResumeContactRequest.builder()
                .fullName("New Name")
                .email("new@example.com")
                .phone("+9999999")
                .location("HCMC")
                .linkedinUrl("https://linkedin.com/in/new")
                .otherInfo("Updated info")
                .build();

        contactMapper.updateEntity(contact, request);

        assertThat(contact.getFullName()).isEqualTo("New Name");
        assertThat(contact.getEmail()).isEqualTo("new@example.com");
        assertThat(contact.getPhone()).isEqualTo("+9999999");
        assertThat(contact.getLocation()).isEqualTo("HCMC");
        assertThat(contact.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/new");
        assertThat(contact.getOtherInfo()).isEqualTo("Updated info");
    }

    // toResponse tests

    @Test
    void toResponse_ShouldReturnNull_WhenEntityIsNull() {
        ResumeContactResponse result = contactMapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponse_ShouldMapAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 22, 10, 0, 0);
        ResumeContact contact = new ResumeContact();
        contact.setId("contact-1");
        contact.setResumeId("resume-1");
        contact.setFullName("John Doe");
        contact.setEmail("john@example.com");
        contact.setPhone("+1234567890");
        contact.setLocation("Hanoi, Vietnam");
        contact.setLinkedinUrl("https://linkedin.com/in/johndoe");
        contact.setOtherInfo("Some info");
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);

        ResumeContactResponse result = contactMapper.toResponse(contact);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("contact-1");
        assertThat(result.getResumeId()).isEqualTo("resume-1");
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        assertThat(result.getLocation()).isEqualTo("Hanoi, Vietnam");
        assertThat(result.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/johndoe");
        assertThat(result.getOtherInfo()).isEqualTo("Some info");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }
}
