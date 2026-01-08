# EJEMPLOS PRÁCTICOS - TRAZA PASO A PASO

Este documento te muestra EXACTAMENTE qué sucede en cada línea de código.

---

## EJEMPLO 1: PREGUNTA "¿Cómo se valida?"

### Escenario: Usuario intenta agregar producto al carrito

**Usuario en navegador hace esto:**
```
- Abre simulador_ventas.html
- Ve producto "Vitamina para perros" - Stock: 5
- Intenta agregar cantidad: -1
- Presiona "Agregar al carrito"
```

### Flujo completo - Paso a paso:

```
PASO 1: Navegador envía POST /add_to_cart
        POST /add_to_cart?productId=1&quantity=-1

PASO 2: SalesController recibe
        @PostMapping("/add_to_cart")
        public String addToCart(@RequestParam int productId,
                                @RequestParam int quantity,
                                ...) {
            String message = salesService.addProductToCart(1, -1, cart);
            //                                               ↑    ↑
            //                                           productId=-1
        }

PASO 3: SalesService.addProductToCart() inicia
        public String addProductToCart(int productId, int quantity, List<Product> cart) {
            // quantity = -1

PASO 4: VALIDACIÓN 1 - ¿El producto existe?
        Optional<Product> productOpt = productRepository.findById(1);
        //
        // Base de datos responde:
        // SELECT * FROM products WHERE id = 1
        // Resultado: Vitamin para perros, Price: 150.00, Stock: 5
        //
        // productOpt.isEmpty() = false (SÍ existe)
        //
        if (productOpt.isEmpty()) {
            return "Error: Producto no encontrado.";  // ← NO se ejecuta
        }

PASO 5: VALIDACIÓN 2 - ¿La cantidad es válida?
        Product product = productOpt.get();  // product = Vitamina (Stock: 5)
        
        if (quantity <= 0 || quantity > product.getStock()) {
            //  ↑ quantity = -1, esto es <= 0 ✓ CONDICIÓN VERDADERA
            return "Error: Cantidad inválida o stock insuficiente.";  // ← SE EJECUTA
        }

PASO 6: Retorna al Controlador
        // No continúa validaciones porque ya retornó
        // message = "Error: Cantidad inválida o stock insuficiente."

PASO 7: SalesController recibe mensaje
        String message = salesService.addProductToCart(1, -1, cart);
        model.addAttribute("message", message);  // ← "Error: Cantidad inválida..."

PASO 8: Controlador retorna vista
        return "redirect:/simulador_ventas";

PASO 9: Thymeleaf renderiza simulador_ventas.html
        <div th:if="${message != null}" class="alert alert-danger">
            <span th:text="${message}"></span>
        </div>

PASO 10: HTML generado
         <div class="alert alert-danger">
             <span>Error: Cantidad inválida o stock insuficiente.</span>
         </div>

PASO 11: Navegador renderiza
         Usuario ve en ROJO (por alert-danger de Bootstrap):
         "Error: Cantidad inválida o stock insuficiente."
```

### ¿QUÉ PASARÍA SI NO HUBIERA VALIDACIÓN?

```
Sin if (quantity <= 0):

PASO 1: quantity = -1 entra a la lógica
        
PASO 2: Se busca item existente en carrito (no existe)
        
PASO 3: Se crea nuevo item
        Product newItem = new Product(1, "Vitamina", 150.00, 5, "unidad");
        newItem.setQuantity(-1);  // ← CANTIDAD NEGATIVA (PROBLEMA)
        
PASO 4: Se agrega al carrito
        cart.add(newItem);
        
PASO 5: Cuando calcula total:
        subtotal = 150.00 * -1 = -150.00  // ← TOTAL NEGATIVO
        
PASO 6: Usuario paga y le DAN DINERO (bug crítico)
```

---

## EJEMPLO 2: PREGUNTA "¿Cómo registrar y validar?"

### Escenario: Usuario intenta registrarse dos veces

**INTENTO 1:**

