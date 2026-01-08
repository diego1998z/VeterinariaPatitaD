# 3. DASHBOARD: ESTADÍSTICAS Y MÉTRICAS

## 📊 Cómo funciona el panel de control (Dashboard)

---

## ¿QUÉ ES UN DASHBOARD?

Un **dashboard** es una página que muestra **resúmenes visuales y métricas** del negocio:
- Total de ventas del día
- Ingresos de citas hoy
- Citas pendientes
- Gráficos de ventas mensuales
- Gráficos de citas mensuales

En tu proyecto: Se actualiza en **TIEMPO REAL** consultando la BD

---

## FLUJO COMPLETO DEL DASHBOARD

```
USUARIO NAVEGA A /dashboard

    ↓

NAVEGADOR ENVÍA GET /dashboard

    ↓

CONTROLLER (DashboardController.java) RECIBE:
@GetMapping("/dashboard")
public String showDashboard(Model model) {
    
    // Inyecta servicios
    SalesService salesService;
    AppointmentService appointmentService;

    ↓

SERVICE (SalesService.java) CALCULA:
1. getTotalVentasHoy()
   └─ Busca ventas de hoy
   └─ Suma los totales

2. getVentasPorMes()
   └─ Busca ventas de cada mes
   └─ Crea mapa: {Enero: 1500, Febrero: 2300, ...}

SERVICE (AppointmentService.java) CALCULA:
1. contarCitasPendientes()
   └─ Cuenta citas futuras

2. getCitasPorMes()
   └─ Cuenta citas por mes
   └─ Crea mapa: {Enero: 10, Febrero: 15, ...}

3. getIngresosCitasHoy()
   └─ Suma precios de servicios de citas de hoy

    ↓

REPOSITORIES ACCEDEN A BD:
- SaleRepository.sumTotalByDate(startOfDay, endOfDay)
  └─ SELECT SUM(total) FROM sales WHERE created_at BETWEEN ? AND ?
  
- SaleRepository.findTotalSalesPerMonth()
  └─ SELECT YEAR(created_at), MONTH(created_at), SUM(total) FROM sales GROUP BY YEAR, MONTH

- AppointmentRepository.countByDateTimeAfter(LocalDateTime.now())
  └─ SELECT COUNT(*) FROM appointments WHERE appointment_date_time > NOW()

- AppointmentRepository.countAppointmentsPerMonth()
  └─ SELECT YEAR(appointment_date_time), MONTH(appointment_date_time), COUNT(*)
     FROM appointments GROUP BY YEAR, MONTH

- AppointmentRepository.findByDateTimeBetween(startOfDay, endOfDay)
  └─ SELECT * FROM appointments 
     WHERE appointment_date_time BETWEEN ? AND ?

    ↓

BD (MySQL) RETORNA RESULTADOS:

Tabla Sales:
┌────┬───────┬─────────────────────┐
│ id │ total │ created_at          │
├────┼───────┼─────────────────────┤
│ 1  │ 94.40 │ 2025-12-10 10:30:00 │
│ 2  │ 150.50│ 2025-12-10 14:30:00 │
│ 3  │ 200   │ 2025-12-09 11:00:00 │
└────┴───────┴─────────────────────┘

Total Hoy: 94.40 + 150.50 = 244.90

Tabla Appointments:
┌────┬──────────────────────────┬────────────┐
│ id │ appointment_date_time    │ service_id │
├────┼──────────────────────────┼────────────┤
│ 1  │ 2025-12-10 09:00:00      │ 1 (S/. 50) │
│ 2  │ 2025-12-10 10:30:00      │ 2 (S/. 75) │
│ 3  │ 2025-12-11 15:00:00      │ 1 (S/. 50) │
└────┴──────────────────────────┴────────────┘

Citas Pendientes: 2 (IDs 2, 3 están en el futuro)
Ingresos Citas Hoy: 50 + 75 = 125

    ↓

SERVICE CONVIERTE A JSON (ObjectMapper):
ventasMap = {
    "Enero": 1500.00,
    "Febrero": 2300.50,
    "Diciembre": 244.90
}

ventasJson = '{"Enero":1500.00,"Febrero":2300.50,"Diciembre":244.90}'

citasMap = {
    "Enero": 10,
    "Febrero": 15,
    "Diciembre": 2
}

citasJson = '{"Enero":10,"Febrero":15,"Diciembre":2}'

    ↓

CONTROLLER AGREGA AL MODEL:
model.addAttribute("totalVentasHoy", 244.90);
model.addAttribute("citasPendientes", 2L);
model.addAttribute("ingresosCitasHoy", 125.00);
model.addAttribute("ventasJson", ventasJson);
model.addAttribute("citasJson", citasJson);

return "dashboard";

    ↓

THYMELEAF (dashboard.html) RENDERIZA:
<div class="metric-card">
    <h3>Ventas Hoy</h3>
    <p th:text="'S/. ' + ${totalVentasHoy}"></p>
</div>
<!-- Resultado: S/. 244.90 -->

<div class="metric-card">
    <h3>Citas Pendientes</h3>
    <p th:text="${citasPendientes}"></p>
</div>
<!-- Resultado: 2 -->

<canvas id="ventasChart"></canvas>
<script>
    const ventasData = ${ventasJson};  // ← JSON para gráfico
    // ventasData = {"Enero":1500.00,"Febrero":2300.50,"Diciembre":244.90}
    
    new Chart(ventasChart, {
        type: 'bar',
        data: {
            labels: Object.keys(ventasData),  // ["Enero", "Febrero", "Diciembre"]
            datasets: [{
                label: 'Ventas Mensuales',
                data: Object.values(ventasData)  // [1500, 2300.50, 244.90]
            }]
        }
    });
</script>

    ↓

NAVEGADOR RENDERIZA DASHBOARD:
┌────────────────────────────────────────┐
│ DASHBOARD - Clínica Veterinaria Patitas│
├────────────────────────────────────────┤
│ 📊 Ventas Hoy: S/. 244.90              │
│ 💰 Ingresos Citas Hoy: S/. 125.00      │
│ ⏳ Citas Pendientes: 2                   │
├────────────────────────────────────────┤
│ [Gráfico Barras - Ventas Mensuales]    │
│ Enero: 1500 | Febrero: 2300 | Dic: 245│
├────────────────────────────────────────┤
│ [Gráfico Barras - Citas Mensuales]     │
│ Enero: 10 | Febrero: 15 | Dic: 2       │
└────────────────────────────────────────┘
```

