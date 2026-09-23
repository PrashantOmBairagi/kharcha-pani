package com.KharchaPani.LenDenMicroservice.repository;

import com.KharchaPani.LenDenMicroservice.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
     List<Client> findAllByUserId(UUID userId);
     Optional<Client> findByIdAndUserId(UUID clientId, UUID userId);
}
