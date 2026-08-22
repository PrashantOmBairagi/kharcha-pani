package com.prashant.kharchapaniapplication.financialmonth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialMonthRepository extends JpaRepository<FinancialMonth, UUID> {

    Optional<FinancialMonth> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    List<FinancialMonth> findByUserIdOrderByYearDescMonthDesc(UUID userId);

    @Query(value = "SELECT * FROM financial_month WHERE user_id = ?1 AND year = YEAR(CURDATE()) AND month = MONTH(CURDATE())", nativeQuery = true)
    Optional<FinancialMonth> findCurrentMonth(UUID userId);
}
