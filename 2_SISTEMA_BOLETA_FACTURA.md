# 2. SISTEMA DE BOLETA (FACTURA/RECIBO)

## 🧾 Cómo se genera y se muestra la boleta/factura

---

## ¿QUÉ ES UNA BOLETA?

Una boleta (o factura) es un **recibo de compra** que se entrega al cliente después de hacer una venta. Incluye:
- Datos del negocio (Clínica Veterinaria PATITAS)
- Nombre del cliente
- Lista de productos/servicios
- Cantidad, precio unitario y total de cada item
- Subtotal, impuesto (IGV) y total
- Fecha y hora
- Número de boleta

En tu proyecto: **Se genera en PDF** que el cliente puede descargar

---

## FLUJO COMPLETO DE UNA BOLETA

```
USUARIO EN NAVEGADOR
├─ Selecciona productos (Vitamina, Comida, etc)
├─ Los agrega al carrito
├─ Hace clic en "Confirmar Venta"
└─ Se genera la boleta automáticamente

    ↓

CONTROLLER (SalesController.java):
@PostMapping("/finalize_sale")
public String finalizeSale(@ModelAttribute("cart") List<Product> cart, Model model) {
    
    // 1. GUARDA EN BD
    List<Product> finalCart = salesService.finalizeSale(cart);
    
    // 2. CALCULA TOTALES
    salesService.calculateTotals(finalCart, model);
    
    // 3. PASA DATOS A THYMELEAF
    model.addAttribute("finalCart", finalCart);
    
    return "confirmacion_envio";  // HTML que muestra boleta
}

    ↓

SERVICE (SalesService.java):
public List<Product> finalizeSale(List<Product> cart) {
    
    // PASO 1: Descuenta stock
    for (Product cartItem : cart) {
        Optional<Product> productOpt = productRepository.findById(cartItem.getId());
        
        productOpt.ifPresent(dbProduct -> {
            int newStock = dbProduct.getStock() - cartItem.getQuantity();
            dbProduct.setStock(newStock);
            productRepository.save(dbProduct);  // ← Guarda cambios
        });
    }
    
    // PASO 2: Calcula totales
    double subtotal = cart.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    double tax = subtotal * IGV_RATE;  // 18%
    double total = subtotal + tax;
    
    // PASO 3: Registra venta en BD
    Sale newSale = new Sale(total, LocalDateTime.now());
    saleRepository.save(newSale);  // ← Guarda en tabla Sales
    
    return cart;
}

    ↓

REPOSITORY (SaleRepository.java):
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // SQL generado automáticamente:
    // INSERT INTO sales (total, created_at) VALUES (123.45, '2025-12-10 14:30:00')
}

    ↓

BD (MySQL):
INSERT INTO sales (total, created_at) VALUES (123.45, '2025-12-10 14:30:00');
Result: Se guarda la venta con ID único

    ↓

CONTROLLER RECIBE Y PASA A VISTA:
model.addAttribute("finalCart", finalCart);
model.addAttribute("subtotal", 104.62);
model.addAttribute("tax", 18.83);
model.addAttribute("total", 123.45);
return "confirmacion_envio";

    ↓

THYMELEAF (confirmacion_envio.html) RENDERIZA:
- Muestra tabla con productos
- Muestra subtotal, IGV, total
- Incluye botón "Descargar Boleta"

    ↓

USUARIO EN NAVEGADOR VE:
┌─────────────────────────────────┐
│ Clínica Veterinaria PATITAS     │
│ Boleta de Venta N° 0001-2345    │
├─────────────────────────────────┤
│ Producto          Cant  Total   │
│ Vitamina            2   S/. 50  │
│ Comida Perro        1   S/. 30  │
├─────────────────────────────────┤
│ Subtotal:              S/. 80   │
│ IGV (18%):             S/. 14.4 │
│ TOTAL:                 S/. 94.4 │
└─────────────────────────────────┘
[Descargar Boleta PDF]

    ↓

JAVASCRIPT (simulador_ventas.html):
// Cuando el usuario hace clic en "Descargar Boleta":
function generateInvoicePDF() {
    
    // 1. OBTIENE DATOS DEL CARRITO
    const finalCart = [
        { name: "Vitamina", price: 25, quantity: 2 },
        { name: "Comida Perro", price: 30, quantity: 1 }
    ];
    
    // 2. OBTIENE TOTALES DEL HTML
    const subtotal = 80;
    const tax = 14.4;
    const total = 94.4;
    
    // 3. CREA DOCUMENTO PDF CON jsPDF
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    let y = 15;
    
    // Encabezado
    doc.setFontSize(18);
    doc.text("Clínica Veterinaria PATITAS", pageWidth / 2, y, { align: "center" });
    y += 7;
    
    doc.setFontSize(10);
    doc.text("RUC: 20512345678 | Villa El Salvador, Lima", pageWidth / 2, y, { align: "center" });
    y += 10;
    
    // Nombre cliente
    doc.text(`Factura de: ${username}`, pageWidth / 2, y, { align: "center" });
    y += 8;
    
    // Número boleta (aleatorio)
    doc.text("BOLETA DE VENTA N° 0001-" + Math.floor(Math.random() * 9000), 15, y);
    y += 5;
    
    // Fecha
    const now = new Date();
    doc.text(`Fecha: ${now.toLocaleDateString('es-ES')}`, 15, y);
    y += 8;
    
    // Tabla de productos
    finalCart.forEach(item => {
        const lineTotal = item.price * item.quantity;
        doc.text(String(item.quantity), 17, y + 5);
        doc.text(item.name, 40, y + 5);
        doc.text(item.price.toFixed(2), 135, y + 5);
        doc.text(lineTotal.toFixed(2), pageWidth - 17, y + 5, { align: "right" });
        y += 7;
    });
    
    // Totales
    y += 5;
    doc.text("Subtotal:", pageWidth - 50, y);
    doc.text(`S/. ${subtotal.toFixed(2)}`, pageWidth - 17, y, { align: "right" });
    y += 5;
    doc.text(`IGV (18%):`, pageWidth - 50, y);
    doc.text(`S/. ${tax.toFixed(2)}`, pageWidth - 17, y, { align: "right" });
    y += 7;
    doc.text("TOTAL:", pageWidth - 50, y);
    doc.text(`S/. ${total.toFixed(2)}`, pageWidth - 17, y, { align: "right" });
    
    // 4. DESCARGA PDF
    doc.save(`Boleta_PatitasVet_${now.getTime()}.pdf`);
}

    ↓

NAVEGADOR DESCARGA PDF:
Archivo: Boleta_PatitasVet_1702250400000.pdf
```

