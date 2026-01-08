# 6. DEPENDENCIAS INTEGRADAS DEL PROYECTO

## 📦 Todas las librerías y frameworks que usa tu proyecto

---

## RESUMEN EJECUTIVO

Tu proyecto usa **Spring Boot 3.5.6** con:
- **Spring Security**: Autenticación y autorización
- **JWT (JJWT)**: Tokens web para APIs
- **Spring Data JPA**: Acceso a datos y ORM
- **Thymeleaf**: Templates HTML dinámicos
- **MySQL**: Base de datos
- **Lombok**: Reducir boilerplate code

---

## DEPENDENCIAS PRINCIPALES

### 1️⃣ SPRING BOOT STARTER SECURITY

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**¿Qué hace?**
Proporciona autenticación y autorización para tu aplicación.

**Componentes principales:**
- `AuthenticationManager`: Valida credenciales
- `PasswordEncoder`: Encripta contraseñas (BCrypt)
- `SecurityFilterChain`: Filtra peticiones HTTP
- `UserDetailsService`: Carga datos del usuario
- `SpringSecurityFilterChain`: Cadena de filtros de seguridad

**En tu proyecto:**
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ← De spring-security
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();  // ← De spring-security
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/dashboard").hasAuthority("TRABAJADOR")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            // ... más configuración
        return http.build();
    }
}
```

---

### 2️⃣ SPRING BOOT STARTER THYMELEAF

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

**¿Qué hace?**
Motor de templates para renderizar vistas HTML dinámicas.

**Sintaxis:**
```html
<!-- Interpolación -->
<p th:text="${message}"></p>

<!-- Condicionales -->
<div th:if="${messageType == 'success'}" class="alert alert-success">
    <span th:text="${message}"></span>
</div>

<!-- Iteración -->
<li th:each="item : ${finalCart}" 
    th:text="${item.name}"></li>

<!-- URLs -->
<form th:action="@{/login}" method="post">
    <input name="usuario" type="text">
</form>

<!-- Atributos -->
<meta name="username" th:content="${usuario}">
```

**En tu proyecto:**
```
src/main/resources/templates/
├─ index.html
├─ login.html
├─ dashboard.html
├─ simulador_citas.html
├─ simulador_ventas.html
├─ confirmacion_envio.html
└─ fragments/
   └─ navbar.html
```

---

### 3️⃣ SPRING BOOT STARTER WEB

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**¿Qué hace?**
Proporciona servidor HTTP (Tomcat) y anotaciones para controladores REST.

**Componentes:**
- `@Controller`: Maneja peticiones HTTP
- `@RestController`: Retorna JSON directamente
- `@GetMapping`, `@PostMapping`, etc: Rutas
- `Model`: Pasa datos a vistas
- `ResponseEntity`: Respuestas HTTP personalizadas

**En tu proyecto:**
```java
@Controller
public class SalesController {
    
    @GetMapping("/simulador_ventas")
    public String showSales(Model model) {
        model.addAttribute("cart", cart);
        return "simulador_ventas";
    }
    
    @PostMapping("/add_to_cart")
    public String addToCart(@RequestParam int productId, ...) {
        // Maneja petición POST
    }
}

@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Retorna JSON
    }
}
```

---

### 4️⃣ SPRING BOOT STARTER DATA JPA

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**¿Qué hace?**
ORM (Object-Relational Mapping) que mapea clases Java a tablas SQL.

**Componentes principales:**
- `@Entity`: Mapea clase a tabla
- `@Table`: Define nombre de tabla
- `@Id`: Clave primaria
- `@GeneratedValue`: Auto-incremento
- `@Column`: Columna en BD
- `JpaRepository`: CRUD automático

**En tu proyecto:**
```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String usuario;
    
    private String contrasena;
    private String role;
}

@Entity
@Table(name = "sales")
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double total;
    private LocalDateTime createdAt;
}

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
    // ↑ Métodos generados automáticamente por JPA
}
```

---

### 5️⃣ JWT (JJWT)

```xml
<!-- jjwt-api -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- jjwt-impl (runtime) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- jjwt-jackson (runtime) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**¿Qué hace?**
Crea y valida tokens JWT para autenticación sin sesiones.

**Token JWT:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuIiwicm9sZSI6IkNMSUVOVEUiLCJpYXQiOjE2MDI1ODA1NzB9.5NZKkQnZPFz3x8-P...

Partes:
1. Header: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
   {
     "alg": "HS256",
     "typ": "JWT"
   }

2. Payload: eyJzdWIiOiJqdWFuIiwicm9sZSI6IkNMSUVOVEUiLCJpYXQiOjE2MDI1ODA1NzB9
   {
     "sub": "juan",      // Subject (usuario)
     "role": "CLIENTE",  // Role
     "iat": 1602580570   // Issued at
   }

