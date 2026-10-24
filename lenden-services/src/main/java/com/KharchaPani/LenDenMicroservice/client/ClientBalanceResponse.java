package com.KharchaPani.LenDenMicroservice.client;

import com.KharchaPani.LenDenMicroservice.enums.BalanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClientBalanceResponse {

    private UUID clientId;

    private BigDecimal totalSent;

    private BigDecimal totalReceived;

    private BigDecimal netAmount;

    private BalanceStatus balanceStatus;

    private long transactionCount;
}
