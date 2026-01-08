# 4. ENCRIPTACIÓN DE CONTRASEÑAS (BCryptPasswordEncoder)

## 🔐 Cómo se protegen y validan las contraseñas

---

## ¿POR QUÉ ENCRIPTAR CONTRASEÑAS?

**NUNCA guardes contraseñas en texto plano en la BD.**

```
❌ MALO: Tabla usuarios
┌────┬──────┬────────────┐
│ id │ user │ password   │
├────┼──────┼────────────┤
│ 1  │ juan │ 123456     │  ← ¡Visible para todos!
│ 2  │ maria│ abc123     │  ← Inseguro
└────┴──────┴────────────┘

✓ BUENO: Tabla usuarios
┌────┬──────┬────────────────────────────────────────────────────┐
│ id │ user │ password (hash)                                     │
├────┼──────┼────────────────────────────────────────────────────┤
│ 1  │ juan │ $2a$10$X5Z9vK2mL3... (hash BCrypt)                 │
│ 2  │ maria│ $2a$10$Q7M4nP8jK9... (hash BCrypt)                 │
└────┴──────┴────────────────────────────────────────────────────┘
```

---

## ¿QUÉ ES BCrypt?

**BCrypt** es un algoritmo de encriptación especialmente diseñado para contraseñas:

### Características:
- ✓ **Unidireccional**: No se puede desencriptar (solo verificar)
- ✓ **Salt automático**: Cada hash es único aunque la contraseña sea igual
- ✓ **Lento a propósito**: Resiste ataques de fuerza bruta (intentos repetidos)
- ✓ **Adaptativo**: Se puede aumentar la dificultad con el tiempo

### Ejemplo de hash BCrypt:

```
Contraseña original: "123456"

Hash BCrypt: $2a$10$X5Z9vK2mL3nP8qR1sT4uVeWxYzAaBbCcDdEeFfGgHhIiJjKkLlMmNn

Desglose:
$2a     → Algoritmo (versión 2a)
$10     → Costo (factor de trabajo)
X5Z9... → Salt (generado aleatoriamente)
vK2mL3n...  → Hash de la contraseña
```

---

## FLUJO COMPLETO: REGISTRACIÓN

```
USUARIO EN NAVEGADOR
├─ Ingresa: usuario = "juan"
├─ Ingresa: contraseña = "123456"
└─ Presiona "Registrarse"

    ↓

POST /api/auth/register
Body JSON: {
    "username": "juan",
    "password": "123456"
}

    ↓

CONTROLLER (JwtAuthenticationController.java):
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
    
    // VALIDACIÓN 1: ¿Ya existe el usuario?
    if (usuarioRepository.existsByUsuario(registerRequest.getUsername())) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "El usuario ya está en uso."));
    }
    
    // PASO 1: Crea usuario
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setUsuario("juan");
    
    // PASO 2: ENCRIPTA la contraseña
    nuevoUsuario.setContrasena(
        passwordEncoder.encode("123456")  // ← BCryptPasswordEncoder
    );
    
    // PASO 3: Asigna rol
    nuevoUsuario.setRole("CLIENTE");
    
    // PASO 4: Guarda en BD
    usuarioRepository.save(nuevoUsuario);
    
    // PASO 5: Genera JWT token
    String jwt = jwtUtil.generateToken("juan", "CLIENTE");
    
    // PASO 6: Retorna respuesta
    return ResponseEntity.ok(Map.of(
        "token", jwt,
        "username", "juan",
        "role", "CLIENTE",
        "message", "Registro exitoso"
    ));
}

    ↓

¿QUÉ SUCEDE EN passwordEncoder.encode("123456")?

1. GENERAR SALT (número aleatorio único)
   Salt = generarAleatorio()
   Ejemplo: X5Z9vK2mL3nP8qR1sT4uVe

2. COMBINAR CONTRASEÑA + SALT
   Combined = "123456" + "X5Z9vK2mL3nP8qR1sT4uVe"

3. APLICAR BCrypt
   hash = bcrypt(Combined, costFactor=10)
   Resultado: $2a$10$X5Z9vK2mL3nP8qR1sT4uVeWxYzAaBbCc...

4. RETORNAR HASH
   $2a$10$X5Z9vK2mL3nP8qR1sT4uVeWxYzAaBbCcDdEeFfGgHhIiJjKkLlMmNn

    ↓

SERVICE GUARDA EN BD:
INSERT INTO usuarios (usuario, contrasena, role)
VALUES ('juan', '$2a$10$X5Z9vK2mL3nP8qR1sT4uVeWxYzAaBbCcDdEeFfGgHhIiJjKkLlMmNn', 'CLIENTE');

    ↓

USUARIO RECIBE RESPUESTA JSON:
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "juan",
    "role": "CLIENTE",
    "message": "Registro exitoso"
}

    ↓

USUARIO VE EN NAVEGADOR:
✓ "Registro exitoso"
Token guardado en localStorage o sessionStorage
```

