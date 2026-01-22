package org.aibles.intellihireresume.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ;
    private final String code;
    private final String message;
}
