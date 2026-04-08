package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.service.AppointmentService;
import com.example.veterinariaPatitas.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;


@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final EmailService emailService;

    public AppointmentController(AppointmentService appointmentService, EmailService emailService) {
        this.appointmentService = appointmentService;//inyeccion de dependencias de servicio
        this.emailService = emailService;
    }

    /**
     * Muestra la vista del simulador de agendamiento.
     */
    @GetMapping("/simulador_citas")
    public String showAppointmentSimulator(Model model) {

    model.addAttribute("availableServices", appointmentService.getAvailableServices());

    model.addAttribute("bookedAppointments", appointmentService.getBookedAppointments());

    // Enviamos la lista completa de médicos
    model.addAttribute("allMedicos", appointmentService.getAllMedicos());

    LocalDate minDate = getNextBusinessDay(LocalDate.now().plusDays(1));

    model.addAttribute("minDate", minDate.format(DateTimeFormatter.ISO_DATE));

        return "simulador_citas"; // Retorna la vista
    }

    
    // Método para agendar una nueva cita
    @PostMapping("/book_appointment")
    public String bookAppointment(
        @RequestParam("serviceId") int serviceId,
        @RequestParam("medicoId") Long medicoId, 
        @RequestParam("date") LocalDate date,
        @RequestParam("time") LocalTime time,
        @RequestParam("clientDni") String clientDni,
        @RequestParam(value = "clientEmail", required = false) String clientEmail,
        @RequestParam("clientName") String clientName,
        Model model) {

        if (clientDni == null || !clientDni.matches("\\d{8}")) {
            model.addAttribute("message", "Error: ingresa un DNI válido de 8 dígitos.");
            model.addAttribute("messageType", "danger");
            return showAppointmentSimulator(model);
        }

        if (clientEmail != null && !clientEmail.isBlank()
                && !clientEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            model.addAttribute("message", "Error: ingresa un correo válido.");
            model.addAttribute("messageType", "danger");
            return showAppointmentSimulator(model);
        }

        if (isWeekend(date)) {
            model.addAttribute("message", "Error: la fecha seleccionada cae fin de semana. Las citas regulares se atienden de lunes a viernes.");
            model.addAttribute("messageType", "danger");
            return showAppointmentSimulator(model);
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

        // 1. Verificar disponibilidad de forma clara 
        boolean isAvailable = appointmentService.isAvailable(appointmentDateTime);

        if (!isAvailable) { 
            
            // Muestra mensaje de error (Ocupado)
            model.addAttribute("message", "Error: ¡Lo sentimos! La hora seleccionada (" + appointmentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + ") ya está ocupada. Por favor, elige otro horario.");
            model.addAttribute("messageType", "danger");
            
        } else {
            
            // 2. Procede a agendar la cita 
            boolean success = appointmentService.bookAppointment(serviceId, medicoId, appointmentDateTime, clientName, clientDni);


            if (success) {
                String baseMessage = "¡Éxito! Su cita ha sido agendada con el servicio de " + appointmentService.getServiceById(serviceId).getName() + " para el " + appointmentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + ".";

                if (clientEmail != null && !clientEmail.isBlank()) {
                    String medicoNombre = appointmentService.getMedicoById(medicoId)
                            .map(m -> m.getNombre())
                            .orElse("No asignado");
                    boolean sent = emailService.sendAppointmentConfirmation(
                            clientEmail,
                            clientName,
                            appointmentService.getServiceById(serviceId).getName(),
                            appointmentDateTime,
                            medicoNombre
                    );
                    if (sent) {
                        baseMessage += " Te enviamos la confirmación al correo.";
                    } else {
                        baseMessage += " No se pudo enviar el correo de confirmación.";
                    }
                }

                model.addAttribute("message", baseMessage);
                model.addAttribute("messageType", "success");//manda dato a la vista
            } else {
                model.addAttribute("message", "Error: No se pudo agendar la cita. El servicio, médico, u horario podría no ser válido.");
                model.addAttribute("messageType", "danger");
            }
        }
        
        // Recarga la vista con los datos actualizados
        return showAppointmentSimulator(model);
    }

    private LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate candidate = date;
        while (isWeekend(candidate)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
