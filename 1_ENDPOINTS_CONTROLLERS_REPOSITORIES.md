# 1. ENDPOINTS - CONTROLADORES - REPOSITORIOS (Flujo Completo)

## 🔄 Cómo están conectados los tres componentes

---

## ESTRUCTURA GENERAL

```
USUARIO (Navegador)
    ↓
PETICIÓN HTTP
    ↓
CONTROLLER (RecibeLa petición)
    ↓
SERVICE (Procesa lógica)
    ↓
REPOSITORY (Accede a BD)
    ↓
MODELO (Datos retornan)
    ↓
CONTROLLER (Retorna respuesta)
    ↓
VIEW (HTML renderizado)
    ↓
USUARIO (Ve resultado)
```

---

## EJEMPLO 1: AGREGAR PRODUCTO AL CARRITO

### El flujo completo paso a paso:

```
USUARIO en navegador
├─ Ve producto "Vitamina para perros"
├─ Selecciona cantidad: 2
└─ Presiona botón "Agregar al carrito"

    ↓

POST /add_to_cart?productId=1&quantity=2 (HTTP)

    ↓

CONTROLLER (SalesController.java) RECIBE:
@PostMapping("/add_to_cart")
public String addToCart(
    @RequestParam int productId,        // ← 1
    @RequestParam int quantity,         // ← 2
    @ModelAttribute("cart") List<Product> cart,
    Model model) {
    
    String message = salesService.addProductToCart(1, 2, cart);
    // ← Llama al servicio

    ↓

SERVICE (SalesService.java) PROCESA:
public String addProductToCart(int productId, int quantity, List<Product> cart) {
    
    // VALIDACIÓN 1: ¿El producto existe?
    Optional<Product> productOpt = productRepository.findById(productId);
    //                            ↑
    //                   REPOSITORY accede a BD
    //                   SQL: SELECT * FROM products WHERE id=1
    
    if (productOpt.isEmpty()) {
        return "Error: Producto no encontrado";
    }
    
    Product product = productOpt.get();  // Obtiene el producto
    
    // VALIDACIÓN 2: ¿Hay stock suficiente?
    if (quantity > product.getStock()) {
        return "Error: Stock insuficiente";
    }
    
    // TODO OK: Agrega al carrito
    cart.add(product);
    return "Producto agregado al carrito";
}

    ↓

REPOSITORY (ProductRepository.java):
public interface ProductRepository extends JpaRepository<Product, Long> {
    // ← JPA genera automáticamente el SQL
    // No escribimos SQL, solo métodos
}

BD (MySQL):
SELECT * FROM products WHERE id=1
Result: { id: 1, name: "Vitamina", price: 150.00, stock: 5 }

    ↓

CONTROLLER RECIBE RESPUESTA:
model.addAttribute("message", "Producto agregado al carrito");
return "redirect:/simulador_ventas";

    ↓

VIEW (simulador_ventas.html) RENDERIZA:
<div th:if="${message != null}" class="alert alert-success">
    <span th:text="${message}"></span>
</div>

    ↓

USUARIO ve en navegador:
✓ "Producto agregado al carrito"
```

---

## CONEXIONES CLAVE EN TU PROYECTO

### CONTROLADOR → SERVICIO

```java
@Controller
@SessionAttributes({"cart", "products"})
public class SalesController {
    
    private final SalesService salesService;  // ← Inyección
    
    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }
    
    @PostMapping("/add_to_cart")
    public String addToCart(...) {
        String message = salesService.addProductToCart(...);  // ← Llama
        return "redirect:/simulador_ventas";
    }
}
```

### SERVICIO → REPOSITORY

```java
@Service
public class SalesService {
    
    @Autowired
    private ProductRepository productRepository;  // ← Inyección
    
    public String addProductToCart(int productId, int quantity, List<Product> cart) {
        Optional<Product> productOpt = productRepository.findById(productId);  // ← Llama
        // ...
    }
}
```

### REPOSITORY → BD

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JPA automáticamente genera:
    // findById(Long id)
    // findAll()
    // save(Product)
    // delete(Product)
    // etc
}
```

---

## EJEMPLO 2: AGENDAR CITA

### Flujo de AppointmentController:

```
USUARIO
├─ Selecciona servicio: "Vacunación"
├─ Selecciona médico: "Dr. López"
├─ Selecciona fecha: 15/12/2025
├─ Selecciona hora: 10:00 AM
└─ Presiona "Agendar"

    ↓

POST /book_appointment [datos]

    ↓

