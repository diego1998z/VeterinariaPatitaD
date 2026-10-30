package com.example.veterinariaPatitas.performance;

import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.repository.AppointmentRepository;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import com.example.veterinariaPatitas.repository.ServiceVetRepository;
import com.example.veterinariaPatitas.service.ApiPeruService;
import com.example.veterinariaPatitas.service.AppointmentService;
import com.example.veterinariaPatitas.service.SalesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PerformanceTest {

    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASURED_ITERATIONS = 20;
    private static final double THRESHOLD_MS = 3000.0;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private SalesService salesService;

    @Autowired
    private ServiceVetRepository serviceVetRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private ApiPeruService apiPeruService;

    @Test
    @DisplayName("E-R01 - GET /simulador_citas responde dentro de 3 segundos")
    void er01_simuladorCitas_respondeDentroDelUmbral() throws Exception {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            mockMvc.perform(get("/simulador_citas"))
                    .andExpect(status().isOk());
        }

        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            long start = System.nanoTime();
            mockMvc.perform(get("/simulador_citas"))
                    .andExpect(status().isOk());
            samples.add(elapsedMs(start));
        }

        Metrics metrics = metrics("GET /simulador_citas", samples);
        printMetrics("E-R01", metrics);
        assertThat(metrics.maximumMs()).isLessThanOrEqualTo(THRESHOLD_MS);
    }

    @Test
    @DisplayName("E-R02 - AppointmentService.bookAppointment registra dentro de 3 segundos")
    void er02_bookAppointment_respondeDentroDelUmbral() {
        ServiceVet service = serviceVetRepository.findAll().stream()
                .filter(candidate -> "Consulta General".equals(candidate.getName()))
                .findFirst()
                .orElseThrow();
        Medico medico = medicoRepository.findAll().stream()
                .filter(candidate -> "Juan".equals(candidate.getNombre()))
                .findFirst()
                .orElseThrow();
        LocalDateTime baseDateTime = LocalDateTime.of(2030, 1, 14, 10, 0);
        int[] slot = {0};

        BooleanSupplier operation = () -> appointmentService.bookAppointment(
                service.getId(), medico.getId(), baseDateTime.plusMinutes(slot[0]++),
                "Cliente Rendimiento " + slot[0]);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            assertThat(operation.getAsBoolean()).isTrue();
        }

        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            long start = System.nanoTime();
            boolean registered = operation.getAsBoolean();
            samples.add(elapsedMs(start));
            assertThat(registered).isTrue();
        }

        Metrics metrics = metrics("AppointmentService.bookAppointment()", samples);
        printMetrics("E-R02", metrics);
        assertThat(metrics.maximumMs()).isLessThanOrEqualTo(THRESHOLD_MS);
        assertThat(appointmentRepository.count()).isGreaterThanOrEqualTo(WARMUP_ITERATIONS + MEASURED_ITERATIONS);
    }

    @Test
    @DisplayName("E-R03 - SalesService.calculateSubtotal calcula dentro de 3 segundos")
    void er03_calculateSubtotal_respondeDentroDelUmbral() {
        List<Product> cart = buildTenProductCart();
        double expectedSubtotal = 10 * 19.90 * 2;
        DoubleSupplier operation = () -> salesService.calculateSubtotal(cart);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            assertThat(operation.getAsDouble()).isCloseTo(expectedSubtotal,
                    org.assertj.core.data.Offset.offset(0.001));
        }

        List<Double> samples = new ArrayList<>();
        for (int i = 0; i < MEASURED_ITERATIONS; i++) {
            long start = System.nanoTime();
            double subtotal = operation.getAsDouble();
            samples.add(elapsedMs(start));
            assertThat(subtotal).isCloseTo(expectedSubtotal,
                    org.assertj.core.data.Offset.offset(0.001));
        }

        Metrics metrics = metrics("SalesService.calculateSubtotal()", samples);
        printMetrics("E-R03", metrics);
        System.out.printf("PERF_RESULT|evidence=E-R03|products=10|unit_price=19.90|quantity=2|subtotal=%.2f%n",
                expectedSubtotal);
        assertThat(metrics.maximumMs()).isLessThanOrEqualTo(THRESHOLD_MS);
    }

    private List<Product> buildTenProductCart() {
        List<Product> cart = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Product product = new Product(i, "Producto rendimiento " + i, 19.90, 100, "Unidad", "PEN");
            product.setQuantity(2);
            cart.add(product);
        }
        return cart;
    }

    private static double elapsedMs(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static Metrics metrics(String operation, List<Double> samples) {
        Collections.sort(samples);
        double average = samples.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        int p95Index = Math.max(0, (int) Math.ceil(samples.size() * 0.95) - 1);
        return new Metrics(operation, average, samples.get(p95Index), samples.getLast());
    }

    private static void printMetrics(String evidence, Metrics metrics) {
        System.out.printf(
                "PERF_METRIC|evidence=%s|operation=%s|warmups=%d|samples=%d|average_ms=%.3f|p95_ms=%.3f|max_ms=%.3f|threshold_ms=%.0f%n",
                evidence, metrics.operation(), WARMUP_ITERATIONS, MEASURED_ITERATIONS,
                metrics.averageMs(), metrics.p95Ms(), metrics.maximumMs(), THRESHOLD_MS);
    }

    private record Metrics(String operation, double averageMs, double p95Ms, double maximumMs) {}
}
