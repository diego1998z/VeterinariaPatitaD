package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.ServiceVet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceVetRepository extends JpaRepository<ServiceVet, Integer> {
}

