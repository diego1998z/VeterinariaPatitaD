# DEPENDENCIAS INTEGRADAS - CÓMO TRABAJAN JUNTAS

## 🔗 Tu proyecto usa estas dependencias clave:

---

## 1️⃣ JPA + Hibernate (Base de Datos)

### ¿Qué hace?
Mapea objetos Java a tablas de BD. No escribes SQL, escribes Java.

### En tu proyecto:

```java
@Entity  // ← Esto es JPA
@Table(name = "productos")
public class Product {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private double price;
    private int stock;
}
```

```java
// Repository usa JPA automáticamente
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);  // ← JPA genera el SQL
}
```

### ¿Cómo te ayuda en defensa?

**Profesor:** "¿Cómo guardas datos en BD sin escribir SQL?"

**Tú:** "Uso JPA. Creo una clase Java con anotaciones `@Entity` y `@Column`. Luego creo un Repository que extiende `JpaRepository`. JPA automáticamente genera las queries SQL y mapea filas de BD a objetos Java. Cuando llamo `repository.save(producto)`, JPA ejecuta el INSERT automáticamente."

---

## 2️⃣ Spring Web (Controladores HTTP)

### ¿Qué hace?
Recibe peticiones HTTP de navegadores y devuelve respuestas.

### En tu proyecto:

```java
@Controller  // ← Spring Web
public class AuthController {
    
    @GetMapping("/login")  // ← Recibe GET /login
    public String loginPage() {
        return "login";  // ← Retorna HTML
    }
    
    @PostMapping("/login")  // ← Recibe POST /login
    public String procesarLogin(@RequestParam String usuario) {
        // ... valida ...
        return "redirect:/";  // ← Redirecciona
    }
}
```

### Flujo:
```
Usuario en navegador ← HTTP GET /login → Spring recibe
Spring llama loginPage() → Retorna "login.html" → Spring envía HTML ← HTTP
```

---

## 3️⃣ Thymeleaf (Vistas HTML)

### ¿Qué hace?
Template engine que convierte HTML + lógica Java a HTML final.

### En tu proyecto:

```html
<!-- login.html -->
<div th:if="${error != null}" class="alert alert-danger">
    <span th:text="${error}"></span>  <!-- ← Accede a error del Model -->
</div>

<table>
    <tr th:each="producto : ${products}">  <!-- ← Itera lista de Java -->
        <td th:text="${producto.name}"></td>
        <td th:text="${producto.price}"></td>
    </tr>
</table>
```

### Cómo viaja el dato:

```
Java Controller:
    model.addAttribute("error", "Usuario no encontrado")
        ↓
Thymeleaf recibe Model:
    <div th:if="${error != null}">
        ↓
HTML final que ve el usuario:
    <div class="alert alert-danger">
        <span>Usuario no encontrado</span>
    </div>
```

---

## 4️⃣ Spring MVC (Patrón Arquitectónico)

### ¿Qué hace?
Organiza tu código en 3 capas: Model, View, Controller.

### En tu proyecto:

```
MODEL (Datos)
├── Usuario.java (entidad JPA)
├── Product.java (entidad JPA)
└── UsuarioRepository (acceso a datos)

VIEW (HTML)
├── login.html (Thymeleaf)
├── simulador_ventas.html (Thymeleaf)
└── index.html (Thymeleaf)

CONTROLLER (Lógica)
├── AuthController (login, logout, registro)
├── SalesController (carrito, compra)
└── MainController (home, páginas estáticas)
```

### Flujo:

```
Usuario hace clic → Controller recibe petición
    ↓
Controller pide datos a Model (Repository + Service)
    ↓
Model devuelve datos a Controller
    ↓
Controller pasa datos a View (Model.addAttribute)
    ↓
View renderiza HTML con datos
    ↓
Usuario ve HTML en navegador
```

---

## 5️⃣ Spring Session (Mantener usuario logueado)

### ¿Qué hace?
Guarda datos del usuario en sesión HTTP para que no tengas que hacer login cada vez.

### En tu proyecto:

```java
@PostMapping("/login")
public String procesarLogin(@RequestParam String usuario, 
                           HttpSession session) {
    if (esValido(usuario)) {
        session.setAttribute("usuario", usuario);  // ← Guarda en sesión
        session.setAttribute("rol", rol);
        return "redirect:/";
    }
}

@GetMapping("/")
public String home(HttpSession session) {
    String usuario = (String) session.getAttribute("usuario");  // ← Lee de sesión
    if (usuario == null) {
        return "redirect:/login";  // Si no está logueado, va a login
    }
    return "index";
}
```

