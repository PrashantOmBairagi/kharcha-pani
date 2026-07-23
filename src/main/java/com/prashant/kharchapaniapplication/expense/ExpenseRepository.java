package com.prashant.kharchapaniapplication.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {
    public Page<Expense> findByUserId(UUID id, Pageable pageable);
    public Optional<Expense> findByIdAndUserId(Long id, UUID userId);
}
