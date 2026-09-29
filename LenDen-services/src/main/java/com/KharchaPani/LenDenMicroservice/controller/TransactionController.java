package com.KharchaPani.LenDenMicroservice.controller;

import com.KharchaPani.LenDenMicroservice.service.TransactionService;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionRequest;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v2/lenden/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getByClient(
            @RequestParam UUID clientId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(transactionService.getTransactionsForClient(clientId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.noContent().build();
    }
}