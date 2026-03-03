package org.aibles.intellihireresume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihireresume.dto.ResumeContactRequest;
import org.aibles.intellihireresume.dto.ResumeContactResponse;
import org.aibles.intellihireresume.exception.ErrorCode;
import org.aibles.intellihireresume.exception.NotFoundException;
import org.aibles.intellihireresume.service.ResumeContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResumeContactController.class)
@ActiveProfiles("test")
class ResumeContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeContactService contactService;

    private String testResumeId;
    private String testUserId;
    private String testContactId;
    private ResumeContactResponse contactResponse;
    private ResumeContactRequest validRequest;

    @BeforeEach
    void setUp() {
        testResumeId = "resume-123";
        testUserId = "user-456";
        testContactId = "contact-789";

        contactResponse = ResumeContactResponse.builder()
                .id(testContactId)
                .resumeId(testResumeId)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .location("Hanoi, Vietnam")
                .linkedinUrl("https://linkedin.com/in/johndoe")
                .otherInfo("Some info")
                .createdAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 22, 10, 0, 0))
                .build();

        validRequest = ResumeContactRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .location("Hanoi, Vietnam")
                .linkedinUrl("https://linkedin.com/in/johndoe")
                .otherInfo("Some info")
                .build();
    }

    // PUT (createOrUpdate) TESTS

    @Test
    void createOrUpdate_ShouldReturn200_WhenValidRequest() throws Exception {
        when(contactService.createOrUpdate(eq(testResumeId), any(ResumeContactRequest.class)))
                .thenReturn(contactResponse);

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testContactId))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.full_name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));

        verify(contactService).createOrUpdate(eq(testResumeId), any(ResumeContactRequest.class));
    }

    @Test
    void createOrUpdate_ShouldReturn200_WhenRequestHasOnlyRequiredFields() throws Exception {
        ResumeContactRequest minimalRequest = ResumeContactRequest.builder()
                .fullName("John Doe")
                .build();

        ResumeContactResponse minimalResponse = ResumeContactResponse.builder()
                .id(testContactId)
                .resumeId(testResumeId)
                .fullName("John Doe")
                .build();

        when(contactService.createOrUpdate(eq(testResumeId), any(ResumeContactRequest.class)))
                .thenReturn(minimalResponse);

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.full_name").value("John Doe"));

        verify(contactService).createOrUpdate(eq(testResumeId), any(ResumeContactRequest.class));
    }

    @Test
    void createOrUpdate_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        ResumeContactRequest invalidRequest = ResumeContactRequest.builder()
                .fullName("John Doe")
                .email("not-a-valid-email")
                .build();

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()));

        verify(contactService, never()).createOrUpdate(any(), any());
    }

    @Test
    void createOrUpdate_ShouldReturn400_WhenPhoneIsInvalid() throws Exception {
        ResumeContactRequest invalidRequest = ResumeContactRequest.builder()
                .fullName("John Doe")
                .phone("not-a-phone")
                .build();

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()));

        verify(contactService, never()).createOrUpdate(any(), any());
    }

    @Test
    void createOrUpdate_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(put("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(contactService, never()).createOrUpdate(any(), any());
    }

    @Test
    void createOrUpdate_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(contactService.createOrUpdate(eq(testResumeId), any(ResumeContactRequest.class)))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(put("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(contactService).createOrUpdate(eq(testResumeId), any(ResumeContactRequest.class));
    }

    // GET TESTS

    @Test
    void getByResumeId_ShouldReturn200_WhenContactExists() throws Exception {
        when(contactService.getByResumeId(testResumeId)).thenReturn(contactResponse);

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testContactId))
                .andExpect(jsonPath("$.data.resume_id").value(testResumeId))
                .andExpect(jsonPath("$.data.full_name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.phone").value("+1234567890"));

        verify(contactService).getByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldReturn400_WhenMissingXUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{resumeId}/contact", testResumeId))
                .andExpect(status().isBadRequest());

        verify(contactService, never()).getByResumeId(any());
    }

    @Test
    void getByResumeId_ShouldReturn404_WhenContactNotFound() throws Exception {
        when(contactService.getByResumeId(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.CONTACT_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.CONTACT_001.getCode()));

        verify(contactService).getByResumeId(testResumeId);
    }

    @Test
    void getByResumeId_ShouldReturn404_WhenResumeNotFound() throws Exception {
        when(contactService.getByResumeId(testResumeId))
                .thenThrow(new NotFoundException(ErrorCode.RES_001));

        mockMvc.perform(get("/api/v1/resumes/{resumeId}/contact", testResumeId)
                        .header("X-User-Id", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.RES_001.getCode()));

        verify(contactService).getByResumeId(testResumeId);
    }
}