```
PASO 1: Usuario llena formulario
        usuario: "juan_perez"
        contraseña: "123456"
        
PASO 2: Envía POST /registro
        POST /registro?usuario=juan_perez&contrasena=123456

PASO 3: AuthController recibe
        @PostMapping("/registro")
        public String procesarRegistro(@RequestParam String usuario,
                                       @RequestParam String contrasena,
                                       Model model) {
            // usuario = "juan_perez"
            // contrasena = "123456"

PASO 4: VALIDACIÓN 1 - ¿El usuario ya existe?
        if (usuarioRepository.findByUsuario("juan_perez").isPresent()) {
            //
            // BD busca: SELECT * FROM usuarios WHERE usuario = 'juan_perez'
            // Resultado: No existe
            //
            // .isPresent() = false (NO existe)
            //
            // Por lo tanto: if(false) no se ejecuta
        }

PASO 5: CREA nuevo usuario
        Usuario nuevo = new Usuario("juan_perez", "123456");
        usuarioRepository.save(nuevo);
        //
        // BD ejecuta: 
        // INSERT INTO usuarios (usuario, contrasena) 
        // VALUES ('juan_perez', '123456')
        //
        // Resultado: Guardado exitosamente con ID = 1

PASO 6: Prepara mensaje de éxito
        model.addAttribute("mensaje", "Usuario creado correctamente, ahora puedes iniciar sesión");

PASO 7: Retorna a login
        return "login";

PASO 8: Usuario ve página login con mensaje verde (alert-success)
```

**INTENTO 2 (mismo usuario):**

```
PASO 1: Usuario (o alguien) intenta registrar NUEVAMENTE
        usuario: "juan_perez"
        contraseña: "654321" (otra contraseña)

PASO 2: POST /registro
        POST /registro?usuario=juan_perez&contrasena=654321

PASO 3: AuthController recibe

PASO 4: VALIDACIÓN 1 - ¿El usuario ya existe?
        if (usuarioRepository.findByUsuario("juan_perez").isPresent()) {
            //
            // BD busca: SELECT * FROM usuarios WHERE usuario = 'juan_perez'
            // Resultado: SÍ existe (lo creamos en el INTENTO 1)
            //
            // .isPresent() = true (SÍ existe)
            //
            // Por lo tanto: if(true) → SE EJECUTA
            //
            model.addAttribute("error", "El usuario ya existe");
            return "registro";  // ← Retorna a la página de registro
        }

PASO 5: No continúa porque ya retornó

PASO 6: Usuario ve página registro con mensaje rojo (alert-danger)
        "El usuario ya existe"
```

### ¿QUÉ SUCEDE EN LA BASE DE DATOS?

**Estado de BD después del INTENTO 1:**
```
Tabla: usuarios
┌────┬──────────────┬────────────┐
│ id │   usuario    │ contraseña │
├────┼──────────────┼────────────┤
│ 1  │ juan_perez   │ 123456     │
└────┴──────────────┴────────────┘
```

**Cuando llega INTENTO 2:**
```
Query: SELECT * FROM usuarios WHERE usuario = 'juan_perez'
Resultado: 1 fila (la que guardamos antes)
Optional<Usuario>: PRESENT (contiene los datos)
isPresent(): true
Decisión: NO guardar, retornar error
```

---

## EJEMPLO 3: PREGUNTA "¿De dónde vienen los mensajes?"

### Escenario: Usuario hace login con contraseña incorrecta

```
PASO 1: Usuario ingresa en navegador
        usuario: "juan_perez"
        contraseña: "INCORRECTA"

PASO 2: Envía POST /login

PASO 3: AuthController.procesarLogin() recibe
        @PostMapping("/login")
        public String procesarLogin(@RequestParam String usuario,
                                    @RequestParam String contrasena,
                                    Model model) {
            //
            // En este punto:
            // usuario = "juan_perez"
            // contrasena = "INCORRECTA"
            // model = objeto VACÍO (sin atributos)

PASO 4: Busca usuario en BD
        return usuarioRepository.findByUsuario("juan_perez")
            //
            // BD: SELECT * FROM usuarios WHERE usuario = 'juan_perez'
            // Resultado: Encuentra a Juan con contraseña '123456'
            // Optional: PRESENT

PASO 5: Verifica contraseña
            .filter(u -> u.getContrasena().equals("INCORRECTA"))
            //
            // u.getContrasena() = "123456"
            // "123456".equals("INCORRECTA") = false
            // .filter() retorna Optional VACÍO

PASO 6: Como el filter retorna vacío, entra a orElseGet()
            .orElseGet(() -> {
                model.addAttribute("error", "Usuario o contraseña incorrectos");
                //                 ↑ Aquí nace el mensaje
                //                 ↑ Se agrega al objeto Model
                return "login";
            });

PASO 7: El objeto Model ahora contiene
        Model {
            error: "Usuario o contraseña incorrectos"
        }

PASO 8: Spring pasa el Model a Thymeleaf
        (Automáticamente, no hay código explícito)

PASO 9: Thymeleaf procesa login.html
        Busca todos los atributos del Model
        Encuentra: error = "Usuario o contraseña incorrectos"

PASO 10: En el HTML
         <div th:if="${error != null}">
             ↑ error NO es null, entonces SI se incluye
             
             <div class="alert alert-danger">
                 ↑ Bootstrap rojo
                 
                 <span th:text="${error}"></span>
                 ↑ Thymeleaf pone el valor aquí
             </div>
         </div>

PASO 11: Thymeleaf genera HTML final
         <div>
             <div class="alert alert-danger">
                 <span>Usuario o contraseña incorrectos</span>
             </div>
         </div>

PASO 12: Navegador renderiza
         ┌─────────────────────────────────────┐
         │ [x] Usuario o contraseña incorrectos│
         │                                     │
         │ Escrito en ROJO por Bootstrap       │
         └─────────────────────────────────────┘
```

