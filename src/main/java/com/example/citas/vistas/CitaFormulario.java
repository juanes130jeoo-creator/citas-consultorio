package com.example.citas.vistas;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.example.citas.modelo.Paciente;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;

// Dialogo para agendar una cita nueva o reprogramar una que ya existe
public class CitaFormulario extends Dialog {

    // horario de atencion: la primera franja es a las 7:00 y la ultima a las 16:30 (termina a las 17:00)
    public static final LocalTime HORA_INICIO = LocalTime.of(7, 0);
    public static final LocalTime HORA_FIN = LocalTime.of(16, 30);

    private static final String TODAS = "Todas";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Cita cita; // si es null es una cita nueva
    private Runnable alGuardar; // lo que se hace despues de guardar (refrescar la vista)

    private List<Medico> medicos = ArchivoDatos.leerMedicos();
    private List<Paciente> pacientes = ArchivoDatos.leerPacientes();

    private ComboBox<Paciente> paciente = new ComboBox<>("Paciente");
    private Select<String> especialidad = new Select<>();
    private Select<Medico> medico = new Select<>();
    private DatePicker fecha = new DatePicker("Fecha");
    private TimePicker hora = new TimePicker("Hora");
    private TextField motivo = new TextField("Motivo de la consulta");

    public CitaFormulario(Cita cita, Runnable alGuardar) {
        this.cita = cita;
        this.alGuardar = alGuardar;

        setHeaderTitle(cita == null ? "Agendar cita" : "Reprogramar cita");

        paciente.setItems(pacientes);
        paciente.setItemLabelGenerator(p -> p.getNombre() + " (" + p.getDocumento() + ")");
        paciente.setPlaceholder("Escriba el nombre o documento");

        // Select de especialidad: al cambiarla se filtran los medicos
        List<String> opciones = new ArrayList<>();
        opciones.add(TODAS);
        for (String esp : Medico.ESPECIALIDADES) {
            opciones.add(esp);
        }
        especialidad.setLabel("Especialidad");
        especialidad.setItems(opciones);
        especialidad.setValue(TODAS);
        especialidad.addValueChangeListener(e -> filtrarMedicos());

        medico.setLabel("Médico");
        medico.setItemLabelGenerator(m -> m.getNombre() + " - " + m.getEspecialidad());
        medico.setPlaceholder("Seleccione el médico");
        filtrarMedicos();

        fecha.setLocale(Locale.of("es", "CO"));
        fecha.setMin(LocalDate.now());

        // TimePicker con saltos de 30 minutos y solo en el horario de atencion
        hora.setStep(Duration.ofMinutes(30));
        hora.setMin(HORA_INICIO);
        hora.setMax(HORA_FIN);
        hora.setLocale(Locale.of("es", "CO"));
        hora.setHelperText("De 7:00 a 16:30, cada 30 minutos");

        motivo.setMaxLength(80);

        if (cita != null) {
            // al reprogramar el paciente y el motivo no se cambian
            paciente.setValue(buscarPaciente(cita.getDocumentoPaciente()));
            paciente.setReadOnly(true);
            medico.setValue(buscarMedico(cita.getIdMedico()));
            fecha.setValue(cita.getFecha());
            hora.setValue(cita.getHora());
            motivo.setValue(cita.getMotivo());
            motivo.setReadOnly(true);
        }

        FormLayout formulario = new FormLayout(paciente, especialidad, medico, fecha, hora, motivo);
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        formulario.setColspan(paciente, 2);
        formulario.setColspan(motivo, 2);
        add(formulario);

        Button guardar = new Button("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addClickListener(e -> guardar());

        Button cancelar = new Button("Cancelar", e -> close());
        getFooter().add(cancelar, guardar);
    }

    private void filtrarMedicos() {
        String esp = especialidad.getValue();
        Medico seleccionado = medico.getValue();

        List<Medico> lista = new ArrayList<>();
        for (Medico m : medicos) {
            if (esp == null || esp.equals(TODAS) || m.getEspecialidad().equals(esp)) {
                lista.add(m);
            }
        }
        medico.setItems(lista);

        // si el medico que estaba elegido sigue en la lista se deja
        if (seleccionado != null && lista.contains(seleccionado)) {
            medico.setValue(seleccionado);
        }
    }

    private void guardar() {
        Paciente p = paciente.getValue();
        Medico m = medico.getValue();
        LocalDate f = fecha.getValue();
        LocalTime h = hora.getValue();
        String mot = motivo.getValue().trim();

        List<Cita> citas = ArchivoDatos.leerCitas();

        String error = validar(p, m, f, h, mot, citas);
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        if (cita == null) {
            Cita nueva = new Cita(ArchivoDatos.siguienteIdCita(), p.getDocumento(), m.getId(), f, h, mot, Cita.PROGRAMADA);
            citas.add(nueva);
            Mensajes.exito("Cita agendada para " + p.getNombre() + " el " + f.format(FORMATO_FECHA) + " a las " + h);
        } else {
            // se busca la cita en la lista leida del archivo y se le cambian los datos
            for (Cita c : citas) {
                if (c.getId() == cita.getId()) {
                    c.setIdMedico(m.getId());
                    c.setFecha(f);
                    c.setHora(h);
                }
            }
            Mensajes.exito("Cita reprogramada para el " + f.format(FORMATO_FECHA) + " a las " + h);
        }

        ArchivoDatos.guardarCitas(citas);
        close();
        alGuardar.run();
    }

    // revisa todas las reglas de la cita, devuelve el error o null si esta bien
    private String validar(Paciente p, Medico m, LocalDate f, LocalTime h, String mot, List<Cita> citas) {
        if (p == null || m == null || f == null || h == null) {
            return "Paciente, médico, fecha y hora son obligatorios";
        }
        if (cita == null && mot.isEmpty()) {
            return "Escriba el motivo de la consulta";
        }
        if (mot.contains("|")) {
            return "El motivo no puede tener el caracter |";
        }
        if (f.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return "El consultorio no atiende los domingos";
        }
        if (h.isBefore(HORA_INICIO) || h.isAfter(HORA_FIN)) {
            return "La hora debe estar entre las 7:00 y las 16:30";
        }
        if (h.getMinute() != 0 && h.getMinute() != 30) {
            return "Las citas son cada 30 minutos (en punto o y media)";
        }
        // no se permiten citas en el pasado
        if (f.isBefore(LocalDate.now())) {
            return "No se pueden agendar citas en días que ya pasaron";
        }
        if (f.equals(LocalDate.now()) && !h.isAfter(LocalTime.now())) {
            return "Esa hora de hoy ya pasó, elija una más tarde";
        }

        for (Cita c : citas) {
            // las canceladas no cuentan y si se esta reprogramando no se compara con ella misma
            if (c.estaCancelada() || (cita != null && c.getId() == cita.getId())) {
                continue;
            }
            boolean mismaHora = c.getFecha().equals(f) && c.getHora().equals(h);
            if (mismaHora && c.getIdMedico() == m.getId()) {
                return m.getNombre() + " ya tiene una cita el " + f.format(FORMATO_FECHA) + " a las " + h;
            }
            if (mismaHora && c.getDocumentoPaciente().equals(p.getDocumento())) {
                return p.getNombre() + " ya tiene otra cita a esa misma hora";
            }
        }
        return null;
    }

    private Medico buscarMedico(int id) {
        for (Medico m : medicos) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    private Paciente buscarPaciente(String documento) {
        for (Paciente p : pacientes) {
            if (p.getDocumento().equals(documento)) {
                return p;
            }
        }
        return null;
    }
}
