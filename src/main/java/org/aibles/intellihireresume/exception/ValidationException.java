package org.aibles.intellihireresume.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}