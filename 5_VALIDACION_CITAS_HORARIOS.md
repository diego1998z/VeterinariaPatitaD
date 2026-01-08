# 5. VALIDACIÓN DE CITAS (HORARIOS DE MÉDICOS)

## 📅 Cómo se valida que una cita sea válida

---

## TIPOS DE VALIDACIÓN

Cuando un usuario intenta agendar una cita, se validan varios aspectos:

```
1. ¿Existe el servicio?           (ServiceVet)
2. ¿Existe el médico?              (Medico)
3. ¿La hora está en horario del médico? (horarioInicio - horarioFin)
4. ¿La hora ya está ocupada?       (AppointmentRepository)
5. ¿La fecha es futuro?            (LocalDate.now())
```

---

## FLUJO COMPLETO DE VALIDACIÓN

```
USUARIO EN NAVEGADOR (simulador_citas.html)
├─ Selecciona servicio: "Vacunación" (ID 1)
├─ Selecciona médico: "Dr. López" (ID 5)
├─ Selecciona fecha: 15/12/2025
├─ Selecciona hora: 10:00 AM
├─ Ingresa nombre cliente: "Juan Pérez"
└─ Presiona "Agendar Cita"

    ↓

POST /book_appointment
Parameters:
  serviceId = 1
  medicoId = 5
  date = 2025-12-15
  time = 10:00:00
  clientName = "Juan Pérez"

    ↓

CONTROLLER (AppointmentController.java) RECIBE:
@PostMapping("/book_appointment")
public String bookAppointment(
    @RequestParam int serviceId,        // 1
    @RequestParam Long medicoId,        // 5
    @RequestParam LocalDate date,       // 2025-12-15
    @RequestParam LocalTime time,       // 10:00:00
    @RequestParam String clientName,    // "Juan Pérez"
    Model model) {
    
    // Combina date + time en un LocalDateTime
    LocalDateTime appointmentDateTime = 
        LocalDateTime.of(date, time);   // 2025-12-15T10:00:00

    // VALIDACIÓN 1: ¿La hora ya está ocupada?
    boolean isAvailable = appointmentService.isAvailable(appointmentDateTime);

    if (!isAvailable) {
        model.addAttribute("message", 
            "Error: ¡La hora seleccionada ya está ocupada!");
        model.addAttribute("messageType", "danger");
        return showAppointmentSimulator(model);  // Recarga la vista
    }

    // VALIDACIÓN PASADA, procede a agendar
    boolean success = appointmentService.bookAppointment(
        serviceId,
        medicoId,
        appointmentDateTime,
        clientName
    );

    ↓

SERVICE (AppointmentService.java) REALIZA VALIDACIONES:
public boolean bookAppointment(int serviceId, Long medicoId, 
                               LocalDateTime appointmentDateTime, 
                               String clientName) {
    
    // VALIDACIÓN 2: ¿Existe el servicio?
    ServiceVet service = serviceRepository.findById(serviceId)
        .orElse(null);
    if (service == null) {
        System.out.println("⛔ Servicio no encontrado");
        return false;  // ← Falla
    }

    // VALIDACIÓN 3: ¿Existe el médico?
    Medico medico = medicoRepository.findById(medicoId)
        .orElse(null);
    if (medico == null) {
        System.out.println("⛔ Médico no encontrado");
        return false;  // ← Falla
    }

    // VALIDACIÓN 4: ¿LA HORA ESTÁ DENTRO DEL HORARIO DEL MÉDICO?
    LocalTime inicio = medico.getHorarioInicio();      // 08:00:00
    LocalTime fin = medico.getHorarioFin();            // 17:00:00
    LocalTime horaCita = appointmentDateTime.toLocalTime();  // 10:00:00

    // Ejemplo de BD:
    // Medico(id=5, nombre="Dr. López", 
    //        horarioInicio=08:00:00, horarioFin=17:00:00)

    // Comprueba: ¿10:00:00 es antes de 08:00:00?
    if (horaCita.isBefore(inicio)) {  // ✗ No
        System.out.println("⛔ Hora antes del horario del médico");
        return false;
    }

    // Comprueba: ¿10:00:00 es después de 17:00:00?
    if (horaCita.isAfter(fin)) {  // ✗ No
        System.out.println("⛔ Hora después del horario del médico");
        return false;
    }

    // ✓ 10:00:00 está entre 08:00:00 y 17:00:00 → VÁLIDO

    // VALIDACIÓN 5: ¿LA HORA YA ESTÁ OCUPADA?
    if (!isAvailable(appointmentDateTime)) {
        // ↓ Llama a repository
        System.out.println("⛔ Hora ya ocupada");
        return false;
    }

    // ✓ TODAS LAS VALIDACIONES PASARON

    // CREAR Y GUARDAR LA CITA
    Appointment appointment = new Appointment(
        service,
        appointmentDateTime,
        clientName
    );
    appointment.setMedico(medico);
    
    appointmentRepository.save(appointment);
    // ↓ SQL: INSERT INTO appointments (service_id, medico_id, 
    //        appointment_date_time, client_name)
    //        VALUES (1, 5, '2025-12-15 10:00:00', 'Juan Pérez')

    System.out.println("✅ Cita registrada correctamente para " + clientName);
    return true;  // ← Éxito
}

// VALIDACIÓN DE DISPONIBILIDAD
public boolean isAvailable(LocalDateTime dateTime) {
    // Busca en la BD si existe una cita en esa hora
    return !appointmentRepository.existsByDateTime(dateTime);
    
    // Si existe: existsByDateTime retorna true → isAvailable retorna false
    // Si NO existe: existsByDateTime retorna false → isAvailable retorna true
}

    ↓

REPOSITORY ACCEDE A BD:
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Busca si existe cita en esa hora
    // SQL: SELECT COUNT(*) FROM appointments 
    //      WHERE appointment_date_time = ?
    boolean existsByDateTime(LocalDateTime dateTime);
}

BD (MySQL) COMPRUEBA:
SELECT COUNT(*) FROM appointments 
WHERE appointment_date_time = '2025-12-15 10:00:00';

Resultado: 0 registros
    ↓ existsByDateTime retorna false
    ↓ isAvailable retorna true
    ↓ Cita puede agendarse

    ↓

CONTROLLER RECIBE RESPUESTA (success = true):
if (success) {
    model.addAttribute("message", 
        "¡Éxito! Su cita ha sido agendada con el servicio de " + 
        appointmentService.getServiceById(1).getName() + 
        " para el 15-12-2025 10:00");
    model.addAttribute("messageType", "success");
} else {
    model.addAttribute("message", 
        "Error: No se pudo agendar la cita...");
    model.addAttribute("messageType", "danger");
}

return showAppointmentSimulator(model);  // Recarga vista

    ↓

THYMELEAF RENDERIZA CONFIRMACIÓN:
<div class="alert alert-success" th:if="${messageType == 'success'}">
    <span th:text="${message}"></span>
</div>

    ↓

USUARIO VE EN NAVEGADOR:
✓ "¡Éxito! Su cita ha sido agendada con el servicio de 
   Vacunación para el 15-12-2025 10:00"
```