### ¿Cómo funciona?

```
Login exitoso:
    session.setAttribute("usuario", "juan") ← Guarda en servidor
        ↓
Navegador recibe Cookie: SESSIONID=abc123
        ↓
Siguientes peticiones llevan Cookie
        ↓
Servidor: "Ah, es la sesión abc123, tiene usuario=juan"
        ↓
Usuario permanece logueado
```

---

## 6️⃣ Spring Validator (Validación)

### ¿Qué hace?
Valida que los datos sean correctos antes de procesarlos.

### En tu proyecto:

```java
@Service
public class SalesService {
    
    public String addProductToCart(int productId, int quantity, List<Product> cart) {
        
        // VALIDACIÓN 1: Existe el producto?
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return "Error: Producto no encontrado.";  // ← Validación
        }
        
        // VALIDACIÓN 2: Cantidad válida?
        if (quantity <= 0 || quantity > product.getStock()) {
            return "Error: Cantidad inválida.";  // ← Validación
        }
        
        // Si pasa validaciones, procesa
        cart.add(product);
        return "Agregado exitosamente";
    }
}
```

### Niveles de validación:

```
1. Frontend (HTML5)
   type="number" min="1" max="10"
   ← Rápido pero no seguro
   
2. Backend (Java)
   if (quantity <= 0) return error;
   ← Seguro, lo que cuenta
   
3. Base de Datos (JPA Constraints)
   @Column(nullable = false)
   ← Última defensa
```

---

## 🔄 CÓMO TRABAJAN INTEGRADAS

### Ejemplo: Usuario compra un producto

```
1. SPRING WEB recibe:
   POST /add_to_cart?productId=1&quantity=2

2. CONTROLLER llama a SERVICE:
   salesService.addProductToCart(1, 2, cart)

3. SERVICE valida con JPA:
   productRepository.findById(1)
   → Query: SELECT * FROM products WHERE id=1
   ← Retorna Product desde BD

4. SERVICE valida reglas de negocio:
   if (quantity > product.getStock()) error

5. Si válido, agrega a Model:
   model.addAttribute("cart", cartActualizado)
   model.addAttribute("message", "Agregado")

6. THYMELEAF renderiza:
   <div th:text="${message}"></div>
   <table th:each="p : ${cart}">
   
7. Usuario ve HTML final en navegador
```

---

## 🎓 Respuestas para defensa sobre dependencias

### Pregunta: "¿Cómo integras JPA con Spring Web?"

**Respuesta:**
"JPA proporciona los datos (entidades, repositories). Spring Web recibe peticiones HTTP y llama a los repositories para obtener datos. Luego pasa esos datos al Thymeleaf mediante Model. Todo funciona junto de forma transparente."

---

### Pregunta: "¿Por qué usas Spring Session?"

**Respuesta:**
"Para mantener el usuario logueado sin hacer login cada vez. Almaceno sus datos en sesión HTTP. Cada vez que hace una petición, el navegador envía una cookie con el ID de sesión, y el servidor reconoce al usuario."

---

### Pregunta: "¿Dónde haces la validación?"

**Respuesta:**
"En tres niveles: HTML5 en frontend (rápido), Java en service (seguro, aquí está la lógica real), y constraints en BD (última defensa). La más importante es la de Java porque es segura y reutilizable."

---

### Pregunta: "¿Cómo llegan los datos desde BD al HTML?"

**Respuesta:**
"JPA busca en BD y retorna objetos Java. El controller agrega estos objetos al Model. Thymeleaf accede al Model con ${}. Luego renderiza HTML final que ve el usuario."

---

## 📊 Matriz: Dónde se usa cada dependencia en tu código

| Dependencia | Ubicación | Propósito |
|-----------|-----------|----------|
| **JPA** | model/*.java | Mapear BD a objetos |
| **Spring Web** | controller/*.java | Recibir peticiones HTTP |
| **Thymeleaf** | resources/templates/*.html | Renderizar HTML |
| **Spring MVC** | Toda la estructura | Organizar código |
| **Session** | Controller (HttpSession param) | Mantener usuario logueado |
| **Validator** | service/*.java | Validar datos |

---

## ✅ Lo clave para defensa:

- Entiende qué hace CADA dependencia
- Entiende CÓMO TRABAJAN JUNTAS
- DÓNDE se usa en tu código específico
- NO memorices código, entiende CONCEPTOS
- Conecta con temas del silabo (MVC, validación, JPA, etc)

