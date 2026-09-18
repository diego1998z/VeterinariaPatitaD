package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.Product;
import com.example.veterinariaPatitas.repository.ProductRepository;
import com.example.veterinariaPatitas.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ApiPeruService apiPeruService;

    @InjectMocks
    private SalesService salesService;

    @Test
    void cp01_calculateSubtotal_conProductoEnSoles_calculaPrecioPorCantidad() {
        Product product = new Product(1, "Alimento", 25.0, 10, "unidad", "PEN");
        product.setPricePen(25.0);
        product.setQuantity(3);

        double subtotal = salesService.calculateSubtotal(List.of(product));

        assertEquals(75.0, subtotal, 0.001);
    }

    @Test
    void cp02_calculateSubtotal_conCarritoVacio_retornaCero() {
        double subtotal = salesService.calculateSubtotal(new ArrayList<>());

        assertEquals(0.0, subtotal, 0.001);
    }

    @Test
    void cp03_addProductToCart_conCantidadMayorAlStock_rechazaProducto() {
        Product product = new Product(1, "Alimento", 25.0, 10, "unidad", "PEN");
        List<Product> cart = new ArrayList<>();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        String result = salesService.addProductToCart(1, 15, cart);

        assertTrue(result.toLowerCase().contains("error"));
        assertTrue(cart.isEmpty());
    }
}
