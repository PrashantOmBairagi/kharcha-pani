package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.client.ClientRequest;
import com.KharchaPani.LenDenMicroservice.enums.ClientStatus;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

}
