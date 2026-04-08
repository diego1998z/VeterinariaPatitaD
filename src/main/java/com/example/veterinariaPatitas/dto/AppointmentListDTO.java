package com.example.veterinariaPatitas.dto;

import java.time.LocalDateTime;

public class AppointmentListDTO {

    private final Long id;
    private final String clientName;
    private final LocalDateTime dateTime;
    private final String serviceName;
    private final Double servicePrice;
    private final String medicoNombre;

    public AppointmentListDTO(Long id,
                              String clientName,
                              LocalDateTime dateTime,
                              String serviceName,
                              Double servicePrice,
                              String medicoNombre) {
        this.id = id;
        this.clientName = clientName;
        this.dateTime = dateTime;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.medicoNombre = medicoNombre;
    }

    public Long getId() {
        return id;
    }

    public String getClientName() {
        return clientName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Double getServicePrice() {
        return servicePrice;
    }

    public String getMedicoNombre() {
        return medicoNombre;
    }
}
