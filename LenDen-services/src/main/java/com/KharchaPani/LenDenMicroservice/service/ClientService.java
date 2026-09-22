package com.KharchaPani.LenDenMicroservice.service;

import com.KharchaPani.LenDenMicroservice.client.Client;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    public Client createClient(Client client){
        Client newClient = new Client();
        return client;
    }
}
