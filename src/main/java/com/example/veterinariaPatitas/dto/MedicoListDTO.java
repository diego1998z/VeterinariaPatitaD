package com.example.veterinariaPatitas.dto;

import java.time.LocalTime;
import java.time.LocalDate;

public class MedicoListDTO {

    private final Long id;
    private final String nombre;
    private final String especialidad;
    private final LocalTime horarioInicio;
    private final LocalTime horarioFin;
    private final String servicioNombre;
    private final LocalDate fechaHorario;

    public MedicoListDTO(Long id,
                         String nombre,
                         String especialidad,
                         LocalTime horarioInicio,
                         LocalTime horarioFin,
                         String servicioNombre,
                         LocalDate fechaHorario) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
        this.servicioNombre = servicioNombre;
        this.fechaHorario = fechaHorario;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public LocalTime getHorarioFin() {
        return horarioFin;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public LocalDate getFechaHorario() {
        return fechaHorario;
    }
}
