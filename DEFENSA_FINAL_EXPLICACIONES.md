# DEFENSA FINAL - EXPLICACIONES POR BLOQUE DE CÓDIGO

## 🎯 Cómo explicar cada bloque sin entrar en detalles línea por línea

---

## BLOQUE 1: Login con Roles

```java
@PostMapping("/login")
public String procesarLogin(
        @RequestParam String usuario,
        @RequestParam String contrasena,
        @RequestParam String rol,
        HttpSession session,
        Model model) {

    // USUARIO TRABAJADOR EJEMPLO
    if (usuario.equals("trabajador") && contrasena.equals("123456") && rol.equals("TRABAJADOR")) {
        session.setAttribute("usuario", usuario);
        session.setAttribute("rol", rol);
        return "redirect:/";
    }

    model.addAttribute("error", "Credenciales incorrectas");
    return "login";
}
```

### ¿Qué hace en general?

**Respuesta directa (30 segundos):**
"Este controlador valida credenciales de login. Verifica que usuario, contraseña y rol sean correctos. Si son válidos, almacena los datos en sesión y redirige al inicio. Si falla, muestra error."

### ¿Cómo explicarlo en defensa?

**Estructura de tu explicación:**

1. **Propósito:** "Es el endpoint que procesa el formulario de login"

2. **Entradas:** 
   - Usuario, contraseña y rol vienen del formulario
   - HttpSession para guardar datos
   - Model para pasar mensajes

3. **Lógica central:**
   - Valida credenciales (usuario=trabajador, contrasena=123456, rol=TRABAJADOR)
   - Si OK → guarda en sesión y va a inicio
   - Si falla → muestra mensaje de error

4. **Salida:** 
   - Redirecciona al home o muestra error

### Ejemplo de cómo responder en vivo:

**Profesor:** "¿Qué hace este método?"

**Tú:** "Es el método que procesa el login. Recibe usuario, contraseña y rol del formulario. Verifica si son correctos. Si lo son, los almacena en la sesión HTTP para que el usuario permanezca logueado durante la navegación. Si las credenciales son incorrectas, retorna un mensaje de error al usuario."

---

## BLOQUE 2: Validación en Service

```java
@Service
public class SalesService {
    
    public String addProductToCart(int productId, int quantity, List<Product> cart) {
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            return "Error: Producto no encontrado.";
        }
        
        Product product = productOpt.get();
        
        if (quantity <= 0 || quantity > product.getStock()) {
            return "Error: Cantidad inválida o stock insuficiente.";
        }
        
        // Agregar al carrito
        cart.add(new Product(product.getId(), product.getName(), 
                             product.getPrice(), quantity));
        return "Producto agregado al carrito con éxito.";
    }
}
```

### ¿Qué hace en general?

**Respuesta directa (30 segundos):**
"Este servicio agrega productos al carrito validando dos cosas: que el producto exista en BD y que haya stock suficiente para la cantidad solicitada."

### Cómo explicarlo:

1. **Propósito:** "Valida y agrega productos al carrito"

2. **Validaciones:**
   - Busca si el producto existe en BD
   - Verifica que la cantidad sea válida (> 0)
   - Verifica que no exceda el stock disponible

3. **Acción:**
   - Si todo es válido, agrega el producto
   - Retorna mensaje de éxito o error

### Respuesta en vivo:

**Profesor:** "¿Por qué validas en el servicio y no en el controlador?"

**Tú:** "Porque la lógica de negocio debe estar en el servicio, no en el controlador. El controlador solo recibe datos y llama al servicio. De esta forma, el código es reutilizable y separamos responsabilidades. Si otro controlador necesita agregar al carrito, reutiliza el mismo servicio."

---

## BLOQUE 3: Manejo de Sesiones

```java
@GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();  // Elimina todos los datos de sesión
    return "redirect:/login";  // Redirige a login
}
```

### ¿Qué hace en general?

**Respuesta directa (15 segundos):**
"Destruye la sesión del usuario y lo redirige a login para que se desconecte."

### Cómo explicarlo:

1. **Propósito:** "Cierra la sesión del usuario"

2. **Acción:**
   - `session.invalidate()` elimina todos los datos almacenados
   - El usuario ya no está autenticado
   - Se redirige a login

### Respuesta en vivo:

**Profesor:** "¿Qué pasa si no invalidas la sesión?"

**Tú:** "El usuario seguiría en la sesión activa incluso después de hacer logout. Sus datos (usuario, rol) seguirían en memoria del servidor, permitiendo que otra persona use la misma sesión."

