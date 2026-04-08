package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Appointment;
import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.dto.AppointmentListDTO;
import com.example.veterinariaPatitas.repository.ServiceRepository;
import com.example.veterinariaPatitas.repository.AppointmentRepository;
import com.example.veterinariaPatitas.repository.MedicoRepository;//obtiene datos de la bd
import com.example.veterinariaPatitas.repository.HorarioMedicoRepository;
import com.example.veterinariaPatitas.model.HorarioMedico;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.time.LocalDateTime;//fecha y hora
import java.time.LocalDate;
import java.time.DayOfWeek;
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

    @Autowired
    private HorarioMedicoRepository horarioMedicoRepository;

    // 1. Obtener lista de servicios (sin filtrar por médicos)
    // Esto permite que el combo "Servicio Requerido" refleje altas/bajas de servicios
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
        return bookAppointment(serviceId, medicoId, appointmentDateTime, clientName, null);
    }

    public boolean bookAppointment(int serviceId, Long medicoId, LocalDateTime appointmentDateTime, String clientName, String clientDni) {
        if (medicoId == null) return false;
        String normalizedDni = normalizeDni(clientDni);
        if (clientDni != null && !clientDni.isBlank() && normalizedDni == null) return false;

        // 1️ Buscar servicio
        ServiceVet service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) return false;

        // 2️ Buscar médico
        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        if (medico == null) return false;

        // 3️ Validar si la hora y día/fecha están dentro del horario del médico
        if (!isMedicoDisponible(medico, appointmentDateTime)) {
            System.out.println(" La cita no está dentro del horario/día del médico.");
            return false;
        }

        // 4️ Verificar disponibilidad
        if (!isAvailable(appointmentDateTime)) {
            System.out.println(" La hora ya está ocupada.");
            return false;
        }

        // 5️ Crear nueva cita
        Appointment appointment = new Appointment(service, appointmentDateTime, clientName);
        appointment.setClientDni(normalizedDni);
        appointment.setMedico(medico);
        appointment.setServicioNombre(service.getName());
        appointment.setServicioPrecio(service.getPrice());
        appointmentRepository.save(appointment);

        System.out.println(" Cita registrada correctamente para " + clientName);
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

    public Optional<Medico> getMedicoById(Long medicoId) {
        if (medicoId == null) return Optional.empty();
        return medicoRepository.findById(medicoId);
    }

    //  7. Obtener cita por ID (Usado por los Controllers)
    public Optional<Appointment> findById(Long id) {
        if (id == null) return Optional.empty();
        return appointmentRepository.findById(id);
    }

    //  8. Listado con DTO para vistas administrativas (JOIN de varias tablas)
    public List<AppointmentListDTO> getAppointmentList() {
        return appointmentRepository.findAppointmentList();
    }

    //  9. Actualizar una cita manteniendo la lógica de disponibilidad
    public boolean updateAppointment(Long id, int serviceId, Long medicoId,
                                     LocalDateTime newDateTime, String clientName) {
        return updateAppointment(id, serviceId, medicoId, newDateTime, clientName, null);
    }

    public boolean updateAppointment(Long id, int serviceId, Long medicoId,
                                     LocalDateTime newDateTime, String clientName, String clientDni) {
        if (id == null || medicoId == null) return false;

        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null) return false;

        ServiceVet service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) return false;

        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        if (medico == null) return false;

        // Validar horario del médico
        if (!isMedicoDisponible(medico, newDateTime)) {
            return false;
        }

        // Validar disponibilidad (permitir misma fecha/hora de la cita actual)
        if (!newDateTime.equals(appointment.getDateTime()) && !isAvailable(newDateTime)) {
            return false;
        }

        appointment.setService(service);
        appointment.setMedico(medico);
        appointment.setDateTime(newDateTime);
        appointment.setClientName(clientName);
        if (clientDni != null) {
            String normalizedDni = normalizeDni(clientDni);
            if (!clientDni.isBlank() && normalizedDni == null) return false;
            appointment.setClientDni(normalizedDni);
        }
        appointment.setServicioNombre(service.getName());
        appointment.setServicioPrecio(service.getPrice());
        appointmentRepository.save(appointment);

        return true;
    }

    public void deleteAppointment(Long id) {
        if (id == null) return;
        appointmentRepository.deleteById(id);
    }

    /**
     * Verifica si un médico está disponible en la fecha y hora indicadas.
     * Usa HorarioMedico si existe; caso contrario usa horarioInicio/horarioFin del médico.
     */
    private boolean isMedicoDisponible(Medico medico, LocalDateTime dateTime) {
        if (medico == null || dateTime == null) return false;
        if (medico.getId() == null) return false;

        List<HorarioMedico> horarios = horarioMedicoRepository.findByMedicoId(medico.getId());
        LocalTime horaCita = dateTime.toLocalTime();
        LocalDate fecha = dateTime.toLocalDate();
        DayOfWeek dia = dateTime.getDayOfWeek();

        if (horarios != null && !horarios.isEmpty()) {
            return horarios.stream().anyMatch(h -> {
                boolean diaValido = false;
                if (h.getFecha() != null) {
                    diaValido = h.getFecha().equals(fecha);
                } else if (h.getDiaSemana() != null) {
                    diaValido = h.getDiaSemana().equals(dia);
                }
                if (!diaValido) return false;
                if (h.getHoraInicio() == null || h.getHoraFin() == null) return false;
                return !horaCita.isBefore(h.getHoraInicio()) && !horaCita.isAfter(h.getHoraFin());
            });
        }

        LocalTime inicio = medico.getHorarioInicio();
        LocalTime fin = medico.getHorarioFin();
        if (inicio == null || fin == null) return false;
        return !horaCita.isBefore(inicio) && !horaCita.isAfter(fin);
    }

    private String normalizeDni(String clientDni) {
        if (clientDni == null) return null;
        String value = clientDni.trim();
        if (value.isEmpty()) return null;
        if (!value.matches("\\d{8}")) return null;
        return value;
    }
    
// dashboard

    // 10. Cuenta el número de citas que están pendientes (futuras)
    public Long contarCitasPendientes() {
        // Usa el método del repositorio con la fecha y hora actual
        return appointmentRepository.countByDateTimeAfter(LocalDateTime.now());
    }

    // 11. Devuelve un mapa de la cantidad de citas por mes para los gráficos.
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
