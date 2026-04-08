package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import com.example.veterinariaPatitas.service.PasswordResetService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class AuthController {

    private final PasswordResetService passwordResetService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(PasswordResetService passwordResetService,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.passwordResetService = passwordResetService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("usuario") String usuario,
                           @RequestParam(value = "email", required = false) String email,
                           @RequestParam("password") String password,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model) {

        String username = usuario == null ? "" : usuario.trim();
        // Normaliza email para evitar duplicados por espacios o mayusculas.
        String normalizedEmail = normalizeEmail(email);

        model.addAttribute("usuario", username);
        model.addAttribute("email", normalizedEmail == null ? "" : normalizedEmail);

        if (username.isBlank()) {
            model.addAttribute("error", "El usuario es obligatorio.");
            return "register";
        }

        if (normalizedEmail == null) {
            model.addAttribute("error", "El correo es obligatorio para recuperar contraseña.");
            return "register";
        }

        if (usuarioRepository.existsByUsuario(username)) {
            model.addAttribute("error", "El usuario ya existe.");
            return "register";
        }

        if (normalizedEmail != null && usuarioRepository.existsByEmail(normalizedEmail)) {
            model.addAttribute("error", "El correo ya está en uso.");
            return "register";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "register";
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(username);
        nuevoUsuario.setEmail(normalizedEmail);
        // Guarda password cifrada, nunca en texto plano.
        nuevoUsuario.setContrasena(passwordEncoder.encode(password));
        // Registro publico crea clientes; el alta de trabajador se hace desde admin.
        nuevoUsuario.setRole("CLIENTE");
        usuarioRepository.save(nuevoUsuario);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String sendResetLink(@RequestParam("identifier") String identifier, Model model) {
        // Dispara generacion/envio de codigo de recuperacion por correo.
        boolean sent = passwordResetService.requestPasswordReset(identifier);
        if (sent) {
            model.addAttribute("message", "Codigo enviado. Revisa tu correo.");
            model.addAttribute("messageType", "success");
            model.addAttribute("identifier", identifier == null ? "" : identifier.trim());
            model.addAttribute("showResetButton", true);
        } else {
            model.addAttribute("message", "No se pudo enviar el codigo. Verifica usuario/correo y configuración SMTP.");
            model.addAttribute("messageType", "danger");
        }
        return "forgot_password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "identifier", required = false) String identifier,
                                    Model model) {
        model.addAttribute("identifier", identifier == null ? "" : identifier.trim());
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("identifier") String identifier,
                                @RequestParam("code") String code,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        model.addAttribute("identifier", identifier == null ? "" : identifier.trim());
        model.addAttribute("code", code == null ? "" : code.trim());

        if (identifier == null || identifier.isBlank()) {
            model.addAttribute("message", "Debes ingresar usuario o correo.");
            model.addAttribute("messageType", "danger");
            return "reset_password";
        }

        if (code == null || code.trim().isEmpty()) {
            model.addAttribute("message", "Debes ingresar el codigo recibido.");
            model.addAttribute("messageType", "danger");
            return "reset_password";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("message", "La contrasena debe tener al menos 6 caracteres.");
            model.addAttribute("messageType", "danger");
            return "reset_password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("message", "Las contrasenas no coinciden.");
            model.addAttribute("messageType", "danger");
            return "reset_password";
        }

        // Valida codigo y actualiza password con hash BCrypt.
        boolean ok = passwordResetService.resetPasswordWithCode(identifier, code, password);
        if (ok) {
            return "redirect:/login?resetSuccess=true";
        }

        model.addAttribute("message", "No se pudo restablecer la contrasena. Verifica usuario/correo, codigo o vencimiento.");
        model.addAttribute("messageType", "danger");
        return "reset_password";
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String normalized = email.trim().toLowerCase();
        if (normalized.isBlank()) return null;
        return normalized;
    }
}