---

## COMPONENTES CLAVE

### 1️⃣ CONTROLADOR: DashboardController.java

```java
@Controller
public class DashboardController {

    private final SalesService salesService;
    private final AppointmentService appointmentService;

    @Autowired
    public DashboardController(SalesService salesService, 
                               AppointmentService appointmentService) {
        this.salesService = salesService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        ObjectMapper mapper = new ObjectMapper();  // Convierte a JSON

        try {
            // MÉTRICA 1: Ventas de hoy
            Double totalVentasHoy = salesService.getTotalVentasHoy();
            if (totalVentasHoy == null) totalVentasHoy = 0.0;
            
            // MÉTRICA 2: Ingresos de citas hoy
            Double ingresosCitasHoy = appointmentService.getIngresosCitasHoy();
            if (ingresosCitasHoy == null) ingresosCitasHoy = 0.0;

            // MÉTRICA 3: Ventas por mes (para gráfico)
            Map<String, Double> ventasMap = salesService.getVentasPorMes();
            if (ventasMap == null) ventasMap = Collections.emptyMap();

            // MÉTRICA 4: Citas pendientes
            Long citasPendientes = appointmentService.contarCitasPendientes();
            if (citasPendientes == null) citasPendientes = 0L;

            // MÉTRICA 5: Citas por mes (para gráfico)
            Map<String, Long> citasMap = appointmentService.getCitasPorMes();
            if (citasMap == null) citasMap = Collections.emptyMap();

            // Convierte mapas a JSON para JavaScript
            String ventasJson = mapper.writeValueAsString(ventasMap);
            String citasJson = mapper.writeValueAsString(citasMap);

            // Agrega al modelo (Thymeleaf puede acceder)
            model.addAttribute("totalVentasHoy", totalVentasHoy);
            model.addAttribute("citasPendientes", citasPendientes);
            model.addAttribute("ingresosCitasHoy", ingresosCitasHoy);
            model.addAttribute("ventasJson", ventasJson);
            model.addAttribute("citasJson", citasJson);

        } catch (Exception e) {
            e.printStackTrace();
            // Valores por defecto si hay error
            model.addAttribute("totalVentasHoy", 0.0);
            model.addAttribute("citasPendientes", 0L);
            model.addAttribute("ingresosCitasHoy", 0.0);
            model.addAttribute("ventasJson", "{}");
            model.addAttribute("citasJson", "{}");
        }

        return "dashboard";
    }
}
```

