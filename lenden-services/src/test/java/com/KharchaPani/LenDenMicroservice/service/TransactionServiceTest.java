package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import com.KharchaPani.LenDenMicroservice.exception.ResourceNotFoundException;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import com.KharchaPani.LenDenMicroservice.repository.TransactionRepository;
import com.KharchaPani.LenDenMicroservice.security.AuthService;
import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionPageResponse;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionRequest;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TransactionService transactionService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CLIENT_ID = UUID.randomUUID();

    private Client ownedClient() {
        Client client = new Client();
        client.setId(CLIENT_ID);
        client.setUserId(USER_ID);
        return client;
    }

    private TransactionRequest request() {
        return new TransactionRequest(
                new BigDecimal("1500.00"),
                CLIENT_ID,
                Instant.parse("2026-09-02T00:00:00Z"),
                "Lent for books",
                TransactionType.SENT
        );
    }

    @Test
    void save_createsTransactionUnderOwnedClient() {
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(Optional.of(ownedClient()));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setId(UUID.randomUUID());
                    return t;
                });

        TransactionResponse response = transactionService.save(request(), CLIENT_ID);

        assertThat(response.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.SENT);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getClient().getId()).isEqualTo(CLIENT_ID);
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void save_rejectsForeignClientId() {
        // IDOR guard: victim's clientId + attacker's userId must 404, never save.
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        when(clientRepository.findByIdAndUserId(CLIENT_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.save(request(), CLIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void update_appliesFieldsOnOwnedTransaction() {
        UUID txId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(USER_ID);

        Transaction existing = new Transaction();
        existing.setId(txId);
        existing.setUserId(USER_ID);
        existing.setClient(ownedClient());
        existing.setAmount(new BigDecimal("100.00"));
        when(transactionRepository.findByIdAndUserId(txId, USER_ID))
                .thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.update(request(), txId);

        assertThat(response.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getDescription()).isEqualTo("Lent for books");
        verify(transactionRepository).save(existing);
    }

    @Test
    void update_rejectsForeignTransaction() {
        UUID txId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(txId, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.update(request(), txId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransaction_returnsOwnedTransaction() {
        UUID txId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(USER_ID);

        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setUserId(USER_ID);
        tx.setClient(ownedClient());
        tx.setAmount(new BigDecimal("500.00"));
        tx.setDateAndTime(Instant.parse("2026-09-01T00:00:00Z"));
        tx.setDescription("Returned");
        tx.setTransactionType(TransactionType.RECEIVED);
        when(transactionRepository.findByIdAndUserId(txId, USER_ID))
                .thenReturn(Optional.of(tx));

        TransactionResponse response = transactionService.getTransaction(txId);

        assertThat(response.getId()).isEqualTo(txId);
        assertThat(response.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.RECEIVED);
    }

    @Test
    void getTransaction_rejectsForeignTransaction() {
        UUID txId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(txId, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction(txId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllTransactions_mapsPageWithOneBasedCurrentPage() {
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        Pageable pageable = PageRequest.of(0, 10);

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setUserId(USER_ID);
        tx.setClient(ownedClient());
        tx.setAmount(new BigDecimal("200.00"));
        tx.setDateAndTime(Instant.parse("2026-09-02T00:00:00Z"));
        tx.setDescription("Chai");
        tx.setTransactionType(TransactionType.SENT);

        when(transactionRepository.findByClientIdAndUserId(pageable, CLIENT_ID, USER_ID))
                .thenReturn(new PageImpl<>(List.of(tx), pageable, 1));

        TransactionPageResponse response =
                transactionService.findAllTransactions(pageable, CLIENT_ID);

        assertThat(response.getExpenses()).hasSize(1);
        assertThat(response.getExpenses().get(0).getClientId()).isEqualTo(CLIENT_ID);
        assertThat(response.getCurrentPage()).isEqualTo(1); // service adds +1 to 0-based page
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void deleteTransaction_deletesOwnedTransaction() {
        UUID txId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(USER_ID);

        Transaction existing = new Transaction();
        existing.setId(txId);
        existing.setUserId(USER_ID);
        existing.setClient(ownedClient());
        when(transactionRepository.findByIdAndUserId(txId, USER_ID))
                .thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(txId);

        verify(transactionRepository).delete(existing);
    }

    @Test
    void deleteTransaction_rejectsForeignTransaction() {
        UUID txId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(USER_ID);
        when(transactionRepository.findByIdAndUserId(txId, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(txId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).delete(any());
    }
}
