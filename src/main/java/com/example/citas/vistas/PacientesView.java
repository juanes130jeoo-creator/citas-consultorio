package com.example.citas.vistas;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "pacientes", layout = MainLayout.class)
@PageTitle("Pacientes")
public class PacientesView extends VerticalLayout {

    public PacientesView() {
        add(new H2("Pacientes"));
        add(new Paragraph("Aquí va la lista de pacientes."));
    }
}
