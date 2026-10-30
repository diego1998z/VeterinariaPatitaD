package com.example.veterinariaPatitas.security;

import com.example.veterinariaPatitas.service.AppointmentService;
import com.example.veterinariaPatitas.service.SalesService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalesService salesService;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    @DisplayName("CP-12.1 - usuario anónimo es redirigido al login")
    void cp12_anonimo_dashboard_redirigeALogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        System.out.println("SECURITY_RESULT|scenario=anonymous_dashboard|expected=redirect_login|actual=redirect_login|status=PASS");
    }

    @Test
    @DisplayName("CP-12.2 - CLIENTE autenticado recibe 403 en dashboard")
    void cp12_cliente_dashboard_recibeForbidden() throws Exception {
        MockHttpSession session = login("cliente1", "123456", "/");
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isForbidden());
        System.out.println("SECURITY_RESULT|scenario=cliente_dashboard|expected=403|actual=403|status=PASS");
    }

    @Test
    @DisplayName("CP-12.3 - TRABAJADOR autenticado accede al dashboard")
    void cp12_trabajador_dashboard_accede() throws Exception {
        MockHttpSession session = login("trabajador", "123456", "/dashboard");
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
        System.out.println("SECURITY_RESULT|scenario=trabajador_dashboard|expected=200|actual=200|status=PASS");
    }

    private MockHttpSession login(String username, String password, String expectedRedirect) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("usuario", username)
                        .param("contrasena", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirect))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
