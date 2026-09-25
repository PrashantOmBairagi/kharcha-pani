package com.KharchaPani.LenDenMicroservice.repository;

import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
