package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.repository.AppointmentRepository;
import com.example.veterinariaPatitas.repository.HorarioMedicoRepository;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import com.example.veterinariaPatitas.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private HorarioMedicoRepository horarioMedicoRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void cp04_isAvailable_conHorarioOcupado_retornaFalse() {
        LocalDateTime dateTime = LocalDateTime.of(2030, 1, 14, 10, 0);
        when(appointmentRepository.existsByDateTime(dateTime)).thenReturn(true);

        boolean available = appointmentService.isAvailable(dateTime);

        assertFalse(available);
    }

    @Test
    void cp05_isAvailable_conHorarioLibre_retornaTrue() {
        LocalDateTime dateTime = LocalDateTime.of(2030, 1, 14, 11, 0);
        when(appointmentRepository.existsByDateTime(dateTime)).thenReturn(false);

        boolean available = appointmentService.isAvailable(dateTime);

        assertTrue(available);
    }
}
