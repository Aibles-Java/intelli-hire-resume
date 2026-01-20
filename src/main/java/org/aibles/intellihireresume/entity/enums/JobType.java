package org.aibles.intellihireresume.entity.enums;

public enum JobType {
    PARSE("Parse", "Initial parsing of resume file to extract basic information"),
    REPARSE("Reparse", "Re-parsing of resume file with updated algorithms"),
    ANALYZE("Analyze", "Advanced analysis of resume content for skills and experience matching");
    
    private final String displayName;
    private final String description;
    
    JobType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isParseJob() {
        return this == PARSE || this == REPARSE;
    }
    
    public boolean isAnalyzeJob() {
        return this == ANALYZE;
    }
    
    public boolean isInitialParse() {
        return this == PARSE;
    }
    
    public boolean isReparse() {
        return this == REPARSE;
    }
    
    public int getPriority() {
        return switch (this) {
            case PARSE -> 1;     // Highest priority
            case REPARSE -> 2;   // Medium priority
            case ANALYZE -> 3;   // Lowest priority
        };
    }
    
    public long getEstimatedDurationMinutes() {
        return switch (this) {
            case PARSE -> 2;     // 2 minutes
            case REPARSE -> 2;   // 2 minutes
            case ANALYZE -> 5;   // 5 minutes
        };
    }
}