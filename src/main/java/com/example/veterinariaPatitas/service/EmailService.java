package com.example.veterinariaPatitas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String fromAddress) {
        // JavaMailSender se configura con spring.mail.* en application.properties.
        this.mailSender = mailSender;
        this.fromAddress = fromAddress == null ? "" : fromAddress.trim();
    }

    public boolean sendAppointmentConfirmation(String toEmail,
                                               String ownerName,
                                               String serviceName,
                                               LocalDateTime dateTime,
                                               String medicoName) {
        // Falla rapido si el correo del destinatario no es valido.
        if (!isValidEmail(toEmail)) return false;
        String fecha = dateTime == null
                ? ""
                : dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        String doctor = medicoName == null || medicoName.isBlank() ? "No asignado" : medicoName;

        String subject = "Confirmacion de cita - Veterinaria Patitas";
        String body = "Hola " + safe(ownerName) + ",\n\n" +
                "Tu cita ha sido registrada correctamente.\n" +
                "Servicio: " + safe(serviceName) + "\n" +
                "Fecha y hora: " + fecha + "\n" +
                "Medico: " + doctor + "\n\n" +
                "Gracias por confiar en Veterinaria Patitas.";

        // Reusa el mismo canal de envio para mantener trazabilidad y control de errores.
        return sendPlainEmail(toEmail, subject, body);
    }

    public boolean sendPasswordResetCodeEmail(String toEmail, String username, String resetCode) {
        if (!isValidEmail(toEmail) || resetCode == null || resetCode.isBlank()) return false;

        String subject = "Recuperacion de contrasena - Veterinaria Patitas";
        String body = "Hola " + safe(username) + ",\n\n" +
                "Recibimos una solicitud para restablecer tu contrasena.\n" +
                "Tu codigo de recuperacion es: " + resetCode + "\n\n" +
                "Este codigo expira en 30 minutos.\n" +
                "Si no solicitaste este cambio, ignora este mensaje.";

        // Envia un codigo one-time (token corto) en lugar de password temporal.
        return sendPlainEmail(toEmail, subject, body);
    }

    private boolean sendPlainEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Usa cuenta remitente configurada en SMTP.
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(toEmail.trim());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("No se pudo enviar correo a {}. Causa: {}", toEmail, ex.getMessage(), ex);
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        // Validacion basica para evitar errores de formato en envio.
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
