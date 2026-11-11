package com.example.citas.vistas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.example.citas.modelo.Paciente;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// BONIFICACION: agenda de la semana de un medico
// es como el tablero del buscaminas: filas = franjas de 30 min, columnas = lunes a sabado
@Route(value = "agenda", layout = MainLayout.class)
@PageTitle("Agenda semanal")
public class AgendaSemanalView extends VerticalLayout {

    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};

    private Select<Medico> selectorMedico = new Select<>();
    private Span textoSemana = new Span();
    private VerticalLayout tablero = new VerticalLayout();

    private LocalDate lunes; // el lunes de la semana que se esta viendo
    private DateTimeFormatter formatoCorto = DateTimeFormatter.ofPattern("dd/MM");

    public AgendaSemanalView() {
        H2 titulo = new H2("Agenda semanal");

        List<Medico> medicos = ArchivoDatos.leerMedicos();
        selectorMedico.setLabel("Médico");
        selectorMedico.setItems(medicos);
        selectorMedico.setItemLabelGenerator(m -> m.getNombre() + " - " + m.getEspecialidad());
        selectorMedico.setWidth("300px");
        if (!medicos.isEmpty()) {
            selectorMedico.setValue(medicos.get(0));
        }
        selectorMedico.addValueChangeListener(e -> dibujarTablero());

        // botones para moverse entre semanas
        Button anterior = new Button("Semana anterior", VaadinIcon.ARROW_LEFT.create());
        anterior.addClickListener(e -> {
            lunes = lunes.minusWeeks(1);
            dibujarTablero();
        });
        Button siguiente = new Button("Semana siguiente", VaadinIcon.ARROW_RIGHT.create());
        siguiente.setIconAfterText(true);
        siguiente.addClickListener(e -> {
            lunes = lunes.plusWeeks(1);
            dibujarTablero();
        });
        Button hoy = new Button("Esta semana", e -> {
            lunes = LocalDate.now().with(DayOfWeek.MONDAY);
            dibujarTablero();
        });

        textoSemana.getStyle().set("font-weight", "bold");

        HorizontalLayout barra = new HorizontalLayout(selectorMedico, anterior, hoy, siguiente, textoSemana);
        barra.getStyle().set("flex-wrap", "wrap");
        barra.setAlignItems(FlexComponent.Alignment.BASELINE);

        Span ayuda = new Span("Dé clic en una casilla \"Libre\" para agendar una cita en ese horario.");
        ayuda.getStyle().set("color", "gray").set("font-size", "0.9em");

        tablero.setPadding(false);
        tablero.setSpacing(false);
        tablero.setWidth("auto");
        tablero.getStyle().set("min-width", "750px");

        // en el celular la tabla no cabe, entonces se puede mover de lado
        Div contenedor = new Div(tablero);
        contenedor.setWidthFull();
        contenedor.getStyle().set("overflow-x", "auto");

        add(titulo, barra, ayuda, contenedor);

        lunes = LocalDate.now().with(DayOfWeek.MONDAY);
        dibujarTablero();
    }

    // borra el tablero y lo vuelve a armar con las citas del medico en esa semana
    private void dibujarTablero() {
        tablero.removeAll();
        LocalDate sabado = lunes.plusDays(5);
        textoSemana.setText("Semana del " + lunes.format(formatoCorto) + " al " + sabado.format(formatoCorto) + " de " + sabado.getYear());

        Medico medico = selectorMedico.getValue();
        if (medico == null) {
            tablero.add(new Span("No hay médicos registrados"));
            return;
        }

        Map<String, String> nombresPacientes = new HashMap<>();
        for (Paciente p : ArchivoDatos.leerPacientes()) {
            nombresPacientes.put(p.getDocumento(), p.getNombre());
        }

        // se guardan las citas del medico en un mapa con la llave "fecha hora" para encontrarlas rapido
        Map<String, Cita> ocupadas = new HashMap<>();
        for (Cita c : ArchivoDatos.leerCitas()) {
            if (c.getIdMedico() == medico.getId() && !c.estaCancelada()) {
                ocupadas.put(c.getFecha() + " " + c.getHora(), c);
            }
        }

        // primera fila: los nombres de los dias
        HorizontalLayout encabezado = nuevaFila();
        encabezado.add(casillaHora(""));
        for (int d = 0; d < 6; d++) {
            LocalDate dia = lunes.plusDays(d);
            Div casilla = casillaBase();
            casilla.setText(DIAS[d] + " " + dia.format(formatoCorto));
            casilla.getStyle().set("font-weight", "bold").set("background", "#e8eef7");
            if (dia.equals(LocalDate.now())) {
                casilla.getStyle().set("background", "#c9dcf5");
            }
            encabezado.add(casilla);
        }
        tablero.add(encabezado);

        // una fila por cada franja de 30 minutos
        LocalTime franja = CitaFormulario.HORA_INICIO;
        while (!franja.isAfter(CitaFormulario.HORA_FIN)) {
            HorizontalLayout fila = nuevaFila();
            fila.add(casillaHora(franja.toString()));

            for (int d = 0; d < 6; d++) {
                LocalDate dia = lunes.plusDays(d);
                Cita cita = ocupadas.get(dia + " " + franja);
                fila.add(crearCasilla(medico, dia, franja, cita, nombresPacientes));
            }
            tablero.add(fila);
            franja = franja.plusMinutes(30);
        }
    }

    private Div crearCasilla(Medico medico, LocalDate dia, LocalTime franja, Cita cita, Map<String, String> nombresPacientes) {
        Div casilla = casillaBase();
        boolean yaPaso = LocalDateTime.of(dia, franja).isBefore(LocalDateTime.now());

        if (cita != null) {
            // casilla ocupada: se muestra el paciente
            casilla.setText(nombresPacientes.getOrDefault(cita.getDocumentoPaciente(), "(paciente eliminado)"));
            casilla.getElement().setAttribute("title", cita.getMotivo());
            casilla.getStyle().set("background", yaPaso ? "#e0e0e0" : "#ffd9a8");
        } else if (yaPaso) {
            casilla.setText("-");
            casilla.getStyle().set("color", "#aaa").set("background", "#f4f4f4");
        } else {
            // casilla libre: al darle clic se abre el formulario con todo puesto
            casilla.setText("Libre");
            casilla.getStyle().set("color", "#2e7d32").set("background", "#eaf6ea").set("cursor", "pointer");
            casilla.addClickListener(e -> {
                CitaFormulario formulario = new CitaFormulario(null, this::dibujarTablero);
                formulario.prellenar(medico.getId(), dia, franja);
                formulario.open();
            });
        }
        return casilla;
    }

    private HorizontalLayout nuevaFila() {
        HorizontalLayout fila = new HorizontalLayout();
        fila.setSpacing(false);
        fila.setWidthFull();
        return fila;
    }

    private Div casillaHora(String texto) {
        Div casilla = casillaBase();
        casilla.setText(texto);
        casilla.getStyle().set("flex", "0 0 60px").set("font-weight", "bold").set("background", "#e8eef7");
        return casilla;
    }

    // estilo comun de todas las casillas
    private Div casillaBase() {
        Div casilla = new Div();
        casilla.getStyle()
                .set("flex", "1")
                .set("min-width", "110px")
                .set("border", "1px solid #ccc")
                .set("padding", "4px")
                .set("font-size", "0.85em")
                .set("text-align", "center")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");
        return casilla;
    }
}
