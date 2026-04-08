package com.example.veterinariaPatitas.config;

import com.example.veterinariaPatitas.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API y formularios de este proyecto no usan token CSRF en frontend.
                .csrf(csrf -> csrf.disable())

                // Se mantiene sesion para login web (formLogin) y tambien JWT para /api/**.
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // Matriz principal de permisos por ruta.
                .authorizeHttpRequests(auth -> auth

                        // Rutas publicas: paginas abiertas, login/registro y API publica.
                        .requestMatchers(
                                "/", "/login", "/logout", "/register",
                                "/css/**", "/js/**", "/img/**", "/favicon.ico",
                                "/sobre_nosotros", "/adoptar", "/envia_mensaje",
                                "/trabaja", "/upload", "/postular",
                                "/simulador_citas", "/book_appointment",
                                "/simulador_ventas",
                                "/api/auth/login", "/api/auth/register",
                                "/api/public/**"
                        ).permitAll()

                        // Panel administrativo solo para rol TRABAJADOR.
                        .requestMatchers("/dashboard", "/admin/**")
                        .hasAuthority("TRABAJADOR")

                        // Cualquier endpoint /api/** (no publico) exige autenticacion.
                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest().permitAll()
                )

                // Login web tradicional (usuario/contrasena) con redireccion por rol.
                .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("usuario")
                    .passwordParameter("contrasena")
                    .successHandler((request, response, authentication) -> {
                        boolean isTrabajador = authentication.getAuthorities().stream()
                                .anyMatch(authority -> "TRABAJADOR".equals(authority.getAuthority()));
                        response.sendRedirect(isTrabajador ? "/dashboard" : "/");
                    })
                    .failureUrl("/login?error=true")
                    .permitAll()
                )

                // Cierre de sesion para interfaz web.
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                );

        // El filtro JWT se ejecuta antes del filtro de usuario/clave de Spring.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Expone AuthenticationManager para endpoints de login API (/api/auth/login).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // Encoder usado en registro, cambio de clave y comparacion en login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
