package com.example.veterinariaPatitas.config;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.model.HorarioMedico;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import com.example.veterinariaPatitas.repository.ProductRepository;
import com.example.veterinariaPatitas.repository.ServiceVetRepository;
import com.example.veterinariaPatitas.repository.HorarioMedicoRepository;
import com.example.veterinariaPatitas.repository.AppointmentRepository;
import org.slf4j.Logger; //registrar mensajes en consola
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

@Configuration //una fuente de definicion de beans
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
     //inicializa para poder mostrar los datos 
    @Bean
    CommandLineRunner initDatabase(//define que el bean sera usado cuando la aplicacion inicie
        ServiceVetRepository serviceRepo,
        MedicoRepository medicoRepo,
        ProductRepository productRepo,
        HorarioMedicoRepository horarioMedicoRepository,
        AppointmentRepository appointmentRepository) {
        return args -> {
            // Limpia duplicados por nombre (re-asigna citas/horarios al médico con menor ID)
            limpiarDuplicadosMedicos(medicoRepo, horarioMedicoRepository, appointmentRepository);

            List<Medico> medicosExistentes = medicoRepo.findAll();
            java.util.function.Function<String, String> normalizar = nombre ->
                    nombre == null ? "" : nombre.trim().toLowerCase();
            java.util.function.Predicate<String> existeMedico = nombre -> {
                String n = normalizar.apply(nombre);
                return medicosExistentes.stream()
                        .anyMatch(m -> normalizar.apply(m.getNombre()).equals(n));
            };

            // Crear servicios (si no existen ya y evita duplicados)
            ServiceVet consultaGeneral = serviceRepo.findAll().stream()
                    .filter(s -> "Consulta General".equals(s.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ServiceVet s = new ServiceVet();
                        s.setName("Consulta General");
                        s.setDescription("Revisión general de la mascota");
                        s.setPrice(50.0);
                        s.setDuration("30min");
                        return serviceRepo.save(s);
                    });

            ServiceVet vacunacion = serviceRepo.findAll().stream()
                    .filter(s -> "Vacunación".equals(s.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ServiceVet s = new ServiceVet();
                        s.setName("Vacunación");
                        s.setDescription("Servicio de vacunación y refuerzos");
                        s.setPrice(30.0);
                        s.setDuration("15min");
                        return serviceRepo.save(s);
                    });

            ServiceVet cirugia = serviceRepo.findAll().stream()
                    .filter(s -> "Cirugía".equals(s.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ServiceVet s = new ServiceVet();
                        s.setName("Cirugía");
                        s.setDescription("Procedimientos quirúrgicos");
                        s.setPrice(250.0);
                        s.setDuration("120min");
                        return serviceRepo.save(s);
                    });

            // Crear médicos
            if (!existeMedico.test("Juan")) {
                Medico medico1 = new Medico();
                medico1.setNombre("Juan");
                medico1.setEspecialidad("Medicina General");
                medico1.setHorarioInicio(LocalTime.of(9, 0));
                medico1.setHorarioFin(LocalTime.of(17, 0));
                medico1.setServicio(consultaGeneral);
                medicoRepo.save(medico1);
                medicosExistentes.add(medico1);
            }

            if (!existeMedico.test("María")) {
                Medico medico2 = new Medico();
                medico2.setNombre("María");
                medico2.setEspecialidad("Cirujana");
                medico2.setHorarioInicio(LocalTime.of(10, 0));
                medico2.setHorarioFin(LocalTime.of(18, 0));
                medico2.setServicio(cirugia);
                medicoRepo.save(medico2);
                medicosExistentes.add(medico2);
            }

            if (!existeMedico.test("Carlos")) {
                Medico medico3 = new Medico();
                medico3.setNombre("Carlos");
                medico3.setEspecialidad("Medicina General");
                medico3.setHorarioInicio(LocalTime.of(8, 0));
                medico3.setHorarioFin(LocalTime.of(16, 0));
                medico3.setServicio(consultaGeneral);
                medicoRepo.save(medico3);
                medicosExistentes.add(medico3);
            }

            // Crear servicios adicionales que puedan venir desde data.sql
            ServiceVet grooming = serviceRepo.findAll().stream()
                    .filter(s -> "Grooming".equals(s.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ServiceVet s = new ServiceVet();
                        s.setName("Grooming");
                        s.setDescription("Servicio de peluquería y baño");
                        s.setPrice(40.0);
                        s.setDuration("45min");
                        return serviceRepo.save(s);
                    });

            ServiceVet emergencia = serviceRepo.findAll().stream()
                    .filter(s -> "Emergencia".equals(s.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ServiceVet s = new ServiceVet();
                        s.setName("Emergencia");
                        s.setDescription("Atención de emergencias 24/7");
                        s.setPrice(0.0);
                        s.setDuration("varios");
                        return serviceRepo.save(s);
                    });
            log.debug("Servicio emergencia listo: {}", emergencia.getName());

            // Añadir médicos extra que aparecen en la sección "Sobre Nosotros"
            // Solo los creamos si no existen por nombre
            if (!existeMedico.test("Aimed Gonzales")) {
                Medico m = new Medico();
                m.setNombre("Aimed Gonzales");
                m.setEspecialidad("Cirugía y Diagnóstico por Imagen");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(cirugia);
                medicoRepo.save(m);
                medicosExistentes.add(m);
            }

            if (!existeMedico.test("Angel Shinno")) {
                Medico m = new Medico();
                m.setNombre("Angel Shinno");
                m.setEspecialidad("Vacunación, Desparasitación y Nutrición");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(vacunacion);
                medicoRepo.save(m);
                medicosExistentes.add(m);
            }

            if (!existeMedico.test("Norvan Sifuentes")) {
                Medico m = new Medico();
                m.setNombre("Norvan Sifuentes");
                m.setEspecialidad("Dermatología");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(consultaGeneral);
                medicoRepo.save(m);
                medicosExistentes.add(m);
            }

            if (!existeMedico.test("Angel Aguirre")) {
                Medico m = new Medico();
                m.setNombre("Angel Aguirre");
                m.setEspecialidad("Traumatología");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(consultaGeneral);
                medicoRepo.save(m);
                medicosExistentes.add(m);
            }

            if (!existeMedico.test("Mario Flores")) {
                Medico m = new Medico();
                m.setNombre("Mario Flores");
                m.setEspecialidad("Dermatología");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(consultaGeneral);
                medicoRepo.save(m);
                medicosExistentes.add(m);
            }

            if (!existeMedico.test("Rolando Campos")) {
                Medico m = new Medico();
                m.setNombre("Rolando Campos");
                m.setEspecialidad("Laboratorio Clínico y Peluquería Canina y Felina");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(grooming);
                medicoRepo.save(m);
                medicosExistentes.add(m);
            }

            // Crear horarios base (Lunes a Viernes) si el médico no tiene horarios
            medicoRepo.findAll().forEach(m -> {
                if (horarioMedicoRepository.countByMedicoId(m.getId()) == 0) {
                    for (DayOfWeek day : new DayOfWeek[] {
                            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
                        HorarioMedico h = new HorarioMedico();
                        h.setMedico(m);
                        h.setDiaSemana(day);
                        h.setHoraInicio(m.getHorarioInicio());
                        h.setHoraFin(m.getHorarioFin());
                        horarioMedicoRepository.save(h);
                    }
                }
            });

            // Crear productos iniciales (si no existen ya)
            long existing = productRepo.count();
            log.info("Existing productos count: {}", existing);
            if (existing == 0) {
                Product p1 = new Product();
                p1.setName("Shampoo Antipulgas para Perro");
                p1.setPrice(50.00);
                p1.setStock(20);
                p1.setUnit("Frasco 250ml");
                productRepo.save(p1);

                Product p2 = new Product();
                p2.setName("Cepillo para Peinar");
                p2.setPrice(24.50);
                p2.setStock(15);
                p2.setUnit("Unidad");
                productRepo.save(p2);

                Product p3 = new Product();
                p3.setName("Manta para Dormir");
                p3.setPrice(89.00);
                p3.setStock(14);
                p3.setUnit("Unidad");
                productRepo.save(p3);

                Product p4 = new Product();
                p4.setName("Camita para Perro - Cozy");
                p4.setPrice(159.00);
                p4.setStock(17);
                p4.setUnit("Unidad");
                productRepo.save(p4);

                Product p5 = new Product();
                p5.setName("Arenero para Gatos - Compacto");
                p5.setPrice(79.90);
                p5.setStock(10);
                p5.setUnit("Unidad");
                productRepo.save(p5);

                Product p6 = new Product();
                p6.setName("Comida Húmeda para Gato - Lata 400g");
                p6.setPrice(12.50);
                p6.setStock(5);
                p6.setUnit("Lata 400g");
                productRepo.save(p6);

                log.info("Seeded default products into productos table (6 items)");//mensaje de exito en la consola
            } else {
                log.info("Skipped seeding products because table is not empty");//error
            }
        };
    }

    private void limpiarDuplicadosMedicos(MedicoRepository medicoRepo,
                                          HorarioMedicoRepository horarioMedicoRepository,
                                          AppointmentRepository appointmentRepository) {
        List<Medico> medicos = medicoRepo.findAll();
        Map<String, List<Medico>> porNombre = medicos.stream()
                .collect(Collectors.groupingBy(m ->
                        m.getNombre() == null ? "" : m.getNombre().trim().toLowerCase()
                ));

        porNombre.forEach((key, lista) -> {
            if (lista.size() <= 1) return;

            lista.sort(Comparator.comparing(Medico::getId));
            Medico keep = lista.get(0);

            for (int i = 1; i < lista.size(); i++) {
                Medico dup = lista.get(i);

                // Re-asignar citas
                List<com.example.veterinariaPatitas.model.Appointment> citas =
                        appointmentRepository.findByMedicoId(dup.getId());
                if (!citas.isEmpty()) {
                    citas.forEach(c -> c.setMedico(keep));
                    appointmentRepository.saveAll(citas);
                }

                // Re-asignar horarios
                List<HorarioMedico> horarios = horarioMedicoRepository.findByMedicoId(dup.getId());
                if (!horarios.isEmpty()) {
                    horarios.forEach(h -> h.setMedico(keep));
                    horarioMedicoRepository.saveAll(horarios);
                }

                medicoRepo.delete(dup);
            }
        });
    }
}
