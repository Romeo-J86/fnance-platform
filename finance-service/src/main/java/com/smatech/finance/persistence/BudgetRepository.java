package com.smatech.finance.persistence;

import com.smatech.finance.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:53
 * projectName Finance Platform
 **/

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(String userId);

    List<Budget> findByUserIdAndMonthAndYear(String userId, Integer month, Integer year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(String userId, String category, Integer month, Integer year);
}