---

## COMPONENTES CLAVE

### 1️⃣ MODELO: Sale.java

```java
@Entity
@Table(name = "sales")
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double total;  // Monto total con IGV
    
    private LocalDateTime createdAt;  // Fecha y hora
    
    // Constructor
    public Sale(Double total, LocalDateTime createdAt) {
        this.total = total;
        this.createdAt = createdAt;
    }
}
```

### 2️⃣ SERVICIO: SalesService.java

```java
@Service
public class SalesService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SaleRepository saleRepository;
    
    private final double IGV_RATE = 0.18;  // 18% de impuesto
    
    // Calcula subtotal, IGV y total
    public void calculateTotals(List<Product> cart, Model model) {
        double subtotal = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double tax = subtotal * IGV_RATE;
        double total = subtotal + tax;
        
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
    }
    
    // Finaliza la venta
    public List<Product> finalizeSale(List<Product> cart) {
        
        // PASO 1: Descuenta stock de cada producto
        for (Product cartItem : cart) {
            Optional<Product> productOpt = productRepository.findById(cartItem.getId());
            
            productOpt.ifPresent(dbProduct -> {
                int newStock = dbProduct.getStock() - cartItem.getQuantity();
                dbProduct.setStock(newStock);
                productRepository.save(dbProduct);  // Actualiza en BD
            });
        }
        
        // PASO 2: Calcula totales
        double subtotal = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double tax = subtotal * IGV_RATE;
        double total = subtotal + tax;
        
        // PASO 3: Registra la venta en la BD
        Sale newSale = new Sale(total, LocalDateTime.now());
        saleRepository.save(newSale);  // INSERT en tabla sales
        
        return cart;
    }
}
```

### 3️⃣ CONTROLADOR: SalesController.java

