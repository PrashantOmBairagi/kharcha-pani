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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-database coverage for the transaction delete path.
 * The mocked {@code TransactionServiceTest} cannot catch query-level
 * failures (e.g. a derived delete query missing {@code @Modifying},
 * which throws at runtime) — this slice runs real JPA against H2,
 * mirroring the exact find-then-delete flow the service uses.
 */
@DataJpaTest
class TransactionRepositoryDeleteTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Client persistClient(UUID userId) {
        Client client = new Client();
        client.setUserId(userId);
        client.setClientFirstName("Del");
        client.setClientLastName("Probe");
        client.setClientMobileNumber("9345678901");
        return clientRepository.save(client);
    }

    private Transaction persistTransaction(Client client, UUID userId) {
        Transaction transaction = new Transaction();
        transaction.setClient(client);
        transaction.setUserId(userId);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDateAndTime(Instant.parse("2026-10-06T00:00:00Z"));
        transaction.setDescription("del me");
        transaction.setTransactionType(TransactionType.SENT);
        return transactionRepository.save(transaction);
    }

    @Test
    void findThenDelete_removesOwnedTransaction() {
        UUID userId = UUID.randomUUID();
        Client client = persistClient(userId);
        Transaction saved = persistTransaction(client, userId);
        entityManager.flush();
        entityManager.clear();

        // exact service flow: find-then-delete
        Optional<Transaction> found =
                transactionRepository.findByIdAndUserId(saved.getId(), userId);
        assertThat(found).isPresent();
        transactionRepository.delete(found.get());
        entityManager.flush();

        assertThat(transactionRepository.findByIdAndUserId(saved.getId(), userId))
                .isEmpty();
    }

    @Test
    void findByIdAndUserId_emptyForForeignUser() {
        UUID owner = UUID.randomUUID();
        UUID intruder = UUID.randomUUID();
        Client client = persistClient(owner);
        Transaction saved = persistTransaction(client, owner);
        entityManager.flush();
        entityManager.clear();

        assertThat(transactionRepository.findByIdAndUserId(saved.getId(), intruder))
                .isEmpty();
        // victim's row untouched
        assertThat(transactionRepository.findByIdAndUserId(saved.getId(), owner))
                .isPresent();
    }
}
