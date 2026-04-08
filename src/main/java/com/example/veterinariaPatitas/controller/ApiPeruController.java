package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.service.ApiPeruService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class ApiPeruController {

    private final ApiPeruService apiPeruService;

    public ApiPeruController(ApiPeruService apiPeruService) {
        this.apiPeruService = apiPeruService;
    }

    @PostMapping("/dni")
    public ResponseEntity<Map<String, Object>> consultarDni(@RequestBody Map<String, String> request) {
        // Endpoint usado por formulario de citas para autocompletar datos del titular.
        String dni = request == null ? null : request.get("dni");
        Map<String, Object> result = apiPeruService.consultarDni(dni);
        return buildResponse(result);
    }

    @PostMapping("/tipo-cambio")
    public ResponseEntity<Map<String, Object>> consultarTipoCambio(
            @RequestBody(required = false) Map<String, String> request) {
        // Se acepta "fecha" o "date" por compatibilidad de clientes.
        String fecha = null;
        if (request != null) {
            fecha = request.get("fecha");
            if ((fecha == null || fecha.isBlank()) && request.get("date") != null) {
                fecha = request.get("date");
            }
        }
        Map<String, Object> result = apiPeruService.consultarTipoCambio(fecha);
        return buildResponse(result);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(Map<String, Object> payload) {
        boolean success = Boolean.TRUE.equals(payload.get("success"));
        if (success) {
            return ResponseEntity.ok(payload);
        }

        // Traduce errores de negocio a codigos HTTP coherentes.
        String message = payload.get("message") == null ? "" : payload.get("message").toString();
        HttpStatus status;
        if (message.contains("Token")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (message.contains("formato") || message.contains("dígitos")) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.BAD_GATEWAY;
        }

        Map<String, Object> response = new LinkedHashMap<>(payload);
        response.put("status", status.value());
        return ResponseEntity.status(status).body(response);
    }
}
