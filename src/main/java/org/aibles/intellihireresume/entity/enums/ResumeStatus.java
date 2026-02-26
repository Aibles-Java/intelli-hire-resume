package org.aibles.intellihireresume.entity.enums;

public enum ResumeStatus {
    UPLOADED("Uploaded"),
    PARSING("Parsing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    DELETED("Deleted");

    private final String displayName;

    ResumeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isParsing() {
        return this == PARSING;
    }

    public boolean canRetry() {
        return this == FAILED;
    }
}