---

## FLUJO COMPLETO: LOGIN

```
USUARIO EN NAVEGADOR
├─ Ingresa: usuario = "juan"
├─ Ingresa: contraseña = "123456"
└─ Presiona "Iniciar Sesión"

    ↓

POST /api/auth/login
Body JSON: {
    "username": "juan",
    "password": "123456"
}

    ↓

CONTROLLER (JwtAuthenticationController.java):
@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    
    try {
        // PASO 1: Usa AuthenticationManager
        // (que usa CustomUserDetailsService y PasswordEncoder)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                "juan",      // usuario
                "123456"     // contraseña (plano, aún)
            )
        );
        
        // ¿QUÉ SUCEDE EN authenticationManager.authenticate()?
        
        // 1. CustomUserDetailsService.loadUserByUsername("juan")
        //    └─ Busca en BD: SELECT * FROM usuarios WHERE usuario = 'juan'
        //    └─ Encuentra: Usuario(id=1, usuario="juan", 
        //                           contrasena="$2a$10$X5Z9vK2mL3...", 
        //                           role="CLIENTE")
        
        // 2. PasswordEncoder.matches("123456", "$2a$10$X5Z9vK2mL3...")
        //    ├─ Extrae el SALT del hash: "X5Z9vK2mL3..."
        //    ├─ Aplica BCrypt a la contraseña + salt
        //    ├─ Compara resultado con el hash guardado
        //    └─ ¿Coinciden? ✓ Sí → Contraseña correcta
        //                    ✗ No → Contraseña incorrecta
        
        // 3. Si matches retorna true, Authentication es exitoso
        //    Si retorna false, lanza AuthenticationException
        
        // PASO 2: Si llega aquí, autenticación exitosa
        
        // PASO 3: Busca el rol
        Optional<Usuario> userOptional = usuarioRepository.findByUsuario("juan");
        String role = userOptional.isPresent() ? 
            userOptional.get().getRole() : "USER";
        
        // PASO 4: Genera JWT token
        String jwt = jwtUtil.generateToken("juan", "CLIENTE");
        
        // PASO 5: Retorna respuesta
        return ResponseEntity.ok(Map.of(
            "token", jwt,
            "username", "juan",
            "role", "CLIENTE"
        ));
        
    } catch (Exception e) {
        // Login fallido
        return ResponseEntity.status(401)
            .body(Map.of("error", "Credenciales incorrectas."));
    }
}

    ↓

¿POR QUÉ passwordEncoder.matches() FUNCIONA?

Hash guardado: $2a$10$X5Z9vK2mL3nP8qR1sT4uVeWxYzAaBbCcDdEeFfGgHhIiJjKkLlMmNn

1. EXTRAE SALT del hash
   Salt = X5Z9vK2mL3nP8qR1sT4uVe (está incluido en el hash)

2. APLICA BCrypt a la contraseña + salt
   hash_nuevo = bcrypt("123456" + X5Z9vK2mL3nP8qR1sT4uVe, costFactor=10)
   Resultado: $2a$10$X5Z9vK2mL3nP8qR1sT4uVeWxYzAaBbCc...

3. COMPARA
   ¿hash_nuevo == hash_guardado?
   ✓ Sí → Contraseña correcta
   ✗ No → Contraseña incorrecta

    ↓

USUARIO RECIBE RESPUESTA JSON:
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "juan",
    "role": "CLIENTE"
}

    ↓

USUARIO VE EN NAVEGADOR:
✓ "Login exitoso"
Token guardado para futuras peticiones
```

---

## COMPONENTES CLAVE

### 1️⃣ CONFIGURACIÓN: SecurityConfig.java

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // ↑ Este Bean se inyecta automáticamente en:
        //   - JwtAuthenticationController
        //   - CustomUserDetailsService
        //   - Cualquier otro componente que lo necesite
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
        // ↑ Encargado de validar credenciales
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())        // ← Desahabilita CSRF para APIs REST
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                
                // Rutas protegidas
                .requestMatchers("/dashboard").hasAuthority("TRABAJADOR")
                .requestMatchers("/api/**").authenticated()
                
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("usuario")         // ← Nombre del campo HTML
                .passwordParameter("contrasena")      // ← Nombre del campo HTML
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        // Agregar filtro JWT para APIs
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 2️⃣ CONTROLADOR: JwtAuthenticationController.java

