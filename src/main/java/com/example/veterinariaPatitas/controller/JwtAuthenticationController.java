package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import com.example.veterinariaPatitas.security.JwtUtil;
import com.example.veterinariaPatitas.security.payload.LoginRequest;
import com.example.veterinariaPatitas.security.payload.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired 
    private PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // 1) Valida credenciales contra BD usando AuthenticationManager + BCrypt.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2) Recupera el rol actual para incluirlo en la respuesta y JWT.
            Optional<Usuario> userOptional = usuarioRepository.findByUsuario(loginRequest.getUsername());
            String role = userOptional.isPresent() ? userOptional.get().getRole() : "USER";

            // 3) Genera token firmado.
            String jwt = jwtUtil.generateToken(loginRequest.getUsername(), role);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "username", loginRequest.getUsername(),
                    "role", role
            ));

        } catch (Exception e) {
            // Respuesta uniforme para no exponer detalles de autenticacion.
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas."));
        }
    }
    
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        // Valida username unico.
        if (usuarioRepository.existsByUsuario(registerRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "El nombre de usuario ya está en uso."));
        }

        String normalizedEmail = normalizeEmail(registerRequest.getEmail());
        if (normalizedEmail != null && usuarioRepository.existsByEmail(normalizedEmail)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "El correo ya está en uso."));
        }
        
        // Registro publico: siempre crea rol CLIENTE.
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(registerRequest.getUsername());
        nuevoUsuario.setContrasena(passwordEncoder.encode(registerRequest.getPassword()));
        if (normalizedEmail != null) {
            nuevoUsuario.setEmail(normalizedEmail);
        } else if (registerRequest.getUsername() != null && registerRequest.getUsername().contains("@")) {
            nuevoUsuario.setEmail(registerRequest.getUsername().trim().toLowerCase());
        }
        nuevoUsuario.setRole("CLIENTE");
        
        usuarioRepository.save(nuevoUsuario);
        
        // Emite JWT inmediato para cliente recien registrado.
        String jwt = jwtUtil.generateToken(nuevoUsuario.getUsuario(), nuevoUsuario.getRole());
        
        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "username", nuevoUsuario.getUsuario(),
                "email", nuevoUsuario.getEmail() == null ? "" : nuevoUsuario.getEmail(),
                "role", nuevoUsuario.getRole(),
                "message", "Registro exitoso."
        ));
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        // Normaliza para evitar duplicados por mayusculas/espacios.
        String value = email.trim().toLowerCase();
        if (value.isEmpty()) return null;
        return value;
    }
}
