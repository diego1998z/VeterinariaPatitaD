package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.PasswordResetToken;
import com.example.veterinariaPatitas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    boolean existsByToken(String token);
    void deleteByUsuario(Usuario usuario);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
