package org.aibles.intellihireresume.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.NOT_FOUND);
    }
}