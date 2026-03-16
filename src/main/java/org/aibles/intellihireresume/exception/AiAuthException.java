package org.aibles.intellihireresume.exception;

import org.springframework.http.HttpStatus;

public class AiAuthException extends BaseException {
    public AiAuthException() {
        super(ErrorCode.AI_001, HttpStatus.UNAUTHORIZED);
    }
}
