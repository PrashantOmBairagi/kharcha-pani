package com.KharchaPani.LenDenMicroservice.client;

import com.KharchaPani.LenDenMicroservice.enums.ClientStatus;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Table(name = "clients")
public class Client {

    private UUID clientId;
    private UUID userID;
    private String clientFirstName;
    private String clientLastName;
    private Integer clientMobileNumber;
    private String clientDescription;
    private ClientStatus clientStatus;
    private Boolean clientAlertsActive;
    private LocalDate nextSettlementDate;
}
