package org.aibles.intellihireresume.entity.enums;

import java.math.BigDecimal;

public enum SeniorityLevel {
    JUNIOR("Junior", "0-2 years of experience", BigDecimal.ZERO, new BigDecimal("2")),
    MID("Mid", "2-5 years of experience", new BigDecimal("2"), new BigDecimal("5")),
    SENIOR("Senior", "5-8 years of experience", new BigDecimal("5"), new BigDecimal("8")),
    LEAD("Lead", "8-12 years of experience", new BigDecimal("8"), new BigDecimal("12")),
    PRINCIPAL("Principal", "12+ years of experience", new BigDecimal("12"), new BigDecimal("50"));
    
    private final String displayName;
    private final String description;
    private final BigDecimal minYears;
    private final BigDecimal maxYears;
    
    SeniorityLevel(String displayName, String description, BigDecimal minYears, BigDecimal maxYears) {
        this.displayName = displayName;
        this.description = description;
        this.minYears = minYears;
        this.maxYears = maxYears;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getMinYears() {
        return minYears;
    }
    
    public BigDecimal getMaxYears() {
        return maxYears;
    }
    
    public boolean isJunior() {
        return this == JUNIOR;
    }
    
    public boolean isMid() {
        return this == MID;
    }
    
    public boolean isSenior() {
        return this == SENIOR;
    }
    
    public boolean isLead() {
        return this == LEAD;
    }
    
    public boolean isPrincipal() {
        return this == PRINCIPAL;
    }
    
    public boolean isExperienced() {
        return this == SENIOR || this == LEAD || this == PRINCIPAL;
    }
    
    public boolean isLeadership() {
        return this == LEAD || this == PRINCIPAL;
    }
    
    public static SeniorityLevel fromYearsOfExperience(BigDecimal years) {
        if (years == null || years.compareTo(BigDecimal.ZERO) < 0) {
            return JUNIOR;
        }
        
        for (SeniorityLevel level : values()) {
            if (years.compareTo(level.minYears) >= 0 && years.compareTo(level.maxYears) < 0) {
                return level;
            }
        }
        
        return PRINCIPAL; // Default to highest level for very high experience
    }
    
    public static SeniorityLevel fromYearsOfExperience(double years) {
        return fromYearsOfExperience(BigDecimal.valueOf(years));
    }
    
    public int getLevel() {
        return ordinal() + 1;
    }
    
    public double getSalaryMultiplier() {
        return switch (this) {
            case JUNIOR -> 1.0;
            case MID -> 1.5;
            case SENIOR -> 2.0;
            case LEAD -> 2.5;
            case PRINCIPAL -> 3.0;
        };
    }
}