---

## COMPONENTES CLAVE

### 1️⃣ MODELO: Medico.java

```java
@Entity
@Table(name = "medico")
public class Medico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;              // "Dr. López"
    private String especialidad;        // "Medicina General"
    
    // HORARIOS: Los tiempos de trabajo del médico
    private LocalTime horarioInicio;    // 08:00:00
    private LocalTime horarioFin;       // 17:00:00
    
    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private ServiceVet servicio;

    // Constructor
    public Medico(String nombre, String especialidad, 
                  LocalTime horarioInicio, LocalTime horarioFin) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
    }

    // Getters
    public LocalTime getHorarioInicio() { return horarioInicio; }
    public LocalTime getHorarioFin() { return horarioFin; }
}
```

### 2️⃣ MODELO: Appointment.java

```java
@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceVet service;  // ← Servicio (vacunación, consulta, etc)
    
    @ManyToOne
    @JoinColumn(name = "medico_id")
    private Medico medico;       // ← Médico asignado (Dr. López)
    
    private LocalDateTime appointmentDateTime;  // 2025-12-15T10:00:00
    
    private String clientName;   // "Juan Pérez"

    // Constructor
    public Appointment(ServiceVet service, LocalDateTime appointmentDateTime, 
                      String clientName) {
        this.service = service;
        this.appointmentDateTime = appointmentDateTime;
        this.clientName = clientName;
    }

    // Getters
    public ServiceVet getService() { return service; }
    public Medico getMedico() { return medico; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
}
```

### 3️⃣ SERVICIO: AppointmentService.java

