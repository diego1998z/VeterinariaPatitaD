/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.veterinariaPatitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
        @GetMapping("/sobre_nosotros")
    public String sobre_nosotros() {
        return "sobre_nosotros";
    }
    
    @GetMapping("/adoptar")
    public String adoptar() {
        return "adoptar";
    }
    
    @GetMapping("/envia_mensaje")
    public String enviarMensaje() {
        return "envia_mensaje";
    }
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("pdfFile") MultipartFile file,
                                   @RequestParam("nombre") String nombre,
                                   Model model) {
        
        // Verifica si se adjuntó un archivo
        if (!file.isEmpty()) {
            try {
                
                String UPLOAD_DIR = "uploads/";
                Path uploadPath = Paths.get(UPLOAD_DIR);
                
                // Crea el directorio si no existe
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Guarda el archivo en el sistema de archivos del servidor
                Path filePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
                Files.write(filePath, file.getBytes());

                model.addAttribute("message", "¡Mensaje y PDF enviado con éxito, " + nombre + "!");
                
            } catch (IOException e) {
                model.addAttribute("error", "Error al subir el archivo.");
                e.printStackTrace();
            }
        } else {
            
            model.addAttribute("message", "¡Mensaje enviado con éxito, " + nombre + "!");
        }

        // Redirige o retorna a una página de confirmación
        return "confirmacion_envio";
    }
     @GetMapping("/trabaja")
    public String trabajaConNosotros() {
        return "trabaja"; // Retorna el archivo trabaja.html
    }
    
    @PostMapping("/postular")
    public String handleJobApplication(@RequestParam("nombres") String nombres,
                                      @RequestParam("apellidos") String apellidos,
                                      @RequestParam("email") String email,
                                      @RequestParam("telefono") String telefono,
                                      @RequestParam("nivel_academico") String nivelAcademico,
                                      @RequestParam("area_trabajo") String areaTrabajo,
                                      @RequestParam("carta_presentacion") String cartaPresentacion,
                                      @RequestParam("curriculum") MultipartFile curriculum,
                                      Model model) {
                                      
        // Lógica para procesar los datos y el archivo
        if (curriculum.isEmpty()) {
            model.addAttribute("error", "Por favor, sube tu currículum.");
            return "trabaja";
        }
        
        try {
            // Define la ruta donde se guardarán los currículums
            String UPLOAD_DIR = "uploads/curriculums/";
            Path uploadPath = Paths.get(UPLOAD_DIR);
            
            // Crea el directorio si no existe
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Crea un nombre de archivo único para evitar sobrescribir
            String fileName = nombres + "_" + apellidos + "_" + System.currentTimeMillis() + ".pdf";
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            
            // Guarda el archivo
            Files.write(filePath, curriculum.getBytes());
            
           
            
            model.addAttribute("message", "¡Gracias por postularte, " + nombres + "! Tu información ha sido enviada.");
            
        } catch (IOException e) {
            model.addAttribute("error", "Error al procesar tu postulación. Por favor, intenta de nuevo.");
            e.printStackTrace();
            return "trabaja"; 
        }
        
        // Redirige o retorna a una página de confirmación
        return "confirmacion_postulacion";
    }
}


