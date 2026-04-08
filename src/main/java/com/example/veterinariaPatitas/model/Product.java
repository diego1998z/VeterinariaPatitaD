/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.veterinariaPatitas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//incrementador
    private int id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    private int stock; 

    @Column(length = 50)
    private String unit; 

    @Transient
    private int quantity; 

    @Transient
    private Double pricePen;

    @Transient
    private Double exchangeRateUsed;

    
    public Product() {}

    
    public Product(int id, String name, double price, int stock, String unit) {
        this(id, name, price, stock, unit, "PEN");
    }

    public Product(int id, String name, double price, int stock, String unit, String currency) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.stock = stock;
        this.unit = unit;
        this.quantity = 0; 
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) { 
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { 
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) { 
        this.price = price;
    }

    public String getCurrency() {
        return normalizeCurrency(currency);
    }

    public void setCurrency(String currency) {
        this.currency = normalizeCurrency(currency);
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getPricePen() {
        return pricePen;
    }

    public void setPricePen(Double pricePen) {
        this.pricePen = pricePen;
    }

    public Double getExchangeRateUsed() {
        return exchangeRateUsed;
    }

    public void setExchangeRateUsed(Double exchangeRateUsed) {
        this.exchangeRateUsed = exchangeRateUsed;
    }

    public double getEffectivePricePen() {
        if (pricePen != null) return pricePen;
        return price;
    }

    @PrePersist
    @PreUpdate
    private void normalizePersistentFields() {
        this.currency = normalizeCurrency(this.currency);
    }

    private String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) return "PEN";
        String upper = value.trim().toUpperCase();
        return "USD".equals(upper) ? "USD" : "PEN";
    }
}