```java
@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;  // ← Inyectado desde SecurityConfig
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // authenticationManager valida credenciales
            // Internamente usa CustomUserDetailsService + PasswordEncoder
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Genera token JWT
            String jwt = jwtUtil.generateToken(
                loginRequest.getUsername(),
                role
            );

            return ResponseEntity.ok(Map.of("token", jwt));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Credenciales incorrectas"));
        }
    }

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        
        // Validar que no exista
        if (usuarioRepository.existsByUsuario(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Usuario ya existe"));
        }

        // Crear usuario con contraseña ENCRIPTADA
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(registerRequest.getUsername());
        
        // ← ENCRIPTACIÓN: BCrypt
        nuevoUsuario.setContrasena(
            passwordEncoder.encode(registerRequest.getPassword())
        );
        
        nuevoUsuario.setRole("CLIENTE");
        usuarioRepository.save(nuevoUsuario);

        // Generar token JWT
        String jwt = jwtUtil.generateToken(
            nuevoUsuario.getUsuario(),
            nuevoUsuario.getRole()
        );

        return ResponseEntity.ok(Map.of(
            "token", jwt,
            "message", "Registro exitoso"
        ));
    }
}
```

### 3️⃣ SERVICIO: CustomUserDetailsService.java

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        // Busca usuario en BD
        Usuario usuario = usuarioRepository.findByUsuario(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado: " + username
            ));

        // Retorna UserDetails con contraseña encriptada
        // (Spring Security luego llamará a PasswordEncoder.matches())
        return org.springframework.security.core.userdetails.User.builder()
            .username(usuario.getUsuario())
            .password(usuario.getContrasena())  // ← Hash BCrypt
            .authorities("ROLE_" + usuario.getRole())
            .build();
    }
}
```

### 4️⃣ MODELO: Usuario.java

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String usuario;
    
    private String contrasena;  // ← Hash BCrypt aquí
    
    private String role;  // CLIENTE, TRABAJADOR, etc.
    
    // Getters y Setters
}
```

---

## COMPARACIÓN: ANTES Y DESPUÉS

### ❌ ANTES (AuthController - Inseguro)

```java
@PostMapping("/login")
public String login(String usuario, String contrasena, HttpSession session, Model model) {
    
    // ❌ PROBLEMA 1: Credenciales hardcodeadas
    if (usuario.equals("trabajador") && contrasena.equals("123456")) {
        session.setAttribute("usuario", usuario);
        return "redirect:/dashboard";
    }
    
    // ❌ PROBLEMA 2: Texto plano en la comparación
    // ❌ PROBLEMA 3: Sin encriptación
    // ❌ PROBLEMA 4: Sin validación de contraseña real en BD
    
    model.addAttribute("messageType", "danger");
    return "login";
}
```

### ✓ DESPUÉS (JwtAuthenticationController - Seguro)

```java
@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    
    try {
        // ✓ SEGURIDAD 1: AuthenticationManager valida
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );
        
        // ✓ SEGURIDAD 2: CustomUserDetailsService busca en BD
        // ✓ SEGURIDAD 3: PasswordEncoder.matches() compara hashes
        // ✓ SEGURIDAD 4: JWT tokens en lugar de sesiones
        
        String jwt = jwtUtil.generateToken(username, role);
        
        return ResponseEntity.ok(Map.of("token", jwt));
        
    } catch (Exception e) {
        // ✓ SEGURIDAD 5: No revelar detalles específicos de error
        return ResponseEntity.status(401)
            .body(Map.of("error", "Credenciales incorrectas"));
    }
}
```

---

## FLOW VISUAL: PasswordEncoder

