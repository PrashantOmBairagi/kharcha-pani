package com.KharchaPani.LenDenMicroservice.controller;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.service.ClientService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("api/v2/lenden")
public class ClientController {

    private final ClientService clientService;

    @PostMapping()
    public ResponseEntity<Client> registerClient(Client requestedClient){
        Client createdClient = clientService.createClient(requestedClient);
        return ResponseEntity.ok().build();
    }

}
