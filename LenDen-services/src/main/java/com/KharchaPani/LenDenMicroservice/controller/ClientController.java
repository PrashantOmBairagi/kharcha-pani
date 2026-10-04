package com.KharchaPani.LenDenMicroservice.controller;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.client.ClientRequest;
import com.KharchaPani.LenDenMicroservice.client.ClientUpdateRequest;
import com.KharchaPani.LenDenMicroservice.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping()
    public ResponseEntity<Client> registerClient(
            @Valid @RequestBody ClientRequest request
        ){
        Client createdClient = clientService.createClient(request);
        return ResponseEntity.ok().body(createdClient);
    }

    @GetMapping()
    public ResponseEntity<List<Client>> getAllClients(){
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok().body(clients);
    }

    @GetMapping()
    public Client getClientById(UUID clientId){
        return clientService.getClientById(clientId);
    }

    @PatchMapping()
    public ResponseEntity<Client> updateClient(@Valid ClientUpdateRequest request){
        return clientService.updateClient(request);
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteClient(@RequestBody UUID clientId){
        return clientService.deleteClient(clientId);
    }
}
