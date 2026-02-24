package org.aibles.intellihireresume.exception;

import org.springframework.http.HttpStatus;

public class InactiveResourceException extends BaseException {

    public InactiveResourceException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.BAD_REQUEST);
    }

    public InactiveResourceException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.BAD_REQUEST);
    }
}