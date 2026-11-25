package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.dto.AdoptablePet;
import com.example.veterinariaPatitas.dto.AdoptionRequestForm;
import com.example.veterinariaPatitas.model.AdoptionRequest;
import com.example.veterinariaPatitas.repository.AdoptionRequestRepository;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class AdoptionService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("^[A-Z0-9-]{8,15}$");
    private static final Set<String> HOUSING_TYPES = Set.of("CASA", "DEPARTAMENTO", "OTRO");
    private static final Map<String, AdoptablePet> PETS = Map.ofEntries(
            Map.entry("asesino", new AdoptablePet("asesino", "Asesino", "/img/asesino.jpg")),
            Map.entry("pirata", new AdoptablePet("pirata", "Pirata", "/img/pirata.jpg")),
            Map.entry("cuto", new AdoptablePet("cuto", "Cuto", "/img/cuto.jpg")),
            Map.entry("blue", new AdoptablePet("blue", "Blue", "/img/blue.avif")),
            Map.entry("bombon", new AdoptablePet("bombon", "Bombón", "/img/bombon.avif")),
            Map.entry("pepon", new AdoptablePet("pepon", "Pepón", "/img/pepon.avif")),
            Map.entry("osito", new AdoptablePet("osito", "Osito", "/img/osito.avif")),
            Map.entry("celeste", new AdoptablePet("celeste", "Celeste", "/img/celeste.avif"))
    );

    private final AdoptionRequestRepository adoptionRequestRepository;

    public AdoptionService(AdoptionRequestRepository adoptionRequestRepository) {
        this.adoptionRequestRepository = adoptionRequestRepository;
    }

    public Optional<AdoptablePet> findPet(String petId) {
        if (petId == null) return Optional.empty();
        return Optional.ofNullable(PETS.get(petId.trim().toLowerCase(Locale.ROOT)));
    }

    public Optional<AdoptionRequest> findRequest(Long id) {
        if (id == null) return Optional.empty();
        return adoptionRequestRepository.findById(id);
    }

    public Map<String, String> validate(AdoptionRequestForm form) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (form == null) {
            errors.put("formulario", "No pudimos leer el formulario. Volvé a intentarlo.");
            return errors;
        }

        validateLength(errors, "nombreCompleto", form.getNombreCompleto(), 3, 120,
                "Ingresá tu nombre completo (entre 3 y 120 caracteres).");

        String document = normalizeUpper(form.getDocumento());
        if (!DOCUMENT_PATTERN.matcher(document).matches()) {
            errors.put("documento", "Ingresá un documento válido de 8 a 15 caracteres.");
        }

        String email = normalize(form.getCorreo());
        if (email.length() > 160 || !EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("correo", "Ingresá un correo electrónico válido.");
        }

        String phone = normalize(form.getTelefono());
        long phoneDigits = phone.chars().filter(Character::isDigit).count();
        if (phone.length() > 20 || phoneDigits < 7 || phoneDigits > 15
                || !phone.matches("[0-9+() .-]+")) {
            errors.put("telefono", "Ingresá un teléfono válido de 7 a 15 dígitos.");
        }

        if (!HOUSING_TYPES.contains(normalizeUpper(form.getTipoVivienda()))) {
            errors.put("tipoVivienda", "Seleccioná un tipo de vivienda.");
        }
        validateLength(errors, "distrito", form.getDistrito(), 2, 100,
                "Ingresá el distrito donde vivís.");

        String otherPets = normalizeUpper(form.getOtrasMascotas());
        if (!Set.of("SI", "NO").contains(otherPets)) {
            errors.put("otrasMascotas", "Indicá si actualmente tenés otras mascotas.");
        } else if ("SI".equals(otherPets)) {
            validateLength(errors, "detalleOtrasMascotas", form.getDetalleOtrasMascotas(), 3, 300,
                    "Contanos brevemente cuántas mascotas tenés y de qué tipo.");
        }

        validateLength(errors, "motivo", form.getMotivo(), 20, 1000,
                "Contanos por qué querés adoptar (mínimo 20 caracteres).");
        if (!form.isAceptaCondiciones()) {
            errors.put("aceptaCondiciones", "Debés aceptar las condiciones para enviar la solicitud.");
        }
        return errors;
    }

    public AdoptionRequest save(String petId, AdoptionRequestForm form) {
        AdoptablePet pet = findPet(petId)
                .orElseThrow(() -> new IllegalArgumentException("La mascota indicada no está disponible."));
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("La solicitud contiene datos inválidos.");
        }

        AdoptionRequest request = new AdoptionRequest();
        request.setPetId(pet.getId());
        request.setPetName(pet.getName());
        request.setNombreCompleto(normalize(form.getNombreCompleto()));
        request.setDocumento(normalizeUpper(form.getDocumento()));
        request.setCorreo(normalize(form.getCorreo()).toLowerCase(Locale.ROOT));
        request.setTelefono(normalize(form.getTelefono()));
        request.setTipoVivienda(normalizeUpper(form.getTipoVivienda()));
        request.setDistrito(normalize(form.getDistrito()));
        request.setTieneOtrasMascotas("SI".equals(normalizeUpper(form.getOtrasMascotas())));
        request.setDetalleOtrasMascotas(request.isTieneOtrasMascotas()
                ? normalize(form.getDetalleOtrasMascotas()) : null);
        request.setMotivo(normalize(form.getMotivo()));
        request.setEstado("PENDIENTE");
        return adoptionRequestRepository.save(request);
    }

    private void validateLength(Map<String, String> errors, String field, String value,
                                int min, int max, String message) {
        int length = normalize(value).length();
        if (length < min || length > max) errors.put(field, message);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeUpper(String value) {
        return normalize(value).toUpperCase(Locale.ROOT);
    }
}
