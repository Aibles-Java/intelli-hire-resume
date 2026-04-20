package org.aibles.intellihireresume.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends BaseException {

    public DuplicateException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.CONFLICT);
    }

    public DuplicateException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.CONFLICT);
    }
}