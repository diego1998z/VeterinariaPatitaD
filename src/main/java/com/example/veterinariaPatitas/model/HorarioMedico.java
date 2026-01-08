package com.example.veterinariaPatitas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Entity
public class HorarioMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//incrementable
    private Long id;
    //relacion con medico por id
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;
    
    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemana;
    
    private LocalTime horaInicio;
    private LocalTime horaFin;
}