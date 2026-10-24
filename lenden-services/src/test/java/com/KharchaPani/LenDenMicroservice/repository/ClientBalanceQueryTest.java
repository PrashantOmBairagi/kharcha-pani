package com.KharchaPani.LenDenMicroservice.repository;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.enums.TransactionType;
import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-database coverage for the client-balance aggregate query
 * backing {@code GET /api/v2/lenden/client/{id}/summary}.
 */
@DataJpaTest
class ClientBalanceQueryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Client persistClient(UUID userId, String firstName) {
        Client client = new Client();
        client.setUserId(userId);
        client.setClientFirstName(firstName);
        client.setClientMobileNumber("9345678901");
        return clientRepository.save(client);
    }

    private void persistTxn(Client client, UUID userId, String amount,
                            TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setClient(client);
        transaction.setUserId(userId);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setDateAndTime(Instant.parse("2026-10-06T00:00:00Z"));
        transaction.setDescription("seed");
        transaction.setTransactionType(type);
        transactionRepository.save(transaction);
    }

    @Test
    void aggregate_sumsByTypeAndCountsRows() {
        UUID userId = UUID.randomUUID();
        Client client = persistClient(userId, "Agg");
        persistTxn(client, userId, "5000.00", TransactionType.SENT);
        persistTxn(client, userId, "1000.00", TransactionType.SENT);
        persistTxn(client, userId, "2000.00", TransactionType.RECEIVED);
        entityManager.flush();
        entityManager.clear();

        java.util.List<Object[]> rows = transactionRepository
                .findBalanceByClientIdAndUserId(client.getId(), userId);

        assertThat(rows).hasSize(1);
        Object[] row = rows.get(0);
        assertThat((BigDecimal) row[0]).isEqualByComparingTo("6000.00");
        assertThat((BigDecimal) row[1]).isEqualByComparingTo("2000.00");
        assertThat((Long) row[2]).isEqualTo(3L);
    }

    @Test
    void aggregate_emptyLedgerReturnsNullsAndZeroCount() {
        UUID userId = UUID.randomUUID();
        Client client = persistClient(userId, "Emp");
        entityManager.flush();
        entityManager.clear();

        java.util.List<Object[]> rows = transactionRepository
                .findBalanceByClientIdAndUserId(client.getId(), userId);

        assertThat(rows).hasSize(1);
        Object[] row = rows.get(0);
        assertThat(row[0]).isNull();
        assertThat(row[1]).isNull();
        assertThat((Long) row[2]).isZero();
    }

    @Test
    void aggregate_scopedToRequestingUser() {
        UUID owner = UUID.randomUUID();
        UUID intruder = UUID.randomUUID();
        Client client = persistClient(owner, "Own");
        persistTxn(client, owner, "100.00", TransactionType.SENT);
        entityManager.flush();
        entityManager.clear();

        java.util.List<Object[]> rows = transactionRepository
                .findBalanceByClientIdAndUserId(client.getId(), intruder);

        assertThat(rows).hasSize(1);
        Object[] row = rows.get(0);
        assertThat(row[0]).isNull();
        assertThat(row[1]).isNull();
        assertThat((Long) row[2]).isZero();
    }
}
