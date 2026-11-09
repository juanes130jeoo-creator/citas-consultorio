package com.example.citas.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cita {

    // estados que puede tener una cita
    public static final String PROGRAMADA = "Programada";
    public static final String CANCELADA = "Cancelada";

    private int id;
    private String documentoPaciente;
    private int idMedico;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private String estado;

    public Cita() {
        estado = PROGRAMADA;
    }

    public Cita(int id, String documentoPaciente, int idMedico, LocalDate fecha, LocalTime hora, String motivo, String estado) {
        this.id = id;
        this.documentoPaciente = documentoPaciente;
        this.idMedico = idMedico;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    // id|documentoPaciente|idMedico|fecha|hora|motivo|estado
    public String toLinea() {
        return id + "|" + documentoPaciente + "|" + idMedico + "|" + fecha + "|" + hora + "|" + motivo + "|" + estado;
    }

    public boolean estaCancelada() {
        return CANCELADA.equals(estado);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocumentoPaciente() {
        return documentoPaciente;
    }

    public void setDocumentoPaciente(String documentoPaciente) {
        this.documentoPaciente = documentoPaciente;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
