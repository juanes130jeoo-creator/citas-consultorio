package com.example.citas.vistas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.example.citas.modelo.Paciente;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// Vista con todas las citas de un paciente, se abre desde la lista de pacientes
// la url queda asi: /historial/1001234567
@Route(value = "historial", layout = MainLayout.class)
@PageTitle("Historial del paciente")
public class HistorialPacienteView extends VerticalLayout implements HasUrlParameter<String> {

    private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void setParameter(BeforeEvent evento, String documento) {
        removeAll();

        Button volver = new Button("Volver a pacientes", VaadinIcon.ARROW_LEFT.create());
        volver.addClickListener(e -> UI.getCurrent().navigate(PacientesView.class));
        add(volver);

        Paciente paciente = null;
        for (Paciente p : ArchivoDatos.leerPacientes()) {
            if (p.getDocumento().equals(documento)) {
                paciente = p;
            }
        }

        if (paciente == null) {
            add(new H2("Paciente no encontrado"));
            add(new Paragraph("No existe un paciente con el documento " + documento));
            return;
        }

        // datos del paciente
        add(new H2("Historial de " + paciente.getNombre()));
        add(new Paragraph("Documento: " + paciente.getDocumento()
                + "  |  Teléfono: " + paciente.getTelefono()
                + "  |  Nacimiento: " + paciente.getFechaNacimiento().format(formato)
                + " (" + paciente.getEdad() + " años)"));

        Map<Integer, Medico> medicos = new HashMap<>();
        for (Medico m : ArchivoDatos.leerMedicos()) {
            medicos.put(m.getId(), m);
        }

        // citas del paciente y conteo por estado
        List<Cita> citas = new ArrayList<>();
        int atendidas = 0;
        int canceladas = 0;
        int pendientes = 0;
        for (Cita c : ArchivoDatos.leerCitas()) {
            if (c.getDocumentoPaciente().equals(documento)) {
                citas.add(c);
                String estado = c.getEstadoParaMostrar();
                if (estado.equals("Atendida")) {
                    atendidas++;
                } else if (estado.equals(Cita.CANCELADA)) {
                    canceladas++;
                } else {
                    pendientes++;
                }
            }
        }
        citas.sort(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora).reversed());

        add(new Paragraph("Total de citas: " + citas.size() + "  |  Atendidas: " + atendidas
                + "  |  Pendientes: " + pendientes + "  |  Canceladas: " + canceladas));

        if (citas.isEmpty()) {
            add(new Paragraph("Este paciente todavía no tiene citas."));
            return;
        }

        Grid<Cita> grid = new Grid<>(Cita.class, false);
        grid.addColumn(c -> c.getFecha().format(formato)).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(Cita::getHora).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(c -> {
            Medico m = medicos.get(c.getIdMedico());
            return m != null ? m.getNombre() : "(médico eliminado)";
        }).setHeader("Médico").setAutoWidth(true);
        grid.addColumn(c -> {
            Medico m = medicos.get(c.getIdMedico());
            return m != null ? m.getEspecialidad() : "";
        }).setHeader("Especialidad").setAutoWidth(true);
        grid.addColumn(Cita::getMotivo).setHeader("Motivo").setAutoWidth(true);
        grid.addColumn(Cita::getEstadoParaMostrar).setHeader("Estado").setAutoWidth(true);
        grid.setItems(citas);
        grid.setSizeFull();

        add(grid);
        setSizeFull();
    }
}
