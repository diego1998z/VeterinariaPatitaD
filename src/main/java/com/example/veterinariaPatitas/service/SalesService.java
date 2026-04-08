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
import java.time.Duration;
import java.util.Map; 
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedHashMap; // Importado para mantener el orden de los meses

@Service
public class SalesService {

    private final double IGV_RATE = 0.18;
    private static final Duration EXCHANGE_RATE_CACHE_TTL = Duration.ofMinutes(15);

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SaleRepository saleRepository; // Repositorio de Ventas inyectado

    @Autowired
    private ApiPeruService apiPeruService;

    private ExchangeRateInfo cachedExchangeRateInfo;
    private LocalDateTime cachedExchangeRateAt;

    /**
     * Carga productos desde BD y prepara precios de visualizacion:
     * - PEN: precio directo.
     * - USD: precio convertido a PEN con tipo de cambio vigente.
     */
    public List<Product> getInitialProducts() {
        List<Product> products = productRepository.findAll();
        prepareProductsForDisplay(products);
        return products;
    }

    //calcula totales 
    public void calculateTotals(List<Product> cart, Model model) {
        double subtotal = calculateSubtotal(cart);
        double tax = subtotal * IGV_RATE;
        double total = subtotal + tax;

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax); // IGV
        model.addAttribute("total", total);
    }

    /**
     * Agrega item al carrito validando existencia, stock y conversion de moneda.
     */
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

        // Prepara precio en soles si el producto esta en USD.
        Product preparedProduct = cloneForDisplay(product, resolveExchangeRateInfo());
        if ("USD".equals(preparedProduct.getCurrency()) && preparedProduct.getPricePen() == null) {
            return "Error: No se pudo obtener tipo de cambio para productos en USD.";
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
            if (existingItem.getPricePen() == null) {
                existingItem.setPricePen(preparedProduct.getPricePen());
                existingItem.setExchangeRateUsed(preparedProduct.getExchangeRateUsed());
                existingItem.setCurrency(preparedProduct.getCurrency());
            }
            existingItem.setQuantity(newTotalQty);
        } else {
            Product newItem = new Product(
                    preparedProduct.getId(),
                    preparedProduct.getName(),
                    preparedProduct.getPrice(),
                    preparedProduct.getStock(),
                    preparedProduct.getUnit(),
                    preparedProduct.getCurrency()
            );
            newItem.setPricePen(preparedProduct.getPricePen());
            newItem.setExchangeRateUsed(preparedProduct.getExchangeRateUsed());
            newItem.setQuantity(quantity);
            cart.add(newItem);
        }

        return "Producto agregado al carrito con éxito.";
    }

    /**
     * Finaliza venta: descuenta stock, calcula totales y registra cabecera de venta.
     */
    public List<Product> finalizeSale(List<Product> cart) {
        prepareCartForDisplay(cart);
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
        double subtotal = calculateSubtotal(cart);
        double tax = subtotal * IGV_RATE;
        double total = subtotal + tax; 
        
        // se registra la venta realizada
        Sale newSale = new Sale(total, LocalDateTime.now()); 
        saleRepository.save(newSale); 
        // -----------------------------------------------------------
        
        // Devuelve el carrito para que el controlador lo use en la redirección
        return new ArrayList<>(cart);
    }

    public void prepareProductsForDisplay(List<Product> products) {
        if (products == null || products.isEmpty()) return;
        // Usa un solo tipo de cambio para toda la renderizacion de esta solicitud.
        ExchangeRateInfo rateInfo = resolveExchangeRateInfo();
        for (Product product : products) {
            Product prepared = cloneForDisplay(product, rateInfo);
            product.setCurrency(prepared.getCurrency());
            product.setPricePen(prepared.getPricePen());
            product.setExchangeRateUsed(prepared.getExchangeRateUsed());
        }
    }

    public void prepareCartForDisplay(List<Product> cart) {
        if (cart == null || cart.isEmpty()) return;
        // Recalcula precios PEN para evitar nulls en carrito restaurado de sesion.
        ExchangeRateInfo rateInfo = resolveExchangeRateInfo();
        for (Product item : cart) {
            if ("USD".equals(item.getCurrency())) {
                if (item.getPricePen() == null) {
                    if (item.getExchangeRateUsed() != null && item.getExchangeRateUsed() > 0) {
                        item.setPricePen(roundMoney(item.getPrice() * item.getExchangeRateUsed()));
                    } else if (rateInfo != null && rateInfo.venta() != null && rateInfo.venta() > 0) {
                        item.setExchangeRateUsed(rateInfo.venta());
                        item.setPricePen(roundMoney(item.getPrice() * rateInfo.venta()));
                    }
                }
            } else {
                item.setPricePen(roundMoney(item.getPrice()));
                item.setExchangeRateUsed(1.0);
            }
        }
    }

    public double calculateSubtotal(List<Product> cart) {
        if (cart == null || cart.isEmpty()) return 0.0;
        return cart.stream()
                .mapToDouble(item -> getUnitPricePen(item) * item.getQuantity())
                .sum();
    }

    public double getUnitPricePen(Product product) {
        if (product == null) return 0.0;
        if (product.getPricePen() != null) return product.getPricePen();
        if ("USD".equals(product.getCurrency())) {
            ExchangeRateInfo rateInfo = resolveExchangeRateInfo();
            if (rateInfo != null && rateInfo.venta() != null && rateInfo.venta() > 0) {
                return roundMoney(product.getPrice() * rateInfo.venta());
            }
        }
        return roundMoney(product.getPrice());
    }

    private Product cloneForDisplay(Product source, ExchangeRateInfo rateInfo) {
        if (source == null) return null;
        Product clone = new Product(
                source.getId(),
                source.getName(),
                source.getPrice(),
                source.getStock(),
                source.getUnit(),
                source.getCurrency()
        );
        clone.setQuantity(source.getQuantity());

        if ("USD".equals(source.getCurrency())) {
            // Conversion USD->PEN con tipo de cambio "venta".
            if (rateInfo != null && rateInfo.venta() != null && rateInfo.venta() > 0) {
                clone.setExchangeRateUsed(rateInfo.venta());
                clone.setPricePen(roundMoney(source.getPrice() * rateInfo.venta()));
            } else {
                clone.setPricePen(null);
                clone.setExchangeRateUsed(null);
            }
        } else {
            clone.setExchangeRateUsed(1.0);
            clone.setPricePen(roundMoney(source.getPrice()));
        }

        return clone;
    }

    private synchronized ExchangeRateInfo resolveExchangeRateInfo() {
        LocalDateTime now = LocalDateTime.now();
        if (cachedExchangeRateInfo != null && cachedExchangeRateAt != null) {
            Duration age = Duration.between(cachedExchangeRateAt, now);
            if (!age.minus(EXCHANGE_RATE_CACHE_TTL).isNegative()) {
                // cache vencido, continuar
            } else {
                // Reutiliza cache para evitar llamar API externa en cada request.
                return cachedExchangeRateInfo;
            }
        }

        ExchangeRateInfo fresh = fetchLatestExchangeRate();
        if (fresh != null) {
            cachedExchangeRateInfo = fresh;
            cachedExchangeRateAt = now;
            return fresh;
        }

        return cachedExchangeRateInfo;
    }

    private ExchangeRateInfo fetchLatestExchangeRate() {
        // Busca hasta 7 dias atras por si hoy no hay tipo de cambio publicado.
        for (int i = 0; i < 7; i++) {
            String fecha = LocalDate.now().minusDays(i).toString();
            Map<String, Object> response = apiPeruService.consultarTipoCambio(fecha);
            if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
                continue;
            }

            Object dataObj = response.get("data");
            if (!(dataObj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;

            Double venta = toDouble(data.get("venta"));
            if (venta == null || venta <= 0) {
                continue;
            }

            String fechaRespuesta = data.get("fecha") == null ? fecha : data.get("fecha").toString();
            return new ExchangeRateInfo(fechaRespuesta, venta);
        }
        return null;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString().trim().replace(",", "."));
        } catch (Exception ex) {
            return null;
        }
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record ExchangeRateInfo(String fecha, Double venta) {}
    
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
        if (product == null) return null;
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
