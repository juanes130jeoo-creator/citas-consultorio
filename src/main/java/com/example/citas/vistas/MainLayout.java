package com.example.citas.vistas;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    }
}
