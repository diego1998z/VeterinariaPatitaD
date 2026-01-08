/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.model.Sale;
import com.example.veterinariaPatitas.repository.ProductRepository;
import com.example.veterinariaPatitas.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Map; 
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedHashMap; // Importado para mantener el orden de los meses

@Service
public class SalesService {

    private final double IGV_RATE = 0.18;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SaleRepository saleRepository; // Repositorio de Ventas inyectado

    //lista de productos inicial
    public List<Product> getInitialProducts() {
        return productRepository.findAll();
    }

    //calcula totales 
    public void calculateTotals(List<Product> cart, Model model) {
        double subtotal = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())//saca el precio por cantidad
                .sum();
        double tax = subtotal * IGV_RATE;
        double total = subtotal + tax;

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax); // IGV
        model.addAttribute("total", total);
    }

    //añade al carrito, verifica que el producto exista y que haya stock por el id
    public String addProductToCart(int productId, int quantity, List<Product> cart) {
        Optional<Product> productOpt = productRepository.findById(productId);
//hace la verficacion sino manda error
        if (productOpt.isEmpty()) {
            return "Error: Producto no encontrado.";
        }

        Product product = productOpt.get();

        if (quantity <= 0 || quantity > product.getStock()) {
            return "Error: Cantidad inválida o stock insuficiente.";
        }

      //si el producto esta en el carrito, lo actualiza y lo suma   
        Optional<Product> existingItemOpt = cart.stream()
                .filter(item -> item.getId() == productId) // busca por id
                .findFirst();//verifica si ya existe en el carrito
        // si existe, actualiza la cantidad
        if (existingItemOpt.isPresent()) {
            Product existingItem = existingItemOpt.get();
            int newTotalQty = existingItem.getQuantity() + quantity;
//verifica que no exceda el stock
            if (newTotalQty > product.getStock()) {
                return "Error: La cantidad total excede el stock disponible.";
            }
//acutliza la cantidad y si no hay crea uno nuevo
            existingItem.setQuantity(newTotalQty);
        } else {
            Product newItem = new Product(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getStock(),
                    product.getUnit()
            );
            newItem.setQuantity(quantity);
            cart.add(newItem);
        }

        return "Producto agregado al carrito con éxito.";
    }

    //finaliza la venta
    public List<Product> finalizeSale(List<Product> cart) {
//busca el producto y descuenta el stock
        for (Product cartItem : cart) {
            Optional<Product> productOpt = productRepository.findById(cartItem.getId());

            productOpt.ifPresent(dbProduct -> {
                int newStock = dbProduct.getStock() - cartItem.getQuantity();
                dbProduct.setStock(newStock);
                productRepository.save(dbProduct); 
            });
        }
        
        // --- Cálculo de Totales para el registro de la Venta ---
        double subtotal = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        double tax = subtotal * IGV_RATE;
        double total = subtotal + tax; 
        
        // se registra la venta realizada
        Sale newSale = new Sale(total, LocalDateTime.now()); 
        saleRepository.save(newSale); 
        // -----------------------------------------------------------
        
        // Devuelve el carrito para que el controlador lo use en la redirección
        return new ArrayList<>(cart);
    }
    
    // metodo para el dashboard

    /**
     * Calcula y devuelve el total de dinero vendido en el día actual usando SaleRepository.
     */
    public Double getTotalVentasHoy() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        // Llama al método del repositorio. Devuelve 0.0 si el resultado es null (no hay ventas).
        return Optional.ofNullable(saleRepository.sumTotalByDate(startOfDay, endOfDay)).orElse(0.0); 
    }

    /**
     * Devuelve un mapa de ventas mensuales para los gráficos usando SaleRepository.
     */
    public Map<String, Double> getVentasPorMes() {
        List<Object[]> monthlySalesData = saleRepository.findTotalSalesPerMonth();
        Map<String, Double> data = new LinkedHashMap<>();
        
        for (Object[] row : monthlySalesData) {
            // Se asume el orden
            Integer monthIndex = (Integer) row[1]; 
            Double total = (Double) row[2];
            String monthName = getMonthName(monthIndex); 
            
            data.put(monthName, total);
        }
        return data;
    }

    /**
     * Método auxiliar para convertir número de mes (1-12) a nombre abreviado.
     */
    private String getMonthName(int month) {
        String[] names = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"};
        if (month >= 1 && month <= 12) {
            return names[month - 1];
        }
        return "N/A";
    }

    // obtiene el producto por id 

    public Optional<Product> getProductById(int id) {
        return productRepository.findById(id);
    }
//guarda el producto en el repositorio
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
//añade stock al producto
    public Optional<Product> addStock(int productId, int addedStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        // verifica si el producto existe y añade el stock
        productOpt.ifPresent(product -> {
            if (addedStock > 0) {
                int newStock = product.getStock() + addedStock;
                product.setStock(newStock);
                productRepository.save(product);
            }
        });
        
        return productOpt;
    }
    //si en un futuro se quiere eliminar
    public void deleteProduct(int productId) {
        productRepository.deleteById(productId);
    }
}