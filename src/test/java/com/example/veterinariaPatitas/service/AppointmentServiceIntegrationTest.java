package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.repository.AppointmentRepository;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import com.example.veterinariaPatitas.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentServiceIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void cp06_bookAppointment_mismoHorario_registraPrimeraYRechazaSegunda() {
        ServiceVet service = new ServiceVet(null, "Consulta general", "Evaluacion veterinaria", 50.0, "30 min");
        service = serviceRepository.save(service);

        Medico medico = new Medico("Dra. Ana Torres", "Medicina general",
                LocalTime.of(8, 0), LocalTime.of(17, 0));
        medico.setServicio(service);
        medico = medicoRepository.save(medico);

        LocalDateTime dateTime = LocalDateTime.of(2030, 1, 14, 10, 0);

        boolean firstBooking = appointmentService.bookAppointment(
                service.getId(), medico.getId(), dateTime, "Juana Perez", "45678912");
        boolean duplicateBooking = appointmentService.bookAppointment(
                service.getId(), medico.getId(), dateTime, "Luis Gomez", "78912345");

        assertTrue(firstBooking);
        assertFalse(duplicateBooking);
        assertEquals(1, appointmentRepository.count());
    }
}
