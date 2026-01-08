# ÍNDICE MAESTRO: GUÍA COMPLETA PARA DEFENSA

## 📚 Documentos de estudio para la defensa final

---

## ESTRUCTURA DE DOCUMENTOS

```
1_ENDPOINTS_CONTROLLERS_REPOSITORIES.md
├─ Cómo fluyen los datos desde navegador hasta BD
├─ Ejemplo: Agregar producto al carrito
├─ Ejemplo: Agendar cita
├─ Flujo completo: petición → controlador → servicio → repository → BD
└─ Respuestas de defensa: "¿Cómo fluyen datos?" "¿Por qué separar capas?"

2_SISTEMA_BOLETA_FACTURA.md
├─ Cómo se genera y descarga la boleta/factura
├─ Flujo: Registración → SalesService → SaleRepository → BD
├─ Generación PDF con jsPDF en JavaScript
├─ Validaciones: stock suficiente, datos completos
└─ Respuestas de defensa: "¿Cómo funciona boleta?" "¿Backend o frontend?"

3_DASHBOARD_METRICAS.md
├─ Cómo se calculan y muestran las métricas
├─ Total de ventas hoy, citas pendientes, ingresos
├─ Gráficos mensuales con Chart.js
├─ Agregación en la BD (SQL) vs JavaScript (visualización)
└─ Respuestas de defensa: "¿Cómo se actualizan métricas?" "¿Dónde se procesan datos?"

4_ENCRIPTACION_PASSWORDS_BCRYPT.md
├─ Cómo se protegen las contraseñas
├─ BCryptPasswordEncoder: encriptación unidireccional
├─ Flujo de REGISTRO: password → encode() → hash guardado en BD
├─ Flujo de LOGIN: password ingresada → matches() comparado con hash
└─ Respuestas de defensa: "¿Cómo protegen contraseñas?" "¿BCrypt vs SHA?"

5_VALIDACION_CITAS_HORARIOS.md
├─ Cómo se valida que una cita sea válida
├─ Validaciones: servicio existe, médico existe, hora en horario, hora libre
├─ Horarios de médicos: horarioInicio - horarioFin
├─ Casos: exitosa, fuera de horario, hora ocupada
└─ Respuestas de defensa: "¿Cómo validan citas?" "¿Cómo evitan superpuestas?"

6_DEPENDENCIAS_INTEGRADAS.md
├─ Todas las librerías que usa el proyecto
├─ Spring Security, JWT, JPA, Thymeleaf, MySQL, Lombok
├─ Qué hace cada dependencia
├─ Cómo se integran entre sí
└─ Respuestas de defensa: "¿Por qué estas dependencias?" "¿Cómo se comunican?"
```

---

## 🎯 CÓMO USAR ESTOS DOCUMENTOS

### Para preparación rápida (15 minutos)
1. Lee el resumen ejecutivo de cada documento (primeras 3 líneas)
2. Mira los diagramas de flujo (FLUJO COMPLETO)
3. Memoriza las respuestas de defensa propuestas

### Para entendimiento profundo (1-2 horas)
1. Lee cada documento completamente
2. Detente en ejemplos prácticos con código real
3. Entiende por qué cada decisión se tomó
4. Practica explicar sin leer

### Para la defensa
1. No memorices, entiende el concepto
2. Explica en voz alta antes de la defensa
3. Si no sabes algo, admite ignorancia pero explora opciones
4. Usa ejemplos de tu propio código cuando sea posible

---

## 📋 TEMAS CUBIERTOS

### Arquitectura General
- ✓ Flujo HTTP: navegador → servidor → BD
- ✓ Patrón MVC: Model-View-Controller
- ✓ Patrón capas: Controller → Service → Repository
- ✓ Inyección de dependencias

### Componentes Principales
- ✓ 10 Controladores (Auth, Dashboard, Appointment, Sales, etc)
- ✓ 8 Repositorios (Usuario, Product, Appointment, Sale, Medico, etc)
- ✓ 4 Servicios (Sales, Appointment, Medico, CustomUserDetails)
- ✓ Configuración Security y JWT

### Funcionalidades
- ✓ Autenticación (login, registro)
- ✓ Autorización (roles, permisos)
- ✓ Carrito de compras (sesiones)
- ✓ Generación de boleta (backend + frontend)
- ✓ Dashboard (métricas, gráficos)
- ✓ Agendamiento de citas (validaciones)

