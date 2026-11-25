package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {
}
