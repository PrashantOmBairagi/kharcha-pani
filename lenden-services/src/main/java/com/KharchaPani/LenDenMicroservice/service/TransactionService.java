package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.exception.ResourceNotFoundException;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import com.KharchaPani.LenDenMicroservice.repository.TransactionRepository;
import com.KharchaPani.LenDenMicroservice.security.AuthService;
import com.KharchaPani.LenDenMicroservice.transaction.Transaction;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionPageResponse;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionRequest;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;
    private final AuthService authService;

    public TransactionResponse save(TransactionRequest request,UUID clientId){
        UUID userId = authService.getCurrentUserId();
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Transaction transaction = new Transaction();

        transaction.setClient(client);
        transaction.setAmount(request.getAmount());
        transaction.setDateAndTime(request.getDateAndTime());
        transaction.setDescription(request.getDescription());
        transaction.setUserId(userId);
        transaction.setTransactionType(request.getTransactionType());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .clientId(savedTransaction.getClient().getId())
                .dateAndTime(savedTransaction.getDateAndTime())
                .description(savedTransaction.getDescription())
                .transactionType(savedTransaction.getTransactionType())
                .userId(savedTransaction.getUserId())
                .build();
    }

    public TransactionResponse update(TransactionRequest request,UUID id){
        UUID userId = authService.getCurrentUserId();
        Transaction oldTransaction = transactionRepository
                .findByIdAndUserId(id,userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Couldn't find Transaction!!"
                        )
                );

        oldTransaction.setAmount(request.getAmount());
        oldTransaction.setDateAndTime(request.getDateAndTime());
        oldTransaction.setDescription(request.getDescription());
        oldTransaction.setUserId(userId);
        oldTransaction.setTransactionType(request.getTransactionType());

        Transaction savedTransaction = transactionRepository.save(oldTransaction);

        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .clientId(savedTransaction.getClient().getId())
                .dateAndTime(savedTransaction.getDateAndTime())
                .description(savedTransaction.getDescription())
                .transactionType(savedTransaction.getTransactionType())
                .userId(savedTransaction.getUserId())
                .build();
    }

    public TransactionResponse getTransaction(UUID id){
        UUID userId = authService.getCurrentUserId();
        Transaction transaction = transactionRepository
                .findByIdAndUserId(id,userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Couldn't find Transaction!!"
                        )
                );
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .clientId(transaction.getClient().getId())
                .dateAndTime(transaction.getDateAndTime())
                .description(transaction.getDescription())
                .transactionType(transaction.getTransactionType())
                .userId(transaction.getUserId())
                .build();
    }

    public TransactionPageResponse findAllTransactions(Pageable pageable, UUID clientId) {
        UUID userId = authService.getCurrentUserId();
        Page<Transaction> page = transactionRepository.findByClientIdAndUserId(pageable, clientId, userId);

        List<TransactionResponse> responsesList = page.getContent()
                .stream()
                .map(transaction -> TransactionResponse.builder()
                        .id(transaction.getId())
                        .userId(transaction.getUserId())
                        .clientId(transaction.getClient().getId())
                        .amount(transaction.getAmount())
                        .dateAndTime(transaction.getDateAndTime())
                        .description(transaction.getDescription())
                        .transactionType(transaction.getTransactionType())
                        .build())
                .toList();

        return new TransactionPageResponse(
                responsesList,
                page.getNumber()+1,
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public void deleteTransaction(UUID id){
        UUID userId = authService.getCurrentUserId();
        Transaction transaction = transactionRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Couldn't find Transaction!!"
                        )
                );
        transactionRepository.delete(transaction);
    }

}
