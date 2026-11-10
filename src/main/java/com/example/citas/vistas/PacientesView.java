package com.example.citas.vistas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Paciente;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "pacientes", layout = MainLayout.class)
@PageTitle("Pacientes")
public class PacientesView extends VerticalLayout {

    private List<Paciente> pacientes;

    private Grid<Paciente> grid = new Grid<>(Paciente.class, false);
    private TextField buscar = new TextField();
    private ComboBox<String> filtroEdad = new ComboBox<>();

    private DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PacientesView() {
        pacientes = ArchivoDatos.leerPacientes();

        H2 titulo = new H2("Pacientes");

        // busqueda en tiempo real
        buscar.setPlaceholder("Buscar por nombre o documento");
        buscar.setWidth("280px");
        buscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscar.setClearButtonVisible(true);
        buscar.setValueChangeMode(ValueChangeMode.EAGER);
        buscar.addValueChangeListener(e -> actualizarGrid());

        // filtro por edad
        filtroEdad.setPlaceholder("Filtrar por edad");
        filtroEdad.setItems("Todos", "Niños (0-17)", "Adultos (18-59)", "Adultos mayores (60+)");
        filtroEdad.setValue("Todos");
        filtroEdad.addValueChangeListener(e -> actualizarGrid());

        Button nuevo = new Button("Nuevo paciente", VaadinIcon.PLUS.create());
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.addClickListener(e -> abrirFormulario(null));

        HorizontalLayout barra = new HorizontalLayout(buscar, filtroEdad, nuevo);
        barra.getStyle().set("flex-wrap", "wrap");
        barra.setAlignItems(FlexComponent.Alignment.BASELINE);

        configurarGrid();

        add(titulo, barra, grid);
        setSizeFull();
        actualizarGrid();
    }

    private void configurarGrid() {
        grid.addColumn(Paciente::getDocumento).setHeader("Documento").setAutoWidth(true);
        grid.addColumn(Paciente::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Paciente::getTelefono).setHeader("Teléfono").setAutoWidth(true);
        grid.addColumn(p -> p.getFechaNacimiento().format(formato)).setHeader("Fecha de nacimiento").setAutoWidth(true);
        grid.addColumn(Paciente::getEdad).setHeader("Edad").setAutoWidth(true);

        // botones de editar y eliminar en cada fila
        grid.addComponentColumn(p -> {
            Button editar = new Button(VaadinIcon.EDIT.create());
            editar.addClickListener(e -> abrirFormulario(p));

            Button eliminar = new Button(VaadinIcon.TRASH.create());
            eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);
            eliminar.addClickListener(e -> confirmarEliminar(p));

            return new HorizontalLayout(editar, eliminar);
        }).setHeader("Acciones").setAutoWidth(true);