### Tecnologías
- ✓ Spring Boot 3.5.6
- ✓ Spring Security + BCrypt
- ✓ JWT (JSON Web Tokens)
- ✓ JPA/Hibernate (ORM)
- ✓ MySQL 8
- ✓ Thymeleaf
- ✓ JavaScript (jsPDF, Chart.js)

---

## 🔍 PREGUNTAS PROBABLES EN DEFENSA

### 1. ENDPOINTS Y FLUJO
**P: "¿Cómo fluyen los datos desde que el usuario hace clic en 'Agregar al carrito' hasta que se guarda en la BD?"**

R: Ver documento 1_ENDPOINTS_CONTROLLERS_REPOSITORIES.md → EJEMPLO 1: AGREGAR PRODUCTO

**P: "¿Por qué separas la aplicación en Controller-Service-Repository?"**

R: Ver documento 1_ENDPOINTS_CONTROLLERS_REPOSITORIES.md → RESPUESTAS PARA DEFENSA

**P: "¿Qué hace cada capa?"**

R: Ver documento 1_ENDPOINTS_CONTROLLERS_REPOSITORIES.md → LISTA COMPLETA DE ENDPOINTS

---

### 2. BOLETA Y FACTURACIÓN
**P: "¿Cómo se genera la boleta?"**

R: Ver documento 2_SISTEMA_BOLETA_FACTURA.md → FLUJO COMPLETO DE UNA BOLETA

**P: "¿Por qué se genera en JavaScript y no en Java?"**

R: Ver documento 2_SISTEMA_BOLETA_FACTURA.md → RESPUESTAS PARA DEFENSA

**P: "¿Qué datos se guardan en la BD y cuáles en HTML?"**

R: Ver documento 2_SISTEMA_BOLETA_FACTURA.md → FLUJO DE DATOS: ANTES Y DESPUÉS

---

### 3. DASHBOARD
**P: "¿Cómo se calculan las métricas del dashboard?"**

R: Ver documento 3_DASHBOARD_METRICAS.md → FLUJO DE DATOS ESPECÍFICO

**P: "¿Se actualiza automáticamente o requiere recargar?"**

R: Ver documento 3_DASHBOARD_METRICAS.md → RESPUESTAS PARA DEFENSA

**P: "¿Dónde se procesan los datos, en Java o JavaScript?"**

R: Ver documento 3_DASHBOARD_METRICAS.md → FLUJO DE DATOS ESPECÍFICO

---

### 4. SEGURIDAD Y CONTRASEÑAS
**P: "¿Cómo se protegen las contraseñas?"**

R: Ver documento 4_ENCRIPTACION_PASSWORDS_BCRYPT.md → ¿POR QUÉ ENCRIPTAR

**P: "¿Qué es BCrypt y por qué lo usas?"**

R: Ver documento 4_ENCRIPTACION_PASSWORDS_BCRYPT.md → ¿QUÉ ES BCrypt?

**P: "¿Qué pasa si se roba la BD?"**

R: Ver documento 4_ENCRIPTACION_PASSWORDS_BCRYPT.md → RESPUESTAS PARA DEFENSA

**P: "¿Cómo AuthenticationManager valida credenciales?"**

R: Ver documento 4_ENCRIPTACION_PASSWORDS_BCRYPT.md → FLUJO COMPLETO: LOGIN

---

### 5. CITAS Y VALIDACIONES
**P: "¿Cómo se valida que una cita sea válida?"**

R: Ver documento 5_VALIDACION_CITAS_HORARIOS.md → TIPOS DE VALIDACIÓN

**P: "¿Cómo evitas que dos citas se superpongan?"**

R: Ver documento 5_VALIDACION_CITAS_HORARIOS.md → VALIDACIONES VISUALES

**P: "¿Qué sucede si dos usuarios agendan la misma hora?"**

R: Ver documento 5_VALIDACION_CITAS_HORARIOS.md → RESPUESTAS PARA DEFENSA

---

### 6. DEPENDENCIAS
**P: "¿Qué dependencias usa tu proyecto?"**

R: Ver documento 6_DEPENDENCIAS_INTEGRADAS.md → RESUMEN EJECUTIVO

**P: "¿Por qué necesitas todas esas librerías?"**

R: Ver documento 6_DEPENDENCIAS_INTEGRADAS.md → DEPENDENCIAS PRINCIPALES

**P: "¿Cómo se comunican estas dependencias?"**

R: Ver documento 6_DEPENDENCIAS_INTEGRADAS.md → FLUJO DE INTEGRACIÓN

