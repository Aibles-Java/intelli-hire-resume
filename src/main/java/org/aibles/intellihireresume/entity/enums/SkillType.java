package org.aibles.intellihireresume.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum SkillType {
    TECHNICAL("Technical", "Technical skills including programming languages, frameworks, and tools"),
    SOFT("Soft", "Soft skills such as communication, leadership, and teamwork"),
    TOOL("Tool", "Tools and technologies used in development and operations");
    
    private final String displayName;
    private final String description;
    
    SkillType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isTechnical() {
        return this == TECHNICAL;
    }
    
    public boolean isSoft() {
        return this == SOFT;
    }
    
    public boolean isTool() {
        return this == TOOL;
    }
    
    public boolean isHardSkill() {
        return this == TECHNICAL || this == TOOL;
    }
    
    public boolean isSoftSkill() {
        return this == SOFT;
    }
    
    public static List<SkillType> getHardSkillTypes() {
        return Arrays.asList(TECHNICAL, TOOL);
    }
    
    public static List<SkillType> getSoftSkillTypes() {
        return Arrays.asList(SOFT);
    }
    
    public int getWeightForScoring() {
        return switch (this) {
            case TECHNICAL -> 3;  // Highest weight
            case TOOL -> 2;       // Medium weight
            case SOFT -> 1;       // Lower weight
        };
    }
}