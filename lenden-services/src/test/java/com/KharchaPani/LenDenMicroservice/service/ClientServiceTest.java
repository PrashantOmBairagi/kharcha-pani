package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.client.ClientBalanceResponse;
import com.KharchaPani.LenDenMicroservice.enums.BalanceStatus;
import com.KharchaPani.LenDenMicroservice.exception.ResourceNotFoundException;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import com.KharchaPani.LenDenMicroservice.repository.TransactionRepository;
import com.KharchaPani.LenDenMicroservice.security.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ClientService clientService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CLIENT_ID = UUID.randomUUID();

    private void givenOwnedClient() {
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setUserId(USER_ID);
        when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(Optional.of(client));
    }

    @Test
    void getClientBalance_receiveWhenSentExceedsReceived() {
        givenOwnedClient();
        when(transactionRepository.findBalanceByClientIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(java.util.List.<Object[]>of(new Object[]{new BigDecimal("5000.00"),
                        new BigDecimal("2000.00"), 3L}));

        ClientBalanceResponse response = clientService.getClientBalance(CLIENT_ID);

        assertThat(response.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(response.getTotalSent()).isEqualByComparingTo("5000.00");
        assertThat(response.getTotalReceived()).isEqualByComparingTo("2000.00");
        assertThat(response.getNetAmount()).isEqualByComparingTo("3000.00");
        assertThat(response.getBalanceStatus()).isEqualTo(BalanceStatus.RECEIVE);
        assertThat(response.getTransactionCount()).isEqualTo(3L);
    }

    @Test
    void getClientBalance_giveWhenReceivedExceedsSent() {
        givenOwnedClient();
        when(transactionRepository.findBalanceByClientIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(java.util.List.<Object[]>of(new Object[]{new BigDecimal("100.00"),
                        new BigDecimal("400.00"), 2L}));

        ClientBalanceResponse response = clientService.getClientBalance(CLIENT_ID);

        assertThat(response.getNetAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getBalanceStatus()).isEqualTo(BalanceStatus.GIVE);
    }

    @Test
    void getClientBalance_settledWhenEmptyLedger() {
        givenOwnedClient();
        when(transactionRepository.findBalanceByClientIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(java.util.List.<Object[]>of(new Object[]{null, null, 0L}));

        ClientBalanceResponse response = clientService.getClientBalance(CLIENT_ID);

        assertThat(response.getTotalSent()).isEqualByComparingTo("0");
        assertThat(response.getTotalReceived()).isEqualByComparingTo("0");
        assertThat(response.getNetAmount()).isEqualByComparingTo("0");
        assertThat(response.getBalanceStatus()).isEqualTo(BalanceStatus.SETTLED);
        assertThat(response.getTransactionCount()).isZero();
    }

    @Test
    void getClientBalance_rejectsForeignClient() {
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientBalance(CLIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");

        verify(transactionRepository, org.mockito.Mockito.never())
                .findBalanceByClientIdAndUserId(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }
}
