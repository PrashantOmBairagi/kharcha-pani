package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import com.KharchaPani.LenDenMicroservice.client.ClientBalanceResponse;
import com.KharchaPani.LenDenMicroservice.client.ClientRequest;
import com.KharchaPani.LenDenMicroservice.client.ClientUpdateRequest;
import com.KharchaPani.LenDenMicroservice.enums.BalanceStatus;
import com.KharchaPani.LenDenMicroservice.enums.ClientStatus;
import com.KharchaPani.LenDenMicroservice.exception.ResourceNotFoundException;
import com.KharchaPani.LenDenMicroservice.repository.ClientRepository;
import com.KharchaPani.LenDenMicroservice.repository.TransactionRepository;
import com.KharchaPani.LenDenMicroservice.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    public Client createClient(ClientRequest request){

        Client client = new Client();
        client.setUserId(authService.getCurrentUserId());
        client.setClientFirstName(request.getClientFirstName());
        client.setClientLastName(request.getClientLastName());
        client.setClientDescription(request.getClientDescription());
        client.setClientMobileNumber(request.getClientMobileNumber());
        client.setClientStatus(ClientStatus.ACTIVE);
        client.setClientAlertsActive(Boolean.FALSE);
        client.setNextSettlementDate(request.getNextSettlementDate());

        return clientRepository.save(client);
    }

    public List<Client> getAllClients(){
        UUID userId = authService.getCurrentUserId();
        return clientRepository.findAllByUserId(userId);
    }

    public Client getClientById(UUID clientId){
        UUID userId = authService.getCurrentUserId();
        return clientRepository
                .findByIdAndUserId(clientId,userId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Client not found"
                        ));
    }

    public Client updateClient(ClientUpdateRequest request,UUID clientId){
        UUID userId = authService.getCurrentUserId();
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
        return clientRepository.save(client);
    }

    public ClientBalanceResponse getClientBalance(UUID clientId) {
        UUID userId = authService.getCurrentUserId();
        clientRepository
                .findByIdAndUserId(clientId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Client not found"
                        ));

        List<Object[]> rows = transactionRepository
                .findBalanceByClientIdAndUserId(clientId, userId);
        Object[] row = rows.isEmpty()
                ? new Object[]{null, null, 0L} : rows.get(0);
        BigDecimal totalSent = row[0] == null
                ? BigDecimal.ZERO : (BigDecimal) row[0];
        BigDecimal totalReceived = row[1] == null
                ? BigDecimal.ZERO : (BigDecimal) row[1];
        long transactionCount = (Long) row[2];

        int cmp = totalSent.compareTo(totalReceived);
        BalanceStatus status = cmp > 0 ? BalanceStatus.RECEIVE
                : cmp < 0 ? BalanceStatus.GIVE : BalanceStatus.SETTLED;

        return new ClientBalanceResponse(
                clientId,
                totalSent,
                totalReceived,
                totalSent.subtract(totalReceived).abs(),
                status,
                transactionCount
        );
    }

    public ResponseEntity<String> deleteClient(UUID clientId) {
        UUID userId = authService.getCurrentUserId();
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
