package com.KharchaPani.LenDenMicroservice.transaction;

import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Client is required.")
    private UUID clientId;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount must be greater than 0.")
    private BigDecimal amount;

    @NotNull(message = "Type is required (SENT or RECEIVED).")
    private TransactionType transactionType;

    @Size(max = 200, message = "Description cannot exceed 200 characters.")
    private String description;

    private LocalDate dateAndTime;
}