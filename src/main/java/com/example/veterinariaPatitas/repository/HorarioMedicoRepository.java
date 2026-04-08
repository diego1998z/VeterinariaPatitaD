package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {
    List<HorarioMedico> findByMedicoId(Long medicoId);
    long countByMedicoId(Long medicoId);
}