### ¿CÓMO VIAJA EL DATO?

```
┌─────────────────────────────────────┐
│ AuthController.java                 │
│ model.addAttribute("error", "...")  │
│                                     │
│ AQUÍ NACE EL MENSAJE ← PUNTO 1      │
└────────────────┬────────────────────┘
                 │
                 │ Spring pasa automáticamente
                 │ el objeto Model a la vista
                 ↓
┌────────────────────────────────────────┐
│ Thymeleaf Template Engine             │
│ Procesa: th:if="${error != null}"     │
│          th:text="${error}"           │
│                                        │
│ ACCEDE AL MENSAJE ← PUNTO 2           │
└────────────────┬─────────────────────┘
                 │
                 │ Genera HTML con el valor
                 ↓
┌────────────────────────────────────────┐
│ login.html (HTML final)               │
│ <div>Usuario o contraseña incorrectos</div>│
│                                        │
│ VIAJA EN EL HTML ← PUNTO 3            │
└────────────────┬─────────────────────┘
                 │
                 │ HTTP Response
                 │ Content-Type: text/html
                 ↓
┌────────────────────────────────────────┐
│ Navegador del Usuario                 │
│                                        │
│ Renderiza HTML                        │
│                                        │
│ USUARIO VE EL MENSAJE ← PUNTO 4       │
└────────────────────────────────────────┘
```

### ¿DÓNDE APARECE EL MENSAJE?

```
UBICACIÓN DEPENDE DEL HTML:

OPCIÓN A - En la parte SUPERIOR (más común):
┌──────────────────────────────────┐
│          NAVBAR                  │
├──────────────────────────────────┤
│ [!] Usuario o contraseña        │  ← AQUÍ
│     incorrectos                 │
├──────────────────────────────────┤
│                                 │
│   Formulario de Login           │
│                                 │
│   [Usuario]                     │
│   [Contraseña]                  │
│   [Iniciar Sesión]              │
│                                 │
└──────────────────────────────────┘

HTML que lo genera:
<header>
    <div th:replace="fragments/navbar :: navbar"></div>
</header>

<div class="container mt-3">
    <div th:if="${error != null}" class="alert alert-danger">
        <span th:text="${error}"></span>
    </div>  ← Aquí está
    
    <form method="post" action="/login">
        ...
    </form>
</div>


OPCIÓN B - En la parte LATERAL (derecha):
┌────────────────────┬──────────────────┐
│   Formulario       │ [!] Usuario o    │
│   de Login         │     contraseña   │
│                    │     incorrectos  │ ← AQUÍ
│   [Usuario]        │                  │
│   [Contraseña]     │                  │
│   [Iniciar]        │                  │
└────────────────────┴──────────────────┘

HTML que lo genera:
<div class="container-fluid">
    <div class="row">
        <div class="col-md-8">
            <form method="post" action="/login">
                ...
            </form>
        </div>
        
        <div class="col-md-4">
            <div th:if="${error != null}" class="alert alert-danger">
                <span th:text="${error}"></span>
            </div>  ← Aquí está
        </div>
    </div>
</div>
```

---

## EJEMPLO 4: FLUJO COMPLETO DE VENTA

### Escenario: Usuario compra productos

