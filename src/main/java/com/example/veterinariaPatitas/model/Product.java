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

    @Column(nullable = false)
    private int stock; 

    @Column(length = 50)
    private String unit; 

    @Transient
    private int quantity; 

    
    public Product() {}

    
    public Product(int id, String name, double price, int stock, String unit) {
        this.id = id;
        this.name = name;
        this.price = price;
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
}