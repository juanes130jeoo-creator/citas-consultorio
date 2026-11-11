package com.example.citas.vistas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.citas.datos.ArchivoDatos;
import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.example.citas.modelo.Paciente;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

// Layout principal con el menu de la izquierda
public class MainLayout extends AppLayout {

    public MainLayout() {
        DrawerToggle boton = new DrawerToggle();

        H1 titulo = new H1("Consultorio Médico");
        titulo.getStyle().set("font-size", "1.3em");
        titulo.getStyle().set("margin", "0");

        addToNavbar(boton, titulo);

        // menu con las vistas
        SideNav menu = new SideNav();
        menu.addItem(new SideNavItem("Citas", CitasView.class, VaadinIcon.CALENDAR.create()));
        menu.addItem(new SideNavItem("Pacientes", PacientesView.class, VaadinIcon.USERS.create()));
        menu.addItem(new SideNavItem("Médicos", MedicosView.class, VaadinIcon.DOCTOR.create()));

        addToDrawer(menu);
        setPrimarySection(Section.DRAWER);

        // el layout se crea una vez cuando se abre la app, por eso el aviso va aca
        mostrarCitasDeHoy();
    }

    private void mostrarCitasDeHoy() {
        Map<String, String> nombresPacientes = new HashMap<>();
        for (Paciente p : ArchivoDatos.leerPacientes()) {
            nombresPacientes.put(p.getDocumento(), p.getNombre());
        }
        Map<Integer, String> nombresMedicos = new HashMap<>();
        for (Medico m : ArchivoDatos.leerMedicos()) {
            nombresMedicos.put(m.getId(), m.getNombre());
        }

        // solo las programadas de hoy, ordenadas por hora
        List<Cita> deHoy = new ArrayList<>();
        for (Cita c : ArchivoDatos.leerCitas()) {
            if (c.getFecha().equals(LocalDate.now()) && !c.estaCancelada()) {
                deHoy.add(c);
            }
        }
        deHoy.sort(Comparator.comparing(Cita::getHora));

        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(false);
        contenido.setSpacing(false);

        if (deHoy.isEmpty()) {
            contenido.add(new Span("No hay citas programadas para hoy"));
        } else {
            Span encabezado = new Span("Citas de hoy (" + deHoy.size() + "):");
            encabezado.getStyle().set("font-weight", "bold");
            contenido.add(encabezado);
            for (Cita c : deHoy) {
                String paciente = nombresPacientes.getOrDefault(c.getDocumentoPaciente(), "?");
                String medico = nombresMedicos.getOrDefault(c.getIdMedico(), "?");
                contenido.add(new Span(c.getHora() + " - " + paciente + " con " + medico));
            }
        }

        Notification aviso = new Notification();
        aviso.setPosition(Notification.Position.TOP_END);
        aviso.setDuration(10000);
        aviso.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

        Button cerrar = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> aviso.close());
        cerrar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        cerrar.getStyle().set("color", "white");

        HorizontalLayout fila = new HorizontalLayout(contenido, cerrar);
        fila.setAlignItems(FlexComponent.Alignment.START);
        aviso.add(fila);
        aviso.open();
    }
}
