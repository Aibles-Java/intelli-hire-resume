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
    JOB_003("JOB_003", "Job already running for this resume"),

    // File errors
    FILE_001("FILE_001", "File not found"),
    FILE_002("FILE_002", "Invalid file type, only PDF and DOCX are allowed"),
    FILE_003("FILE_003", "File size exceeds 5MB limit"),
    FILE_004("FILE_004", "File already exists for this resume"),

    // Contact errors
    CONTACT_001("CONTACT_001", "Contact not found"),
    CONTACT_002("CONTACT_002", "Invalid contact data"),

    // Experience errors
    EXP_001("EXP_001", "Experience not found"),
    EXP_002("EXP_002", "Invalid date range"),
    EXP_003("EXP_003", "Experience does not belong to resume"),

    // Education errors
    EDU_001("EDU_001", "Education not found"),
    EDU_002("EDU_002", "Invalid year range"),
    EDU_003("EDU_003", "Education does not belong to resume"),

    // Skill catalog errors
    SKILL_001("SKILL_001", "Skill not found"),

    // Resume skill errors
    RESUME_SKILL_001("RESUME_SKILL_001", "Resume skill not found"),
    RESUME_SKILL_002("RESUME_SKILL_002", "Skill already added to resume"),

    // Skill profile errors
    PROFILE_001("PROFILE_001", "Skill profile not found"),

    // AI provider errors
    AI_001("AI_001", "AI provider authentication failed"),
    AI_002("AI_002", "AI provider request failed"),
    AI_003("AI_003", "AI parsing failed after maximum retries");

    private final String code;
    private final String message;
}
