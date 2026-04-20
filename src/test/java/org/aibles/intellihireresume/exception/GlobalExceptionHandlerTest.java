package org.aibles.intellihireresume.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.Valid;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // Direct method call tests

    @Test
    void handleBaseException_ShouldReturn404_WhenNotFoundException() {
        NotFoundException ex = new NotFoundException(ErrorCode.RES_001);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.RES_001.getCode());
    }

    @Test
    void handleBaseException_ShouldReturn400_WhenBadRequestException() {
        BadRequestException ex = new BadRequestException(ErrorCode.EXP_002);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.EXP_002.getCode());
    }

    @Test
    void handleBaseException_ShouldReturn409_WhenDuplicateException() {
        DuplicateException ex = new DuplicateException(ErrorCode.RES_004);

        ResponseEntity<BaseResponse<Void>> response = handler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.RES_004.getCode());
    }

    @Test
    void handleMaxUploadSizeExceededException_ShouldReturn400() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(5 * 1024 * 1024L);

        ResponseEntity<BaseResponse<Void>> response = handler.handleMaxUploadSizeExceededException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.FILE_003.getCode());
    }

    @Test
    void handleException_ShouldReturn500_WhenUnexpectedException() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<BaseResponse<Void>> response = handler.handleException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.COM_002.getCode());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error", new MockHttpInputMessage(new byte[0]));

        ResponseEntity<BaseResponse<Void>> response = handler.handleHttpMessageNotReadableException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleConstraintViolationException_ShouldReturn400() {
        ConstraintViolationException ex = new ConstraintViolationException(
                "Constraint violation", new HashSet<>());

        ResponseEntity<BaseResponse<Void>> response = handler.handleConstraintViolationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.COM_001.getCode());
    }

    // MockMvc test controllers

    @Data
    static class TestRequest {
        @Size(max = 5, message = "name too long")
        private String name;
    }

    @RestController
    static class TestValidationController {
        @PostMapping("/test/validate")
        public String validate(@Valid @RequestBody TestRequest request) {
            return "ok";
        }

        @GetMapping("/test/header")
        public String withHeader(@RequestHeader("X-Required-Header") String header) {
            return "ok";
        }
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturn400WithFieldErrors() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestValidationController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String body = "{\"name\": \"toolongvalue\"}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_001.getCode()))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void handleMissingRequestHeaderException_ShouldReturn400() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestValidationController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/test/header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error_code").value(ErrorCode.COM_004.getCode()));
    }
}
