package com.example.citas.modelo;

public class Medico {

    // especialidades que maneja el consultorio
    public static final String[] ESPECIALIDADES = {
        "Medicina general", "Pediatría", "Odontología", "Dermatología",
        "Ginecología", "Cardiología", "Psicología"
    };

    private int id;
    private String nombre;
    private String especialidad;

    public Medico() {
    }

    public Medico(int id, String nombre, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    // id|nombre|especialidad
    public String toLinea() {
        return id + "|" + nombre + "|" + especialidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}
