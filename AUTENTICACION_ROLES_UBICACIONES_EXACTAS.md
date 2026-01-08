# AUTENTICACIÓN Y VALIDACIÓN DE ROLES - UBICACIONES EXACTAS EN EL CÓDIGO

## 📍 ARCHIVO 1: navbar.html (Navegación condicional)

**Ruta:** `src/main/resources/templates/fragments/navbar.html`

### ❌ LOGIN (Solo aparece si NO está autenticado)
```html
<!-- LÍNEA 37-42: LOGIN (solo si NO está logueado) -->
<li class="nav-item" id="nav-login-link">
    <a class="nav-link" href="#" data-bs-toggle="modal" data-bs-target="#roleLoginModal">
        <i class="fa-solid fa-right-to-bracket"></i> Login
    </a>
</li>
```
**¿Cómo funciona?**
- El `id="nav-login-link"` se oculta/muestra con JavaScript
- Si no hay sesión activa → Se muestra el botón Login
- Si hay sesión activa → Se oculta (JavaScript en index.html lo controla)

---

### ✓ MENÚ DE USUARIO (Solo aparece si SÍ está autenticado)
```html
<!-- LÍNEA 45-66: MENÚ DE TRABAJADOR (solo cuando inicia sesión) -->
<li class="nav-item dropdown d-none" id="nav-user-menu">
    <a class="nav-link dropdown-toggle" href="#" id="userDropdown"
       role="button" data-bs-toggle="dropdown" aria-expanded="false">
        <i class="fa-solid fa-user"></i> Usuario
    </a>

    <!-- Dropdown correcto -->
    <ul class="dropdown-menu" aria-labelledby="userDropdown">

        <li>
            <a class="dropdown-item" th:href="@{/dashboard}">
                <i class="fa-solid fa-shop"></i> Dashboard
            </a>
        </li>

        <li><hr class="dropdown-divider"></li>

        <li>
            <a class="dropdown-item" href="#" id="logoutBtn">
                <i class="fa-solid fa-right-from-bracket"></i> Cerrar sesión
            </a>
        </li>

    </ul>
</li>
```

**¿Cómo funciona?**
- `id="nav-user-menu"` tiene clase `d-none` (oculto por defecto)
- JavaScript lo muestra cuando se detecta sesión activa
- El Dashboard solo es accesible si tienes rol TRABAJADOR (validado en backend)

---

## 📍 ARCHIVO 2: login.html (Formulario de Login)

**Ruta:** `src/main/resources/templates/login.html`

### Formulario de autenticación
```html
<!-- LÍNEA 13-28: FORMULARIO DE LOGIN -->
<form th:action="@{/login}" method="post" class="w-50">

    <div class="mb-3">
        <label>Usuario</label>
        <input type="text" name="usuario" class="form-control" required>
    </div>

    <div class="mb-3">
        <label>Contraseña</label>
        <input type="password" name="contrasena" class="form-control" required>
    </div>

    <button type="submit" class="btn btn-primary">Ingresar</button>
</form>
```

### Mostrar errores de autenticación
```html
<!-- LÍNEA 30: VALIDACIÓN CON THYMELEAF -->
<p th:if="${error}" th:text="${error}" class="text-danger mt-3"></p>
```

**¿Qué valida?**
- `th:if="${error}"` → Si existe mensaje de error en el Model
- Muestra: "Credenciales incorrectas" (desde AuthController o SecurityConfig)
- Si no hay error, no se renderiza nada

---

## 📍 ARCHIVO 3: simulador_ventas.html (Carrito validado)

**Ruta:** `src/main/resources/templates/simulador_ventas.html`

### Validación: Usuario logueado
```html
<!-- LÍNEA 20: META TAG CON USUARIO DE SESIÓN -->
<meta name="username" th:if="${session.usuarioLogeado != null}" 
      th:content="${session.usuarioLogeado}" />
```

**¿Qué valida?**
- `th:if="${session.usuarioLogeado != null}"` → Solo si hay usuario en sesión
- Guarda el username en meta tag para JavaScript
- El HTML de la boleta usa este username

---

### Validación: Carrito vacío vs carrito con items
```html
<!-- LÍNEA 89: SI CARRITO ESTÁ VACÍO -->
<p th:if="${#lists.isEmpty(cart)}" id="empty-cart-message" 
   class="text-center text-muted">
    Tu carrito está vacío. Agrega productos para continuar.
</p>

<!-- LÍNEA 94-96: SI CARRITO TIENE ITEMS -->
<ul class="list-group list-group-flush" th:unless="${#lists.isEmpty(cart)}">
    <li th:each="item : ${cart}" 
        class="list-group-item d-flex justify-content-between align-items-center">
        <!-- Muestra cada item -->
    </li>
</ul>
```

