package com.example.veterinariaPatitas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ApiPeruService {

    private static final String DNI_ENDPOINT = "/dni";
    private static final String TIPO_CAMBIO_ENDPOINT = "/tipo-de-cambio";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final String apiToken;

    public ApiPeruService(RestTemplateBuilder restTemplateBuilder,
                          ObjectMapper objectMapper,
                          @Value("${apiperu.base-url:https://apiperu.dev/api}") String apiBaseUrl,
                          @Value("${apiperu.token:}") String apiToken) {
        // Timeouts cortos para evitar bloquear la app si el proveedor externo demora.
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(8))
                .readTimeout(Duration.ofSeconds(12))
                .build();
        this.objectMapper = objectMapper;
        this.apiBaseUrl = sanitizeBaseUrl(apiBaseUrl);
        this.apiToken = apiToken == null ? "" : apiToken.trim();
    }

    public Map<String, Object> consultarDni(String dni) {
        // Valida formato localmente antes de llamar al servicio externo.
        String dniLimpio = dni == null ? "" : dni.trim();
        if (!dniLimpio.matches("\\d{8}")) {
            return buildResponse(false, "El DNI debe tener 8 dígitos.", null);
        }
        if (apiToken.isBlank()) {
            return buildResponse(false, "Token de APIPeru no configurado en APIPERU_TOKEN.", null);
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("dni", dniLimpio);

        // Consulta APIPeru y parsea JSON tolerando variaciones de campos.
        JsonNode root = executePost(DNI_ENDPOINT, requestBody);
        if (root == null) {
            return buildResponse(false, "No se pudo obtener respuesta de APIPeru.", null);
        }

        if (!readSuccess(root)) {
            return buildResponse(false, readMessage(root, "No se encontró información para ese DNI."), null);
        }

        JsonNode data = readDataNode(root);
        String numero = firstNonBlank(readText(data, "numero"), dniLimpio);
        String nombres = readText(data, "nombres");
        String apellidoPaterno = firstNonBlank(readText(data, "apellido_paterno"), readText(data, "apellidoPaterno"));
        String apellidoMaterno = firstNonBlank(readText(data, "apellido_materno"), readText(data, "apellidoMaterno"));
        String nombreCompleto = firstNonBlank(
                readText(data, "nombre_completo"),
                readText(data, "nombreCompleto"),
                buildFullName(nombres, apellidoPaterno, apellidoMaterno)
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("numero", numero);
        payload.put("nombres", defaultString(nombres));
        payload.put("apellidoPaterno", defaultString(apellidoPaterno));
        payload.put("apellidoMaterno", defaultString(apellidoMaterno));
        payload.put("nombreCompleto", defaultString(nombreCompleto));

        // Retorna un contrato estable para frontend interno.
        return buildResponse(true, "Consulta de DNI exitosa.", payload);
    }

    public Map<String, Object> consultarTipoCambio(String fecha) {
        // El token es obligatorio para consumir el proveedor.
        if (apiToken.isBlank()) {
            return buildResponse(false, "Token de APIPeru no configurado en APIPERU_TOKEN.", null);
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        if (fecha != null && !fecha.isBlank()) {
            String fechaLimpia = fecha.trim();
            if (!fechaLimpia.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return buildResponse(false, "La fecha debe tener formato YYYY-MM-DD.", null);
            }
            // Se envian ambas claves por compatibilidad con ejemplos del proveedor.
            requestBody.put("date", fechaLimpia);
            requestBody.put("fecha", fechaLimpia);
        }

        // Consulta API externa y normaliza respuesta.
        JsonNode root = executePost(TIPO_CAMBIO_ENDPOINT, requestBody);
        if (root == null) {
            return buildResponse(false, "No se pudo obtener respuesta de APIPeru.", null);
        }

        if (!readSuccess(root)) {
            return buildResponse(false, readMessage(root, "No se pudo consultar el tipo de cambio."), null);
        }

        JsonNode data = readDataNode(root);

        Double compra = firstNonNull(
                readDouble(data, "compra"),
                readDouble(data, "precio_compra"),
                readDouble(data, "purchase"),
                readDouble(data, "sunat.compra"),
                readDouble(root, "compra")
        );
        Double venta = firstNonNull(
                readDouble(data, "venta"),
                readDouble(data, "precio_venta"),
                readDouble(data, "sale"),
                readDouble(data, "sunat.venta"),
                readDouble(root, "venta")
        );

        String fechaConsulta = firstNonBlank(
                readText(data, "fecha_busqueda"),
                readText(data, "fecha"),
                readText(data, "date"),
                readText(root, "fecha")
        );

        String moneda = firstNonBlank(readText(data, "moneda"), "USD");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fecha", defaultString(fechaConsulta));
        payload.put("moneda", moneda);
        payload.put("compra", compra);
        payload.put("venta", venta);

        // Contrato de salida uniforme para UI y servicios internos.
        return buildResponse(true, "Consulta de tipo de cambio exitosa.", payload);
    }

    private JsonNode executePost(String path, Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            // Autenticacion por Bearer token entregado por APIPeru.
            headers.setBearerAuth(apiToken);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiBaseUrl + path,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.hasBody() || response.getBody() == null || response.getBody().isBlank()) {
                return null;
            }
            return objectMapper.readTree(response.getBody());
        } catch (HttpStatusCodeException ex) {
            // Si el proveedor responde con error JSON, intenta parsearlo para mostrar mensaje util.
            try {
                String rawBody = ex.getResponseBodyAsString();
                if (rawBody != null && !rawBody.isBlank()) {
                    return objectMapper.readTree(rawBody);
                }
            } catch (Exception ignored) {
                return null;
            }
        } catch (ResourceAccessException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    private String sanitizeBaseUrl(String url) {
        if (url == null || url.isBlank()) return "https://apiperu.dev/api";
        String value = url.trim();
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private boolean readSuccess(JsonNode root) {
        if (root == null) return false;
        if (root.has("success")) return root.path("success").asBoolean(false);
        if (root.has("estado")) return root.path("estado").asBoolean(false);
        return false;
    }

    private JsonNode readDataNode(JsonNode root) {
        if (root == null) return objectMapper.createObjectNode();
        // Soporta payloads donde "data" viene anidado o en raiz.
        JsonNode data = root.path("data");
        if (!data.isMissingNode() && !data.isNull()) return data;
        return root;
    }

    private String readMessage(JsonNode root, String defaultMessage) {
        return firstNonBlank(
                readText(root, "message"),
                readText(root, "mensaje"),
                defaultMessage
        );
    }

    private String readText(JsonNode source, String path) {
        JsonNode node = readPath(source, path);
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        String value = node.asText();
        return value == null ? null : value.trim();
    }

    private Double readDouble(JsonNode source, String path) {
        JsonNode node = readPath(source, path);
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        if (node.isNumber()) return node.asDouble();
        String text = node.asText();
        if (text == null) return null;
        // Normaliza coma/punto para parsear decimales de distintas fuentes.
        String normalized = text.trim().replace(",", ".");
        if (normalized.isEmpty()) return null;
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private JsonNode readPath(JsonNode source, String path) {
        if (source == null || path == null || path.isBlank()) return null;
        JsonNode current = source;
        String[] parts = path.split("\\.");
        for (String part : parts) {
            if (current == null || current.isNull() || current.isMissingNode()) return null;
            current = current.path(part);
        }
        return current;
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        if (values == null) return null;
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String buildFullName(String nombres, String apellidoPaterno, String apellidoMaterno) {
        String fullName = String.join(" ",
                defaultString(nombres),
                defaultString(apellidoPaterno),
                defaultString(apellidoMaterno)
        ).trim();
        return fullName.isBlank() ? null : fullName;
    }

    private Map<String, Object> buildResponse(boolean success, String message, Map<String, Object> data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}
