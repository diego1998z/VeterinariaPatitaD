package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Appointment;
import com.example.veterinariaPatitas.model.HorarioMedico;
import com.example.veterinariaPatitas.model.Medico;
import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.model.Usuario;
import com.example.veterinariaPatitas.repository.UsuarioRepository;
import com.example.veterinariaPatitas.service.AppointmentService;
import com.example.veterinariaPatitas.service.HorarioMedicoService;
import com.example.veterinariaPatitas.service.MedicoService;
import com.example.veterinariaPatitas.service.SalesService;
import com.example.veterinariaPatitas.service.ServiceVetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

@Controller
@RequestMapping("/admin")
public class AdminCrudController {

    private final ServiceVetService serviceVetService;
    private final MedicoService medicoService;
    private final AppointmentService appointmentService;
    private final HorarioMedicoService horarioMedicoService;
    private final SalesService salesService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminCrudController(ServiceVetService serviceVetService,
                               MedicoService medicoService,
                               AppointmentService appointmentService,
                               HorarioMedicoService horarioMedicoService,
                               SalesService salesService,
                               UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.serviceVetService = serviceVetService;
        this.medicoService = medicoService;
        this.appointmentService = appointmentService;
        this.horarioMedicoService = horarioMedicoService;
        this.salesService = salesService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------- SERVICIOS --------------------
    @GetMapping("/servicios")
    public String servicios(Model model) {
        loadServiciosModel(model, new ServiceVet());
        return "admin_servicios";
    }

    @GetMapping("/servicios/editar/{id}")
    public String editarServicio(@PathVariable Integer id, Model model) {
        ServiceVet service = serviceVetService.findById(id).orElse(new ServiceVet());
        loadServiciosModel(model, service);
        return "admin_servicios";
    }

    @PostMapping("/servicios/guardar")
    public String guardarServicio(@ModelAttribute("serviceForm") ServiceVet serviceForm,
                                  RedirectAttributes redirectAttributes) {
        serviceVetService.save(serviceForm);
        redirectAttributes.addFlashAttribute("message", "Servicio guardado correctamente.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/servicios";
    }

    @PostMapping("/servicios/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id,
                                   RedirectAttributes redirectAttributes) {
        serviceVetService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Servicio eliminado.");
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/servicios";
    }

    private void loadServiciosModel(Model model, ServiceVet serviceForm) {
        model.addAttribute("services", serviceVetService.findAll());
        model.addAttribute("serviceForm", serviceForm);
    }

    // -------------------- PRODUCTOS --------------------
    @GetMapping("/productos")
    public String productos(Model model) {
        loadProductosModel(model, new Product());
        return "admin_productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Integer id, Model model) {
        Product product = salesService.getProductById(id).orElse(new Product());
        loadProductosModel(model, product);
        return "admin_productos";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@RequestParam(required = false) Integer id,
                                  @RequestParam String name,
                                  @RequestParam double price,
                                  @RequestParam int stock,
                                  @RequestParam(defaultValue = "PEN") String currency,
                                  @RequestParam(required = false) String unit,
                                  RedirectAttributes redirectAttributes) {

        Product product = (id != null && id > 0)
                ? salesService.getProductById(id).orElse(new Product())
                : new Product();

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setCurrency(currency);
        product.setUnit(unit);

        salesService.saveProduct(product);
        redirectAttributes.addFlashAttribute("message", "Producto guardado correctamente.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id,
                                   RedirectAttributes redirectAttributes) {
        salesService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "Producto eliminado.");
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/productos";
    }

    private void loadProductosModel(Model model, Product productForm) {
        model.addAttribute("products", salesService.getInitialProducts());
        model.addAttribute("productForm", productForm);
    }

    // -------------------- USUARIOS --------------------
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        loadUsuariosModel(model, new Usuario(), false);
        return "admin_usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioRepository.findById(id).orElse(new Usuario());
        loadUsuariosModel(model, usuario, true);
        return "admin_usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam(required = false) Long id,
                                 @RequestParam String usuario,
                                 @RequestParam(required = false) String email,
                                 @RequestParam String role,
                                 @RequestParam(required = false) String contrasena,
                                 RedirectAttributes redirectAttributes) {

        Usuario entity = (id != null)
                ? usuarioRepository.findById(id).orElse(new Usuario())
                : new Usuario();

        String username = usuario == null ? "" : usuario.trim();
        String normalizedEmail = (email == null || email.isBlank()) ? null : email.trim().toLowerCase();
        String normalizedRole = normalizeRole(role);

        if (username.isBlank()) {
            redirectAttributes.addFlashAttribute("message", "El usuario es obligatorio.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/usuarios";
        }

        if (!username.equals(entity.getUsuario()) && usuarioRepository.existsByUsuario(username)) {
            redirectAttributes.addFlashAttribute("message", "Ya existe ese nombre de usuario.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/usuarios";
        }

        if (normalizedEmail != null) {
            boolean emailEnUso = usuarioRepository.findByEmail(normalizedEmail)
                    .map(existing -> !existing.getId().equals(entity.getId()))
                    .orElse(false);
            if (emailEnUso) {
                redirectAttributes.addFlashAttribute("message", "El correo ya está en uso.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/usuarios";
            }
        }

        if ((id == null || entity.getId() == null) && (contrasena == null || contrasena.length() < 6)) {
            redirectAttributes.addFlashAttribute("message", "La contraseña para nuevo usuario debe tener al menos 6 caracteres.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/usuarios";
        }

        entity.setUsuario(username);
        entity.setEmail(normalizedEmail);
        entity.setRole(normalizedRole);

        if (contrasena != null && !contrasena.isBlank()) {
            if (contrasena.length() < 6) {
                redirectAttributes.addFlashAttribute("message", "La contraseña debe tener al menos 6 caracteres.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/admin/usuarios";
            }
            entity.setContrasena(passwordEncoder.encode(contrasena));
        }

        usuarioRepository.save(entity);
        redirectAttributes.addFlashAttribute("message", "Usuario guardado correctamente.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("message", "Usuario no encontrado.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/usuarios";
        }

        if ("trabajador".equalsIgnoreCase(usuario.getUsuario())) {
            redirectAttributes.addFlashAttribute("message", "No puedes eliminar el usuario base trabajador.");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/admin/usuarios";
        }

        usuarioRepository.delete(usuario);
        redirectAttributes.addFlashAttribute("message", "Usuario eliminado.");
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/usuarios";
    }

    private void loadUsuariosModel(Model model, Usuario usuarioForm, boolean editMode) {
        model.addAttribute("users", usuarioRepository.findAll());
        model.addAttribute("usuarioForm", usuarioForm);
        model.addAttribute("editMode", editMode);
        model.addAttribute("roles", java.util.List.of("CLIENTE", "TRABAJADOR"));
    }

    private String normalizeRole(String role) {
        if ("TRABAJADOR".equalsIgnoreCase(role)) return "TRABAJADOR";
        return "CLIENTE";
    }

    // -------------------- MEDICOS --------------------
    @GetMapping("/medicos")
    public String medicos(@RequestParam(required = false) String especialidad, Model model) {
        loadMedicosModel(model, new Medico(), especialidad);
        return "admin_medicos";
    }

    @GetMapping("/medicos/editar/{id}")
    public String editarMedico(@PathVariable Long id, Model model) {
        Medico medico = medicoService.findById(id).orElse(new Medico());
        loadMedicosModel(model, medico, null);
        if (medico.getServicio() != null) {
            model.addAttribute("selectedServiceId", medico.getServicio().getId());
        }
        return "admin_medicos";
    }

    @PostMapping("/medicos/guardar")
    public String guardarMedico(@RequestParam(required = false) Long id,
                                @RequestParam String nombre,
                                @RequestParam String especialidad,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horarioInicio,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horarioFin,
                                @RequestParam(required = false) Integer servicioId,
                                RedirectAttributes redirectAttributes) {

        Medico medico = (id != null)
                ? medicoService.findById(id).orElse(new Medico())
                : new Medico();

        medico.setNombre(nombre);
        medico.setEspecialidad(especialidad);
        medico.setHorarioInicio(horarioInicio);
        medico.setHorarioFin(horarioFin);

        if (servicioId != null) {
            serviceVetService.findById(servicioId).ifPresent(medico::setServicio);
        } else {
            medico.setServicio(null);
        }

        medicoService.save(medico);
        redirectAttributes.addFlashAttribute("message", "Médico guardado correctamente.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/medicos";
    }

    @PostMapping("/medicos/eliminar/{id}")
    public String eliminarMedico(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        medicoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Médico eliminado.");
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/medicos";
    }

    private void loadMedicosModel(Model model, Medico medicoForm, String especialidad) {
        model.addAttribute("medicos", medicoService.getMedicoListByEspecialidad(especialidad));
        model.addAttribute("services", serviceVetService.findAll());
        model.addAttribute("medicoForm", medicoForm);
        model.addAttribute("especialidadFiltro", especialidad);
        model.addAttribute("especialidades", medicoService.getEspecialidades());
    }

    // -------------------- HORARIOS DE MÉDICOS --------------------
    @GetMapping("/medicos/{id}/horarios")
    public String horariosMedico(@PathVariable Long id, Model model) {
        Medico medico = medicoService.findById(id).orElse(null);
        if (medico == null) {
            return "redirect:/admin/medicos";
        }

        model.addAttribute("medico", medico);
        model.addAttribute("horarios", horarioMedicoService.findByMedicoId(id));
        model.addAttribute("daysOfWeek", DayOfWeek.values());
        model.addAttribute("dayNames", getDayNamesEs());
        return "admin_horarios";
    }

    @PostMapping("/medicos/{id}/horarios/guardar")
    public String guardarHorario(@PathVariable Long id,
                                 @RequestParam(required = false, defaultValue = "") String diaSemana,
                                 @RequestParam(required = false, defaultValue = "") String fecha,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
                                 RedirectAttributes redirectAttributes) {

        Medico medico = medicoService.findById(id).orElse(null);
        if (medico == null) {
            redirectAttributes.addFlashAttribute("message", "Médico no encontrado.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/medicos";
        }

        DayOfWeek dia = parseDiaSemana(diaSemana);

        LocalDate fechaParsed = null;
        if (fecha != null && !fecha.isBlank()) {
            try {
                fechaParsed = LocalDate.parse(fecha);
            } catch (Exception ex) {
                fechaParsed = null;
            }
        }

        if (fechaParsed == null && dia == null) {
            redirectAttributes.addFlashAttribute("message", "Debe seleccionar un día o una fecha específica.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/medicos/" + id + "/horarios";
        }

        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico);
        horario.setDiaSemana(dia);
        horario.setFecha(fechaParsed);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horarioMedicoService.save(horario);

        redirectAttributes.addFlashAttribute("message", "Horario registrado correctamente.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/medicos/" + id + "/horarios";
    }

    @PostMapping("/medicos/{medicoId}/horarios/eliminar/{horarioId}")
    public String eliminarHorario(@PathVariable Long medicoId,
                                  @PathVariable Long horarioId,
                                  RedirectAttributes redirectAttributes) {
        horarioMedicoService.delete(horarioId);
        redirectAttributes.addFlashAttribute("message", "Horario eliminado.");
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/medicos/" + medicoId + "/horarios";
    }

    private java.util.Map<String, String> getDayNamesEs() {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        map.put(DayOfWeek.MONDAY.name(), "Lunes");
        map.put(DayOfWeek.TUESDAY.name(), "Martes");
        map.put(DayOfWeek.WEDNESDAY.name(), "Miércoles");
        map.put(DayOfWeek.THURSDAY.name(), "Jueves");
        map.put(DayOfWeek.FRIDAY.name(), "Viernes");
        map.put(DayOfWeek.SATURDAY.name(), "Sábado");
        map.put(DayOfWeek.SUNDAY.name(), "Domingo");
        return map;
    }

    private DayOfWeek parseDiaSemana(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;

        try {
            return DayOfWeek.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException ex) {
            // continuar
        }

       
        String n = v.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");

        switch (n) {
            case "lunes":
                return DayOfWeek.MONDAY;
            case "martes":
                return DayOfWeek.TUESDAY;
            case "miercoles":
                return DayOfWeek.WEDNESDAY;
            case "jueves":
                return DayOfWeek.THURSDAY;
            case "viernes":
                return DayOfWeek.FRIDAY;
            case "sabado":
                return DayOfWeek.SATURDAY;
            case "domingo":
                return DayOfWeek.SUNDAY;
            default:
                return null;
        }
    }

    // -------------------- CITAS --------------------
    @GetMapping("/citas")
    public String citas(Model model) {
        loadCitasModel(model, null);
        return "admin_citas";
    }

    @GetMapping("/citas/editar/{id}")
    public String editarCita(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.findById(id).orElse(null);
        loadCitasModel(model, appointment);
        return "admin_citas";
    }

    @PostMapping("/citas/guardar")
    public String guardarCita(@RequestParam(required = false) Long id,
                              @RequestParam("serviceId") int serviceId,
                              @RequestParam("medicoId") Long medicoId,
                              @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                              @RequestParam("clientName") String clientName,
                              RedirectAttributes redirectAttributes) {

        if (isWeekend(date)) {
            redirectAttributes.addFlashAttribute("message", "No se pudo guardar la cita. Las citas regulares se atienden de lunes a viernes.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/citas";
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
        boolean success;

        if (id == null) {
            success = appointmentService.bookAppointment(serviceId, medicoId, appointmentDateTime, clientName);
        } else {
            success = appointmentService.updateAppointment(id, serviceId, medicoId, appointmentDateTime, clientName);
        }

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Cita guardada correctamente.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "No se pudo guardar la cita. Verifica disponibilidad y horario.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }

        return "redirect:/admin/citas";
    }

    @PostMapping("/citas/eliminar/{id}")
    public String eliminarCita(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        appointmentService.deleteAppointment(id);
        redirectAttributes.addFlashAttribute("message", "Cita eliminada.");
        redirectAttributes.addFlashAttribute("messageType", "warning");
        return "redirect:/admin/citas";
    }

    private void loadCitasModel(Model model, Appointment editAppointment) {
        model.addAttribute("appointmentList", appointmentService.getAppointmentList());
        model.addAttribute("availableServices", appointmentService.getAvailableServices());
        model.addAttribute("allMedicos", appointmentService.getAllMedicos());

        LocalDate minDate = getNextBusinessDay(LocalDate.now().plusDays(1));
        model.addAttribute("minDate", minDate.format(DateTimeFormatter.ISO_DATE));

        if (editAppointment != null) {
            model.addAttribute("editId", editAppointment.getId());
            model.addAttribute("clientName", editAppointment.getClientName());
            if (editAppointment.getService() != null) {
                model.addAttribute("selectedServiceId", editAppointment.getService().getId());
            }
            if (editAppointment.getMedico() != null) {
                model.addAttribute("selectedMedicoId", editAppointment.getMedico().getId());
            }
            if (editAppointment.getDateTime() != null) {
                model.addAttribute("selectedDate",
                        editAppointment.getDateTime().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                model.addAttribute("selectedTime",
                        editAppointment.getDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
    }

    private LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate candidate = date;
        while (isWeekend(candidate)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
