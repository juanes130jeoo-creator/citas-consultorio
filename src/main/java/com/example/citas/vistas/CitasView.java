package com.example.citas.vistas;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.example.citas.modelo.Paciente;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Citas")
public class CitasView extends VerticalLayout {

    private Grid<Cita> grid = new Grid<>(Cita.class, false);

    // para mostrar el nombre en vez del documento o el id
    private Map<String, Paciente> pacientes = new HashMap<>();
    private Map<Integer, Medico> medicos = new HashMap<>();

    private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CitasView() {
        H2 titulo = new H2("Citas");

        Button nueva = new Button("Agendar cita", VaadinIcon.PLUS.create());
        nueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nueva.addClickListener(e -> new CitaFormulario(null, this::actualizar).open());

        grid.addColumn(c -> c.getFecha().format(formato)).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(Cita::getHora).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(c -> nombrePaciente(c)).setHeader("Paciente").setAutoWidth(true);
        grid.addColumn(c -> nombreMedico(c)).setHeader("Médico").setAutoWidth(true);
        grid.addColumn(c -> especialidad(c)).setHeader("Especialidad").setAutoWidth(true);
        grid.addColumn(Cita::getMotivo).setHeader("Motivo").setAutoWidth(true);
        grid.addColumn(Cita::getEstado).setHeader("Estado").setAutoWidth(true);
        grid.setSizeFull();

        add(titulo, nueva, grid);
        setSizeFull();
        actualizar();
    }

    private void actualizar() {
        pacientes.clear();
        for (Paciente p : ArchivoDatos.leerPacientes()) {
            pacientes.put(p.getDocumento(), p);
        }
        medicos.clear();
        for (Medico m : ArchivoDatos.leerMedicos()) {
            medicos.put(m.getId(), m);
        }

        List<Cita> citas = ArchivoDatos.leerCitas();
        // ordenadas por fecha y hora
        citas.sort(Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora));
        grid.setItems(citas);
    }

    private String nombrePaciente(Cita c) {
        Paciente p = pacientes.get(c.getDocumentoPaciente());
        return p != null ? p.getNombre() : "(paciente eliminado)";
    }

    private String nombreMedico(Cita c) {
        Medico m = medicos.get(c.getIdMedico());
        return m != null ? m.getNombre() : "(médico eliminado)";
    }

    private String especialidad(Cita c) {
        Medico m = medicos.get(c.getIdMedico());
        return m != null ? m.getEspecialidad() : "";
    }
}