```
REGISTRO:
usuario ingresa "123456"
    ↓
passwordEncoder.encode("123456")
    ├─ Genera SALT aleatorio
    ├─ Aplica BCrypt
    └─ Retorna: $2a$10$X5Z9vK2mL3nP8qR1sT4uVe...
    ↓
Se guarda en BD el HASH (nunca la contraseña original)

LOGIN:
usuario ingresa "123456"
    ↓
authenticationManager.authenticate()
    ├─ CustomUserDetailsService.loadUserByUsername()
    │   └─ Obtiene de BD: $2a$10$X5Z9vK2mL3nP8qR1sT4uVe...
    ├─ PasswordEncoder.matches("123456", "$2a$10$X5Z9vK2mL3nP8qR1sT4uVe...")
    │   ├─ Extrae SALT del hash
    │   ├─ Aplica BCrypt a "123456" + SALT
    │   └─ Compara con hash original
    └─ Retorna true/false
    ↓
Si true: Login exitoso
Si false: Credenciales incorrectas
```

---

## RESPUESTAS PARA DEFENSA

### Pregunta: "¿Cómo se protegen las contraseñas?"

**Respuesta:**
"Usamos BCryptPasswordEncoder, un algoritmo especialmente diseñado para contraseñas. Cuando el usuario se registra, la contraseña en texto plano se encripta con BCrypt, generando un hash único que se guarda en la BD. La contraseña original nunca se guarda. Al hacer login, Spring Security valida la contraseña ingresada comparándola contra el hash con el método matches(), que extrae el salt del hash, lo aplica a la contraseña ingresada, y compara el resultado."

### Pregunta: "¿Por qué BCrypt es mejor que SHA o MD5?"

**Respuesta:**
"BCrypt es más lento a propósito, lo que previene ataques de fuerza bruta. SHA y MD5 son rápidos, permitiendo a los atacantes intentar millones de combinaciones por segundo. BCrypt tiene un 'factor de costo' que se puede aumentar con el tiempo. Además, BCrypt genera un salt único para cada contraseña, incluso si dos usuarios tienen la misma contraseña, sus hashes serán diferentes."

### Pregunta: "¿Qué sucede si se roba la BD?"

**Respuesta:**
"Si alguien roba la tabla de usuarios, solo obtiene los hashes, no las contraseñas. Como BCrypt es unidireccional, no se puede desencriptar. El atacante tendría que gastar mucho tiempo intentando hacer fuerza bruta contra cada hash individualmente. Con el factor de costo configurado en 10, cada intento toma ~100ms. Para crackear una contraseña de 8 caracteres tomaría billones de años."

### Pregunta: "¿Cómo AuthenticationManager valida credenciales?"

**Respuesta:**
"AuthenticationManager recibe usuario y contraseña en texto plano. Llama a CustomUserDetailsService.loadUserByUsername() que busca en la BD y obtiene el usuario con su hash de contraseña. Luego llama a PasswordEncoder.matches() pasando la contraseña ingresada y el hash de la BD. Si coinciden, retorna true y la autenticación es exitosa. Si no coinciden, lanza AuthenticationException."

### Pregunta: "¿Por qué usas JWT en lugar de sesiones?"

**Respuesta:**
"Para APIs REST es mejor usar JWT (tokens) en lugar de sesiones. Las sesiones necesitan almacenamiento del lado del servidor, JWT es stateless. El cliente guarda el token y lo envía en cada petición. Spring Security valida el token contra una firma criptográfica. Así es más escalable: múltiples servidores sin compartir estado de sesiones."

---

## TABLA RESUMEN

| Paso | Operación | Resultado |
|------|-----------|-----------|
| **REGISTRO** | Contraseña plana ingresada | "123456" |
| | BCryptPasswordEncoder.encode() | Hash: $2a$10$X5Z9... |
| | Guarda en BD | Tabla: usuarios.contrasena = $2a$10$X5Z9... |
| **LOGIN** | Contraseña plana ingresada | "123456" |
| | AuthenticationManager.authenticate() | Busca usuario en BD |
| | PasswordEncoder.matches() | Compara "123456" vs hash |
| | Resultado | ✓ Correcto o ✗ Incorrecto |
| | Si correcto, genera JWT | Token válido |

---

## MODELO DE SEGURIDAD

```
Credenciales (usuario, password)
    ↓
POST /login o POST /register
    ↓
JwtAuthenticationController
    ↓
AuthenticationManager
    ├─ CustomUserDetailsService (busca en BD)
    ├─ PasswordEncoder (compara hashes con BCrypt)
    └─ Retorna Authentication exitosa o excepción
    ↓
Si exitosa: Genera JWT token
Si falla: Retorna error 401
    ↓
Cliente recibe token
    ↓
Cliente incluye token en headers en próximas peticiones:
Authorization: Bearer eyJhbGciOi...
    ↓
JwtFilter valida token
    ↓
Si válido: Petición procesada
Si inválido: Retorna error 401
```