3. Signature: 5NZKkQnZPFz3x8-P... (validación criptográfica)
```

**En tu proyecto:**
```java
@Service
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    // Generar token
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    // Validar token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // Extraer username
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        String jwt = jwtUtil.generateToken(request.getUsername(), role);
        
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}
```

---

### 6️⃣ MYSQL CONNECTOR

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**¿Qué hace?**
Driver JDBC para conectarse a MySQL.

**En tu proyecto (application.properties):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/veterinariapatitas
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

---

### 7️⃣ LOMBOK

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

**¿Qué hace?**
Reduce boilerplate code generando getters, setters, constructores, etc.

**Anotaciones:**
```java
@Data  // Genera getters, setters, equals, hashCode, toString
public class Usuario {
    private Long id;
    private String usuario;
    private String contrasena;
}

// Equivalente a:
public class Usuario {
    private Long id;
    private String usuario;
    private String contrasena;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    
    // ... más getters/setters
    
    @Override
    public String toString() { ... }
    
    @Override
    public boolean equals(Object obj) { ... }
    
    @Override
    public int hashCode() { ... }
}
```

---

### 8️⃣ THYMELEAF EXTRAS SPRINGSECURITY6

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

**¿Qué hace?**
Integra Spring Security con Thymeleaf para mostrar/ocultar contenido por rol.

**En tu proyecto (templates):**
```html
<!-- Mostrar si usuario autenticado -->
<div sec:authorize="isAuthenticated()">
    <p>¡Bienvenido, <span sec:authentication="name"></span>!</p>
</div>

<!-- Mostrar si tiene rol TRABAJADOR -->
<div sec:authorize="hasRole('TRABAJADOR')">
    <a href="/dashboard">Dashboard</a>
</div>

<!-- Mostrar si NO autenticado -->
<div sec:authorize="isAnonymous()">
    <a href="/login">Iniciar Sesión</a>
</div>
```

---

### 9️⃣ SPRING AI (PDF DOCUMENT READER)

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

**¿Qué hace?**
Lee y procesa archivos PDF con IA.

**En tu proyecto:** Aparentemente no se usa directamente, pero está disponible para futura integración de lectura de documentos.

---

### 🔟 SPRING CLOUD STARTER

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter</artifactId>
</dependency>
```

**¿Qué hace?**
Base para arquitecturas microservicios (no se usa actualmente en tu proyecto, pero está para futura escalabilidad).

---

## DEPENDENCIAS TRANSITIVAS (Generadas automáticamente)

Cuando importas `spring-boot-starter-security`, Spring automáticamente trae:
- `spring-security-core`
- `spring-security-web`
- `spring-security-config`
- `spring-security-crypto` (incluye BCrypt)

Cuando importas `spring-boot-starter-data-jpa`, Spring automáticamente trae:
- `org.hibernate.orm:hibernate-core` (ORM)
- `jakarta.persistence:jakarta.persistence-api` (JPA)
- `jakarta.transaction:jakarta.transaction-api`

---

## FLUJO DE INTEGRACIÓN

```
Request HTTP
    ↓
Spring Boot Starter Web (Tomcat)
    ↓
Spring Security
    ├─ SecurityFilterChain
    ├─ AuthenticationManager
    ├─ PasswordEncoder (BCrypt)
    └─ UserDetailsService
    ↓
JWT Filter (Valida tokens con JJWT)
    ↓
Controller
    ├─ Model (pasar datos a Thymeleaf)
    └─ ResponseEntity (retornar JSON)
    ↓
Service
    └─ Lógica de negocio
    ↓
Repository (Spring Data JPA)
    └─ Consultas a BD
    ↓
MySQL Connector
    └─ Driver JDBC
    ↓
MySQL BD
    └─ Datos persistidos
    ↓
Respuesta
    ├─ HTML (Thymeleaf renderizado)
    └─ JSON (REST API)
    ↓
Cliente (Navegador/App)
```

---

## CICLO DE VIDA DE UNA PETICIÓN

```
Petición HTTP POST /login

    ↓

Spring Boot Starter Web
└─ Recibe y procesa petición

    ↓

Spring Security Filter Chain
├─ CSRF protection (deshabilitada para APIs)
├─ Sesión policy (IF_REQUIRED)
└─ Autorización (permitAll, requiresAuth, etc)

    ↓

JWT Filter (si aplica)
├─ Extrae token del header
├─ Valida con JJWT
└─ Establece authentication

    ↓

JwtAuthenticationController
├─ Recibe RequestBody (LoginRequest)
├─ Usa AuthenticationManager (Spring Security)
│   ├─ Valida con PasswordEncoder (BCrypt)
│   └─ Carga UserDetails (CustomUserDetailsService)
├─ Genera JWT (JJWT)
└─ Retorna ResponseEntity con token

    ↓

Response 200 OK
└─ JSON: { "token": "eyJh...", "username": "juan" }
```

---

## TABLA RESUMEN

