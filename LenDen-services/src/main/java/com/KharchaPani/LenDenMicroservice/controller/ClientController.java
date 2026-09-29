package com.KharchaPani.LenDenMicroservice.controller;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.client.ClientRequest;
import com.KharchaPani.LenDenMicroservice.client.ClientUpdateRequest;
import com.KharchaPani.LenDenMicroservice.service.ClientService;
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
@RequestMapping("api/v2/lenden/client")
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PostMapping
    public ResponseEntity<Client> registerClient(
            @Valid @RequestBody ClientRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(clientService.getAllClients(userId));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<Client> getClientById(
            @PathVariable UUID clientId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(clientService.getClientById(clientId, userId));
    }

    @PatchMapping("/{clientId}")
    public ResponseEntity<Client> updateClient(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientUpdateRequest request,
            @AuthenticationPrincipal UUID userId) {
        return clientService.updateClient(request, clientId, userId);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> deleteClient(
            @PathVariable UUID clientId,
            @AuthenticationPrincipal UUID userId) {
        return clientService.deleteClient(clientId, userId);
    }
}