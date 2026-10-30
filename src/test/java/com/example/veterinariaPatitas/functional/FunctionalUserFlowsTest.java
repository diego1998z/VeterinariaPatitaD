package com.example.veterinariaPatitas.functional;

import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.model.Sale;
import com.example.veterinariaPatitas.repository.AppointmentRepository;
import com.example.veterinariaPatitas.repository.MedicoRepository;
import com.example.veterinariaPatitas.repository.ProductRepository;
import com.example.veterinariaPatitas.repository.SaleRepository;
import com.example.veterinariaPatitas.repository.ServiceVetRepository;
import com.example.veterinariaPatitas.service.ApiPeruService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FunctionalUserFlowsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private ServiceVetRepository serviceVetRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @MockitoBean
    private ApiPeruService apiPeruService;

    @Test
    @DisplayName("CP-07 - agregar dos productos muestra subtotal S/ 100.00")
    void cp07_agregarDosProductos_muestraSubtotalCorrecto() throws Exception {
        Product product = productRepository.findAll().stream()
                .filter(candidate -> "Shampoo Antipulgas para Perro".equals(candidate.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(product.getPrice()).isEqualTo(50.00);
        double expectedSubtotal = product.getPrice() * 2;
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/add_to_cart")
                        .session(session)
                        .param("productId", String.valueOf(product.getId()))
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/simulador_ventas"));

        MvcResult result = mockMvc.perform(get("/simulador_ventas").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("simulador_ventas"))
                .andExpect(model().attribute("subtotal", expectedSubtotal))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shampoo Antipulgas para Perro")))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Product> cart = (List<Product>) result.getModelAndView().getModel().get("cart");

        assertThat(cart).hasSize(1);
        assertThat(cart.getFirst().getQuantity()).isEqualTo(2);
        assertThat(cart.getFirst().getEffectivePricePen()).isEqualTo(50.00);
    }

    @Test
    @DisplayName("CP-08 - DNI de siete dígitos rechaza la cita sin persistirla")
    void cp08_dniDeSieteDigitos_rechazaCitaSinPersistirla() throws Exception {
        long appointmentsBefore = appointmentRepository.count();

        mockMvc.perform(post("/book_appointment")
                        .param("serviceId", "1")
                        .param("medicoId", "1")
                        .param("date", "2030-01-14")
                        .param("time", "10:00")
                        .param("clientDni", "1234567")
                        .param("clientName", "Cliente Funcional"))
                .andExpect(status().isOk())
                .andExpect(view().name("simulador_citas"))
                .andExpect(model().attribute("message", "Error: ingresa un DNI válido de 8 dígitos."))
                .andExpect(model().attribute("messageType", "danger"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error: ingresa un DNI válido de 8 dígitos.")));

        assertThat(appointmentRepository.count()).isEqualTo(appointmentsBefore);
    }

    @Test
    @DisplayName("CP-09 - datos válidos registran y persisten la cita")
    void cp09_datosValidos_registraCitaExitosamente() throws Exception {
        var service = serviceVetRepository.findAll().stream()
                .filter(candidate -> "Consulta General".equals(candidate.getName()))
                .findFirst()
                .orElseThrow();
        var medico = medicoRepository.findAll().stream()
                .filter(candidate -> "Juan".equals(candidate.getNombre()))
                .findFirst()
                .orElseThrow();
        long appointmentsBefore = appointmentRepository.count();

        mockMvc.perform(post("/book_appointment")
                        .param("serviceId", String.valueOf(service.getId()))
                        .param("medicoId", String.valueOf(medico.getId()))
                        .param("date", "2030-01-14")
                        .param("time", "10:00")
                        .param("clientDni", "12345678")
                        .param("clientName", "Cliente Funcional Válido"))
                .andExpect(status().isOk())
                .andExpect(view().name("simulador_citas"))
                .andExpect(model().attribute("messageType", "success"))
                .andExpect(model().attribute("message", org.hamcrest.Matchers.containsString("¡Éxito! Su cita ha sido agendada")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("¡Éxito! Su cita ha sido agendada")));

        assertThat(appointmentRepository.count()).isEqualTo(appointmentsBefore + 1);
        var appointment = appointmentRepository.findAll().stream()
                .filter(candidate -> "12345678".equals(candidate.getClientDni()))
                .findFirst()
                .orElseThrow();
        assertThat(appointment.getClientName()).isEqualTo("Cliente Funcional Válido");
        assertThat(appointment.getDateTime().toString()).isEqualTo("2030-01-14T10:00");
        assertThat(appointment.getMedico().getId()).isEqualTo(medico.getId());
        assertThat(appointment.getService().getId()).isEqualTo(service.getId());
    }

    @Test
    @DisplayName("CP-10 - finalizar venta persiste total, descuenta stock y limpia carrito")
    void cp10_finalizarVenta_persisteTotalDescuentaStockYLimpiaCarrito() throws Exception {
        Product product = productRepository.findAll().stream()
                .filter(candidate -> "Shampoo Antipulgas para Perro".equals(candidate.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(product.getPrice()).isEqualTo(50.00);
        double expectedSubtotal = product.getPrice() * 2;
        double expectedTax = expectedSubtotal * 0.18;
        double expectedTotal = expectedSubtotal + expectedTax;
        int stockBefore = product.getStock();
        long salesBefore = saleRepository.count();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/add_to_cart")
                        .session(session)
                        .param("productId", String.valueOf(product.getId()))
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/simulador_ventas"));

        MvcResult finalizeResult = mockMvc.perform(post("/finalize_sale").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/simulador_ventas"))
                .andReturn();

        Map<String, ?> flash = finalizeResult.getFlashMap();
        assertThat(flash.get("message")).isEqualTo("¡Venta confirmada!.");
        assertThat((Double) flash.get("subtotal")).isCloseTo(expectedSubtotal, org.assertj.core.data.Offset.offset(0.001));
        assertThat((Double) flash.get("tax")).isCloseTo(expectedTax, org.assertj.core.data.Offset.offset(0.001));
        assertThat((Double) flash.get("total")).isCloseTo(expectedTotal, org.assertj.core.data.Offset.offset(0.001));

        assertThat(saleRepository.count()).isEqualTo(salesBefore + 1);
        Sale sale = saleRepository.findAll().getLast();
        assertThat(sale.getTotalAmount()).isCloseTo(expectedTotal, org.assertj.core.data.Offset.offset(0.001));
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(stockBefore - 2);

        MvcResult refreshed = mockMvc.perform(get("/simulador_ventas").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("simulador_ventas"))
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Product> cart = (List<Product>) refreshed.getModelAndView().getModel().get("cart");
        assertThat(cart).isEmpty();
    }
}
