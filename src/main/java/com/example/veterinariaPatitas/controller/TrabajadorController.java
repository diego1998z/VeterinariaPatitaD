/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.veterinariaPatitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrabajadorController {

    @GetMapping("/trabajador")
    public String mostrarLoginTrabajador() {
        return "trabajador"; // el nombre de la vista (trabajador-login.html en templates)
    }
}