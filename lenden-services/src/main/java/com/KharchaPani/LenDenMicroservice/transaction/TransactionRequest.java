package com.KharchaPani.LenDenMicroservice.transaction;

import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TransactionRequest {
    private BigDecimal amount;

    private UUID clientId;

    private Instant dateAndTime;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
}
