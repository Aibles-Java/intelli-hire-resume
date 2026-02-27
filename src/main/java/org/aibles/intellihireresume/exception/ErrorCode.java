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
    COM_003("COM_003", "Bad request"),
    COM_004("COM_004", "Missing required header"),

    // Parse job errors
    JOB_001("JOB_001", "Parse job not found"),
    JOB_002("JOB_002", "Cannot cancel job in current status"),
    JOB_003("JOB_003", "Job already running for this resume");

    private final String code;
    private final String message;
}
