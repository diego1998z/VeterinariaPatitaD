package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.service.SalesService;
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
@SessionAttributes({"cart", "products"}) // Mantiene carrito y productos en sesión
public class SalesController {

    private final SalesService salesService;
    private static final Logger log = LoggerFactory.getLogger(SalesController.class);

    // Inyección del servicio
    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    /**
     * lista productos
     */
    @ModelAttribute("products")
    public List<Product> initializeProducts() {
        return salesService.getInitialProducts();
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
    public String simuladorVentas(@ModelAttribute("cart") List<Product> cart, Model model) {
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
//verifica que el carrito no este vacio, sino muestra un error y manda ese dato a la vista
        log.info("/finalize_sale called; cart size before finalize: {}", cart == null ? 0 : cart.size());

        if (cart == null || cart.isEmpty()) {
            log.warn("finalize_sale: carrito vacío, redirigiendo");
            model.addAttribute("message", "Error: El carrito está vacío.");//manda el dato a la vista
            return "redirect:/simulador_ventas";
        }

        // descuenta del stock del producto y obtiene el carrito final
        
    List<Product> finalCart = salesService.finalizeSale(cart);
    log.info("finalize_sale: finalCart size after finalizeSale: {}", finalCart == null ? 0 : finalCart.size());

        // Calcular totales basados en el carrito finalizado (y pasarlos como flash attributes)
        double subtotal = 0.0;
        if (finalCart != null) {
            subtotal = finalCart.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
        double tax = subtotal * 0.18; // IGV 18%
        double total = subtotal + tax;

        
        // tiene que pasar el redirect, así que los añadimos como flash attributes más abajo.
        salesService.calculateTotals(finalCart, model);

    // Crear una representación segura (DTO) del carrito para pasar al template 
    // una lista segura con los datos necesarios para pasar a la vista
    List<java.util.Map<String, Object>> finalCartSafe = new java.util.ArrayList<>();
    if (finalCart != null) {
        for (Product p : finalCart) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("price", p.getPrice());
            map.put("quantity", p.getQuantity());
            finalCartSafe.add(map);
        }
    }

    // aflash attibuter para la vista, la copia segura del carrito
    redirectAttributes.addFlashAttribute("finalCart", finalCartSafe);
    
    redirectAttributes.addFlashAttribute("subtotal", subtotal);
    redirectAttributes.addFlashAttribute("tax", tax);
    redirectAttributes.addFlashAttribute("total", total);
    // log para verificar
    log.info("finalize_sale: added finalCart (safe) to flash attributes, redirecting to /simulador_ventas");

        // Limpiar carrito en sesión completando el estado de sesión
        status.setComplete();

        // Mensaje que verá el usuario en la página redirigida
        redirectAttributes.addFlashAttribute("message", "¡Venta confirmada!.");

        // Redirigimos a la vista del simulador; el template procesará 'finalCart' y generará el PDF
        return "redirect:/simulador_ventas";
    }
}