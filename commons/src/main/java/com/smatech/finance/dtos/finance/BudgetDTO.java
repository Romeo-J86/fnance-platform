package com.smatech.finance.dtos.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 15:17
 * projectName Finance Platform
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {
    private Long id;
    private String userId;
    private String category;
    private BigDecimal amount;
    private Integer month;
    private Integer year;
    private LocalDateTime createdAt;
}
