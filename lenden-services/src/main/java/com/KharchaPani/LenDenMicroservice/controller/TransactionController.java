package com.KharchaPani.LenDenMicroservice.controller;

import com.KharchaPani.LenDenMicroservice.service.TransactionService;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionPageResponse;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionRequest;
import com.KharchaPani.LenDenMicroservice.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v2/lenden/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/{clientId}")
    public ResponseEntity<TransactionResponse> addTransaction(@RequestBody TransactionRequest saveRequest,@PathVariable UUID clientId){
        TransactionResponse response =  transactionService.save(saveRequest,clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @RequestBody TransactionRequest updateRequest,
            @PathVariable UUID id
    ){
        TransactionResponse response = transactionService.update(updateRequest,id);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable UUID id){
        transactionService.deleteTransaction(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Delete Success!!");
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getResponse(
            @PathVariable UUID transactionId
    ){
        TransactionResponse response =transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/all/{clientId}"})
    public ResponseEntity<TransactionPageResponse> getAllTransaction(
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "dateAndTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir

    ){
        List<String> allowedSortFields = List.of("dateAndTime" , "amount");
        String safeSortBy = allowedSortFields.contains(sortBy) ? sortBy : "dateAndTime";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageNo-1,pageSize,direction,safeSortBy);
       TransactionPageResponse response = transactionService.findAllTransactions(pageable,clientId);
       return ResponseEntity.ok(response);
    }
}
