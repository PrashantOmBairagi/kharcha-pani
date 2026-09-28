package com.KharchaPani.LenDenMicroservice.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import com.KharchaPani.LenDenMicroservice.enums.ClientStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "client_id", updatable = false, nullable = false)
    private UUID id;

    private UUID userId;
    private String clientFirstName;
    private String clientLastName;
    private String clientMobileNumber;
    private String clientDescription;

    @Enumerated(EnumType.STRING)
    private ClientStatus clientStatus;
    private Boolean clientAlertsActive;
    private LocalDate nextSettlementDate;

    @JsonIgnore
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}
