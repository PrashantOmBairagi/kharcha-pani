package com.prashant.kharchapaniapplication.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Page<Expense> findByUserId(UUID id, Pageable pageable);

    Page<Expense> findByUserIdAndFinancialMonthId(UUID userId, UUID financialMonthId, Pageable pageable);

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);
}
