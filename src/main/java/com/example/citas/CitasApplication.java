package com.example.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.citas.datos.ArchivoDatos;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;

// el tema Lumo hay que ponerlo aca para que los componentes se vean bien
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
public class CitasApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		// la primera vez crea los .txt con datos de ejemplo
		ArchivoDatos.crearDatosDeEjemplo();
		SpringApplication.run(CitasApplication.class, args);
	}

}
