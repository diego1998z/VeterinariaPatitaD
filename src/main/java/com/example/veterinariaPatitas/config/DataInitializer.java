package com.example.veterinariaPatitas.config;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import com.example.veterinariaPatitas.repository.ProductRepository;
import com.example.veterinariaPatitas.repository.ServiceVetRepository;
import org.slf4j.Logger; //registrar mensajes en consola
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalTime;

@Configuration //una fuente de definicion de beans
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
     //inicializa para poder mostrar los datos 
    @Bean
    CommandLineRunner initDatabase(//define que el bean sera usado cuando la aplicacion inicie
        ServiceVetRepository serviceRepo,
        MedicoRepository medicoRepo,
        ProductRepository productRepo) {
        return args -> {
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
            Medico medico1 = new Medico();
            medico1.setNombre("Juan");
            medico1.setEspecialidad("Medicina General");
            medico1.setHorarioInicio(LocalTime.of(9, 0));
            medico1.setHorarioFin(LocalTime.of(17, 0));
            medico1.setServicio(consultaGeneral);
            medicoRepo.save(medico1);

            Medico medico2 = new Medico();
            medico2.setNombre("María");
            medico2.setEspecialidad("Cirujana");
            medico2.setHorarioInicio(LocalTime.of(10, 0));
            medico2.setHorarioFin(LocalTime.of(18, 0));
            medico2.setServicio(cirugia);
            medicoRepo.save(medico2);

            Medico medico3 = new Medico();
            medico3.setNombre("Carlos");
            medico3.setEspecialidad("Medicina General");
            medico3.setHorarioInicio(LocalTime.of(8, 0));
            medico3.setHorarioFin(LocalTime.of(16, 0));
            medico3.setServicio(consultaGeneral);
            medicoRepo.save(medico3);

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

            // Añadir médicos extra que aparecen en la sección "Sobre Nosotros"
            // Solo los creamos si no existen por nombre
            if (medicoRepo.findAll().stream().noneMatch(m -> "Aimed Gonzales".equals(m.getNombre()))) {
                Medico m = new Medico();
                m.setNombre("Aimed Gonzales");
                m.setEspecialidad("Cirugía y Diagnóstico por Imagen");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(cirugia);
                medicoRepo.save(m);
            }

            if (medicoRepo.findAll().stream().noneMatch(m -> "Angel Shinno".equals(m.getNombre()))) {
                Medico m = new Medico();
                m.setNombre("Angel Shinno");
                m.setEspecialidad("Vacunación, Desparasitación y Nutrición");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(vacunacion);
                medicoRepo.save(m);
            }

            if (medicoRepo.findAll().stream().noneMatch(m -> "Norvan Sifuentes".equals(m.getNombre()))) {
                Medico m = new Medico();
                m.setNombre("Norvan Sifuentes");
                m.setEspecialidad("Dermatología");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(consultaGeneral);
                medicoRepo.save(m);
            }

            if (medicoRepo.findAll().stream().noneMatch(m -> "Angel Aguirre".equals(m.getNombre()))) {
                Medico m = new Medico();
                m.setNombre("Angel Aguirre");
                m.setEspecialidad("Traumatología");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(consultaGeneral);
                medicoRepo.save(m);
            }

            if (medicoRepo.findAll().stream().noneMatch(m -> "Mario Flores".equals(m.getNombre()))) {
                Medico m = new Medico();
                m.setNombre("Mario Flores");
                m.setEspecialidad("Dermatología");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(consultaGeneral);
                medicoRepo.save(m);
            }

            if (medicoRepo.findAll().stream().noneMatch(m -> "Rolando Campos".equals(m.getNombre()))) {
                Medico m = new Medico();
                m.setNombre("Rolando Campos");
                m.setEspecialidad("Laboratorio Clínico y Peluquería Canina y Felina");
                m.setHorarioInicio(LocalTime.of(9, 0));
                m.setHorarioFin(LocalTime.of(17, 0));
                m.setServicio(grooming);
                medicoRepo.save(m);
            }

            // Crear productos iniciales (si no existen ya)
            long existing = productRepo.count();
            log.info("Existing productos count: {}", existing);
            if (existing == 0) {
                Product p1 = new Product();
                p1.setName("Shampoo Antipulgas para Perro");
                p1.setPrice(49.90);
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
}