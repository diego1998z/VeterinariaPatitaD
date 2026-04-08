package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.repository.MedicoRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class EstadoClinicaController {

    private static final LocalTime HORA_APERTURA = LocalTime.of(7, 0);
    private static final LocalTime HORA_CIERRE = LocalTime.of(21, 0);

    private final MedicoRepository medicoRepository;

    public EstadoClinicaController(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @GetMapping("/estado-clinica")
    public Map<String, Object> estadoClinica() {
        // Indicador simple para cliente externo: si estamos dentro del horario de atencion.
        LocalTime ahora = LocalTime.now();
        boolean atencionActiva = !ahora.isBefore(HORA_APERTURA) && !ahora.isAfter(HORA_CIERRE);

        // Payload publico que se consume desde HTML+JS fuera de Spring MVC.
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nombreClinica", "Veterinaria Patitas");
        data.put("direccion", "Villa El Salvador, Lima");
        data.put("telefono", "986974982");
        data.put("horario", "07:00 - 21:00");
        data.put("atencionActiva", atencionActiva);
        data.put("medicosRegistrados", medicoRepository.count());
        data.put("fechaServidor", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        data.put("horaServidor", ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Respuesta estandar success/data para facilitar consumo en frontend.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
}
