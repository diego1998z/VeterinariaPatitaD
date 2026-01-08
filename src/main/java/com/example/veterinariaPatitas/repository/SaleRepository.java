package com.example.veterinariaPatitas.repository;

import com.example.veterinariaPatitas.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // 1. Obtener el total de ventas de un día (KPI)
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDateTime BETWEEN :startOfDay AND :endOfDay")
    Double sumTotalByDate(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 2. Obtener totales por mes 
    @Query("SELECT YEAR(s.saleDateTime), MONTH(s.saleDateTime), SUM(s.totalAmount) " +
            "FROM Sale s " +
            "GROUP BY YEAR(s.saleDateTime), MONTH(s.saleDateTime) " +
            "ORDER BY YEAR(s.saleDateTime), MONTH(s.saleDateTime)")
    List<Object[]> findTotalSalesPerMonth(); 
}