```java
@Service
public class AppointmentService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    // Verificar disponibilidad
    public boolean isAvailable(LocalDateTime dateTime) {
        return !appointmentRepository.existsByDateTime(dateTime);
    }

    // Agendar cita con validaciones
    public boolean bookAppointment(int serviceId, Long medicoId, 
                                   LocalDateTime appointmentDateTime, 
                                   String clientName) {
        
        // VALIDACIÓN 1: ¿Existe el servicio?
        ServiceVet service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) {
            return false;
        }

        // VALIDACIÓN 2: ¿Existe el médico?
        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        if (medico == null) {
            return false;
        }

        // VALIDACIÓN 3: ¿La hora está dentro del horario del médico?
        LocalTime inicio = medico.getHorarioInicio();
        LocalTime fin = medico.getHorarioFin();
        LocalTime horaCita = appointmentDateTime.toLocalTime();

        // ¿Antes del horario?
        if (horaCita.isBefore(inicio)) {
            System.out.println("⛔ Hora antes del horario del médico");
            return false;
        }

        // ¿Después del horario?
        if (horaCita.isAfter(fin)) {
            System.out.println("⛔ Hora después del horario del médico");
            return false;
        }

        // VALIDACIÓN 4: ¿La hora ya está ocupada?
        if (!isAvailable(appointmentDateTime)) {
            System.out.println("⛔ Hora ya ocupada");
            return false;
        }

        // ✓ TODAS LAS VALIDACIONES PASARON

        // Crear y guardar cita
        Appointment appointment = new Appointment(service, appointmentDateTime, clientName);
        appointment.setMedico(medico);
        appointmentRepository.save(appointment);

        System.out.println("✅ Cita registrada para " + clientName);
        return true;
    }

    // Obtener servicios disponibles
    public List<ServiceVet> getAvailableServices() {
        return serviceRepository.findAll();
    }

    // Obtener médicos disponibles
    public List<Medico> getAllMedicos() {
        return medicoRepository.findAll();
    }

    // Obtener citas agendadas
    public List<Appointment> getBookedAppointments() {
        return appointmentRepository.findAll();
    }
}
```

### 4️⃣ CONTROLADOR: AppointmentController.java

```java
@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Mostrar formulario
    @GetMapping("/simulador_citas")
    public String showAppointmentSimulator(Model model) {
        
        // Obtiene datos para rellenar los dropdowns
        model.addAttribute("availableServices", 
            appointmentService.getAvailableServices());
        model.addAttribute("allMedicos", 
            appointmentService.getAllMedicos());
        model.addAttribute("bookedAppointments", 
            appointmentService.getBookedAppointments());

        // Fecha mínima: mañana (no se puede agendar para hoy o pasado)
        LocalDate minDate = LocalDate.now().plusDays(1);
        model.addAttribute("minDate", minDate.format(DateTimeFormatter.ISO_DATE));

        return "simulador_citas";
    }

    // Procesar agendamiento
    @PostMapping("/book_appointment")
    public String bookAppointment(
            @RequestParam("serviceId") int serviceId,
            @RequestParam("medicoId") Long medicoId,
            @RequestParam("date") LocalDate date,
            @RequestParam("time") LocalTime time,
            @RequestParam("clientName") String clientName,
            Model model) {

        // Combina date + time
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

        // VALIDACIÓN 1: ¿La hora está disponible?
        boolean isAvailable = appointmentService.isAvailable(appointmentDateTime);

        if (!isAvailable) {
            model.addAttribute("message", 
                "Error: ¡Lo sentimos! La hora seleccionada (" + 
                appointmentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + 
                ") ya está ocupada. Por favor, elige otro horario.");
            model.addAttribute("messageType", "danger");
        } else {
            
            // PROCEDE A AGENDAR
            boolean success = appointmentService.bookAppointment(
                serviceId, 
                medicoId, 
                appointmentDateTime, 
                clientName
            );

            if (success) {
                model.addAttribute("message", 
                    "¡Éxito! Su cita ha sido agendada con el servicio de " + 
                    appointmentService.getServiceById(serviceId).getName() + 
                    " para el " + 
                    appointmentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + ".");
                model.addAttribute("messageType", "success");
            } else {
                model.addAttribute("message", 
                    "Error: No se pudo agendar la cita. El servicio, médico, u horario podría no ser válido.");
                model.addAttribute("messageType", "danger");
            }
        }

        // Recarga la vista con el mensaje
        return showAppointmentSimulator(model);
    }
}
```

