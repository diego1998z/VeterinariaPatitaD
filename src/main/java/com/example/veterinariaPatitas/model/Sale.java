package com.example.veterinariaPatitas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales") // nombre de la bd
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Total final de la venta (incluyendo impuestos)
    @Column(nullable = false)
    private double totalAmount; 
    
    // Fecha y hora en que se finalizó la venta
    @Column(nullable = false)
    private LocalDateTime saleDateTime; 

    // Constructor vacío (necesario para JPA)
    public Sale() {}

    // Constructor para registrar una venta
    public Sale(double totalAmount, LocalDateTime saleDateTime) {
        this.totalAmount = totalAmount;
        this.saleDateTime = saleDateTime;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(LocalDateTime saleDateTime) {
        this.saleDateTime = saleDateTime;
    }
}