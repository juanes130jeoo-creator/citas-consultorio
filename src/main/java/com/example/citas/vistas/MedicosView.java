package com.example.citas.vistas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "medicos", layout = MainLayout.class)
@PageTitle("Médicos")
public class MedicosView extends VerticalLayout {

    private static final String TODAS = "Todas las especialidades";

    private List<Medico> medicos;

    private Grid<Medico> grid = new Grid<>(Medico.class, false);
    private TextField buscar = new TextField();
    private Select<String> filtroEspecialidad = new Select<>();

    public MedicosView() {
        medicos = ArchivoDatos.leerMedicos();

        H2 titulo = new H2("Médicos");

        buscar.setPlaceholder("Buscar por nombre");
        buscar.setWidth("250px");
        buscar.setPrefixComponent(VaadinIcon.SEARCH.create());
        buscar.setClearButtonVisible(true);
        buscar.setValueChangeMode(ValueChangeMode.EAGER);
        buscar.addValueChangeListener(e -> actualizarGrid());

        // Select para filtrar por especialidad (uno de los 3 componentes)
        List<String> opciones = new ArrayList<>();
        opciones.add(TODAS);
        for (String esp : Medico.ESPECIALIDADES) {
            opciones.add(esp);
        }
        filtroEspecialidad.setItems(opciones);
        filtroEspecialidad.setValue(TODAS);
        filtroEspecialidad.setWidth("230px");
        filtroEspecialidad.addValueChangeListener(e -> actualizarGrid());

        Button nuevo = new Button("Nuevo médico", VaadinIcon.PLUS.create());
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevo.addClickListener(e -> abrirFormulario(null));

        HorizontalLayout barra = new HorizontalLayout(buscar, filtroEspecialidad, nuevo);
        barra.getStyle().set("flex-wrap", "wrap");
        barra.setAlignItems(FlexComponent.Alignment.BASELINE);

        configurarGrid();

        add(titulo, barra, grid);
        setSizeFull();
        actualizarGrid();
    }

    private void configurarGrid() {
        grid.addColumn(Medico::getId).setHeader("Id").setAutoWidth(true);
        grid.addColumn(Medico::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Medico::getEspecialidad).setHeader("Especialidad").setAutoWidth(true);
        grid.addColumn(m -> contarCitasPendientes(m)).setHeader("Citas pendientes").setAutoWidth(true);

        grid.addComponentColumn(m -> {
            Button editar = new Button(VaadinIcon.EDIT.create());
            editar.addClickListener(e -> abrirFormulario(m));

            Button eliminar = new Button(VaadinIcon.TRASH.create());
            eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);
            eliminar.addClickListener(e -> confirmarEliminar(m));

            return new HorizontalLayout(editar, eliminar);
        }).setHeader("Acciones").setAutoWidth(true);

        grid.setSizeFull();
    }

    private void actualizarGrid() {
        String texto = buscar.getValue().trim().toLowerCase();
        String especialidad = filtroEspecialidad.getValue();

        List<Medico> filtrados = new ArrayList<>();
        for (Medico m : medicos) {
            boolean coincideNombre = m.getNombre().toLowerCase().contains(texto);
            boolean coincideEsp = especialidad == null || especialidad.equals(TODAS)
                    || m.getEspecialidad().equals(especialidad);
            if (coincideNombre && coincideEsp) {
                filtrados.add(m);
            }
        }
        grid.setItems(filtrados);
    }

    // cuenta las citas programadas de hoy en adelante
    private int contarCitasPendientes(Medico m) {
        int contador = 0;
        for (Cita c : ArchivoDatos.leerCitas()) {
            if (c.getIdMedico() == m.getId() && !c.estaCancelada() && !c.getFecha().isBefore(LocalDate.now())) {
                contador++;
            }
        }
        return contador;
    }

    private void abrirFormulario(Medico medico) {
        boolean esNuevo = medico == null;

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esNuevo ? "Nuevo médico" : "Editar médico");

        TextField nombre = new TextField("Nombre completo");
        Select<String> especialidad = new Select<>();
        especialidad.setLabel("Especialidad");
        especialidad.setItems(Medico.ESPECIALIDADES);
        especialidad.setPlaceholder("Seleccione...");

        if (!esNuevo) {
            nombre.setValue(medico.getNombre());
            especialidad.setValue(medico.getEspecialidad());
        }

        FormLayout formulario = new FormLayout(nombre, especialidad);
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        dialogo.add(formulario);

        Button guardar = new Button("Guardar");
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addClickListener(e -> {
            String nom = nombre.getValue().trim();
            String esp = especialidad.getValue();

            if (nom.isEmpty() || esp == null) {
                Mensajes.error("El nombre y la especialidad son obligatorios");
                return;
            }
            if (nom.length() < 3 || !nom.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+")) {
                Mensajes.error("El nombre solo puede tener letras y espacios");
                return;
            }

            if (esNuevo) {
                medicos.add(new Medico(ArchivoDatos.siguienteIdMedico(), nom, esp));
                Mensajes.exito("Médico agregado");
            } else {
                medico.setNombre(nom);
                medico.setEspecialidad(esp);
                Mensajes.exito("Médico actualizado");
            }

            ArchivoDatos.guardarMedicos(medicos);
            actualizarGrid();
            dialogo.close();
        });

        Button cancelar = new Button("Cancelar", e -> dialogo.close());
        dialogo.getFooter().add(cancelar, guardar);

        dialogo.open();
    }

    private void confirmarEliminar(Medico m) {
        if (contarCitasPendientes(m) > 0) {
            Mensajes.error("No se puede eliminar: el médico tiene citas pendientes. Cancélalas o reprográmalas primero.");
            return;
        }

        ConfirmDialog confirmar = new ConfirmDialog();
        confirmar.setHeader("Eliminar médico");
        confirmar.setText("¿Seguro que quieres eliminar a " + m.getNombre() + "?");
        confirmar.setCancelable(true);
        confirmar.setCancelText("Cancelar");
        confirmar.setConfirmText("Eliminar");
        confirmar.setConfirmButtonTheme("error primary");
        confirmar.addConfirmListener(e -> {
            medicos.remove(m);
            ArchivoDatos.guardarMedicos(medicos);
            actualizarGrid();
            Mensajes.exito("Médico eliminado");
        });
        confirmar.open();
    }
}
