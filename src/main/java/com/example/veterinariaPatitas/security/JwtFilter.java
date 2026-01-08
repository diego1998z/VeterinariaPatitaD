package com.example.veterinariaPatitas.security;


import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;//roles y permisos
import org.springframework.security.core.context.SecurityContextHolder;//almacena la info del usuario autenticado
import org.springframework.util.StringUtils;//vverifica si una cadena tiene contenido
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
@Component
public class JwtFilter extends OncePerRequestFilter {//asegura una sola ejecucion por solicitud

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override //metodo que filtra cada solicitud http
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
//lee el header de autorizacion
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {//verifica que tenga el token
            String token = header.substring(7); 
            if (jwtUtil.validateToken(token)) {//valida el token y la informacion dentro
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // creo el objeto de autenticacion con el rol
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }//verifica que el usuario cumple con el token y puede seguir
        }

        filterChain.doFilter(request, response);//verifica el siguiente filtro en la cadena
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // se aplica el filtro solo para las rutas api
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }
}
