package com.example.citas.vistas;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "medicos", layout = MainLayout.class)
@PageTitle("Médicos")
public class MedicosView extends VerticalLayout {

    public MedicosView() {
        add(new H2("Médicos"));
        add(new Paragraph("Aquí va la lista de médicos."));
    }
}
