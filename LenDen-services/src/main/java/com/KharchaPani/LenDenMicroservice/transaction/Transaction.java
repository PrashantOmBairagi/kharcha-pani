package com.KharchaPani.LenDenMicroservice.transaction;


import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    private BigDecimal amount;

    private UUID clientId;

    private LocalDate dateAndTime;

    private String description;

    private TransactionType transactionType;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "clientId",
            nullable = false
    )
    private Client client;

}
