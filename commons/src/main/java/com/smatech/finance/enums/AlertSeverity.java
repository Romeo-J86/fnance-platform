package com.smatech.finance.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * createdBy romeo
 * createdDate 1/12/2025
 * createdTime 20:17
 * projectName Finance Platform
 **/

@Getter
public enum AlertSeverity {
    CRITICAL(4, "CRITICAL", "Exceeds budget",
            "Immediate attention required - budget has been exceeded"),
    HIGH(3, "HIGH", "Approaching budget limit",
            "Close to budget limit - consider reducing spending"),
    MEDIUM(2, "MEDIUM", "Above normal spending",
            "Spending is higher than usual - monitor closely"),
    LOW(1, "LOW", "Within budget range",
            "Spending is within acceptable range"),
    INFO(0, "INFO", "Informational",
            "General information about spending patterns");

    private final int priority;
    private final String level;
    private final String shortDescription;
    private final String detailedDescription;

    AlertSeverity(int priority, String level, String shortDescription, String detailedDescription) {
        this.priority = priority;
        this.level = level;
        this.shortDescription = shortDescription;
        this.detailedDescription = detailedDescription;
    }

    public static AlertSeverity fromPercentage(BigDecimal percentage) {
        if (percentage == null) {
            return null;
        }

        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            return CRITICAL;
        } else if (percentage.compareTo(new BigDecimal("90")) >= 0) {
            return HIGH;
        } else if (percentage.compareTo(new BigDecimal("75")) >= 0) {
            return MEDIUM;
        } else if (percentage.compareTo(new BigDecimal("60")) >= 0) {
            return LOW;
        }
        return null; // No alert needed below 60%
    }

    // Helper method to get color representation (for UI if needed)
    public String getColorCode() {
        return switch (this) {
            case CRITICAL -> "#DC2626"; // Red
            case HIGH -> "#EA580C";     // Orange
            case MEDIUM -> "#F59E0B";   // Amber
            case LOW -> "#10B981";      // Emerald
            case INFO -> "#3B82F6";     // Blue
        };
    }

    // Action recommendations based on severity
    public String getActionRequired() {
        return switch (this) {
            case CRITICAL -> "IMMEDIATE_ACTION";
            case HIGH -> "URGENT_REVIEW";
            case MEDIUM -> "MONITOR_CLOSELY";
            case LOW -> "CONTINUE_MONITORING";
            case INFO -> "AWARENESS_ONLY";
        };
    }
}