### 5️⃣ REPOSITORIOS

```java
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // ¿Existe una cita en esa hora exacta?
    // SQL: SELECT COUNT(*) FROM appointments WHERE appointment_date_time = ?
    boolean existsByDateTime(LocalDateTime dateTime);

    // Contar citas futuras (pendientes)
    // SQL: SELECT COUNT(*) FROM appointments WHERE appointment_date_time > NOW()
    Long countByDateTimeAfter(LocalDateTime dateTime);

    // Obtener citas en un rango de fechas
    // SQL: SELECT * FROM appointments WHERE appointment_date_time BETWEEN ? AND ?
    List<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    // Contar citas por mes
    // SQL: SELECT YEAR(...), MONTH(...), COUNT(*) FROM appointments GROUP BY YEAR, MONTH
    List<Object[]> countAppointmentsPerMonth();
}

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    // JPA genera CRUD automáticamente
}

public interface ServiceRepository extends JpaRepository<ServiceVet, Integer> {
    // JPA genera CRUD automáticamente
}
```

---

## EJEMPLO PRÁCTICO: VALIDACIÓN PASO A PASO

### Caso 1: Cita EXITOSA

```
Entrada:
  serviceId = 1 (Vacunación)
  medicoId = 5 (Dr. López)
  date = 2025-12-15
  time = 10:00:00
  clientName = "Juan Pérez"

Validación 1: ¿Servicio existe?
  SELECT * FROM servicios WHERE id = 1
  Result: ServiceVet(id=1, name="Vacunación", price=50)
  ✓ Existe

Validación 2: ¿Médico existe?
  SELECT * FROM medico WHERE id = 5
  Result: Medico(id=5, nombre="Dr. López", horarioInicio=08:00:00, horarioFin=17:00:00)
  ✓ Existe

Validación 3: ¿Hora en horario del médico?
  Hora de cita: 10:00:00
  Horario inicio: 08:00:00
  Horario fin: 17:00:00
  ¿10:00:00 es después de 08:00:00? ✓ Sí
  ¿10:00:00 es antes de 17:00:00? ✓ Sí
  ✓ Dentro del horario

Validación 4: ¿Hora disponible?
  SELECT COUNT(*) FROM appointments 
  WHERE appointment_date_time = '2025-12-15 10:00:00'
  Result: 0 (no hay cita en esa hora)
  ✓ Disponible

Acción: INSERTAR CITA
  INSERT INTO appointments (service_id, medico_id, appointment_date_time, client_name)
  VALUES (1, 5, '2025-12-15 10:00:00', 'Juan Pérez')

Resultado: ✓ ÉXITO
```

### Caso 2: Cita FALLIDA (fuera de horario)

```
Entrada:
  serviceId = 1
  medicoId = 5
  date = 2025-12-15
  time = 18:00:00  ← Después del horario (fin=17:00:00)
  clientName = "María López"

Validación 3: ¿Hora en horario del médico?
  Hora de cita: 18:00:00
  Horario inicio: 08:00:00
  Horario fin: 17:00:00
  ¿18:00:00 es después de 17:00:00? ✓ Sí
  horaCita.isAfter(fin) = true
  ⛔ FALLA: Hora fuera del horario del médico

Resultado: ✗ FALLA
  Mensaje: "Error: No se pudo agendar la cita. El servicio, médico, u horario podría no ser válido."
```

### Caso 3: Cita FALLIDA (hora ocupada)

```
Entrada:
  serviceId = 1
  medicoId = 5
  date = 2025-12-15
  time = 10:00:00
  clientName = "Pedro García"

Validación 4: ¿Hora disponible?
  SELECT COUNT(*) FROM appointments 
  WHERE appointment_date_time = '2025-12-15 10:00:00'
  Result: 1 (ya hay cita en esa hora - Juan Pérez)
  existsByDateTime retorna true
  isAvailable retorna false
  ⛔ FALLA: Hora ocupada

Resultado: ✗ FALLA
  Mensaje: "Error: ¡Lo sentimos! La hora seleccionada (15-12-2025 10:00) 
            ya está ocupada. Por favor, elige otro horario."
```

---

## BASE DE DATOS: ESTRUCTURA

