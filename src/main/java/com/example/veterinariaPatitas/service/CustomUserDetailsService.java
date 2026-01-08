package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;//roles
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;
//busca el usuario en la bd y lo convierte en un userdetails de spring security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//verifica si el usuario existe o no y muestra un error 
        Usuario u = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
//convierte la cadena en un rol y guarda en una lista el usuario y cifra la contra
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(u.getRole()));

        return User.builder()
                .username(u.getUsuario())
                .password(u.getContrasena())
                .authorities(authorities) 
                .build();
    }
}