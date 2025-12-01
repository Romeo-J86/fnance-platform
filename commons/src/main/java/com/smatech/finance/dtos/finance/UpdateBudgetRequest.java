package com.smatech.finance.dtos.finance;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * createdBy romeo
 * createdDate 1/12/2025
 * createdTime 14:14
 * projectName Finance Platform
 **/

@Builder
public record UpdateBudgetRequest(
        BigDecimal amount,
        Integer month,
        Integer year
) {}
