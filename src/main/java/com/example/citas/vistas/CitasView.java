package com.example.citas.vistas;

import java.time.LocalDate;
import java.time.LocalTime;
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Citas")
public class CitasView extends VerticalLayout {

    private static final String TODAS = "Todas las especialidades";

    // un grid para cada pestaña
    private Grid<Cita> gridHoy = crearGrid(true);
    private Grid<Cita> gridProximas = crearGrid(true);
    private Grid<Cita> gridHistorial = crearGrid(false);

    private Tab tabHoy;
    private Tab tabProximas;
    private Tab tabHistorial;

    private TextField buscar = new TextField();
    private Select<String> filtroEspecialidad = new Select<>();

    // para mostrar el nombre en vez del documento o el id
    private Map<String, Paciente> pacientes = new HashMap<>();
    private Map<Integer, Medico> medicos = new HashMap<>();

    private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CitasView() {
        H2 titulo = new H2("Citas");

        buscar.setPlaceholder("Buscar paciente, médico o motivo");
        buscar.setWidth("280px");
        buscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscar.setClearButtonVisible(true);
        buscar.setValueChangeMode(ValueChangeMode.EAGER);
        buscar.addValueChangeListener(e -> actualizar());

        List<String> opciones = new ArrayList<>();
        opciones.add(TODAS);
        for (String esp : Medico.ESPECIALIDADES) {
            opciones.add(esp);
        }
        filtroEspecialidad.setItems(opciones);
        filtroEspecialidad.setValue(TODAS);
        filtroEspecialidad.setWidth("230px");
        filtroEspecialidad.addValueChangeListener(e -> actualizar());

        Button nueva = new Button("Agendar cita", VaadinIcon.PLUS.create());
        nueva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nueva.addClickListener(e -> new CitaFormulario(null, this::actualizar).open());

        HorizontalLayout barra = new HorizontalLayout(buscar, filtroEspecialidad, nueva);
        barra.getStyle().set("flex-wrap", "wrap");
        barra.setAlignItems(FlexComponent.Alignment.BASELINE);

        // TabSheet con las tres pestañas
        TabSheet pestanas = new TabSheet();
        tabHoy = pestanas.add("Hoy", gridHoy);
        tabProximas = pestanas.add("Próximas", gridProximas);
        tabHistorial = pestanas.add("Historial", gridHistorial);
        pestanas.setSizeFull();

        add(titulo, barra, pestanas);
        setSizeFull();
        actualizar();
    }

    private Grid<Cita> crearGrid(boolean conAcciones) {
        Grid<Cita> grid = new Grid<>(Cita.class, false);
        grid.addColumn(c -> c.getFecha().format(formato)).setHeader("Fecha").setAutoWidth(true);
        grid.addColumn(Cita::getHora).setHeader("Hora").setAutoWidth(true);
        grid.addColumn(c -> nombrePaciente(c)).setHeader("Paciente").setAutoWidth(true);
        grid.addColumn(c -> nombreMedico(c)).setHeader("Médico").setAutoWidth(true);
        grid.addColumn(c -> especialidad(c)).setHeader("Especialidad").setAutoWidth(true);
        grid.addColumn(Cita::getMotivo).setHeader("Motivo").setAutoWidth(true);
        grid.addColumn(c -> estadoParaMostrar(c)).setHeader("Estado").setAutoWidth(true);

        if (conAcciones) {
            grid.addComponentColumn(c -> {
                HorizontalLayout botones = new HorizontalLayout();
                // solo se puede cambiar una cita que sigue programada
                if (estadoParaMostrar(c).equals(Cita.PROGRAMADA)) {
                    Button reprogramar = new Button("Reprogramar", VaadinIcon.CALENDAR_CLOCK.create());
                    reprogramar.addThemeVariants(ButtonVariant.LUMO_SMALL);
                    reprogramar.addClickListener(e -> new CitaFormulario(c, this::actualizar).open());

                    Button cancelar = new Button("Cancelar", VaadinIcon.CLOSE_SMALL.create());
                    cancelar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                    cancelar.addClickListener(e -> confirmarCancelar(c));

                    botones.add(reprogramar, cancelar);
                }
                return botones;
            }).setHeader("Acciones").setAutoWidth(true);
        }
        grid.setSizeFull();
        return grid;
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

        LocalDate hoy = LocalDate.now();
        List<Cita> deHoy = new ArrayList<>();
        List<Cita> proximas = new ArrayList<>();
        List<Cita> historial = new ArrayList<>();

        for (Cita c : ArchivoDatos.leerCitas()) {
            if (!cumpleFiltros(c)) {
                continue;
            }
            if (c.getFecha().equals(hoy)) {
                deHoy.add(c);
            } else if (c.getFecha().isAfter(hoy) && !c.estaCancelada()) {
                proximas.add(c);
            } else {
                // las pasadas y las canceladas van al historial
                historial.add(c);
            }
        }

        Comparator<Cita> porFecha = Comparator.comparing(Cita::getFecha).thenComparing(Cita::getHora);
        deHoy.sort(porFecha);
        proximas.sort(porFecha);
        historial.sort(porFecha.reversed()); // el historial de la mas reciente a la mas vieja

        gridHoy.setItems(deHoy);
        gridProximas.setItems(proximas);
        gridHistorial.setItems(historial);

        tabHoy.setLabel("Hoy (" + deHoy.size() + ")");
        tabProximas.setLabel("Próximas (" + proximas.size() + ")");
        tabHistorial.setLabel("Historial (" + historial.size() + ")");
    }

    private boolean cumpleFiltros(Cita c) {
        String texto = buscar.getValue().trim().toLowerCase();
        String esp = filtroEspecialidad.getValue();

        boolean coincideTexto = nombrePaciente(c).toLowerCase().contains(texto)
                || c.getDocumentoPaciente().contains(texto)
                || nombreMedico(c).toLowerCase().contains(texto)
                || c.getMotivo().toLowerCase().contains(texto);
        boolean coincideEsp = esp == null || esp.equals(TODAS) || especialidad(c).equals(esp);

        return coincideTexto && coincideEsp;
    }

    private void confirmarCancelar(Cita c) {
        ConfirmDialog confirmar = new ConfirmDialog();
        confirmar.setHeader("Cancelar cita");
        confirmar.setText("¿Cancelar la cita de " + nombrePaciente(c) + " el " + c.getFecha().format(formato)
                + " a las " + c.getHora() + "?");
        confirmar.setCancelable(true);
        confirmar.setCancelText("No");
        confirmar.setConfirmText("Sí, cancelar");
        confirmar.setConfirmButtonTheme("error primary");
        confirmar.addConfirmListener(e -> {
            List<Cita> citas = ArchivoDatos.leerCitas();
            for (Cita cita : citas) {
                if (cita.getId() == c.getId()) {
                    cita.setEstado(Cita.CANCELADA);
                }
            }
            ArchivoDatos.guardarCitas(citas);
            actualizar();
            Mensajes.exito("Cita cancelada");
        });
        confirmar.open();
    }

    // en pantalla una cita programada que ya paso se muestra como Atendida
    private String estadoParaMostrar(Cita c) {
        if (c.estaCancelada()) {
            return Cita.CANCELADA;
        }
        boolean yaPaso = c.getFecha().isBefore(LocalDate.now())
                || (c.getFecha().equals(LocalDate.now()) && c.getHora().isBefore(LocalTime.now()));
        return yaPaso ? "Atendida" : Cita.PROGRAMADA;
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
