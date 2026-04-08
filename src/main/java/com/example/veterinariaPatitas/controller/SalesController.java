package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.dto.InvoiceDownloadRequest;
import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.service.InvoicePdfService;
import com.example.veterinariaPatitas.service.SalesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;//para manejar estado de sesion
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes({"cart", "products"}) // Mantiene carrito y catalogo durante la sesion del usuario.
public class SalesController {

    private final SalesService salesService;
    private final InvoicePdfService invoicePdfService;
    private static final Logger log = LoggerFactory.getLogger(SalesController.class);

    // Inyección del servicio
    public SalesController(SalesService salesService, InvoicePdfService invoicePdfService) {
        this.salesService = salesService;
        this.invoicePdfService = invoicePdfService;
    }

    /**
     * lista productos
     */
    @ModelAttribute("products")
    public List<Product> initializeProducts() {
        List<Product> products = salesService.getInitialProducts();
        salesService.prepareProductsForDisplay(products);
        return products;
    }

    /**
     * incia con el carrito vacio
     */
    @ModelAttribute("cart")
    public List<Product> initializeCart() {
        return new ArrayList<>();
    }

    /**
     * carga la pagina princiapl de ventas
     */
    @GetMapping("/simulador_ventas")
    public String simuladorVentas(@ModelAttribute("products") List<Product> products,
                                  @ModelAttribute("cart") List<Product> cart,
                                  Model model) {
        salesService.prepareProductsForDisplay(products);
        salesService.prepareCartForDisplay(cart);
        // calcila los totales si no estan calculados 
        if (!model.containsAttribute("subtotal")) {
            salesService.calculateTotals(cart, model);
        }
        return "simulador_ventas";
    }

    /**
     * añade al carrito
     */
    @PostMapping("/add_to_cart")
    public String addToCart(@RequestParam int productId, @RequestParam int quantity,
                            @ModelAttribute("products") List<Product> products,
                            @ModelAttribute("cart") List<Product> cart,
                            Model model) {

        // se agrega el producto teniendo en cuenta el stock por los datos del productp
        String message = salesService.addProductToCart(productId, quantity, cart); 
        model.addAttribute("message", message);

        return "redirect:/simulador_ventas";
    }

    /**
     * elimina un producto
     */
    @PostMapping("/remove_from_cart")
    public String removeFromCart(@RequestParam int productId,
                                 @ModelAttribute("cart") List<Product> cart,
                                 Model model) {

        cart.removeIf(item -> item.getId() == productId);//verifica si el producto existe y lo elimina sino muestra error
        model.addAttribute("message", "Producto eliminado del carrito.");
        return "redirect:/simulador_ventas";
    }

    /**
     * vaciar
     */
    @PostMapping("/clear_cart")
    public String clearCart(@ModelAttribute("cart") List<Product> cart, Model model) {
        cart.clear();//se obtiene la lista del carrito y se limpia
        model.addAttribute("message", "El carrito ha sido vaciado.");
        return "redirect:/simulador_ventas";
    }

    /**
     * finalizada venta
     */
    @PostMapping("/finalize_sale")
    public String finalizeSale(@ModelAttribute("products") List<Product> products,
                               @ModelAttribute("cart") List<Product> cart,
                               SessionStatus status, Model model,
                               RedirectAttributes redirectAttributes) {
        // Evita finalizar ventas vacias.
        log.info("/finalize_sale called; cart size before finalize: {}", cart == null ? 0 : cart.size());

        if (cart == null || cart.isEmpty()) {
            log.warn("finalize_sale: carrito vacío, redirigiendo");
            model.addAttribute("message", "Error: El carrito está vacío.");//manda el dato a la vista
            return "redirect:/simulador_ventas";
        }

        salesService.prepareProductsForDisplay(products);
        salesService.prepareCartForDisplay(cart);

        // Finaliza logica de venta (stock + registro de venta) y conserva copia para boleta.
        List<Product> finalCart = salesService.finalizeSale(cart);
        log.info("finalize_sale: finalCart size after finalizeSale: {}", finalCart == null ? 0 : finalCart.size());

        // Calcular totales basados en el carrito finalizado (y pasarlos como flash attributes)
        double subtotal = salesService.calculateSubtotal(finalCart);
        double tax = subtotal * 0.18; // IGV 18%
        double total = subtotal + tax;

        
        // tiene que pasar el redirect, así que los añadimos como flash attributes más abajo.
        salesService.calculateTotals(finalCart, model);

        // Serializa solo campos necesarios para generar PDF en frontend/backend.
        List<java.util.Map<String, Object>> finalCartSafe = new java.util.ArrayList<>();
        if (finalCart != null) {
            for (Product p : finalCart) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                map.put("price", salesService.getUnitPricePen(p));
                map.put("quantity", p.getQuantity());
                finalCartSafe.add(map);
            }
        }

        // Pasa datos al redirect para pintar resumen y habilitar descarga de boleta.
        redirectAttributes.addFlashAttribute("finalCart", finalCartSafe);
        redirectAttributes.addFlashAttribute("subtotal", subtotal);
        redirectAttributes.addFlashAttribute("tax", tax);
        redirectAttributes.addFlashAttribute("total", total);
        log.info("finalize_sale: added finalCart (safe) to flash attributes, redirecting to /simulador_ventas");

        // Limpiar carrito en sesión completando el estado de sesión
        status.setComplete();

        // Mensaje que verá el usuario en la página redirigida
        redirectAttributes.addFlashAttribute("message", "¡Venta confirmada!.");

        // Redirigimos a la vista del simulador; el template procesará 'finalCart' y generará el PDF
        return "redirect:/simulador_ventas";
    }

    @PostMapping(value = "/invoice/download", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadInvoice(@RequestBody InvoiceDownloadRequest request,
                                                  HttpSession session) {
        // Rechaza solicitud sin detalle de items.
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String username = request.getUsername();
        // Si frontend no envia username, usa el de la sesion autenticada.
        Object usernameFromSession = session.getAttribute("usuarioLogeado");
        if ((username == null || username.isBlank()) && usernameFromSession != null) {
            username = usernameFromSession.toString();
        }

        // Genera boleta en backend (OpenPDF) y responde como archivo descargable.
        byte[] pdfData = invoicePdfService.generateInvoicePdf(request, username);
        String fileName = "Boleta_PatitasVet_" + System.currentTimeMillis() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(pdfData);
    }
}