### 2️⃣ SERVICIO: SalesService.java

```java
@Service
public class SalesService {

    @Autowired
    private SaleRepository saleRepository;

    // Calcula total de ventas del día
    public Double getTotalVentasHoy() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        // Llama al repository con rango de hoy
        return Optional.ofNullable(
            saleRepository.sumTotalByDate(startOfDay, endOfDay)
        ).orElse(0.0);
    }

    // Obtiene ventas agrupadas por mes
    public Map<String, Double> getVentasPorMes() {
        List<Object[]> monthlySalesData = saleRepository.findTotalSalesPerMonth();
        Map<String, Double> data = new LinkedHashMap<>();
        
        for (Object[] row : monthlySalesData) {
            Integer monthIndex = (Integer) row[1];  // Mes (1-12)
            Double total = (Double) row[2];         // Total
            
            String monthName = getMonthName(monthIndex);
            data.put(monthName, total);
        }
        return data;
    }

    private String getMonthName(int month) {
        String[] names = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", 
                         "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"};
        return (month >= 1 && month <= 12) ? names[month - 1] : "N/A";
    }
}
```

### 3️⃣ SERVICIO: AppointmentService.java

```java
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    // Cuenta citas futuras (pendientes)
    public Long contarCitasPendientes() {
        return appointmentRepository.countByDateTimeAfter(LocalDateTime.now());
    }

    // Obtiene citas agrupadas por mes
    public Map<String, Long> getCitasPorMes() {
        List<Object[]> monthlyCitasData = appointmentRepository.countAppointmentsPerMonth();
        Map<String, Long> data = new LinkedHashMap<>();
        
        for (Object[] row : monthlyCitasData) {
            Integer monthIndex = (Integer) row[1];  // Mes
            Long total = (Long) row[2];             // Conteo
            
            String monthName = getMonthName(monthIndex);
            data.put(monthName, total);
        }
        return data;
    }

    // Calcula ingresos de citas del día
    public Double getIngresosCitasHoy() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        // Obtiene todas las citas de hoy
        List<Appointment> citasDeHoy = appointmentRepository
            .findByDateTimeBetween(startOfDay, endOfDay);
        
        // Suma los precios de los servicios
        return citasDeHoy.stream()
                .mapToDouble(cita -> {
                    if (cita.getService() != null) {
                        return cita.getService().getPrice();
                    }
                    return 0.0;
                })
                .sum();
    }

    private String getMonthName(int month) {
        String[] names = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", 
                         "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"};
        return (month >= 1 && month <= 12) ? names[month - 1] : "N/A";
    }
}
```

### 4️⃣ REPOSITORIOS

```java
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    // Suma el total de ventas en un rango de fechas
    // SQL: SELECT SUM(total) FROM sales WHERE created_at BETWEEN ? AND ?
    Double sumTotalByDate(LocalDateTime start, LocalDateTime end);
    
    // Obtiene ventas agrupadas por mes
    // SQL: SELECT YEAR(created_at), MONTH(created_at), SUM(total) 
    //      FROM sales GROUP BY YEAR(created_at), MONTH(created_at)
    List<Object[]> findTotalSalesPerMonth();
}

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Cuenta citas futuras
    // SQL: SELECT COUNT(*) FROM appointments WHERE appointment_date_time > ?
    Long countByDateTimeAfter(LocalDateTime dateTime);
    
    // Cuenta citas agrupadas por mes
    // SQL: SELECT YEAR(appointment_date_time), MONTH(appointment_date_time), COUNT(*)
    //      FROM appointments GROUP BY YEAR, MONTH
    List<Object[]> countAppointmentsPerMonth();
    
    // Obtiene citas en un rango de fechas
    // SQL: SELECT * FROM appointments 
    //      WHERE appointment_date_time BETWEEN ? AND ?
    List<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
```