| Dependencia | Versión | Propósito | Remplazable |
|-----------|---------|----------|-----------|
| spring-boot-starter-security | 3.5.6 | Autenticación/Autorización | Keycloak, OAuth2 |
| spring-boot-starter-thymeleaf | 3.5.6 | Templates HTML | Freemarker, Velocity |
| spring-boot-starter-web | 3.5.6 | REST/MVC | Quarkus, Micronaut |
| spring-boot-starter-data-jpa | 3.5.6 | ORM/CRUD | MyBatis, jOOQ |
| jjwt | 0.11.5 | JWT tokens | Nimbus JOSE + JWT |
| mysql-connector-j | runtime | JDBC Driver | MariaDB, PostgreSQL |
| lombok | optional | Reduce boilerplate | MapStruct |
| thymeleaf-extras-springsecurity6 | 3.5.6 | Security + Templates | - |

---

## CÓMO AGREGAR NUEVAS DEPENDENCIAS

### Ejemplo: Agregar PostGreSQL en lugar de MySQL

```xml
<!-- Reemplazar -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Con -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Ejemplo: Agregar Validación (para DTO validation)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Luego en controlador:
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    // @Valid valida automáticamente
}
```

---

## VERSIONES Y COMPATIBILIDAD

### Tu configuración actual:
- **Java**: 21
- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2025.0.0
- **Spring AI**: 1.0.2

### Compatibilidad importante:
- Spring Boot 3.x requiere Java 17+ ✓
- Spring Security 6.x funciona con Spring Boot 3.x ✓
- JWT 0.11.5 compatible con Java 21 ✓
- Thymeleaf 3.x soporta Spring Security 6.x ✓

---

## RESPUESTAS PARA DEFENSA

### Pregunta: "¿Por qué usas todas estas dependencias?"

**Respuesta:**
"Spring Security para proteger la aplicación con autenticación. JWT para APIs stateless. Thymeleaf para vistas dinámicas. JPA para acceso a datos. MySQL para persistencia. Cada una tiene un propósito específico y son estándares en Spring Boot."

### Pregunta: "¿Qué sucede si eliminamos una dependencia?"

**Respuesta:**
"Si eliminas spring-boot-starter-security, no tendrías autenticación. Sin JPA, no podrías acceder a la BD automáticamente. Sin Thymeleaf, tendrías que usar otro motor de templates. Sin JWT, no podrías generar tokens. Cada dependencia es crítica para su parte del sistema."

### Pregunta: "¿Cómo se comunican estas dependencias entre sí?"

**Respuesta:**
"Spring Boot actúa como orquestador. Las anotaciones como @Configuration, @Bean, @Autowired vinculan componentes. Por ejemplo, SecurityConfig define un PasswordEncoder bean que Spring inyecta en JwtAuthenticationController automáticamente. JPA genera métodos CRUD que los servicios llaman. Todo funciona mediante inyección de dependencias e integración configurada."

### Pregunta: "¿Qué pasa si hay conflicto de versiones?"

**Respuesta:**
"Spring Boot maneja compatibilidad automáticamente en spring-boot-starter-parent. Las versiones de sub-dependencias se alinean. Si hay conflicto, Maven muestra error y debes excluir explícitamente o actualizar versiones. En tu caso, está bien especificar JJWT 0.11.5 porque es explícito."

---

## DIAGRAMA: CAPAS Y DEPENDENCIAS

```
┌─────────────────────────────────────────────────────┐
│ VISTA (Navegador)                                   │
│ HTML + CSS + JavaScript                             │
└───────────────────┬─────────────────────────────────┘
                    │ HTTP Request/Response
                    ↓
┌─────────────────────────────────────────────────────┐
│ SPRING BOOT STARTER WEB                             │
│ (Recibe peticiones, enruta a controladores)        │
└───────────────────┬─────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│ SPRING SECURITY + JWT FILTER                        │
│ (Autenticación, autorización, tokens)              │
└───────────────────┬─────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│ CONTROLADORES (@Controller, @RestController)       │
│ (Procesan lógica de petición)                      │
└───────────────────┬─────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│ SERVICIOS (Business Logic)                          │
│ (Implementan reglas de negocio)                    │
└───────────────────┬─────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│ SPRING DATA JPA (Repositories)                      │
│ (Acceso a datos, ORM)                             │
└───────────────────┬─────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│ MySQL Connector J (JDBC Driver)                     │
│ (Comunicación con BD)                              │
└───────────────────┬─────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│ MYSQL DATABASE                                      │
│ (Datos persistidos)                                │
└─────────────────────────────────────────────────────┘
```

---

## CHECKLIST: Dependencias instaladas

✓ spring-boot-starter-security (Autenticación)
✓ spring-boot-starter-thymeleaf (Templates)
✓ spring-boot-starter-web (REST/MVC)
✓ spring-boot-starter-data-jpa (ORM)
✓ jjwt-api + jjwt-impl + jjwt-jackson (JWT)
✓ mysql-connector-j (MySQL Driver)
✓ lombok (Reduce código)
✓ thymeleaf-extras-springsecurity6 (Security + Thymeleaf)
✓ spring-ai-pdf-document-reader (AI - futuro)
✓ spring-cloud-starter (Microservicios - futuro)
✓ spring-boot-starter-test (Testing)
✓ spring-security-test (Security Testing)

