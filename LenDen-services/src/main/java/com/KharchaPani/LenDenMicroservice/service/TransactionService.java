package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.exception.ResourceNotFoundException;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import com.KharchaPani.LenDenMicroservice.repository.TransactionRepository;
import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionRequest;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;

    public TransactionResponse createTransaction(TransactionRequest request, UUID userId) {
        Client client = clientRepository
                .findByIdAndUserId(request.getClientId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setClient(client);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setDescription(request.getDescription());
        transaction.setDateAndTime(
                request.getDateAndTime() != null ? request.getDateAndTime() : LocalDate.now()
        );

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public List<TransactionResponse> getTransactionsForClient(UUID clientId, UUID userId) {
        clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        return transactionRepository.findAllByClient_IdAndUserId(clientId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteTransaction(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getClient().getId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getDescription(),
                transaction.getDateAndTime()
        );
    }
}