**¿Qué valida?**
- `th:if` muestra mensaje si carrito vacío
- `th:unless` muestra items si carrito NO está vacío
- `th:each` itera sobre cada producto en el carrito

---

### Validación: Boleta generada
```html
<!-- LÍNEA 160-171: DATOS FINALES (BOLETA) -->
<div id="final-cart-data" style="display:none;" th:if="${finalCart != null}">
    <ul>
        <li th:each="item : ${finalCart}"
            th:data-name="${item.name}"
            th:data-price="${item.price}"
            th:data-quantity="${item.quantity}">
        </li>
    </ul>
</div>

<div th:if="${finalCart != null}" class="mx-auto text-center my-3">
    <button id="download-invoice-button" onclick="generateInvoicePDF()" 
            class="btn btn-success">
        <i class="fa-solid fa-file-pdf me-2"></i> Descargar Boleta
    </button>
</div>
```

**¿Qué valida?**
- `th:if="${finalCart != null}"` → Solo si la venta fue finalizada
- Muestra botón de descarga de PDF
- Almacena datos en atributos `th:data-*` para JavaScript

---

## 📍 ARCHIVO 4: simulador_citas.html (Validación de citas)

**Ruta:** `src/main/resources/templates/simulador_citas.html`

### Validación: Mensaje de confirmación/error
```html
<!-- LÍNEA 34-45: MOSTRAR MENSAJES DE VALIDACIÓN -->
<div th:if="${message}" id="notification"
     class="alert"
     th:classappend="${messageType == 'success' ? 'alert-success' : 'alert-danger'}">
    <span th:text="${message}"></span>
</div>
```

**¿Qué valida?**
- `th:if="${message}"` → Si hay mensaje de validación
- `th:classappend` → Agrega clase según `messageType`
- Muestra: "Cita agendada" o "Hora ocupada" (desde AppointmentService)

---

### Validación: Listar servicios disponibles
```html
<!-- LÍNEA 63-68: ITERAR SERVICIOS DISPONIBLES -->
<select name="serviceId" required>
    <option value="">-- Selecciona un servicio --</option>
    <option th:each="service : ${availableServices}"
            th:value="${service.id}"
            th:text="${service.name}">
    </option>
</select>
```

**¿Qué valida?**
- `th:each="service : ${availableServices}"` → Itera servicios desde BD
- Cada option tiene `th:value` con ID del servicio
- Si no hay servicios, el dropdown está vacío

---

### Validación: Listar médicos disponibles
```html
<!-- LÍNEA 76-81: ITERAR MÉDICOS DISPONIBLES -->
<select name="medicoId" required>
    <option value="">-- Selecciona un médico --</option>
    <option th:each="med : ${allMedicos}"
            th:value="${med.id}"
            th:text="${med.nombre}">
    </option>
</select>
```

**¿Qué valida?**
- `th:each="med : ${allMedicos}"` → Itera médicos desde BD
- Muestra nombre del médico
- Si se selecciona, se envía medicoId al controller

---

### Validación: Listar citas agendadas
```html
<!-- LÍNEA 122: SI NO HAY CITAS -->
<div th:if="${#lists.isEmpty(bookedAppointments)}"
     class="alert alert-info">
    <p>Aún no hay citas agendadas.</p>
</div>

<!-- LÍNEA 127-137: SI HAY CITAS -->
<div th:unless="${#lists.isEmpty(bookedAppointments)}">
    <h5>Citas Agendadas:</h5>
    <ul>
        <li th:each="appointment : ${bookedAppointments}"
            th:text="${appointment.clientName + ' - ' + appointment.appointmentDateTime}">
        </li>
    </ul>
</div>
```

**¿Qué valida?**
- `th:if` muestra alerta si NO hay citas
- `th:unless` muestra lista si SÍ hay citas
- `th:each` itera sobre todas las citas

---

## 📍 ARCHIVO 5: index.html (Validación de sesión con JavaScript)

**Ruta:** `src/main/resources/templates/index.html`

### JavaScript que valida sesión
```javascript
// En el <script> al final del index.html
document.addEventListener('DOMContentLoaded', function() {
    
    // Obtener usuario de localStorage (guardado después de login)
    const usuarioLogeado = localStorage.getItem('usuarioLogeado');
    
    // VALIDACIÓN: ¿Hay usuario en localStorage?
    if (usuarioLogeado) {
        // SÍ → Usuario autenticado
        
        // OCULTAR botón Login
        document.getElementById('nav-login-link').style.display = 'none';
        
        // MOSTRAR menú de usuario
        document.getElementById('nav-user-menu').classList.remove('d-none');
        
    } else {
        // NO → Usuario no autenticado
        
        // MOSTRAR botón Login
        document.getElementById('nav-login-link').style.display = 'block';
        
        // OCULTAR menú de usuario
        document.getElementById('nav-user-menu').classList.add('d-none');
    }
});
```

