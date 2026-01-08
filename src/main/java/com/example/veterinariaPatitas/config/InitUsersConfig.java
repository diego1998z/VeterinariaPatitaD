package com.example.veterinariaPatitas.config;

import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;//define tareas  en automatico
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;//importa la interfaz de security para cifrar contra

@Configuration//indica que tiene beans
public class InitUsersConfig {
//define la inicializacion de usuarios
    @Bean
    public CommandLineRunner initUsers(UsuarioRepository usuarioRepository,
                                       PasswordEncoder encoder) { //lo define como un bean e innyecta el cifrado
        return args -> {

            
            Usuario trabajador = usuarioRepository.findByUsuario("trabajador")
                    .orElse(new Usuario());//busca si existe en la bd sino lo crea

            trabajador.setUsuario("trabajador");
            trabajador.setContrasena(encoder.encode("123456"));//spring security cifra la contra
            trabajador.setRole("TRABAJADOR");
            usuarioRepository.save(trabajador);
            System.out.println("TRABAJADOR actualizado/creado ✔");

            
            Usuario clientePrueba = usuarioRepository.findByUsuario("cliente1")//cliente prueba
                    .orElse(new Usuario());
            clientePrueba.setUsuario("cliente1");
            clientePrueba.setContrasena(encoder.encode("123456"));//cifra la contra
            clientePrueba.setRole("CLIENTE"); 
            usuarioRepository.save(clientePrueba);
            System.out.println("CLIENTE de prueba actualizado/creado ✔");
        };
    }
}