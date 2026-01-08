# INFORME DETALLADO: Errores Encontrados y Soluciones Aplicadas
## Proyecto: Catálogo JWT con Spring Boot, JPA, Security y Thymeleaf

**Fecha:** 30 de Noviembre, 2025  
**Objetivo:** Integración completa de dependencias Spring Boot con autenticación JWT, gestión de imágenes y base de datos MySQL.

---

## 📋 ÍNDICE DE ERRORES CORREGIDOS

1. [Error 1: Nombres de entidades en minúsculas](#error-1-nombres-de-entidades-en-minúsculas)
2. [Error 2: Incompatibilidad de versión en Spring Boot](#error-2-incompatibilidad-de-versión-en-spring-boot)
3. [Error 3: Dependencia incorrecta "spring-boot-starter-webmvc"](#error-3-dependencia-incorrecta-spring-boot-starter-webmvc)
4. [Error 4: Dependencias de test no válidas en pom.xml](#error-4-dependencias-de-test-no-válidas-en-pomxml)
5. [Error 5: Import no utilizado (@Value) en JwtUtil.java](#error-5-import-no-utilizado-value-en-jwtutiljava)
6. [Error 6: Dialecto de Hibernate no especificado en application.properties](#error-6-dialecto-de-hibernate-no-especificado-en-applicationproperties)
7. [Error 7: Falta de configuración de contraseña MySQL](#error-7-falta-de-configuración-de-contraseña-mysql)
8. [Error 8: Archivos HTML duplicados con contenido idéntico](#error-8-archivos-html-duplicados-con-contenido-idéntico)
9. [Error 9: NullPointerException en ImageService por inyección de @Value](#error-9-nullpointerexception-en-imageservice-por-inyección-de-value)
10. [Error 10: Propiedad "file.upload-dir" faltante en application.properties](#error-10-propiedad-fileupload-dir-faltante-en-applicationproperties)

---

## 🔴 DETALLE DE CADA ERROR

### ERROR 1: Nombres de entidades en minúsculas

**PROBLEMA:**
- Las clases estaban nombradas `image.java` (minúscula) en lugar de `Image.java`
- Las clases estaban nombradas `user.java` (minúscula) en lugar de `User.java`
- Esto causaba conflictos en:
  - Los repositorios que referenciaban `user` vs `User`
  - Los servicios que usaban `user` vs `User`
  - Las anotaciones JPA que esperaban `@Entity public class User`

**IMPACTO:**
- ❌ Error: "The public type Image must be defined in its own file"
- ❌ Error: "The public type User must be defined in its own file"
- ❌ Inconsistencia de tipos en toda la aplicación

**SOLUCIÓN:**
```java
// ANTES (INCORRECTO):
public class image {
    @ManyToOne 
    @JoinColumn(name = "user_id")
    private User uploader;  // Referencia a User pero clase se llama 'image'
}

// DESPUÉS (CORRECTO):
@Entity
@Data
@NoArgsConstructor
public class Image {  // PascalCase según convención Java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fileName;
    private String storedPath; 
    private String contentType;
    private Long size;
    private String description;
    private LocalDateTime uploadDate = LocalDateTime.now();

    @ManyToOne 
    @JoinColumn(name = "user_id")
    private User uploader;
}
```

**EXPLICACIÓN TÉCNICA:**
- En Java, las clases públicas deben coincidir con el nombre del archivo
- Las convenciones de nombres (PascalCase para clases) evitan conflictos
- JPA necesita consistencia en los tipos de datos para mapear relaciones

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **JPA (Jakarta Persistence)** - Las anotaciones `@Entity` requieren nombres consistentes
- 🏷️ **Hibernate** - El ORM necesita que las clases y archivos coincidan

---

### ERROR 2: Incompatibilidad de versión en Spring Boot

**PROBLEMA:**
```xml
<!-- ANTES (INCORRECTO): -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version>  <!-- ❌ Versión muy nueva, incompatible con Java 17 -->
</parent>
<properties>
    <java.version>17</java.version>  <!-- Conflicto: Java 17 con Spring 4.0.0 -->
</properties>
```

**IMPACTO:**
- ❌ Incompatibilidad con Jakarta EE
- ❌ Conflictos con dependencias de Spring Security y JPA
- ❌ Problemas de compilación

**SOLUCIÓN:**
```xml
<!-- DESPUÉS (CORRECTO): -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>  <!-- ✅ Compatible con Java 17 -->
</parent>
<properties>
    <java.version>17</java.version>  <!-- ✅ Compatible -->
</properties>
```

**EXPLICACIÓN TÉCNICA:**
- Spring Boot 3.x usa Jakarta EE (javax -> jakarta)
- Spring Boot 4.0.0 es experimental y no es recomendable para proyectos nuevos
- La compatibilidad de versiones es crucial en proyectos Spring

**MATRIZ DE COMPATIBILIDAD:**
| Spring Boot | Java | Jakarta | Estado |
|------------|------|---------|--------|
| 3.3.4 | 17, 21 | 10.x | ✅ Recomendado |
| 4.0.0 | 21+ | 11.x | ⚠️ Experimental |

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Spring Boot Parent** - Define todas las versiones subordinadas
- 🏷️ **Jakarta EE** - Marco de aplicaciones empresariales

---

### ERROR 3: Dependencia incorrecta "spring-boot-starter-webmvc"

**PROBLEMA:**
```xml
<!-- ANTES (INCORRECTO): -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>  <!-- ❌ No existe -->
</dependency>
```

**IMPACTO:**
- ❌ Error: "Artifact not found"
- ❌ La aplicación no compilaría
- ❌ Maven no puede descargar la dependencia inexistente

**SOLUCIÓN:**
```xml
<!-- DESPUÉS (CORRECTO): -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>  <!-- ✅ Existe y es correcto -->
</dependency>
```

**EXPLICACIÓN TÉCNICA:**
- `spring-boot-starter-web` incluye:
  - Spring MVC
  - Tomcat embebido
  - Serialización JSON
  - Manejo de peticiones HTTP
- La dependencia correcta automáticamente configura todo lo necesario

**QUÉ INCLUYE `spring-boot-starter-web`:**
```
spring-boot-starter-web
├── spring-webmvc
├── spring-boot-starter-tomcat
├── spring-boot-starter-json
└── spring-boot-starter-validation
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Spring Web** - Manejo de peticiones HTTP
- 🏷️ **Spring MVC** - Controladores y vistas

---

### ERROR 4: Dependencias de test no válidas en pom.xml

**PROBLEMA:**
```xml
<!-- ANTES (INCORRECTO): -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>  <!-- ❌ No existe -->
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-test</artifactId>  <!-- ❌ No existe -->
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf-test</artifactId>  <!-- ❌ No existe -->
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>  <!-- ❌ No existe -->
    <scope>test</scope>
</dependency>
```

**IMPACTO:**
- ❌ Maven no encontraría estas dependencias
- ❌ Los tests no podrían ejecutarse
- ❌ Errores de compilación

**SOLUCIÓN:**
```xml
<!-- DESPUÉS (CORRECTO): -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>  <!-- ✅ Incluye todo para tests -->
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>  <!-- ✅ Tests de Security -->
    <scope>test</scope>
</dependency>
```

**EXPLICACIÓN TÉCNICA:**
- `spring-boot-starter-test` es un "starter" que agrupa:
  - JUnit 5
  - Mockito
  - AssertJ
  - Spring Test
- No existen starters individuales para cada módulo en testing

**QUÉ INCLUYE `spring-boot-starter-test`:**
```
spring-boot-starter-test
├── junit-jupiter
├── mockito-core
├── assertj-core
├── spring-test
└── spring-boot-test
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Spring Boot Test** - Framework de testing

---

### ERROR 5: Import no utilizado (@Value) en JwtUtil.java

**PROBLEMA:**
```java
// ANTES (INCORRECTO):
package com.example.demo.jwt;
import org.springframework.beans.factory.annotation.Value;  // ❌ Importado pero no usado
import io.jsonwebtoken.Claims;
// ... resto de imports

@Component
public class JwtUtil {
    private static final String SECRET = "...";  // ← Se usa valor hardcodeado, no @Value
}
```

**IMPACTO:**
- ⚠️ Warning: "Import is never used"
- ❌ Código desordenado
- ❌ Confusión para futuros desarrolladores

**SOLUCIÓN:**
```java
// DESPUÉS (CORRECTO):
package com.example.demo.jwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private static final String SECRET = "EstaEsMiClaveSecretaSuperSeguraParaFirmarElJWTMinimalistaDeSpring123456";
    
    // Código sin imports no utilizados
}
```

**EXPLICACIÓN TÉCNICA:**
- El análisis estático del IDE detecta imports no usados
- Es una mala práctica dejar código muerto
- Facilita el mantenimiento del código

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Spring Framework** - Anotación @Value para inyección

---

### ERROR 6: Dialecto de Hibernate no especificado en application.properties

**PROBLEMA:**
```properties
# ANTES (INCORRECTO):
spring.datasource.url=jdbc:mysql://localhost:3306/db_seguridad_jwt?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
# ❌ Falta configuración del dialecto
```

**IMPACTO:**
- ⚠️ Hibernate no optimiza las queries para MySQL
- ⚠️ Advertencia: "MySQLDialect does not need to be specified explicitly"
- ⚠️ Rendimiento subóptimo

**SOLUCIÓN:**
```properties
# DESPUÉS (CORRECTO):
spring.datasource.url=jdbc:mysql://localhost:3306/db_seguridad_jwt?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# CONFIGURACIÓN DE JPA/HIBERNATE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect  # ✅ Agregado
```

**EXPLICACIÓN TÉCNICA:**
- El dialecto le dice a Hibernate qué tipo de BD está usando
- Cada BD tiene diferencias en SQL
- Hibernate genera SQL optimizado según el dialecto

**DIFERENCIAS DE DIALECTOS:**
```
MySQL      → org.hibernate.dialect.MySQLDialect
PostgreSQL → org.hibernate.dialect.PostgreSQLDialect
Oracle     → org.hibernate.dialect.OracleDialect
H2         → org.hibernate.dialect.H2Dialect
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Hibernate** - ORM que necesita dialecto
- 🏷️ **JPA** - Interface que Hibernate implementa

---

### ERROR 7: Falta de configuración de contraseña MySQL

**PROBLEMA:**
```properties
# ANTES (INCORRECTO):
spring.datasource.username=root
spring.datasource.password=root  # ❌ Contraseña incorrecta
```

**IMPACTO:**
- ❌ Error de conexión: "Access denied for user 'root'@'localhost'"
- ❌ La aplicación no puede conectarse a la BD

**SOLUCIÓN:**
```properties
# DESPUÉS (CORRECTO):
spring.datasource.username=root
spring.datasource.password=admin  # ✅ Contraseña correcta
```

**EXPLICACIÓN TÉCNICA:**
- Las credenciales de BD deben coincidir con tu configuración de MySQL
- En this caso, la contraseña correcta era `admin`, no `root`
- Es importante no hardcodear credenciales en producción

**MEJORES PRÁCTICAS:**
```properties
# Para desarrollo:
spring.datasource.password=admin

# Para producción (usar variables de entorno):
spring.datasource.password=${DB_PASSWORD}
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **MySQL Connector** - Requiere autenticación correcta

---

### ERROR 8: Archivos HTML duplicados con contenido idéntico

**PROBLEMA:**
```
src/main/resources/templates/
├── login.html       (Contenido: Formulario de login)
└── catalog.html     (Contenido: EXACTAMENTE IGUAL A login.html ❌)
```

**Contenido duplicado:**
```html
<!-- Ambos archivos tenían esto: -->
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login - Catálogo JWT</title>
</head>
<body>
    <h2>🔒 Iniciar Sesión</h2>
    <form method="post" th:action="@{/login}">
        ...formulario de login...
    </form>
</body>
</html>
```

**IMPACTO:**
- ❌ El catálogo muestra formulario de login en lugar de galería
- ❌ Los usuarios no pueden ver las imágenes
- ❌ Funcionalidad rota

**SOLUCIÓN:**
```html
<!-- login.html (SE MANTIENE IGUAL) -->
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Login - Catálogo JWT</title>
</head>
<body>
    <h2>🔒 Iniciar Sesión</h2>
    <form method="post" th:action="@{/login}">
        <input type="text" name="username" required>
        <input type="password" name="password" required>
        <button type="submit">Entrar</button>
    </form>
</body>
</html>

<!-- catalog.html (SE REEMPLAZA CON GALERÍA CORRECTA) -->
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Catálogo de Imágenes - JWT</title>
</head>
<body>
    <h1>📸 Galería de Imágenes</h1>
    
    <div class="upload-section">
        <h3>Subir Nueva Imagen</h3>
        <form method="post" th:action="@{/upload}" enctype="multipart/form-data">
            <input type="file" name="file" accept="image/*" required>
            <textarea name="description" required></textarea>
            <button type="submit">Subir Imagen</button>
        </form>
    </div>

    <div class="gallery">
        <div th:each="image : ${images}">
            <img th:src="@{'/uploads/' + ${image.storedPath}}" th:alt="${image.fileName}">
            <p th:text="${image.description}"></p>
            <p th:text="${image.uploader.username}"></p>
        </div>
    </div>
</body>
</html>
```

**EXPLICACIÓN TÉCNICA:**
- Cada vista (HTML) en Thymeleaf debe tener su propia responsabilidad
- MVC: Cada controlador mapea a una vista diferente
- Los datos se pasan mediante `Model` y se renderizan con `th:*`

**FLUJO DE CONTROLADORES:**
```java
@GetMapping("/login")          // → Retorna login.html
public String loginPage() { return "login"; }

@GetMapping("/catalog")        // → Retorna catalog.html
public String viewCatalog(Model model) {
    model.addAttribute("images", imageService.findAll());
    return "catalog";
}
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Thymeleaf** - Template engine que procesa HTML
- 🏷️ **Spring Web MVC** - Mapeo de vistas

---

### ERROR 9: NullPointerException en ImageService por inyección de @Value

**PROBLEMA:**
```java
// ANTES (INCORRECTO):
@Service
public class ImageService {
    private final ImageRepository imageRepository;

    @Value("${file.upload-dir}")  // ← Se inyecta en construcción
    private String uploadDir;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
        try {
            Files.createDirectories(Paths.get(uploadDir));  // ❌ uploadDir es NULL aquí
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el directorio", e);
        }
    }
}
```

**IMPACTO:**
- ❌ NullPointerException: `uploadDir` es null en el constructor
- ❌ La aplicación no inicia
- ❌ Error: "Failed to instantiate [ImageService]: Constructor threw exception"

**CAUSA TÉCNICA:**
- Spring inyecta valores `@Value` **DESPUÉS** de ejecutar el constructor
- En el constructor, los campos aún no están inyectados

**SOLUCIÓN:**
```java
// DESPUÉS (CORRECTO - Opción 1: Usar @PostConstruct):
@Service
public class ImageService {
    private final ImageRepository imageRepository;

    @Value("${file.upload-dir:uploads/}")  // ✅ Con valor por defecto
    private String uploadDir;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
        // ← No inicializar aquí
    }

    @PostConstruct  // ✅ Se ejecuta DESPUÉS de la inyección
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
    }

    public Image storeImage(MultipartFile file, String description, User uploader) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path targetLocation = Paths.get(uploadDir).resolve(uniqueFileName);
        
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        Image image = new Image();
        image.setFileName(originalFileName);
        image.setStoredPath(uniqueFileName);
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setDescription(description);
        image.setUploader(uploader);
        
        return imageRepository.save(image);
    }
}
```

**EXPLICACIÓN TÉCNICA - Ciclo de vida de un Bean:**
```
1. Constructor ejecuta ← uploadDir aún es NULL
2. Inyección de dependencias (@Autowired, @Value)
3. @PostConstruct ejecuta ← uploadDir ya tiene valor
4. Bean está listo para usar
```

**SOLUCIÓN ALTERNATIVA (Opción 2: Constructor con parámetros):**
```java
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final String uploadDir;

    public ImageService(ImageRepository imageRepository, 
                       @Value("${file.upload-dir:uploads/}") String uploadDir) 
                       throws IOException {
        this.imageRepository = imageRepository;
        this.uploadDir = uploadDir;
        Files.createDirectories(Paths.get(uploadDir));  // ✅ Ahora funciona
    }
}
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Spring Framework** - Inyección de dependencias
- 🏷️ **Spring Boot** - Configuración de propiedades

---

### ERROR 10: Propiedad "file.upload-dir" faltante en application.properties

**PROBLEMA:**
```properties
# ANTES (INCORRECTO):
spring.application.name=demo
spring.datasource.url=jdbc:mysql://localhost:3306/db_seguridad_jwt?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
# ❌ Falta: file.upload-dir
```

**IMPACTO:**
- ❌ `@Value("${file.upload-dir}")` no encuentra la propiedad
- ❌ Spring intenta inyectar `null`
- ❌ NullPointerException en ImageService

**SOLUCIÓN:**
```properties
# DESPUÉS (CORRECTO):
spring.application.name=demo

# CONFIGURACIÓN DE BASE DE DATOS (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/db_seguridad_jwt?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# CONFIGURACIÓN DE JPA/HIBERNATE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# CONFIGURACIÓN DEL SERVIDOR WEB
server.port=8080

# CONFIGURACIÓN DE CARGA DE ARCHIVOS
file.upload-dir=uploads/  # ✅ Agregado
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

**EXPLICACIÓN TÉCNICA:**
- `application.properties` contiene todas las configuraciones de la aplicación
- Spring lee estas propiedades y las inyecta con `@Value`
- Pueden tener valores por defecto: `@Value("${file.upload-dir:uploads/")`

**ESTRUCTURA DE PROPIEDADES:**
```properties
# Formato: clave=valor
# Categoría.Propiedad=valor

# Base de datos
spring.datasource.url=...
spring.datasource.username=...

# Aplicación personalizada
file.upload-dir=...
app.jwt.secret=...
app.jwt.expiration=...
```

**DEPENDENCIAS INVOLUCRADAS:**
- 🏷️ **Spring Boot** - Carga de propiedades
- 🏷️ **Spring Framework** - Inyección de valores

---

## 📊 TABLA RESUMEN DE ERRORES

| # | Error | Severidad | Dependencia | Solución |
|---|-------|-----------|-------------|----------|
| 1 | Entidades en minúsculas | 🔴 CRÍTICA | JPA/Hibernate | Renombrar a `Image.java` y `User.java` |
| 2 | Spring Boot 4.0.0 incompatible | 🔴 CRÍTICA | Spring Boot | Cambiar a versión 3.3.4 |
| 3 | `spring-boot-starter-webmvc` | 🔴 CRÍTICA | Spring Boot | Cambiar a `spring-boot-starter-web` |
| 4 | Dependencias de test inválidas | 🔴 CRÍTICA | Maven | Usar `spring-boot-starter-test` |
| 5 | Import `@Value` no usado | 🟡 ADVERTENCIA | Spring | Remover import innecesario |
| 6 | Dialecto Hibernate faltante | 🟡 ADVERTENCIA | JPA/Hibernate | Agregar `spring.jpa.properties.hibernate.dialect` |
| 7 | Contraseña BD incorrecta | 🔴 CRÍTICA | MySQL | Usar contraseña correcta `admin` |
| 8 | HTML duplicados | 🔴 CRÍTICA | Thymeleaf | Crear `catalog.html` con galería |
| 9 | NullPointerException en ImageService | 🔴 CRÍTICA | Spring/IO | Usar `@PostConstruct` o inyección constructor |
| 10 | Propiedad `file.upload-dir` faltante | 🔴 CRÍTICA | Spring Boot | Agregar a `application.properties` |

---

## 🎓 CONCEPTOS CLAVE PARA SUSTENTACIÓN

### 1. **Convenciones de Nombres en Java**
```java
// ✅ CORRECTO
public class UserRepository        // Interfaz
public class User                  // Entidad
public class UserService           // Servicio
public class LoginController       // Controlador

// ❌ INCORRECTO
public class user                  // Minúscula
public class UserSERVICE           // Mayúsculas inconsistentes
```

### 2. **Ciclo de Vida de Beans en Spring**
```
1. Creación del Bean (Constructor)
2. Inyección de Dependencias
3. Métodos @PostConstruct
4. Bean listo
5. Métodos @PreDestroy antes de eliminar
```

### 3. **Diferencia entre @Value y Constructor Injection**
```java
// INCORRECTO: @Value en constructor
@Service
public class MyService {
    @Value("${property}")
    private String value;  // NULL aquí
    
    public MyService() {
        System.out.println(value);  // ❌ null
    }
}

// CORRECTO: Constructor injection
@Service
public class MyService {
    private final String value;
    
    public MyService(@Value("${property}") String value) {
        this.value = value;  // ✅ tiene valor
        System.out.println(value);
    }
}

// CORRECTO: @PostConstruct
@Service
public class MyService {
    @Value("${property}")
    private String value;
    
    @PostConstruct
    public void init() {
        System.out.println(value);  // ✅ tiene valor
    }
}
```

### 4. **Propiedades de Hibernate DDL**
```properties
# ddl-auto opciones:
spring.jpa.hibernate.ddl-auto=create         # Crea siempre
spring.jpa.hibernate.ddl-auto=create-drop    # Crea y borra al cerrar
spring.jpa.hibernate.ddl-auto=update         # Actualiza si existe
spring.jpa.hibernate.ddl-auto=validate       # Solo valida
spring.jpa.hibernate.ddl-auto=none           # No hace nada
```

### 5. **Estructura de Dependencias en pom.xml**
```xml
<!-- Starter: agrupa múltiples dependencias -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- Incluye automáticamente:
         - spring-webmvc
         - spring-boot-starter-tomcat
         - spring-boot-starter-json
         - spring-boot-starter-validation
    -->
</dependency>
```

### 6. **Integración JWT + Security + JPA**
```java
// Flujo de autenticación:
1. Usuario envía credenciales → AuthController
2. AuthController usa AuthenticationManager (Spring Security)
3. UserService carga User desde BD (JPA)
4. Si es válido → Generar JWT (JwtUtil)
5. JWT se almacena en Cookie
6. Siguientes peticiones llevan JWT en Cookie
7. JwtRequestFilter valida JWT
8. Usuario accede a recursos protegidos
```

---

## 🔧 MATRIZ DE DEPENDENCIAS UTILIZADAS

| Dependencia | Versión | Propósito |
|-------------|---------|----------|
| spring-boot-starter-data-jpa | 3.3.4 | Acceso a BD con ORM |
| spring-boot-starter-security | 3.3.4 | Autenticación y autorización |
| spring-boot-starter-web | 3.3.4 | MVC y controladores REST |
| spring-boot-starter-thymeleaf | 3.3.4 | Template engine para vistas |
| mysql-connector-j | 8.x | Driver JDBC para MySQL |
| lombok | 1.18.x | Reduce código boilerplate |
| jjwt-api | 0.11.5 | Generación y validación de JWT |
| jjwt-impl | 0.11.5 | Implementación de JWT |
| jjwt-jackson | 0.11.5 | Serialización JSON en JWT |
| hibernate-validator | 8.x | Validación de entidades |

---

## 🚀 CHECKLIST PARA FUTUROS PROYECTOS

- [ ] Usar PascalCase para nombres de clases
- [ ] Verificar compatibilidad de versiones Spring Boot con Java
- [ ] Usar starters de Spring Boot (spring-boot-starter-*)
- [ ] Incluir dialecto de Hibernate en application.properties
- [ ] Verificar credenciales de BD antes de ejecutar
- [ ] Evitar lógica de inicialización en constructores (usar @PostConstruct)
- [ ] Incluir todas las propiedades necesarias en application.properties
- [ ] Verificar que no haya imports no utilizados
- [ ] Una clase pública por archivo
- [ ] Usar inyección de dependencias correctamente

---

**FIN DEL INFORME**