---

## BLOQUE 4: Pasar datos a vistas (Model)

```java
@GetMapping("/simulador_ventas")
public String simulador(Model model, 
                       @ModelAttribute("cart") List<Product> cart) {
    List<Product> productos = salesService.getInitialProducts();
    
    model.addAttribute("products", productos);
    model.addAttribute("cart", cart);
    model.addAttribute("total", calculateTotal(cart));
    
    return "simulador_ventas";  // Thymeleaf procesa estos datos
}
```

### ¿Qué hace en general?

**Respuesta directa (30 segundos):**
"Obtiene la lista de productos y carrito, los almacena en el Model para que Thymeleaf los acceda en la vista, y retorna el HTML."

### Cómo explicarlo:

1. **Propósito:** "Prepara datos para la vista"

2. **Datos que pasa:**
   - Lista de productos disponibles
   - Carrito del usuario
   - Total a pagar

3. **Cómo viajan:**
   - Se almacenan en `Model`
   - Thymeleaf accede con `${products}`, `${cart}`, etc.
   - Se renderizan en HTML

### Respuesta en vivo:

**Profesor:** "¿Cómo sabe Thymeleaf qué datos mostrar?"

**Tú:** "Porque usamos model.addAttribute() para pasar los datos desde Java a Thymeleaf. En el HTML, accedemos con ${}. Por ejemplo, `model.addAttribute('products', list)` y en HTML hacemos `<div th:each='p : ${products}'>`"

---

## BLOQUE 5: Controlador REST vs MVC

```java
// CONTROLADOR MVC (Retorna HTML)
@GetMapping("/")
public String home(Model model) {
    model.addAttribute("titulo", "Inicio");
    return "index";  // Retorna vista HTML
}

// CONTROLADOR REST (Retorna JSON)
@PostMapping("/api/producto")
@ResponseBody
public ResponseEntity<Producto> crear(@RequestBody Producto p) {
    Producto guardado = productRepository.save(p);
    return ResponseEntity.ok(guardado);  // Retorna JSON
}
```

### ¿Qué diferencia hay?

**Respuesta directa (30 segundos):**
"MVC retorna HTML para que el navegador lo renderice. REST retorna JSON para que aplicaciones cliente lo procesen. MVC es para web tradicional, REST es para APIs."

---

## BLOQUE 6: Flujo Completo de una Compra

### El flujo general es:

```
Usuario en navegador
    ↓
Selecciona productos (cantidad)
    ↓
POST /add_to_cart [productId=1, quantity=2]
    ↓
SalesController recibe
    ↓
SalesService valida (existe?, stock?)
    ↓
Si válido → Agrega a carrito (sesión)
Si inválido → Retorna error (Model)
    ↓
Thymeleaf renderiza con mensaje
    ↓
Usuario ve carrito actualizado
```

### Cómo explicarlo en defensa:

**Profesor:** "Cuéntame todo el flujo desde que el usuario agrega un producto."

**Tú:** "El usuario selecciona un producto y cantidad en el navegador. Hace clic en agregar, que envía una petición POST al servidor con el ID del producto y la cantidad. 

Mi controlador recibe esta petición y llama al servicio. El servicio valida dos cosas: que el producto exista en la base de datos y que haya stock suficiente.

Si todo es válido, agrega el producto a una lista de carrito que se almacena en la sesión del usuario. Si falla la validación, retorna un mensaje de error.

El controlador pasa el mensaje (éxito o error) al Model, que Thymeleaf usa para renderizar la vista. El usuario ve el carrito actualizado con el mensaje correspondiente."

---

## 🎯 RESUMEN: Cómo responder en defensa final

| Pregunta | Respuesta Estrategia |
|----------|-------------------|
| "¿Qué hace este método?" | 1. Propósito. 2. Entradas. 3. Validaciones/Lógica. 4. Salida |
| "¿Por qué lo hiciste así?" | Explica la decisión de diseño (MVC, validación en service, etc) |
| "¿Qué pasa si...?" | Sigue el flujo: qué sucede en cada capa |
| "¿Cómo integra JPA/Security/etc?" | Muestra dónde se usa en tu código específico |

---

## 📝 Recuerda:

✓ No expliques línea por línea
✓ Explica el PROPÓSITO del bloque
✓ Explica la LÓGICA general
✓ Da EJEMPLOS concretos
✓ Conecta con TEMAS del silabo (MVC, validación, sesiones, JPA, etc)

