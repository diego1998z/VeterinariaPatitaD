package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {
}