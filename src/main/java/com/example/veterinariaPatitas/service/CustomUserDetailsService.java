package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Carga el usuario desde la tabla usuarios.
        Usuario u = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Convierte el rol persistido (CLIENTE/TRABAJADOR) a autoridad de Spring.
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(u.getRole()));

        // Retorna el objeto que Spring Security usa para autenticar y autorizar.
        return User.builder()
                .username(u.getUsuario())
                .password(u.getContrasena())
                .authorities(authorities)
                .build();
    }
}