---

## 🚀 TIPS PARA LA DEFENSA

### 1. Responde sin memorizar
- NO repitas el documento palabra por palabra
- EXPLICA con tus propias palabras
- USA ejemplos del código

### 2. Comienza general, profundiza si preguntan
- **General**: "La aplicación usa Spring Boot con autenticación y autorización"
- **Si preguntan más**: "Específicamente, Spring Security con BCryptPasswordEncoder"
- **Si preguntan aún más**: "BCrypt genera un hash único con salt automático..."

### 3. Si no sabes, busca la respuesta lógica
- No todos los detalles están documentados
- Usa la lógica: "Probablemente así funciona porque..."
- Propón soluciones: "Una forma de hacerlo sería..."

### 4. Mantén compostura
- Si cometes error, corrígete: "Disculpa, me equivoqué, debería ser..."
- Si no recuerdas, admite: "No recuerdo el nombre exacto, pero es un componente que..."
- Muestra confianza en lo que sí sabes

### 5. Practica explicar en voz alta
- Antes de la defensa, lee cada documento en voz alta
- Ajusta tu velocidad, tono, claridad
- Pide a alguien que te escuche y haga preguntas

---

## 🧪 CASOS DE PRUEBA

### Caso 1: Usuario nuevo quiere comprar
1. Accede a /simulador_ventas (SalesController.showSales)
2. Ve productos de ProductRepository.findAll()
3. Agrega al carrito (SalesService.addProductToCart)
4. Verifica stock: ¿product.getStock() >= quantity?
5. Presiona "Confirmar Venta" (SalesController.finalizeSale)
6. Descuenta stock: product.setStock(stock - quantity)
7. Calcula totales: subtotal + IGV (18%)
8. Guarda en tabla sales (SaleRepository.save)
9. Genera boleta en PDF (JavaScript + jsPDF)
10. Usuario descarga: Boleta_PatitasVet_[timestamp].pdf

### Caso 2: Usuario nuevo quiere agendar cita
1. Accede a /simulador_citas (AppointmentController.showAppointmentSimulator)
2. Ve servicios (AppointmentService.getAvailableServices)
3. Ve médicos (AppointmentService.getAllMedicos)
4. Selecciona: servicio, médico, fecha, hora, nombre
5. Presiona "Agendar" (AppointmentController.bookAppointment)
6. Validaciones:
   - ¿Servicio existe? (ServiceRepository.findById)
   - ¿Médico existe? (MedicoRepository.findById)
   - ¿Hora en horario? (medico.getHorarioInicio/Fin)
   - ¿Hora disponible? (AppointmentRepository.existsByDateTime)
7. Si OK: Guarda cita (AppointmentRepository.save)
8. Usuario ve confirmación con mensaje de éxito

### Caso 3: Usuario intenta login
1. Accede a /login
2. Ingresa usuario y contraseña
3. POST /api/auth/login (JwtAuthenticationController.authenticateUser)
4. AuthenticationManager valida:
   - CustomUserDetailsService.loadUserByUsername(usuario)
   - PasswordEncoder.matches(passwordIngresada, hashEnBd)
5. Si OK: Genera JWT token (JwtUtil.generateToken)
6. Retorna token en JSON
7. Futuras peticiones incluyen: Authorization: Bearer [token]

### Caso 4: Ver dashboard
1. Accede a /dashboard (DashboardController.showDashboard)
2. Controllers llama a servicios:
   - SalesService.getTotalVentasHoy()
   - SalesService.getVentasPorMes()
   - AppointmentService.contarCitasPendientes()
   - AppointmentService.getCitasPorMes()
   - AppointmentService.getIngresosCitasHoy()
3. Services consultan repositories:
   - SaleRepository.sumTotalByDate(startOfDay, endOfDay)
   - SaleRepository.findTotalSalesPerMonth()
   - AppointmentRepository.countByDateTimeAfter()
   - AppointmentRepository.countAppointmentsPerMonth()
   - AppointmentRepository.findByDateTimeBetween()
4. Repositories ejecutan SQL en MySQL
5. Controller agrega al modelo (Model)
6. ObjectMapper convierte a JSON (para JavaScript)
7. Thymeleaf renderiza dashboard.html
8. JavaScript dibuja gráficos (Chart.js)
9. Usuario ve métricas y gráficos

---

## 📊 MATRIZ: Componentes vs Funcionalidades

