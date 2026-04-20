package org.aibles.intellihireresume.service.impl;

import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.entity.Resume;
import org.aibles.intellihireresume.entity.ResumeContact;
import org.aibles.intellihireresume.entity.enums.ResumeStatus;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.mapper.ResumeContactMapper;
import org.aibles.intellihireresume.repository.ResumeContactRepository;
import org.aibles.intellihireresume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeContactServiceImplTest {

    @Mock
    private ResumeContactRepository contactRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeContactMapper contactMapper = new ResumeContactMapper();

    private ResumeContactServiceImpl contactService;

    private String testResumeId;
    private String testUserId;
    private String testContactId;
    private Resume testResume;
    private ResumeContact testContact;
    private ResumeContactRequest testRequest;

    @BeforeEach
    void setUp() {
        contactService = new ResumeContactServiceImpl(contactRepository, resumeRepository, contactMapper);

        testResumeId = "resume-123";
        testUserId = "user-456";
        testContactId = "contact-789";

        testResume = Resume.builder()
                .id(testResumeId)
                .userId(testUserId)
                .status(ResumeStatus.UPLOADED)
                .isActive(true)
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        testContact = new ResumeContact();
        testContact.setId(testContactId);
        testContact.setResumeId(testResumeId);
        testContact.setFullName("John Doe");
        testContact.setEmail("john@example.com");
        testContact.setPhone("+1234567890");
        testContact.setLocation("Hanoi, Vietnam");
        testContact.setLinkedinUrl("https://linkedin.com/in/johndoe");
        testContact.setOtherInfo("Some info");
        testContact.setCreatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));
        testContact.setUpdatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0));

        testRequest = ResumeContactRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .location("Hanoi, Vietnam")
                .linkedinUrl("https://linkedin.com/in/johndoe")
                .otherInfo("Some info")
                .build();
    }

    // CREATE OR UPDATE TESTS

    @Test
    void createOrUpdate_ShouldCreateNewContact_WhenNoExistingContact() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(contactRepository.save(any(ResumeContact.class))).thenReturn(testContact);

        // When
        ResumeContactResponse result = contactService.createOrUpdate(testResumeId, testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testContactId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(contactRepository).findByResumeId(testResumeId);
        verify(contactRepository).save(any(ResumeContact.class));
    }

    @Test
    void createOrUpdate_ShouldUpdateExistingContact_WhenContactExists() {
        // Given
        ResumeContactRequest updateRequest = ResumeContactRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .phone("+9876543210")
                .location("Ho Chi Minh City, Vietnam")
                .linkedinUrl("https://linkedin.com/in/janedoe")
                .otherInfo("Updated info")
                .build();

        ResumeContact updatedContact = new ResumeContact();
        updatedContact.setId(testContactId);
        updatedContact.setResumeId(testResumeId);
        updatedContact.setFullName("Jane Doe");
        updatedContact.setEmail("jane@example.com");

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(ResumeContact.class))).thenReturn(updatedContact);

        // When
        ResumeContactResponse result = contactService.createOrUpdate(testResumeId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testContactId);
        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(contactRepository).findByResumeId(testResumeId);
        verify(contactRepository).save(testContact);
    }

    @Test
    void createOrUpdate_ShouldThrowNotFoundException_WhenResumeNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contactService.createOrUpdate(testResumeId, testRequest))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(contactRepository, never()).findByResumeId(any());
        verify(contactRepository, never()).save(any());
    }

    @Test
    void createOrUpdate_ShouldCreateContactWithNullFields_WhenRequestHasNullFields() {
        // Given
        ResumeContactRequest minimalRequest = ResumeContactRequest.builder()
                .fullName("John Doe")
                .build();

        ResumeContact savedContact = new ResumeContact();
        savedContact.setId(testContactId);
        savedContact.setResumeId(testResumeId);
        savedContact.setFullName("John Doe");

        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());
        when(contactRepository.save(any(ResumeContact.class))).thenReturn(savedContact);

        // When
        ResumeContactResponse result = contactService.createOrUpdate(testResumeId, minimalRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isNull();

        verify(contactRepository).save(any(ResumeContact.class));
    }

    // GET BY RESUME ID TESTS

    @Test
    void getByResumeId_ShouldReturnContactResponse_WhenContactExists() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.of(testContact));

        // When
        ResumeContactResponse result = contactService.getByResumeId(testResumeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testContactId);
        assertThat(result.getResumeId()).isEqualTo(testResumeId);
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        assertThat(result.getLocation()).isEqualTo("Hanoi, Vietnam");
        assertThat(result.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/johndoe");

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(contactRepository).findByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldThrowNotFoundException_WhenContactNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.of(testResume));
        when(contactRepository.findByResumeId(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contactService.getByResumeId(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTACT_001.getCode());

        verify(resumeRepository).findByIdActive(testResumeId);
        verify(contactRepository).findByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldThrowNotFoundException_WhenResumeNotFound() {
        // Given
        when(resumeRepository.findByIdActive(testResumeId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contactService.getByResumeId(testResumeId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_001.getCode());

        verify(contactRepository, never()).findByResumeId(any());
    }
}
