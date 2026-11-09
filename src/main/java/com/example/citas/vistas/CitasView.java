package com.example.citas.vistas;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Citas")
public class CitasView extends VerticalLayout {

    public CitasView() {
        add(new H2("Citas"));
        add(new Paragraph("Aquí van a ir las citas del consultorio."));
    }
}
