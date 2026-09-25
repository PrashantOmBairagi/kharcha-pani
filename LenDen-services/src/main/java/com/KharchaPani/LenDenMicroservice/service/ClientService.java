package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.client.ClientRequest;
import com.KharchaPani.LenDenMicroservice.client.ClientUpdateRequest;
import com.KharchaPani.LenDenMicroservice.enums.ClientStatus;
import com.KharchaPani.LenDenMicroservice.exception.ResourceNotFoundException;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public Client createClient(ClientRequest request, UUID userId){

        Client client = new Client();
        client.setUserId(userId);
        client.setClientFirstName(request.getClientFirstName());
        client.setClientLastName(request.getClientLastName());
        client.setClientDescription(request.getClientDescription());
        client.setClientMobileNumber(request.getClientMobileNumber());
        client.setClientStatus(ClientStatus.ACTIVE);
        client.setClientAlertsActive(Boolean.FALSE);
        client.setNextSettlementDate(request.getNextSettlementDate());

        return clientRepository.save(client);
    }

    public List<Client> getAllClients(UUID userId){
        return clientRepository.findAllByUserId(userId);
    }

    public Client getClientById(UUID clientId, UUID userId){
        Client client = clientRepository
                .findByIdAndUserId(clientId,userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Client not found"
                        ));
        return client;
    }

    public ResponseEntity<Client> updateClient(ClientUpdateRequest request, UUID clientId){
        UUID userId = randomUUID();
        Client client = clientRepository
                .findByIdAndUserId(clientId,userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Client not found"
                        ));
        if(request.getClientFirstName() != null){
            client.setClientFirstName(request.getClientFirstName());
        }
        if(request.getClientLastName() != null){
            client.setClientLastName(request.getClientLastName());
        }
        if(request.getClientMobileNumber() != null){
            client.setClientMobileNumber(request.getClientMobileNumber());
        }
        if(request.getClientDescription() != null){
            client.setClientDescription(request.getClientDescription());
        }
        if(request.getNextSettlementDate() != null){
            client.setNextSettlementDate(request.getNextSettlementDate());
        }
        clientRepository.save(client);
        return ResponseEntity.ok(client);
    }

    public ResponseEntity<String> deleteClient(UUID clientId , UUID userId) {
       Client client = clientRepository
               .findByIdAndUserId(clientId,userId)
               .orElseThrow(
                       ()-> new ResourceNotFoundException(
                               "Client not found"
                       ));
       clientRepository.delete(client);
       return ResponseEntity.ok().body("Client deleted");
    }
}
