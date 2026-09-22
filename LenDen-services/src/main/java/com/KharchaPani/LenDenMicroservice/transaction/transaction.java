package com.KharchaPani.LenDenMicroservice.transaction;


import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Table(name = "transactions")
public class transaction {
    @Id
    private UUID id;

    private UUID userId;

    private BigDecimal amount;

    private UUID clientId;

    private LocalDate dateAndTime;

    private String description;

    private TransactionType transactionType;

}