```java
@Controller
@SessionAttributes({"cart", "products"})
public class SalesController {
    
    @Autowired
    private SalesService salesService;
    
    @PostMapping("/finalize_sale")
    public String finalizeSale(
            @ModelAttribute("cart") List<Product> cart,
            Model model) {
        
        // Finaliza la venta (guarda en BD, descuenta stock)
        List<Product> finalCart = salesService.finalizeSale(cart);
        
        // Calcula totales
        salesService.calculateTotals(finalCart, model);
        
        // Pasa carrito final a la vista
        model.addAttribute("finalCart", finalCart);
        
        // Limpia el carrito de la sesión
        cart.clear();
        
        // Muestra página de confirmación
        return "confirmacion_envio";
    }
}
```

### 4️⃣ REPOSITORIO: SaleRepository.java

```java
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // JPA genera automáticamente SQL para:
    // - save() → INSERT
    // - findAll() → SELECT *
    // - delete() → DELETE
    // etc
}
```

### 5️⃣ BASE DE DATOS: Tabla Sales

```sql
CREATE TABLE sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ejemplo de registro:
INSERT INTO sales (total, created_at) VALUES (94.40, '2025-12-10 14:30:00');
```

---

## GENERACIÓN DE PDF CON jsPDF

### ¿Por qué se usa JavaScript?

En el **BACKEND** (Java/Spring):
- Se calcula totales y se guarda en BD
- Se renderiza la página HTML

En el **FRONTEND** (JavaScript/jsPDF):
- Se genera el PDF directamente en el navegador
- No consume recursos del servidor
- El usuario descarga instantáneamente

### Estructura del PDF

```
┌─────────────────────────────────────────────┐
│ Clínica Veterinaria PATITAS                 │  ← Encabezado (setFontSize(18))
│ RUC: 20512345678 | Villa El Salvador, Lima  │  ← Subtítulo
├─────────────────────────────────────────────┤
│ Factura de: Juan Pérez                      │  ← Nombre cliente
│ BOLETA DE VENTA N° 0001-5234                │  ← Número único
│ Fecha: 10/12/2025  Hora: 14:30:00           │  ← Fecha/Hora
├─────────────────────────────────────────────┤
│ Cant. Descripción        P. Unit.    Total  │  ← Encabezado tabla
│   2  Vitamina para Perro   S/. 25.00 S/. 50 │
│   1  Comida Premium        S/. 30.00 S/. 30 │
├─────────────────────────────────────────────┤
│ Subtotal:                          S/. 80.00│
│ IGV (18%):                        S/. 14.40 │
│ TOTAL:                            S/. 94.40 │
├─────────────────────────────────────────────┤
│ ¡Gracias por su compra!                     │  ← Pie de página
└─────────────────────────────────────────────┘
```

### Código de generación clave

```javascript
// 1. Obtiene datos
const finalCart = document.getElementById('final-cart-data');
const subtotal = parseCurrency(document.getElementById('subtotal').textContent);
const tax = parseCurrency(document.getElementById('tax').textContent);

// 2. Crea documento PDF
const doc = new jsPDF();
let y = 15;

// 3. Agrega contenido
doc.setFontSize(18);
doc.text("Clínica Veterinaria PATITAS", pageWidth / 2, y, { align: "center" });

// 4. Itera sobre productos
finalCart.forEach(item => {
    doc.text(item.name, 40, y + 5);
    doc.text(item.price.toFixed(2), 135, y + 5);
    y += 7;
});

// 5. Descarga
doc.save(`Boleta_${now.getTime()}.pdf`);
```

---

## VALIDACIONES EN BOLETA

```javascript
function generateInvoicePDF() {
    
    // ✓ Validación 1: ¿Existen datos del carrito?
    const finalCartElement = document.getElementById('final-cart-data');
    if (!finalCartElement) {
        console.error('No se encontraron datos de la venta');
        return;
    }
    
    // ✓ Validación 2: ¿Está cargada la librería jsPDF?
    if (!window.jspdf) {
        console.error('jsPDF no está cargado');
        return;
    }
    
    // ✓ Validación 3: ¿Se puede leer el JSON del carrito?
    try {
        finalCart = JSON.parse(finalCartElement.textContent);
    } catch (e) {
        console.warn('No se pudo parsear JSON, intentando reconstruir...');
        // Reconstruye desde atributos data-* del HTML
    }
    
    // ✓ Validación 4: ¿Hay al menos un item?
    if (!finalCart || finalCart.length === 0) {
        console.error('Carrito vacío');
        return;
    }
}
```

---

## FLUJO DE DATOS: ANTES Y DESPUÉS

