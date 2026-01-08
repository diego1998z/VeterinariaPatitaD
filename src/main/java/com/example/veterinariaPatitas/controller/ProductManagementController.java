package com.example.veterinariaPatitas.controller;

import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.service.SalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/management/products")
public class ProductManagementController {

    private final SalesService salesService;

    public ProductManagementController(SalesService salesService) {
        this.salesService = salesService;
    }

    /**
     * Endpoint para crear un nuevo producto. Solo TRABAJADOR.
     * Requiere un cuerpo JSON con name, price, stock, unit.
     */
    @PreAuthorize("hasAuthority('TRABAJADOR')")
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = salesService.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    /**
     * Endpoint para aumentar el stock de un producto existente. Solo TRABAJADOR.
     */
    @PreAuthorize("hasAuthority('TRABAJADOR')")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, 
                                         @RequestBody java.util.Map<String, Integer> update) {
        
        Integer addedStock = update.get("addedStock");

        if (addedStock == null || addedStock <= 0) {
            return ResponseEntity.badRequest().body("El campo 'addedStock' debe ser un número positivo.");
        }

        Optional<Product> updatedProduct = salesService.addStock(id, addedStock);

        return updatedProduct.map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Endpoint para modificar completamente un producto (ej: precio o nombre). Solo TRABAJADOR.
     * 
     */
    @PreAuthorize("hasAuthority('TRABAJADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody Product productDetails) {
        
        
        return salesService.getProductById(id)
            .map(existingProduct -> {
                existingProduct.setName(productDetails.getName());
                existingProduct.setPrice(productDetails.getPrice());
                existingProduct.setStock(productDetails.getStock()); 
                existingProduct.setUnit(productDetails.getUnit());
                Product updated = salesService.saveProduct(existingProduct);
                return ResponseEntity.ok(updated);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para eliminar un producto. Solo TRABAJADOR.
     */
    @PreAuthorize("hasAuthority('TRABAJADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        salesService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}