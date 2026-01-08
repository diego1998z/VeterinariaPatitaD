package com.example.veterinariaPatitas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String usuario,
            @RequestParam String contrasena,
            @RequestParam String rol,
            HttpSession session,
            Model model) {

        // ---- USUARIO TRABAJADOR  ----
        if (usuario.equals("trabajador") && contrasena.equals("123456") && rol.equals("TRABAJADOR")) {
            session.setAttribute("usuario", usuario);
            session.setAttribute("rol", rol);
            return "redirect:/";
        }

        model.addAttribute("error", "Credenciales incorrectas");
        return "login";
    }

    
}
