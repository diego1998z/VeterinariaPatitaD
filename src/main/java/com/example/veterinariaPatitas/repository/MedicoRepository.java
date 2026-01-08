package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Buscar médicos por especialidad (no sensible a mayúsculas/minúsculas)
    List<Medico> findByEspecialidadContainingIgnoreCase(String especialidad);

    // Buscar médicos por ID del servicio
    List<Medico> findByServicioId(Long servicioId);
}