CONTROLLER (AppointmentController.java) RECIBE:
@PostMapping("/book_appointment")
public String bookAppointment(
    @RequestParam int serviceId,
    @RequestParam Long medicoId,
    @RequestParam LocalDate date,
    @RequestParam LocalTime time,
    @RequestParam String clientName,
    Model model) {
    
    LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
    
    // Llama al servicio para verificar disponibilidad
    boolean isAvailable = appointmentService.isAvailable(appointmentDateTime);
    //                    ↑
    //                    SERVICE

    ↓

SERVICE (AppointmentService.java) VERIFICA:
public boolean isAvailable(LocalDateTime dateTime) {
    // Busca citas en ese horario
    List<Appointment> conflicting = appointmentRepository
        .findByAppointmentDateTime(dateTime);
    //       ↑
    //       REPOSITORY → BD
    
    return conflicting.isEmpty();
}

public boolean bookAppointment(int serviceId, Long medicoId, 
                               LocalDateTime dateTime, String clientName) {
    // Crea nueva cita
    Appointment appointment = new Appointment();
    appointment.setServiceId(serviceId);
    appointment.setMedicoId(medicoId);
    appointment.setAppointmentDateTime(dateTime);
    appointment.setClientName(clientName);
    
    // Guarda en BD
    appointmentRepository.save(appointment);
    //                    ↑
    //                    SQL: INSERT INTO appointments (...)
    
    return true;
}

    ↓

REPOSITORY:
public interface AppointmentRepository 
    extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByAppointmentDateTime(LocalDateTime dateTime);
}

BD (MySQL):
INSERT INTO appointments (service_id, medico_id, appointment_date_time, client_name)
VALUES (1, 5, '2025-12-15 10:00:00', 'Juan Pérez');

    ↓

CONTROLLER RECIBE RESPUESTA:
if (success) {
    model.addAttribute("message", "¡Cita agendada exitosamente!");
    model.addAttribute("messageType", "success");
} else {
    model.addAttribute("message", "Error al agendar cita");
    model.addAttribute("messageType", "danger");
}

return "redirect:/simulador_citas";

    ↓

USUARIO ve éxito o error
```

---

## LISTA COMPLETA DE ENDPOINTS EN TU PROYECTO

| Endpoint | Método | Controlador | Qué hace |
|----------|--------|-------------|----------|
| `/login` | GET | AuthController | Muestra formulario de login |
| `/login` | POST | AuthController | Procesa login (valida credenciales) |
| `/simulador_ventas` | GET | SalesController | Muestra página de compras |
| `/add_to_cart` | POST | SalesController | Agrega producto al carrito |
| `/remove_from_cart` | POST | SalesController | Elimina producto del carrito |
| `/simulador_citas` | GET | AppointmentController | Muestra página de citas |
| `/book_appointment` | POST | AppointmentController | Agrega cita |
| `/dashboard` | GET | DashboardController | Muestra estadísticas |

---

## INYECCIÓN DE DEPENDENCIAS

### ¿Qué es?

Spring automáticamente "inyecta" servicios y repositorios en controladores.

### Cómo funciona en tu código:

```java
@Controller
public class SalesController {
    
    private final SalesService salesService;  // ← Declara
    
    public SalesController(SalesService salesService) {  // ← Constructor
        this.salesService = salesService;
    }
    
    // Ahora puede usar salesService en cualquier método
}
```

### Por qué es importante:

✓ Evita crear nuevas instancias manualmente
✓ Facilita testing (puedes inyectar mocks)
✓ Código más limpio y organizado
✓ Spring maneja el ciclo de vida

---

## RESPUESTAS PARA DEFENSA

### Pregunta: "¿Cómo fluyen los datos desde la vista hasta la BD?"

**Respuesta:**
"El usuario interactúa con la vista (HTML). Al hacer clic, se envía una petición HTTP al controlador. El controlador recibe los parámetros y los pasa al servicio. El servicio contiene la lógica: valida datos, consulta el repositorio si es necesario. El repositorio usa JPA para acceder a la BD con queries automáticas. Los resultados regresan al servicio, luego al controlador, que los pasa al Model. Finalmente, Thymeleaf renderiza la vista con los datos."

---

### Pregunta: "¿Por qué separas en Controlador, Servicio y Repository?"

**Respuesta:**
"Siguiendo el patrón MVC y principios de responsabilidad única. El Controlador solo maneja HTTP. El Servicio tiene la lógica de negocio (validaciones, cálculos). El Repository solo accede a datos. Si necesito reutilizar la lógica, puedo llamar al mismo servicio desde otro controlador. Si quiero cambiar la BD, solo cambio el repository."

---

### Pregunta: "¿Cómo llaman entre sí estos componentes?"

**Respuesta:**
"Por inyección de dependencias. Spring automáticamente crea instancias de los servicios y las inyecta en los controladores. Los servicios inyectan repositorios. Cada componente llama al siguiente a través de métodos públicos."

---

## RESUMEN VISUAL

```
Navegador (Usuario)
    ↓
@GetMapping/@PostMapping (Endpoint)
    ↓
@Controller (Recibe petición)
    ↓
@Service (Procesa lógica)
    ↓
@Repository (Accede BD)
    ↓
JPA/Hibernate (Ejecuta SQL)
    ↓
MySQL BD (Datos reales)
    ↓
(Vuelta inversa)
    ↓
Respuesta HTTP
    ↓
Thymeleaf (Renderiza HTML)
    ↓
Navegador (Usuario ve resultado)
```

