package com.smatech.finance.dtos.finance;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:17
 * projectName Finance Platform
 **/

@Builder
public record BudgetDTO(
        Long id,
        String userId,
        String category,
        BigDecimal amount,
        Integer month,
        Integer year,
        LocalDateTime createdAt
) {}