---

## 📍 ARCHIVO 6: SecurityConfig.java (VALIDACIÓN DE ROLES EN BACKEND)

**Ruta:** `src/main/java/com/example/veterinariaPatitas/config/SecurityConfig.java`

### ✓ VALIDACIÓN 1: Rutas públicas (todos pueden acceder)
```java
// LÍNEA 40-47: RUTAS PÚBLICAS
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/", "/login", "/logout",
        "/css/**", "/js/**", "/img/**", "/favicon.ico",
        "/sobre_nosotros", "/adoptar", "/envia_mensaje",
        "/trabaja", "/upload", "/postular",
        "/simulador_citas", "/book_appointment",
        "/simulador_ventas", "/api/auth/login", "/api/auth/register"
    ).permitAll()  // ← Acceso para TODOS sin autenticación
```

**¿Qué valida?**
- Estas rutas NO requieren login
- Cualquiera puede acceder

---

### ✓ VALIDACIÓN 2: Dashboard - Solo TRABAJADOR
```java
// LÍNEA 49-51: RUTA PROTEGIDA - SOLO TRABAJADOR
.requestMatchers("/dashboard")
.hasAuthority("TRABAJADOR")  // ← Solo si rol es TRABAJADOR
```

**¿Qué valida?**
- `/dashboard` requiere autenticación
- Y requiere rol `TRABAJADOR` exactamente
- Si intenta usuario con rol `CLIENTE` → Acceso denegado

---

### ✓ VALIDACIÓN 3: APIs - Requieren autenticación
```java
// LÍNEA 53-55: RUTAS API PROTEGIDAS
.requestMatchers("/api/**")
.authenticated()  // ← Requiere token JWT válido
```

**¿Qué valida?**
- `/api/auth/login` y `/api/auth/register` son públicas (permitAll arriba)
- Otras rutas `/api/**` requieren token JWT

---

### ✓ VALIDACIÓN 4: Formulario de Login
```java
// LÍNEA 59-66: CONFIGURACIÓN DEL FORMULARIO DE LOGIN
.formLogin(form -> form
    .loginPage("/login")
    .loginProcessingUrl("/login") 
    .usernameParameter("usuario")      // ← Campo HTML
    .passwordParameter("contrasena")   // ← Campo HTML
    .defaultSuccessUrl("/dashboard", true)  // ← Redirige aquí si login OK
    .permitAll()
)
```

**¿Qué valida?**
- Busca campos `usuario` y `contrasena` en el formulario
- Si login OK → Redirige a `/dashboard`
- Si login falla → Muestra error en `/login`

---

## 📍 ARCHIVO 7: JwtAuthenticationController.java (VALIDACIÓN JWT)

**Ruta:** `src/main/java/com/example/veterinariaPatitas/controller/JwtAuthenticationController.java`

### ✓ LOGIN: Valida credenciales
```java
// LÍNEA 37-56: VALIDACIÓN DE CREDENCIALES
@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    
    try {
        // VALIDACIÓN 1: ¿Contraseña correcta?
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),      // usuario ingresado
                        loginRequest.getPassword()       // contraseña ingresada
                )
        );
        // ↑ Si contraseña incorrecta → Lanza AuthenticationException

        // VALIDACIÓN 2: Obtener rol del usuario
        Optional<Usuario> userOptional = usuarioRepository.findByUsuario(loginRequest.getUsername());
        String role = userOptional.isPresent() ? 
            userOptional.get().getRole() : "USER";

        // VALIDACIÓN PASADA: Generar JWT token
        String jwt = jwtUtil.generateToken(loginRequest.getUsername(), role);

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "username", loginRequest.getUsername(),
                "role", role
        ));

    } catch (Exception e) {
        // ❌ FALLA: Credenciales incorrectas
        return ResponseEntity.status(401)
            .body(Map.of("error", "Credenciales incorrectas."));
    }
}
```

**¿Qué valida?**
- Busca usuario en BD
- Compara contraseña con BCrypt
- Genera token JWT si OK
- Retorna error 401 si falla

---

### ✓ REGISTRO: Valida usuario nuevo
```java
// LÍNEA 60-87: VALIDACIÓN DE REGISTRO
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
    
    // VALIDACIÓN 1: ¿Usuario ya existe?
    if (usuarioRepository.existsByUsuario(registerRequest.getUsername())) {
        return ResponseEntity
                .badRequest()  // ← Error 400
                .body(Map.of("error", "El nombre de usuario ya está en uso."));
    }
    
    // VALIDACIÓN PASADA: Crear usuario nuevo
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setUsuario(registerRequest.getUsername());
    
    // VALIDACIÓN 2: Encriptar contraseña
    nuevoUsuario.setContrasena(
        passwordEncoder.encode(registerRequest.getPassword())
    );
    
    // Asignar rol por defecto
    nuevoUsuario.setRole("CLIENTE");
    
    usuarioRepository.save(nuevoUsuario);

    // Generar JWT automáticamente
    String jwt = jwtUtil.generateToken(nuevoUsuario.getUsuario(), nuevoUsuario.getRole());
    
    return ResponseEntity.ok(Map.of(
            "token", jwt,
            "username", nuevoUsuario.getUsuario(),
            "role", nuevoUsuario.getRole(),
            "message", "Registro exitoso."
    ));
}
```

