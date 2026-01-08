/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.veterinariaPatitas.model;

import jakarta.persistence.*; 
import java.util.List;


@Entity                     
@Table(name = "servicios")  
public class ServiceVet {

    @Id                     
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer id;

    @Column(nullable = false, length = 100) 
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(length = 50)
    private String duration; 

    @OneToMany(mappedBy = "servicio")
    private List<Medico> medicos;

    public ServiceVet() {
    }

    
    public ServiceVet(Integer id, String name, String description, double price, String duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
        
    }

    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
    public List<Medico> getMedicos() {
    return medicos;
    }

    public void setMedicos(List<Medico> medicos) {
    this.medicos = medicos;
     }


    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", duration='" + duration + '\'' +
                '}';
    }
}