```sql
CREATE TABLE medico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100),
    horario_inicio TIME NOT NULL,  -- 08:00:00
    horario_fin TIME NOT NULL,     -- 17:00:00
    servicio_id INT
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id INT,
    medico_id BIGINT,
    appointment_date_time DATETIME NOT NULL UNIQUE,  -- 2025-12-15 10:00:00
    client_name VARCHAR(100),
    FOREIGN KEY (service_id) REFERENCES servicios(id),
    FOREIGN KEY (medico_id) REFERENCES medico(id)
);

-- Ejemplo de datos:
INSERT INTO medico VALUES (5, 'Dr. López', 'Medicina General', '08:00:00', '17:00:00', 1);
INSERT INTO appointments VALUES (1, 1, 5, '2025-12-15 10:00:00', 'Juan Pérez');
```

---

## VALIDACIONES VISUALES

```
┌─────────────────────────────────────┐
│ VALIDACIÓN 1: Servicio              │
├─────────────────────────────────────┤
│ Existe en BD? → ✓ o ✗               │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ VALIDACIÓN 2: Médico                │
├─────────────────────────────────────┤
│ Existe en BD? → ✓ o ✗               │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ VALIDACIÓN 3: Horario del Médico    │
├─────────────────────────────────────┤
│ Inicio: 08:00 | Fin: 17:00          │
│ Hora solicitada: 10:00              │
│ ¿10:00 >= 08:00? ✓                  │
│ ¿10:00 <= 17:00? ✓                  │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ VALIDACIÓN 4: Disponibilidad        │
├─────────────────────────────────────┤
│ ¿Hay otra cita a las 10:00? ✗       │
│ → Hora disponible ✓                 │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ RESULTADO: CITA AGENDADA ✓          │
└─────────────────────────────────────┘
```

---

## RESPUESTAS PARA DEFENSA

### Pregunta: "¿Cómo se valida que una cita sea válida?"

**Respuesta:**
"Se validan cuatro cosas en orden. Primero, verifico que el servicio y el médico existan en la BD. Segundo, extraigo el LocalTime del médico (horarioInicio y horarioFin) y compruebo que la hora de la cita esté entre esos horarios usando isBefore() e isAfter(). Tercero, consulto la BD para verificar que no haya otra cita en esa hora exacta usando existsByDateTime(). Si todas pasan, agendo la cita."

### Pregunta: "¿Por qué separas validaciones en el controlador y el servicio?"

**Respuesta:**
"El controlador valida disponibilidad rápidamente (existsByDateTime) antes de pasar al servicio, como una validación inicial. El servicio valida todo lo demás (servicio existe, médico existe, horario del médico). Así es más eficiente: si la hora no está disponible, ni siquiera llamamos al servicio."

### Pregunta: "¿Qué pasa si dos usuarios agendan la misma hora simultáneamente?"

**Respuesta:**
"Es un problema de concurrencia. Para evitarlo, la columna appointment_date_time debería tener UNIQUE constraint en la BD. Así, si dos inserts suceden simultáneamente, la BD rechaza uno. También podríamos usar transacciones con PESSIMISTIC_LOCK o añadir un double-check en el servicio."

### Pregunta: "¿Cómo se asegura que un médico no tenga citas superpuestas?"

**Respuesta:**
"Cada cita se asocia con un médico específico (medico_id). Cuando agendo una cita, no solo verifico que esa hora esté disponible en general, sino que el médico en cuestión no tenga otra cita a esa hora. Aunque actualmente no tengo un query específico, el UNIQUE en appointment_date_time también ayuda."

### Pregunta: "¿Cómo se maneja el cambio de horario del médico (si trabaja diferente el lunes que el jueves)?"

**Respuesta:**
"En el modelo actual, cada médico tiene un horarioInicio y horarioFin único para todo el día. Para horarios complejos, necesitaría una tabla separada HorarioMedico con días de la semana (lunes=08:00-17:00, sábado cerrado, etc.). Luego, en la validación, buscaría el horario según el día de la semana de la cita solicitada."

---

## TABLA RESUMEN

| Validación | Componente | SQL | Resultado |
|-----------|-----------|-----|-----------|
| **Servicio existe** | Service | SELECT FROM servicios WHERE id=? | ✓/✗ |
| **Médico existe** | Service | SELECT FROM medico WHERE id=? | ✓/✗ |
| **Hora en horario** | Service | LocalTime.isBefore/isAfter() | ✓/✗ |
| **Hora disponible** | Controller/Repository | SELECT COUNT FROM appointments WHERE datetime=? | ✓/✗ |

