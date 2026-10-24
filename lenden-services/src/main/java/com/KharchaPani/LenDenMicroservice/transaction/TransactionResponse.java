package com.KharchaPani.LenDenMicroservice.transaction;

import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;

    private UUID userId;

    private UUID clientId;

    private BigDecimal amount;

    private Instant dateAndTime;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
}
