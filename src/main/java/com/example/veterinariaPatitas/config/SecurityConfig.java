package com.example.veterinariaPatitas.config;

import com.example.veterinariaPatitas.security.JwtFilter;//filtro para proteger las rutas api
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;//proceso de autenticacion
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;//encriptador de contraseñas
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableMethodSecurity//habilita la seguridad con anotaciones
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // inyeccion del filtro jwt
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//regula la cadena de filtros de http
        http
                .csrf(csrf -> csrf.disable())//desahibilita para rutas rest

                
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) //gestion de sesiones
                )

                .authorizeHttpRequests(auth -> auth//incializa autorization de rutas

                        // permite acceso publico a estas rutas 
                        .requestMatchers(
                                "/", "/login", "/logout",
                                "/css/**", "/js/**", "/img/**", "/favicon.ico",
                                "/sobre_nosotros", "/adoptar", "/envia_mensaje",
                                "/trabaja", "/upload", "/postular",
                                "/simulador_citas", "/book_appointment",
                                "/simulador_ventas", "/api/auth/login", "/api/auth/register"
                        ).permitAll()

                        // rutas protegida solo para el trabajador
                        .requestMatchers("/dashboard")
                        .hasAuthority("TRABAJADOR") // Debe coincidir con CustomUserDetailsService

                        // Rutas API protegidas con jwt
                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest().permitAll()//cualquier otra ruta es permitida
                )
                
                
                .formLogin(form -> form //formulario que define el login y valida las credenciales para el acceso por spring security
                    .loginPage("/login")
                    .loginProcessingUrl("/login") 
                    .usernameParameter("usuario") 
                    .passwordParameter("contrasena") 
                    .defaultSuccessUrl("/dashboard", true) 
                    .permitAll()
                )
                
                // cierre de sesion valida y elimina cookies
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                );

        // Añadir filtro JWT para APIs
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    // define el bean para la autencicacion
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
    //define el bean para el cifrado de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}