package com.KharchaPani.LenDenMicroservice.repository;

import com.KharchaPani.LenDenMicroservice.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

}
