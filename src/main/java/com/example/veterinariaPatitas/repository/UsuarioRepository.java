/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //busca usuarios si ya existen en la bd
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
}

