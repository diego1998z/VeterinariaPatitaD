package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.HorarioMedico;
import com.example.veterinariaPatitas.repository.HorarioMedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioMedicoService {

    private final HorarioMedicoRepository horarioMedicoRepository;

    public HorarioMedicoService(HorarioMedicoRepository horarioMedicoRepository) {
        this.horarioMedicoRepository = horarioMedicoRepository;
    }

    public List<HorarioMedico> findByMedicoId(Long medicoId) {
        if (medicoId == null) return List.of();
        return horarioMedicoRepository.findByMedicoId(medicoId);
    }

    public Optional<HorarioMedico> findById(Long id) {
        if (id == null) return Optional.empty();
        return horarioMedicoRepository.findById(id);
    }

    public HorarioMedico save(HorarioMedico horarioMedico) {
        if (horarioMedico == null) return null;
        return horarioMedicoRepository.save(horarioMedico);
    }

    public void delete(Long id) {
        if (id == null) return;
        horarioMedicoRepository.deleteById(id);
    }
}
