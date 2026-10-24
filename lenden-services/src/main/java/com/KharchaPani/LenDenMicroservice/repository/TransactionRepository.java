package com.KharchaPani.LenDenMicroservice.repository;

import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    Page<Transaction> findByClientIdAndUserId(Pageable pageable, UUID clientId,UUID userId);

    /**
     * Aggregate rows for one client's ledger.
     * Each row = { totalSent (BigDecimal, null when empty),
     *              totalReceived (BigDecimal, null when empty),
     *              transactionCount (Long) }.
     * No GROUP BY, so this is exactly one row; declared as a List because
     * single-row multi-select mapping differs across Spring Data versions.
     * Ownership is enforced by the caller via findByIdAndUserId first.
     */
    @Query("""
            SELECT SUM(CASE WHEN t.transactionType = com.KharchaPani.LenDenMicroservice.enums.TransactionType.SENT THEN t.amount ELSE NULL END),
                   SUM(CASE WHEN t.transactionType = com.KharchaPani.LenDenMicroservice.enums.TransactionType.RECEIVED THEN t.amount ELSE NULL END),
                   COUNT(t)
            FROM Transaction t
            WHERE t.client.id = :clientId AND t.userId = :userId
            """)
    List<Object[]> findBalanceByClientIdAndUserId(UUID clientId, UUID userId);
}
