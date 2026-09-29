package com.KharchaPani.LenDenMicroservice.transaction;

import com.KharchaPani.LenDenMicroservice.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID clientId,
        BigDecimal amount,
        TransactionType transactionType,
        String description,
        LocalDate dateAndTime
) {}