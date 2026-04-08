package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.dto.MedicoListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Buscar médicos por especialidad (no sensible a mayúsculas/minúsculas)
    List<Medico> findByEspecialidadContainingIgnoreCase(String especialidad);

    // Buscar médicos por ID del servicio
    List<Medico> findByServicioId(Long servicioId);

    // Listado usando DTO con relación al servicio
    @Query("SELECT new com.example.veterinariaPatitas.dto.MedicoListDTO(" +
           "m.id, m.nombre, m.especialidad, m.horarioInicio, m.horarioFin, s.name, MIN(h.fecha)) " +
           "FROM Medico m " +
           "LEFT JOIN m.servicio s " +
           "LEFT JOIN m.horarios h " +
           "GROUP BY m.id, m.nombre, m.especialidad, m.horarioInicio, m.horarioFin, s.name " +
           "ORDER BY m.id DESC")
    List<MedicoListDTO> findMedicoList();

    @Query("SELECT new com.example.veterinariaPatitas.dto.MedicoListDTO(" +
           "m.id, m.nombre, m.especialidad, m.horarioInicio, m.horarioFin, s.name, MIN(h.fecha)) " +
           "FROM Medico m " +
           "LEFT JOIN m.servicio s " +
           "LEFT JOIN m.horarios h " +
           "WHERE LOWER(m.especialidad) LIKE LOWER(CONCAT('%', :especialidad, '%')) " +
           "GROUP BY m.id, m.nombre, m.especialidad, m.horarioInicio, m.horarioFin, s.name " +
           "ORDER BY m.id DESC")
    List<MedicoListDTO> findMedicoListByEspecialidad(@Param("especialidad") String especialidad);

    @Query("SELECT DISTINCT m.especialidad FROM Medico m ORDER BY m.especialidad")
    List<String> findDistinctEspecialidades();
}


