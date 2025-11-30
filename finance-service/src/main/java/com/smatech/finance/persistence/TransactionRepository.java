package com.smatech.finance.persistence;

import com.smatech.finance.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * createdBy romeo
 * createdDate 29/11/2025
 * createdTime 14:52
 * projectName Finance Platform
 **/

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(String userId);

    List<Transaction> findByUserIdAndCategory(String userId, String category);

    List<Transaction> findByUserIdAndTransactionDateBetween(String userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND " +
            "(:category IS NULL OR t.category = :category) AND " +
            "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transactionDate <= :endDate) AND " +
            "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR t.amount <= :maxAmount)")
    List<Transaction> findTransactionsWithFilters(
            @Param("userId") String userId,
            @Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND " +
            "YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month " +
            "GROUP BY t.category")
    List<Object[]> getSpendingByCategory(@Param("userId") String userId,
                                         @Param("month") int month,
                                         @Param("year") int year);
}
