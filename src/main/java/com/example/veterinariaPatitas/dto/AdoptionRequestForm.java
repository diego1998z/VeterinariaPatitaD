package com.example.veterinariaPatitas.dto;

public class AdoptionRequestForm {

    private String nombreCompleto;
    private String documento;
    private String correo;
    private String telefono;
    private String tipoVivienda;
    private String distrito;
    private String otrasMascotas;
    private String detalleOtrasMascotas;
    private String motivo;
    private boolean aceptaCondiciones;

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
    public String getOtrasMascotas() { return otrasMascotas; }
    public void setOtrasMascotas(String otrasMascotas) { this.otrasMascotas = otrasMascotas; }
    public String getDetalleOtrasMascotas() { return detalleOtrasMascotas; }
    public void setDetalleOtrasMascotas(String detalleOtrasMascotas) { this.detalleOtrasMascotas = detalleOtrasMascotas; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public boolean isAceptaCondiciones() { return aceptaCondiciones; }
    public void setAceptaCondiciones(boolean aceptaCondiciones) { this.aceptaCondiciones = aceptaCondiciones; }
}