        grid.setSizeFull();
    }

    private void actualizarGrid() {
        String texto = buscar.getValue().trim().toLowerCase();
        String rango = filtroEdad.getValue();

        List<Paciente> filtrados = new ArrayList<>();
        for (Paciente p : pacientes) {
            boolean coincide = p.getNombre().toLowerCase().contains(texto) || p.getDocumento().contains(texto);
            if (coincide && cumpleFiltroEdad(p, rango)) {
                filtrados.add(p);
            }
        }
        grid.setItems(filtrados);
    }

    private boolean cumpleFiltroEdad(Paciente p, String rango) {
        if (rango == null || rango.equals("Todos")) {
            return true;
        }
        int edad = p.getEdad();
        if (rango.startsWith("Niños")) {
            return edad < 18;
        } else if (rango.startsWith("Adultos (")) {
            return edad >= 18 && edad < 60;
        } else {
            return edad >= 60;
        }
    }

    // si paciente es null es uno nuevo, si no se esta editando
    private void abrirFormulario(Paciente paciente) {
        boolean esNuevo = paciente == null;

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esNuevo ? "Nuevo paciente" : "Editar paciente");

        TextField documento = new TextField("Documento");
        TextField nombre = new TextField("Nombre completo");
        TextField telefono = new TextField("Teléfono");
        DatePicker nacimiento = new DatePicker("Fecha de nacimiento");
        nacimiento.setLocale(Locale.of("es", "CO"));
        nacimiento.setMax(LocalDate.now());

        if (!esNuevo) {
            documento.setValue(paciente.getDocumento());
            documento.setReadOnly(true); // el documento no se cambia porque las citas lo usan
            nombre.setValue(paciente.getNombre());
            telefono.setValue(paciente.getTelefono());
            nacimiento.setValue(paciente.getFechaNacimiento());
        }

        FormLayout formulario = new FormLayout(documento, nombre, telefono, nacimiento);
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        dialogo.add(formulario);

        Button guardar = new Button("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addClickListener(e -> {
            String doc = documento.getValue().trim();
            String nom = nombre.getValue().trim();
            String tel = telefono.getValue().trim();
            LocalDate fecha = nacimiento.getValue();

            String error = validar(doc, nom, tel, fecha);
            if (error != null) {
                Mensajes.error(error);
                return;
            }

            if (esNuevo) {
                if (buscarPorDocumento(doc) != null) {
                    Mensajes.error("Ya existe un paciente con el documento " + doc);
                    return;
                }
                pacientes.add(new Paciente(doc, nom, tel, fecha));
                Mensajes.exito("Paciente agregado");
            } else {
                paciente.setNombre(nom);
                paciente.setTelefono(tel);
                paciente.setFechaNacimiento(fecha);
                Mensajes.exito("Paciente actualizado");
            }

            ArchivoDatos.guardarPacientes(pacientes);
            actualizarGrid();
            dialogo.close();
        });

        Button cancelar = new Button("Cancelar", e -> dialogo.close());
        dialogo.getFooter().add(cancelar, guardar);

        dialogo.open();
    }

    // revisa los campos, si hay error devuelve el mensaje y si todo esta bien devuelve null
    private String validar(String doc, String nom, String tel, LocalDate fecha) {
        if (doc.isEmpty() || nom.isEmpty() || tel.isEmpty() || fecha == null) {
            return "Todos los campos son obligatorios";
        }
        if (!doc.matches("\\d{6,10}")) {
            return "El documento debe tener solo números (entre 6 y 10 dígitos)";
        }
        if (nom.length() < 3 || !nom.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
            return "El nombre solo puede tener letras y espacios";
        }
        if (!tel.matches("\\d{7,10}")) {
            return "El teléfono debe tener entre 7 y 10 números";
        }
        if (fecha.isAfter(LocalDate.now())) {
            return "La fecha de nacimiento no puede ser en el futuro";
        }
        if (fecha.getYear() < 1900) {
            return "La fecha de nacimiento no es válida";
        }
        return null;
    }

    private Paciente buscarPorDocumento(String doc) {
        for (Paciente p : pacientes) {
            if (p.getDocumento().equals(doc)) {
                return p;
            }
        }
        return null;
    }

    private void confirmarEliminar(Paciente p) {
        // no se deja borrar si tiene citas pendientes
        for (Cita c : ArchivoDatos.leerCitas()) {
            boolean esDelPaciente = c.getDocumentoPaciente().equals(p.getDocumento());
            boolean pendiente = !c.estaCancelada() && !c.getFecha().isBefore(LocalDate.now());
            if (esDelPaciente && pendiente) {
                Mensajes.error("No se puede eliminar: " + p.getNombre() + " tiene citas pendientes. Cancélalas primero.");
                return;
            }
        }

        ConfirmDialog confirmar = new ConfirmDialog();
        confirmar.setHeader("Eliminar paciente");
        confirmar.setText("¿Seguro que quieres eliminar a " + p.getNombre() + "? Esto no se puede deshacer.");
        confirmar.setCancelable(true);
        confirmar.setCancelText("Cancelar");
        confirmar.setConfirmText("Eliminar");
        confirmar.setConfirmButtonTheme("error primary");
        confirmar.addConfirmListener(e -> {
            pacientes.remove(p);
            ArchivoDatos.guardarPacientes(pacientes);
            actualizarGrid();
            Mensajes.exito("Paciente eliminado");
        });
        confirmar.open();
    }
}
