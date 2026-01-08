package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;

@Service
public class MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    // ✅ Listar todos los médicos
    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    // ✅ Listar médicos por especialidad
    public List<Medico> listarPorEspecialidad(String especialidad) {
        return medicoRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    // ✅ Listar médicos según el servicio al que pertenecen
    public List<Medico> listarPorServicioId(Long servicioId) {
        return medicoRepository.findByServicioId(servicioId);
    }

    // ✅ Filtrar por disponibilidad en una hora específica (ejemplo: "14:30")
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
}



