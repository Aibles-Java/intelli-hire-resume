package org.aibles.intellihireresume.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Resume errors
    RES_001("RES_001", "Resume not found"),
    RES_002("RES_002", "Resume already exists"),
    RES_003("RES_003", "Invalid resume status"),
    RES_004("RES_004", "Resume title already exists"),
    RES_005("RES_005", "Access denied to resume"),

    // Common errors
    COM_001("COM_001", "Validation error"),
    COM_002("COM_002", "Internal server error"),
    COM_003("COM_003", "Bad request");

    private final String code;
    private final String message;
}
