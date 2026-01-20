package org.aibles.intellihireresume.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum JobStatus {
    QUEUED("Queued", "Job is waiting to be processed"),
    RUNNING("Running", "Job is currently being processed"),
    SUCCEEDED("Succeeded", "Job completed successfully"),
    FAILED("Failed", "Job failed to complete"),
    CANCELED("Canceled", "Job was canceled before completion");
    
    private final String displayName;
    private final String description;
    
    JobStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return this == SUCCEEDED || this == FAILED || this == CANCELED;
    }
    
    public boolean isInProgress() {
        return this == QUEUED || this == RUNNING;
    }
    
    public boolean isSuccessful() {
        return this == SUCCEEDED;
    }
    
    public boolean isFailed() {
        return this == FAILED;
    }
    
    public boolean isCanceled() {
        return this == CANCELED;
    }
    
    public boolean canRetry() {
        return this == FAILED;
    }
    
    public boolean canCancel() {
        return this == QUEUED || this == RUNNING;
    }
    
    public static List<JobStatus> getActiveStatuses() {
        return Arrays.asList(QUEUED, RUNNING);
    }
    
    public static List<JobStatus> getCompletedStatuses() {
        return Arrays.asList(SUCCEEDED, FAILED, CANCELED);
    }
    
    public static List<JobStatus> getRetriableStatuses() {
        return Arrays.asList(FAILED);
    }
}