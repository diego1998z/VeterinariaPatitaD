/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.veterinariaPatitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.veterinariaPatitas.model.ServiceVet;
import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceVet, Integer> {
    // Solo servicios que tengan al menos un médico asignado
    @Query("SELECT DISTINCT s FROM ServiceVet s JOIN s.medicos m")
    List<ServiceVet> findWithMedicos();
}