| Funcionalidad | Controller | Service | Repository | Modelo | DB Table |
|---|---|---|---|---|---|
| **Login** | AuthController / JwtAuthController | CustomUserDetailsService | UsuarioRepository | Usuario | usuarios |
| **Registro** | JwtAuthController | - | UsuarioRepository | Usuario | usuarios |
| **Carrito** | SalesController | SalesService | ProductRepository | Product | products |
| **Boleta** | SalesController | SalesService | SaleRepository | Sale | sales |
| **Citas** | AppointmentController | AppointmentService | AppointmentRepository | Appointment | appointments |
| **Médicos** | AppointmentController / MedicoController | AppointmentService / MedicoService | MedicoRepository | Medico | medico |
| **Dashboard** | DashboardController | SalesService / AppointmentService | SaleRepository / AppointmentRepository | Sale / Appointment | sales / appointments |

---

## 🎓 CONCEPTOS CLAVE

### 1. MVC (Model-View-Controller)
- **Model**: Datos (entities, objetos)
- **View**: Presentación (HTML con Thymeleaf)
- **Controller**: Lógica de petición

### 2. N-Capas (Three-Tier Architecture)
- **Presentation**: Controller (recibe peticiones)
- **Business Logic**: Service (procesa)
- **Data Access**: Repository (accede BD)

### 3. ORM (Object-Relational Mapping)
- Mapea clases Java a tablas SQL
- JPA es la interfaz, Hibernate es la implementación
- @Entity = Tabla, atributos = columnas

### 4. RESTful API
- GET: Obtener datos
- POST: Crear datos
- PUT: Actualizar datos
- DELETE: Eliminar datos
- ResponseEntity: Respuesta HTTP personalizada

### 5. Inyección de Dependencias
- Spring crea y gestiona instancias
- @Autowired: Inyecta automáticamente
- Reduce acoplamiento, facilita testing

### 6. Spring Security
- Autenticación: ¿Quién eres?
- Autorización: ¿Qué puedes hacer?
- UserDetails, PasswordEncoder, AuthenticationManager

### 7. JWT (JSON Web Tokens)
- Token con payload encriptado
- Stateless (no requiere sesión en servidor)
- Cliente envía en cada petición

### 8. BCrypt
- Algoritmo de encriptación para passwords
- Unidireccional (no se desencripta)
- Salt automático (cada hash es único)

---

## ✅ CHECKLIST PRE-DEFENSA

- [ ] He leído los 6 documentos
- [ ] Entiendo el flujo: Navegador → Controller → Service → Repository → BD
- [ ] Entiendo las 5 validaciones de citas
- [ ] Entiendo cómo se genera la boleta
- [ ] Entiendo cómo se calculan las métricas del dashboard
- [ ] Entiendo BCrypt y PasswordEncoder
- [ ] Entiendo JWT tokens
- [ ] Entiendo Spring Security
- [ ] Conozco los 10 controladores de mi proyecto
- [ ] Conozco los 8 repositorios de mi proyecto
- [ ] Conozco los 4 servicios de mi proyecto
- [ ] Entiendo todas las dependencias
- [ ] Puedo explicar mi proyecto SIN leer documentos
- [ ] Puedo responder preguntas inesperadas con lógica

---

## 🎬 COMIENZA AQUÍ

1. **Si tienes 15 minutos**: Lee RESUMEN EJECUTIVO de cada documento
2. **Si tienes 1 hora**: Lee FLUJO COMPLETO de cada documento
3. **Si tienes 2+ horas**: Lee documentos completos + practica explicar
4. **Si tienes 1 día**: Lee todo, practica con alguien, haz preguntas

---

## 📞 SI ALGO NO ESTÁ CLARO

Para cada duda, identifica:
1. ¿A qué documento pertenece? (Endpoints, Boleta, Dashboard, Security, Citas, Dependencias)
2. ¿Qué sección del documento? (Flujo, Modelo, Servicio, Respuestas)
3. ¿Qué código específico? (Lee el código en src/main/java)
4. ¿Puedo probarlo localmente? (Ejecuta la aplicación, prueba la funcionalidad)

---

## 🏁 BUENA SUERTE EN LA DEFENSA

**Recuerda**: 
- No necesitas saber TODO los detalles
- Necesitas entender CÓMO funciona el sistema
- La defensa no es memorización, es explicación
- Si cometes un error, corrígete
- Si no sabes, piensa en voz alta

**Enfoque**: Explica tu proyecto como si enseñaras a alguien nuevo a usarlo.

