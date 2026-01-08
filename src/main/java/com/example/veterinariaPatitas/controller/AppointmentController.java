package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Appointment; 
import com.example.veterinariaPatitas.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;//inyeccion de dependencias de servicio
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

    LocalDate minDate = LocalDate.now().plusDays(1);

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
        @RequestParam("clientName") String clientName,
        Model model) {


        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

        // 1. Verificar disponibilidad de forma clara 
        boolean isAvailable = appointmentService.isAvailable(appointmentDateTime);

        if (!isAvailable) { 
            
            // Muestra mensaje de error (Ocupado)
            model.addAttribute("message", "Error: ¡Lo sentimos! La hora seleccionada (" + appointmentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + ") ya está ocupada. Por favor, elige otro horario.");
            model.addAttribute("messageType", "danger");
            
        } else {
            
            // 2. Procede a agendar la cita 
            boolean success = appointmentService.bookAppointment(serviceId, medicoId, appointmentDateTime, clientName);


            if (success) {
                model.addAttribute("message", "¡Éxito! Su cita ha sido agendada con el servicio de " + appointmentService.getServiceById(serviceId).getName() + " para el " + appointmentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + ".");
                model.addAttribute("messageType", "success");//manda dato a la vista
            } else {
                model.addAttribute("message", "Error: No se pudo agendar la cita. El servicio, médico, u horario podría no ser válido.");
                model.addAttribute("messageType", "danger");
            }
        }
        
        // Recarga la vista con los datos actualizados
        return showAppointmentSimulator(model);
    }
    
    
}