---

## FLUJO DE DATOS ESPECÍFICO

### ¿Cómo se calcula "Total Ventas Hoy"?

```
Usuario accede a /dashboard
    ↓
Controller llama: salesService.getTotalVentasHoy()
    ↓
Service crea rango de hoy:
    startOfDay = 2025-12-10 00:00:00
    endOfDay   = 2025-12-10 23:59:59
    ↓
Service llama: saleRepository.sumTotalByDate(start, end)
    ↓
Repository ejecuta SQL:
    SELECT SUM(total) FROM sales 
    WHERE created_at BETWEEN '2025-12-10 00:00:00' AND '2025-12-10 23:59:59'
    ↓
BD retorna resultado:
    Si hay 2 ventas: 94.40 + 150.50 = 244.90
    Si no hay ventas: null → Service convierte a 0.0
    ↓
Service retorna: 244.90
    ↓
Controller agrega a Model:
    model.addAttribute("totalVentasHoy", 244.90)
    ↓
Thymeleaf renderiza:
    <p th:text="'S/. ' + ${totalVentasHoy}"></p>
    → Resultado: "S/. 244.90"
    ↓
Usuario ve en dashboard:
    📊 Ventas Hoy: S/. 244.90
```

### ¿Cómo se crean los gráficos?

```
Controller obtiene ventasMap:
{
    "Enero": 1500.00,
    "Febrero": 2300.50,
    "Diciembre": 244.90
}

ObjectMapper convierte a JSON:
ventasJson = '{"Enero":1500.00,"Febrero":2300.50,"Diciembre":244.90}'

Thymeleaf pasa a Inline Script:
<script>
    const ventasData = ${ventasJson};
    // ventasData ahora es un objeto JavaScript
</script>

JavaScript usa Chart.js (u otra librería):
new Chart(document.getElementById('ventasChart'), {
    type: 'bar',
    data: {
        labels: Object.keys(ventasData),      // ["Enero", "Febrero", "Diciembre"]
        datasets: [{
            label: 'Ventas Mensuales',
            data: Object.values(ventasData)   // [1500, 2300.50, 244.90]
        }]
    }
});

Resultado: Gráfico de barras
┌──────────────────────────────────┐
│ 2500 ├─────┐                      │
│ 2000 ├─────┼────┐                 │
│ 1500 ├─┐   ├────┤                 │
│ 1000 ├─┼───┼────┤                 │
│  500 ├─┼───┼────┤─┐               │
│    0 └─┴───┴────┴─┴──────────────┘
│      Ene  Feb  Dic                │
└──────────────────────────────────┘
```

---

## VALIDACIONES Y MANEJO DE ERRORES

```java
try {
    // Intenta obtener datos
    Double totalVentasHoy = salesService.getTotalVentasHoy();
    
    // ✓ Validación 1: Null check
    if (totalVentasHoy == null) 
        totalVentasHoy = 0.0;
    
    // ✓ Validación 2: Map vacío si no hay datos
    Map<String, Double> ventasMap = salesService.getVentasPorMes();
    if (ventasMap == null) 
        ventasMap = Collections.emptyMap();
    
    // ✓ Validación 3: Convierte a JSON seguramente
    String ventasJson = mapper.writeValueAsString(ventasMap);
    
} catch (Exception e) {
    e.printStackTrace();
    
    // ✓ Fallback: Atributos con valores por defecto
    model.addAttribute("totalVentasHoy", 0.0);
    model.addAttribute("ventasJson", "{}");  // JSON vacío
}
```

---

## OPTIMIZACIONES

### 1. Consultas SQL personalizadas

```java
// En lugar de descargar TODO y procesar en Java:
List<Sale> allSales = saleRepository.findAll();  // ❌ Ineficiente

// Usa queries específicas:
Double total = saleRepository.sumTotalByDate(start, end);  // ✅ Rápido

// La BD calcula, no Java
```

### 2. Agregación en la BD

```java
// ❌ Malo: Descargar y agrupar en Java
List<Sale> allSales = saleRepository.findAll();
Map<String, Double> grouped = allSales.stream()
    .collect(Collectors.groupingBy(...));

// ✅ Bueno: Group BY en SQL
List<Object[]> monthlySales = saleRepository.findTotalSalesPerMonth();
// SELECT YEAR(...), MONTH(...), SUM(total) FROM sales GROUP BY YEAR, MONTH
```

