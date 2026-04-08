package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.Appointment;
import com.example.veterinariaPatitas.dto.AppointmentListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 1. Verificar si ya existe una cita en un momento exacto
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.dateTime = ?1") 
    boolean existsByDateTime(LocalDateTime dateTime);
    
    // 2. Contar citas pendientes (citas futuras)
    Long countByDateTimeAfter(LocalDateTime dateTime); 

    // 3. Obtener datos por mes para el gráfico 
    /**
     * Devuelve la cantidad de citas por mes.
     * Resultado: Lista de arrays con [año, mes, total_citas]
     */
    @Query("SELECT YEAR(a.dateTime), MONTH(a.dateTime), COUNT(a) " + 
           "FROM Appointment a " +
           "GROUP BY YEAR(a.dateTime), MONTH(a.dateTime) " + 
           "ORDER BY YEAR(a.dateTime) ASC, MONTH(a.dateTime) ASC")
    List<Object[]> countAppointmentsPerMonth();
    List<Appointment> findByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<Appointment> findByMedicoId(Long medicoId);

    // Listado completo usando DTO y relaciones con Servicio y Médico
    @Query("SELECT new com.example.veterinariaPatitas.dto.AppointmentListDTO(" +
           "a.id, a.clientName, a.dateTime, s.name, s.price, m.nombre) " +
           "FROM Appointment a " +
           "JOIN a.service s " +
           "LEFT JOIN a.medico m " +
           "ORDER BY a.dateTime DESC")
    List<AppointmentListDTO> findAppointmentList();
}
