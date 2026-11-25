package com.example.veterinariaPatitas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_adopcion")
public class AdoptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mascota_id", nullable = false, length = 40)
    private String petId;

    @Column(name = "mascota_nombre", nullable = false, length = 80)
    private String petName;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, length = 15)
    private String documento;

    @Column(nullable = false, length = 160)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "tipo_vivienda", nullable = false, length = 20)
    private String tipoVivienda;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(name = "tiene_otras_mascotas", nullable = false)
    private boolean tieneOtrasMascotas;

    @Column(name = "detalle_otras_mascotas", length = 300)
    private String detalleOtrasMascotas;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @PrePersist
    private void beforeInsert() {
        if (creadaEn == null) creadaEn = LocalDateTime.now();
        if (estado == null || estado.isBlank()) estado = "PENDIENTE";
    }

    public Long getId() { return id; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getTipoVivienda() { return tipoVivienda; }
    public void setTipoVivienda(String tipoVivienda) { this.tipoVivienda = tipoVivienda; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public boolean isTieneOtrasMascotas() { return tieneOtrasMascotas; }
    public void setTieneOtrasMascotas(boolean tieneOtrasMascotas) { this.tieneOtrasMascotas = tieneOtrasMascotas; }
    public String getDetalleOtrasMascotas() { return detalleOtrasMascotas; }
    public void setDetalleOtrasMascotas(String detalleOtrasMascotas) { this.detalleOtrasMascotas = detalleOtrasMascotas; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
}
