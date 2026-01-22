package org.aibles.intellihireresume.entity.enums;

public enum ResumeStatus {
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed");
    
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
    
    public boolean isProcessing() {
        return this == PROCESSING;
    }
    
    public boolean canRetry() {
        return this == FAILED;
    }
}