package com.example.veterinariaPatitas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//auto incrementable
    private Long id;

    //relacion muchos a uno con servicio por el id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServiceVet service;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false, length = 100)
    private String clientName;
 //relacion muchos a uno con medico por id
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }



    public Appointment() {}


    public Appointment(ServiceVet service, LocalDateTime dateTime, String clientName) {
        this.service = service;
        this.dateTime = dateTime;
        this.clientName = clientName;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ¡CORREGIDO! El tipo de retorno es ServiceVet
    public ServiceVet getService() {
        return service;
    }

    // ¡CORREGIDO! El tipo de argumento es ServiceVet
    public void setService(ServiceVet service) {
        this.service = service;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}