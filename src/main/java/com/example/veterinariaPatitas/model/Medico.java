package com.example.veterinariaPatitas.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "medico")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String especialidad;

    // Campos para los horarios
    private LocalTime horarioInicio;
    private LocalTime horarioFin;

    // Relación con el servicio
    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private ServiceVet servicio;

    //  Constructor vacío (obligatorio para JPA)
    public Medico() {
    }

    //  Constructor con parámetros
    public Medico(String nombre, String especialidad, LocalTime horarioInicio, LocalTime horarioFin) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
    }

    //  Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFin() {
        return horarioFin;
    }

    public void setHorarioFin(LocalTime horarioFin) {
        this.horarioFin = horarioFin;
    }

    public ServiceVet getServicio() {
        return servicio;
    }

    public void setServicio(ServiceVet servicio) {
        this.servicio = servicio;
    }
}




