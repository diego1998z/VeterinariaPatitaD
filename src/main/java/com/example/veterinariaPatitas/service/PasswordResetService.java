package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.PasswordResetToken;
import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.PasswordResetTokenRepository;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom random = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UsuarioRepository usuarioRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public boolean requestPasswordReset(String identifier) {
        // 1) Busca usuario por username o correo.
        Optional<Usuario> userOpt = findUsuarioByIdentifier(identifier);
        if (userOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = userOpt.get();
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            log.warn("Solicitud de recuperación: usuario '{}' sin correo registrado.", usuario.getUsuario());
            return false;
        }

        // 2) Limpia tokens previos del usuario y expirados para evitar acumulacion.
        tokenRepository.deleteByUsuario(usuario);
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        // 3) Crea codigo de 6 digitos con vigencia de 30 minutos.
        String resetCode = generateUniqueCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setToken(resetCode);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        tokenRepository.save(token);

        // 4) Si el envio falla, revierte el token para no dejar codigos huerfanos.
        boolean sent = emailService.sendPasswordResetCodeEmail(usuario.getEmail(), usuario.getUsuario(), resetCode);
        if (!sent) {
            tokenRepository.delete(token);
            log.error("No se pudo enviar correo de recuperación a '{}' (usuario '{}').",
                    usuario.getEmail(), usuario.getUsuario());
            return false;
        }

        log.info("Correo de recuperación enviado a '{}' (usuario '{}').", usuario.getEmail(), usuario.getUsuario());
        return true;
    }

    @Transactional
    public boolean resetPasswordWithCode(String identifier, String code, String newPassword) {
        // Validacion minima de password.
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }

        Optional<Usuario> userOpt = findUsuarioByIdentifier(identifier);
        if (userOpt.isEmpty()) {
            return false;
        }
        Usuario usuario = userOpt.get();

        // Verifica que el codigo exista, no este usado y no este vencido.
        Optional<PasswordResetToken> tokenOpt = getValidToken(code);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.getUsuario() == null || !token.getUsuario().getId().equals(usuario.getId())) {
            return false;
        }

        // Actualiza password cifrada y marca token como usado (one-time).
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        token.setUsed(true);
        tokenRepository.save(token);
        return true;
    }

    private Optional<PasswordResetToken> getValidToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) return Optional.empty();
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenValue.trim());
        if (tokenOpt.isEmpty()) return Optional.empty();
        PasswordResetToken token = tokenOpt.get();
        if (token.isUsed()) return Optional.empty();
        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) return Optional.empty();
        return Optional.of(token);
    }

    private Optional<Usuario> findUsuarioByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            log.warn("Solicitud de recuperación rechazada: identificador vacío.");
            return Optional.empty();
        }

        String value = identifier.trim();
        // Soporta recuperar por correo o por username.
        Optional<Usuario> userOpt = value.contains("@")
                ? usuarioRepository.findByEmail(value.toLowerCase())
                : usuarioRepository.findByUsuario(value);

        if (userOpt.isEmpty()) {
            log.warn("Solicitud de recuperación: usuario no encontrado para '{}'.", value);
        }
        return userOpt;
    }

    private String generateUniqueCode() {
        // Reintenta algunas veces para evitar colision en codigos activos.
        for (int i = 0; i < 10; i++) {
            String code = String.format("%06d", random.nextInt(1_000_000));
            if (!tokenRepository.existsByToken(code)) {
                return code;
            }
        }
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
