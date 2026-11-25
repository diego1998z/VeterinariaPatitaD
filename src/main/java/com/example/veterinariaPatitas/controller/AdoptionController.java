package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.dto.AdoptablePet;
import com.example.veterinariaPatitas.dto.AdoptionRequestForm;
import com.example.veterinariaPatitas.model.AdoptionRequest;
import com.example.veterinariaPatitas.service.AdoptionService;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @GetMapping("/adopciones/{mascotaId}/solicitud")
    public String showRequest(@PathVariable String mascotaId, Model model,
                              RedirectAttributes redirectAttributes) {
        Optional<AdoptablePet> pet = adoptionService.findPet(mascotaId);
        if (pet.isEmpty()) {
            redirectAttributes.addFlashAttribute("adoptionError",
                    "La mascota seleccionada no está disponible para adopción.");
            return "redirect:/adoptar";
        }
        model.addAttribute("mascota", pet.get());
        if (!model.containsAttribute("solicitud")) {
            model.addAttribute("solicitud", new AdoptionRequestForm());
        }
        return "solicitud_adopcion";
    }

    @PostMapping("/adopciones/{mascotaId}/solicitud")
    public String submitRequest(@PathVariable String mascotaId,
                                @ModelAttribute("solicitud") AdoptionRequestForm form,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Optional<AdoptablePet> pet = adoptionService.findPet(mascotaId);
        if (pet.isEmpty()) {
            redirectAttributes.addFlashAttribute("adoptionError",
                    "La mascota seleccionada no está disponible para adopción.");
            return "redirect:/adoptar";
        }

        Map<String, String> errors = adoptionService.validate(form);
        if (!errors.isEmpty()) {
            model.addAttribute("mascota", pet.get());
            model.addAttribute("errores", errors);
            return "solicitud_adopcion";
        }

        AdoptionRequest request = adoptionService.save(mascotaId, form);
        return "redirect:/adopciones/confirmacion/" + request.getId();
    }

    @GetMapping("/adopciones/confirmacion/{solicitudId}")
    public String showConfirmation(@PathVariable Long solicitudId, Model model,
                                   RedirectAttributes redirectAttributes) {
        Optional<AdoptionRequest> request = adoptionService.findRequest(solicitudId);
        if (request.isEmpty()) {
            redirectAttributes.addFlashAttribute("adoptionError",
                    "No encontramos la solicitud indicada.");
            return "redirect:/adoptar";
        }
        model.addAttribute("solicitud", request.get());
        return "confirmacion_adopcion";
    }
}
