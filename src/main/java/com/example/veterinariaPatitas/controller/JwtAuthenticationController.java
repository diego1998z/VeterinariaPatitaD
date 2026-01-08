package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import com.example.veterinariaPatitas.security.JwtUtil;//importa la utilidad de jwt
import com.example.veterinariaPatitas.security.payload.LoginRequest; //modela la peticion Json de login
import com.example.veterinariaPatitas.security.payload.RegisterRequest; // modela la peticion Json de registro
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;//respuesta http personalizada
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; // encriptador de contraseñas
import org.springframework.web.bind.annotation.*;

import java.util.Map;//contruir las respuestas json
import java.util.Optional;//resultado de busqueda en la bd

@RestController
@RequestMapping("/api/auth")//URl base para autenticacion
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired 
    private PasswordEncoder passwordEncoder; // NUEVO


    @PostMapping("/login")//mapea solicitudes post de login
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        
        try {
            // Intenta autenticar al usuario (usa CustomUserDetailsService y PasswordEncoder)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Si es exitoso, busca el rol
            Optional<Usuario> userOptional = usuarioRepository.findByUsuario(loginRequest.getUsername());
            String role = userOptional.isPresent() ? userOptional.get().getRole() : "USER";//valor por defecto sino lo encuentra

            // Genera y devuelve el token JWT
            String jwt = jwtUtil.generateToken(loginRequest.getUsername(), role);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "username", loginRequest.getUsername(),
                    "role", role
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas."));
        }
    }
    
    
    @PostMapping("/register")//mapea las solicitudes post de registro
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        //valida que no exista el usuario
        if (usuarioRepository.existsByUsuario(registerRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "El nombre de usuario ya está en uso."));
        }
        
        // Crea y guarda el nuevo usuario con rol CLIENTE
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(registerRequest.getUsername());
        nuevoUsuario.setContrasena(passwordEncoder.encode(registerRequest.getPassword()));
        nuevoUsuario.setRole("CLIENTE"); // Rol fijo para nuevos registros
        
        usuarioRepository.save(nuevoUsuario);
        
        // Logea automáticamente al cliente recién registrado y le genera un jwt
        String jwt = jwtUtil.generateToken(nuevoUsuario.getUsuario(), nuevoUsuario.getRole());
        
        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "username", nuevoUsuario.getUsuario(),
                "role", nuevoUsuario.getRole(),
                "message", "Registro exitoso."
        ));
    }
}