package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.service.MedicoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")//responde a la ruta de api medicos
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    //  Devuelve médicos disponibles según servicio, especialidad y hora
    @GetMapping("/buscar")
    public List<Medico> getDisponibles(
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) String hora
    ) {
        // Obtener lista base
        List<Medico> medicos;

        if (servicioId != null)
            medicos = medicoService.listarPorServicioId(servicioId);
        else if (especialidad != null && !especialidad.isEmpty())
            medicos = medicoService.listarPorEspecialidad(especialidad);
        else
            medicos = medicoService.listarTodos();

        // 2️⃣ Filtrar por horario si se pasa una hora
        if (hora != null && !hora.isEmpty())
            medicos = medicoService.filtrarPorDisponibilidad(medicos, hora);

        return medicos;
    }
}


