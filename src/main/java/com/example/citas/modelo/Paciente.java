package com.example.citas.modelo;

import java.time.LocalDate;

public class Paciente {

    private String documento;
    private String nombre;
    private String telefono;
    private LocalDate fechaNacimiento;

    public Paciente() {
    }

    public Paciente(String documento, String nombre, String telefono, LocalDate fechaNacimiento) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    // asi se guarda en el archivo: documento|nombre|telefono|fecha
    public String toLinea() {
        return documento + "|" + nombre + "|" + telefono + "|" + fechaNacimiento;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