**¿Qué valida?**
- Usuario no debe existir
- Encripta contraseña con BCrypt
- Asigna rol CLIENTE
- Genera JWT después de registrar

---

## 📋 RESUMEN: FLUJO COMPLETO DE AUTENTICACIÓN

```
1. USUARIO ENTRA A /
   └─ index.html carga
   └─ JavaScript valida si hay "usuarioLogeado" en localStorage
   └─ Si SÍ: Muestra menú de usuario + Dashboard
   └─ Si NO: Muestra botón Login

2. USUARIO HACE CLIC EN LOGIN
   └─ navbar.html muestra modal
   └─ login.html se carga

3. USUARIO INGRESA CREDENCIALES
   └─ POST /login con usuario y contraseña
   └─ SecurityConfig.formLogin() intercepta
   └─ AuthenticationManager valida (usando PasswordEncoder + BCrypt)

4. ¿CREDENCIALES CORRECTAS?
   └─ SÍ: Crea sesión
   └─ JavaScript guarda "usuarioLogeado" en localStorage
   └─ Redirige a /dashboard
   └─ SecurityConfig valida que tenga rol TRABAJADOR
   └─ Dashboard se muestra ✓
   
   └─ NO: Muestra error en login.html
   └─ "Credenciales incorrectas" aparece con <p th:if="${error}">

5. USUARIO INTENTA ACCEDER A /api/auth/login (API REST)
   └─ JwtAuthenticationController intercepta
   └─ authenticationManager valida credenciales
   └─ Genera JWT con JJWT
   └─ Retorna token JSON
   └─ Cliente guarda token
   └─ Próximas peticiones incluyen: Authorization: Bearer [token]

6. USUARIO HACE CLIC EN LOGOUT
   └─ JavaScript limpia localStorage
   └─ SecurityConfig.logout() elimina sesión
   └─ Redirige a /login
   └─ Botón Login vuelve a aparecer ✓
```

---

## 🔑 PALABRAS CLAVE THYMELEAF PARA VALIDACIÓN

| Sintaxis | Significado | Ejemplo |
|---------|-----------|---------|
| `th:if="${condition}"` | Mostrar si condición es true | `th:if="${message}"` |
| `th:unless="${condition}"` | Mostrar si condición es false | `th:unless="${#lists.isEmpty(cart)}"` |
| `th:each="item : ${list}"` | Iterar lista | `th:each="product : ${products}"` |
| `th:text="${variable}"` | Mostrar texto del variable | `th:text="${error}"` |
| `th:classappend="${condition ? 'class1' : 'class2'}"` | Agregar clase condicional | `th:classappend="${messageType == 'success' ? 'alert-success' : 'alert-danger'}"` |
| `th:href="@{/url}"` | Generar URL | `th:href="@{/dashboard}"` |
| `th:value="${variable}"` | Valor en input | `th:value="${service.id}"` |
| `th:if="${session.attribute}"` | Validar sesión | `th:if="${session.usuarioLogeado != null}"` |

---

## 🔐 VALIDACIONES DE SEGURIDAD (RESUMEN)

```
NIVEL 1: Thymeleaf (Frontend)
├─ th:if="${user}" → Mostrar/ocultar elementos
├─ th:each → Iterar datos seguros
└─ Datos sensibles NO se exponen

NIVEL 2: JavaScript (Frontend)
├─ Valida sesión en localStorage
├─ Oculta/muestra menús
└─ Maneja logout

NIVEL 3: Spring Security (Backend)
├─ .permitAll() → Rutas públicas
├─ .hasAuthority("TRABAJADOR") → Requiere rol específico
├─ .authenticated() → Requiere cualquier autenticación
└─ FormLogin + LogoutHandler

NIVEL 4: BCrypt (Backend)
├─ passwordEncoder.encode() → Encripta al registrar
├─ authenticationManager.authenticate() → Valida al login
└─ PasswordEncoder.matches() → Compara contraseñas

NIVEL 5: JWT (Backend)
├─ jwtUtil.generateToken() → Crea token firmado
├─ jwtFilter → Valida token en cada request
└─ Expiration automática

NIVEL 6: BD (Backend)
├─ usuarioRepository.existsByUsuario() → Valida no duplicados
└─ Roles almacenados: CLIENTE, TRABAJADOR
```

