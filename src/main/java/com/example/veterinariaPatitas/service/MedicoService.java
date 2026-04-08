package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.dto.MedicoListDTO;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    //  Listar todos los médicos
    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    //  Listar médicos por especialidad
    public List<Medico> listarPorEspecialidad(String especialidad) {
        if (especialidad == null) return List.of();
        return medicoRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    //  Listar médicos según el servicio al que pertenecen
    public List<Medico> listarPorServicioId(Long servicioId) {
        if (servicioId == null) return List.of();
        return medicoRepository.findByServicioId(servicioId);
    }

    //  Filtrar por disponibilidad en una hora específica (ejemplo: "14:30")
    public List<Medico> filtrarPorDisponibilidad(List<Medico> medicos, String hora) {
        if (hora == null || hora.isEmpty()) return medicos;

        LocalTime horaConsulta = LocalTime.parse(hora);

        return medicos.stream()
                .filter(m -> {
                    LocalTime inicio = m.getHorarioInicio();
                    LocalTime fin = m.getHorarioFin();
                    return inicio != null && fin != null &&
                           !horaConsulta.isBefore(inicio) && !horaConsulta.isAfter(fin);
                })
                .toList();
    }

    //  CRUD básico para gestión de médicos
    public Optional<Medico> findById(Long id) {
        if (id == null) return Optional.empty();
        return medicoRepository.findById(id);
    }

    public Medico save(Medico medico) {
        return medicoRepository.save(medico);
    }

    public void delete(Long id) {
        if (id == null) return;
        medicoRepository.deleteById(id);
    }

    // ✅ Listado con DTO (relación Medico -> Servicio)
    public List<MedicoListDTO> getMedicoList() {
        return medicoRepository.findMedicoList();
    }

    // ✅ Listado con filtro por especialidad (DTO)
    public List<MedicoListDTO> getMedicoListByEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) {
            return getMedicoList();
        }
        return medicoRepository.findMedicoListByEspecialidad(especialidad);
    }

    public List<String> getEspecialidades() {
        return medicoRepository.findDistinctEspecialidades();
    }
}