### ANTES (En el carrito)
```html
<div id="cart-items">
    <ul>
        <li data-name="Vitamina" data-price="25" data-quantity="2"></li>
        <li data-name="Comida" data-price="30" data-quantity="1"></li>
    </ul>
</div>

Carrito (session) = [
    {id: 1, name: "Vitamina", price: 25, quantity: 2},
    {id: 2, name: "Comida", price: 30, quantity: 1}
]
```

### DESPUÉS (BD y PDF)

```
BD (Tabla Sales):
┌────┬───────┬─────────────────────┐
│ id │ total │ created_at          │
├────┼───────┼─────────────────────┤
│ 1  │ 94.40 │ 2025-12-10 14:30:00 │
│ 2  │ 150.50│ 2025-12-10 15:45:30 │
└────┴───────┴─────────────────────┘

PDF Descargado: Boleta_PatitasVet_1702250400000.pdf
├─ Encabezado: Clínica Veterinaria PATITAS
├─ Cliente: Juan Pérez
├─ Productos: Vitamina, Comida
├─ Subtotal: S/. 80.00
├─ IGV: S/. 14.40
└─ Total: S/. 94.40
```

---

## RESPUESTAS PARA DEFENSA

### Pregunta: "¿Cómo funciona el sistema de boleta?"

**Respuesta:**
"Cuando el usuario confirma la venta, el controlador llama al servicio. El servicio descuenta el stock de cada producto en la BD, calcula subtotal e IGV, y registra la venta en la tabla Sales. Luego, Thymeleaf renderiza la página con los datos. El usuario ve un botón 'Descargar Boleta' que ejecuta JavaScript. Este JavaScript toma los datos del HTML, usa la librería jsPDF para generar un documento PDF con el formato de boleta, y el navegador lo descarga."

### Pregunta: "¿Por qué se genera el PDF en JavaScript y no en Java?"

**Respuesta:**
"Generarla en JavaScript es más eficiente porque se ejecuta en el navegador del usuario, no en el servidor. El servidor ya guardó los datos en la BD. Usar JavaScript con jsPDF es rápido, instantáneo para el usuario, y no consume recursos del servidor. Si fuera en Java, necesitaríamos una librería adicional como iText o Apache PDFBox, más configuración, y sería más lento."

### Pregunta: "¿Qué datos se guardan en la BD y cuáles en el HTML?"

**Respuesta:**
"En la BD se guarda la información persistente: el total de la venta, la fecha/hora, y el ID de la venta. En el HTML se pasan los detalles temporales: lista de productos, cantidades, precios, subtotal, IGV. El HTML es transitorio (se renderiza y se olvida), la BD es permanente (histórico de ventas)."

### Pregunta: "¿Cómo se valida que la boleta sea correcta?"

**Respuesta:**
"Hay varias validaciones. Primero, en el servicio: verificamos que haya stock suficiente antes de descuenta. Al generar el PDF, verificamos que existan datos del carrito, que esté cargada la librería jsPDF, y que haya al menos un producto. Si algo falla, mostramos un error en la consola. El PDF muestra el cálculo de subtotal + IGV = total, que se puede verificar manualmente."

---

## TABLA RESUMEN

| Componente | Función | Tecnología |
|-----------|---------|-----------|
| **SalesController** | Recibe petición POST | Java/Spring |
| **SalesService** | Descuenta stock, calcula totales, registra venta | Java/Spring |
| **SaleRepository** | Guarda venta en tabla Sales | JPA/Hibernate |
| **Tabla Sales** | Almacena histórico de ventas | MySQL |
| **Thymeleaf** | Renderiza HTML con datos | Templates |
| **JavaScript (jsPDF)** | Genera PDF cliente-side | jQuery/jsPDF |

---

## RESUMEN VISUAL

```
Carrito (Sesión)
    ↓
Confirmar Venta (POST)
    ↓
SalesController recibe
    ↓
SalesService procesa:
    ├─ Descuenta stock
    ├─ Calcula totales
    └─ Guarda en Sales
    ↓
ProductRepository + SaleRepository
    ↓
BD (MySQL)
    ├─ UPDATE products (stock)
    └─ INSERT sales (total, fecha)
    ↓
Thymeleaf renderiza confirmacion_envio.html
    ↓
Usuario ve: Boleta + Botón "Descargar"
    ↓
JavaScript genera PDF con jsPDF
    ↓
Navegador descarga: Boleta_PatitasVet_[timestamp].pdf
    ↓
Usuario recibe comprobante
```

