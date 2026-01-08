package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Appointment;
import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.repository.ServiceRepository;
import com.example.veterinariaPatitas.repository.AppointmentRepository;
import com.example.veterinariaPatitas.repository.MedicoRepository;//obtiene datos de la bd

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.time.LocalDateTime;//fecha y hora
import java.time.LocalTime;
import java.util.LinkedHashMap; // Para mantener el orden de los meses
import java.util.List;
import java.util.Optional; 

@Service
public class AppointmentService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    // 1. Obtener lista de servicios disponibles
    public List<ServiceVet> getAvailableServices() {
        return serviceRepository.findAll();
    }

    //  2. Obtener lista de citas agendadas
    public List<Appointment> getBookedAppointments() {
        return appointmentRepository.findAll();
    }

    //  3. Verificar disponibilidad de hora
    public boolean isAvailable(LocalDateTime dateTime) {
        return !appointmentRepository.existsByDateTime(dateTime);
    }

    //  4. Agendar cita (con servicio y médico)
    public boolean bookAppointment(int serviceId, Long medicoId, LocalDateTime appointmentDateTime, String clientName) {
        // 1️ Buscar servicio
        ServiceVet service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) return false;

        // 2️ Buscar médico
        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        if (medico == null) return false;

        // 3️ Validar si la hora de la cita está dentro del horario del médico
        LocalTime inicio = medico.getHorarioInicio();
        LocalTime fin = medico.getHorarioFin();
        LocalTime horaCita = appointmentDateTime.toLocalTime();

        if (horaCita.isBefore(inicio) || horaCita.isAfter(fin)) {
            System.out.println("⛔ La hora seleccionada está fuera del horario del médico.");
            return false;
        }

        // 4️ Verificar disponibilidad
        if (!isAvailable(appointmentDateTime)) {
            System.out.println("⛔ La hora ya está ocupada.");
            return false;
        }

        // 5️ Crear nueva cita
        Appointment appointment = new Appointment(service, appointmentDateTime, clientName);
        appointment.setMedico(medico);
        appointmentRepository.save(appointment);

        System.out.println("✅ Cita registrada correctamente para " + clientName);
        return true;
    }

    //  5. Obtener servicio por ID
    public ServiceVet getServiceById(int serviceId) {
        return serviceRepository.findById(serviceId).orElse(null);
    }

    //  6. Obtener todos los médicos
    public List<Medico> getAllMedicos() {
        return medicoRepository.findAll();
    }

    //  7. Obtener cita por ID (Usado por los Controllers)
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }
    
// dashboard

    // 8. Cuenta el número de citas que están pendientes (futuras)
    public Long contarCitasPendientes() {
        // Usa el método del repositorio con la fecha y hora actual
        return appointmentRepository.countByDateTimeAfter(LocalDateTime.now());
    }

    // 9. Devuelve un mapa de la cantidad de citas por mes para los gráficos.
    public Map<String, Long> getCitasPorMes() {
        // Llama a la consulta compleja del repositorio
        List<Object[]> monthlyCitasData = appointmentRepository.countAppointmentsPerMonth();
        Map<String, Long> data = new LinkedHashMap<>();
        
        // Procesar los resultados (se espera [año, mes, total])
        for (Object[] row : monthlyCitasData) {
            Integer monthIndex = (Integer) row[1]; //mes
            Long total = (Long) row[2];          // El conteo de citas
            
            // Convertir el índice numérico del mes a su nombre abreviado
            String monthName = getMonthName(monthIndex); 
            
            data.put(monthName, total);
        }
        return data;
    }

    /**
     * Método auxiliar para convertir el índice numérico del mes (1-12) a su nombre abreviado.
     */
    private String getMonthName(int month) {
        // Array de nombres de meses
        String[] names = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"};
        // El índice del mes en SQL (1-12) se mapea a nuestro array (0-11)
        if (month >= 1 && month <= 12) {
            return names[month - 1];
        }
        return "N/A";
    }
    public Double getIngresosCitasHoy() {
    LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
    LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
    
    // 1. Obtener todas las citas para hoy usando el nuevo método del repositorio
    List<Appointment> citasDeHoy = appointmentRepository.findByDateTimeBetween(startOfDay, endOfDay);
    
    // 2. Sumar el precio de los servicios de cada cita
    return citasDeHoy.stream()
            .mapToDouble(cita -> {
                // Asegura que la relación service no es nula antes de obtener el precio
                if (cita.getService() != null) {
                    return cita.getService().getPrice();
                }
                return 0.0;
            })
            .sum();
}
}