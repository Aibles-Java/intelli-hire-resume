package org.aibles.intellihireresume.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.dto.BaseResponse;
import org.aibles.intellihireresume.dto.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException ex) {
        log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        
        List<FieldError> errors = List.of(FieldError.builder()
                .field("general")
                .message(ex.getMessage())
                .rejectedValue(null)
                .build());
                
        return ResponseEntity
                .status(ex.getStatus())
                .body(BaseResponse.error(ex.getErrorCode(), errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());

        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_001.getCode(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("ConstraintViolationException: {}", ex.getMessage());

        List<FieldError> errors = ex.getConstraintViolations().stream()
                .map(violation -> FieldError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_001.getCode(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_003.getCode(), null));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex) {
        log.warn("MissingRequestHeaderException: {}", ex.getMessage());

        List<FieldError> errors = List.of(FieldError.builder()
                .field(ex.getHeaderName())
                .message("Missing required header: " + ex.getHeaderName())
                .rejectedValue(null)
                .build());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_004.getCode(), errors));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        log.warn("MaxUploadSizeExceededException: {}", ex.getMessage());
        List<FieldError> errors = List.of(FieldError.builder()
                .field("file")
                .message(ErrorCode.FILE_003.getMessage())
                .rejectedValue(null)
                .build());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.FILE_003.getCode(), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(ErrorCode.COM_002.getCode(), null));
    }
}