```
USUARIO INICIA:
Login exitoso → session.setAttribute("usuarioLogeado", "juan_perez")

PASO 1: Accede a /simulador_ventas

PASO 2: SalesController.simuladorVentas() se ejecuta
        @GetMapping("/simulador_ventas")
        public String simuladorVentas(Model model,
                                      @ModelAttribute("cart") List<Product> cart) {
            
            List<Product> productos = salesService.getInitialProducts();
            //
            // BD: SELECT * FROM products
            // Resultado:
            // - Vitamina (id=1, precio=150, stock=5)
            // - Vacuna (id=2, precio=200, stock=3)
            //
            
            model.addAttribute("products", productos);
            //                            ↑ Los productos van a la vista
            
            return "simulador_ventas";
        }

PASO 3: Thymeleaf genera tabla con productos
        <table>
            <tr th:each="product : ${products}">
                <!--
                Primera iteración: product = Vitamina (id=1, precio=150, stock=5)
                Segunda iteración: product = Vacuna (id=2, precio=200, stock=3)
                -->
                <td th:text="${product.getName()}"></td>
                <td th:text="${product.getPrice()}"></td>
                <td>
                    <input type="number" name="quantity" value="1">
                    <button onclick="agregarAlCarrito(${product.getId()})">
                        Agregar al carrito
                    </button>
                </td>
            </tr>
        </table>

PASO 4: Usuario ve tabla y hace clic en "Agregar al carrito"
        Selecciona: Vitamina, cantidad 2
        Presiona botón

PASO 5: JavaScript envía POST /add_to_cart?productId=1&quantity=2

PASO 6: SalesController.addToCart() recibe
        String message = salesService.addProductToCart(1, 2, cart);
        //
        // Service valida:
        // - Producto existe? SÍ (Vitamina id=1)
        // - Cantidad válida? SÍ (2 > 0 y 2 <= 5)
        // - Ya en carrito? NO
        //
        // Crea nuevo item y lo agrega
        // message = "Producto agregado al carrito con éxito."
        
        model.addAttribute("message", message);

PASO 7: Retorna y renderiza
        Usuario ve en VERDE: "Producto agregado al carrito con éxito."
        Usuario ve carrito actualizado:
        ┌─────────────┬────────┬────────┐
        │ Producto    │ Precio │ Cantidad│
        ├─────────────┼────────┼────────┤
        │ Vitamina    │ 150.00 │ 2      │
        └─────────────┴────────┴────────┘

PASO 8: Usuario agrega otro producto
        Selecciona: Vacuna, cantidad 3
        
PASO 9: POST /add_to_cart?productId=2&quantity=3
        Service valida todo OK
        Agrega Vacuna al carrito

PASO 10: Usuario hace clic en "Finalizar Venta"
         POST /finalize_sale

PASO 11: SalesController.finalizarVenta() se ejecuta
         salesService.finalizeSale(cart);
         //
         // Para cada producto en carrito:
         //
         // Producto 1: Vitamina
         // - Busca en BD: SELECT * FROM products WHERE id=1
         // - stock actual = 5
         // - stock nuevo = 5 - 2 = 3
         // - Guarda: UPDATE products SET stock=3 WHERE id=1
         //
         // Producto 2: Vacuna
         // - stock actual = 3
         // - stock nuevo = 3 - 3 = 0
         // - Guarda: UPDATE products SET stock=0 WHERE id=2
         //
         
         // Limpia carrito
         cart.clear();
         
         // Estado BD después:
         // Vitamina: stock = 3 (era 5, vendimos 2)
         // Vacuna: stock = 0 (era 3, vendimos 3, se agotó)

PASO 12: Genera PDF con jsPDF (navegador)
         const doc = new jsPDF();
         doc.text("Recibo de Venta", 10, 10);
         
         // Agrega cada producto
         // Calcula totales
         
         doc.save("recibo.pdf");
         
         Usuario descarga PDF automáticamente

PASO 13: Carrito se vacía, usuario ve mensaje de éxito
         "Venta finalizada exitosamente"
```

---

## RESUMEN DE LOS 4 EJEMPLOS

| Ejemplo | Pregunta | Validación | Resultado |
|---------|----------|-----------|-----------|
| 1 | ¿Cómo se valida? | quantity <= 0 | Error en rojo |
| 2 | ¿Cómo registrar? | usuario ya existe | Error, no guarda |
| 3 | ¿Mensajes? | Model → Thymeleaf → HTML | Error en cualquier posición |
| 4 | Todo junto | Múltiples validaciones | Venta completa con PDF |

---

## CÓMO USAR ESTE DOCUMENTO

1. **Lee uno de los ejemplos**
2. **Entiende cada PASO y qué sucede en BD**
3. **Abre tu IDE y busca el código que corresponde**
4. **Ejecuta mentalmente el flujo 3 veces**
5. **Practica explicándolo en voz alta sin leer**

Cuando domines estos 4 ejemplos, podrás responder CUALQUIER pregunta sobre validación, registro o mensajes.