### 3. Caché de datos (opcional)

```java
@Cacheable("dashboard")  // Caché por 5 minutos
public Double getTotalVentasHoy() {
    // Se ejecuta solo si no está en caché
}
```

---

## RESUMEN VISUAL

```
Punto de entrada: /dashboard

    ↓

DashboardController
├─ Llama SalesService.getTotalVentasHoy()
├─ Llama SalesService.getVentasPorMes()
├─ Llama AppointmentService.contarCitasPendientes()
├─ Llama AppointmentService.getCitasPorMes()
└─ Llama AppointmentService.getIngresosCitasHoy()

    ↓

Services consultan Repositories
├─ SaleRepository: sumTotalByDate, findTotalSalesPerMonth
└─ AppointmentRepository: countByDateTimeAfter, countAppointmentsPerMonth, findByDateTimeBetween

    ↓

Repositories ejecutan SQL en BD (MySQL)

    ↓

Resultados retornan al Controller

    ↓

ObjectMapper convierte a JSON

    ↓

Model agrega atributos

    ↓

Thymeleaf renderiza dashboard.html

    ↓

JavaScript (Chart.js) dibuja gráficos

    ↓

Usuario ve Dashboard con métricas y gráficos en tiempo real
```

---

## RESPUESTAS PARA DEFENSA

### Pregunta: "¿Cómo se actualizan las métricas del dashboard?"

**Respuesta:**
"El dashboard NO se actualiza automáticamente. Cada vez que el usuario recarga la página (o accede a /dashboard), el controlador consulta nuevamente a los servicios, que consultan la BD. Los servicios usan queries específicas que agrupan y suman datos directamente en la BD con SQL. Luego, ObjectMapper convierte los resultados a JSON, Thymeleaf renderiza la página con esos datos, y JavaScript dibuja los gráficos."

### Pregunta: "¿Por qué separas cálculos entre Java y JavaScript?"

**Respuesta:**
"En Java (backend) se hacen los cálculos pesados: consultar BD, agrupar datos, sumas. En JavaScript (frontend) se hace la visualización: convertir datos a gráficos con Chart.js. Así separamos responsabilidades. El backend es responsable de la lógica y la BD, el frontend solo de mostrar."

### Pregunta: "¿Cómo obtienes los datos mensuales?"

**Respuesta:**
"En el repositorio uso una query SQL personalizada que agrupa por mes: SELECT YEAR(created_at), MONTH(created_at), SUM(total) FROM sales GROUP BY YEAR, MONTH. La BD devuelve una lista de arrays con [año, mes, total]. Luego convierto eso a un mapa ordenado {Enero: valor, Febrero: valor, ...} que puedo pasar a JSON para JavaScript."

### Pregunta: "¿Qué sucede si no hay datos?"

**Respuesta:**
"En el controlador tengo validaciones: si el resultado es null, asigno 0.0. Si el mapa está vacío, asigno un mapa vacío. Si hay excepción, tengo un catch block que asigna valores por defecto. Así evito errores 500 y muestro un dashboard limpio incluso sin datos."

---

## TABLA RESUMEN

| Métrica | Cálculo | SQL |
|---------|---------|-----|
| **Ventas Hoy** | SUM de ventas en rango 00:00-23:59 de hoy | `SELECT SUM(total) FROM sales WHERE created_at BETWEEN ? AND ?` |
| **Citas Pendientes** | COUNT de citas futuras | `SELECT COUNT(*) FROM appointments WHERE appointment_date_time > NOW()` |
| **Ingresos Citas Hoy** | SUM de precios de servicios en citas de hoy | `SELECT SUM(service.price) FROM appointments WHERE DATE(appointment_date_time) = TODAY()` |
| **Ventas/Mes** | GROUP BY YEAR/MONTH | `SELECT YEAR(...), MONTH(...), SUM(total) GROUP BY YEAR, MONTH` |
| **Citas/Mes** | GROUP BY YEAR/MONTH | `SELECT YEAR(...), MONTH(...), COUNT(*) GROUP BY YEAR, MONTH` |

