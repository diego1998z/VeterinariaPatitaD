package com.example.veterinariaPatitas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "horarios_medicos")
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

    // Fecha específica (opcional). Si está presente, se usa en lugar del día de semana.
    private LocalDate fecha;
    
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
