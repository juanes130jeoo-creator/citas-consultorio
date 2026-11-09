package com.example.citas.datos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.citas.modelo.Cita;
import com.example.citas.modelo.Medico;
import com.example.citas.modelo.Paciente;

// Clase que lee y escribe los archivos .txt
// cada tipo de dato tiene su archivo y los campos van separados por |
public class ArchivoDatos {

    private static final String CARPETA = "datos";
    private static final String ARCHIVO_PACIENTES = CARPETA + "/pacientes.txt";
    private static final String ARCHIVO_MEDICOS = CARPETA + "/medicos.txt";
    private static final String ARCHIVO_CITAS = CARPETA + "/citas.txt";

    // ---------------- PACIENTES ----------------

    public static List<Paciente> leerPacientes() {
        List<Paciente> pacientes = new ArrayList<>();
        for (String linea : leerLineas(ARCHIVO_PACIENTES)) {
            try {
                String[] partes = linea.split("\\|", -1);
                LocalDate nacimiento = LocalDate.parse(partes[3]);
                pacientes.add(new Paciente(partes[0], partes[1], partes[2], nacimiento));
            } catch (Exception e) {
                // si la linea esta mala no se cae, solo la salta
                System.out.println("Linea mala en pacientes.txt: " + linea);
            }
        }
        return pacientes;
    }

    public static void guardarPacientes(List<Paciente> pacientes) {
        List<String> lineas = new ArrayList<>();
        for (Paciente p : pacientes) {
            lineas.add(p.toLinea());
        }
        escribirLineas(ARCHIVO_PACIENTES, lineas);
    }

    // ---------------- MEDICOS ----------------

    public static List<Medico> leerMedicos() {
        List<Medico> medicos = new ArrayList<>();
        for (String linea : leerLineas(ARCHIVO_MEDICOS)) {
            try {
                String[] partes = linea.split("\\|", -1);
                int id = Integer.parseInt(partes[0]);
                medicos.add(new Medico(id, partes[1], partes[2]));
            } catch (Exception e) {
                System.out.println("Linea mala en medicos.txt: " + linea);
            }
        }
        return medicos;
    }

    public static void guardarMedicos(List<Medico> medicos) {
        List<String> lineas = new ArrayList<>();
        for (Medico m : medicos) {
            lineas.add(m.toLinea());
        }
        escribirLineas(ARCHIVO_MEDICOS, lineas);
    }

    public static int siguienteIdMedico() {
        int mayor = 0;
        for (Medico m : leerMedicos()) {
            if (m.getId() > mayor) {
                mayor = m.getId();
            }
        }
        return mayor + 1;
    }

    // ---------------- CITAS ----------------

    public static List<Cita> leerCitas() {
        List<Cita> citas = new ArrayList<>();
        for (String linea : leerLineas(ARCHIVO_CITAS)) {
            try {
                String[] partes = linea.split("\\|", -1);
                int id = Integer.parseInt(partes[0]);
                int idMedico = Integer.parseInt(partes[2]);
                LocalDate fecha = LocalDate.parse(partes[3]);
                LocalTime hora = LocalTime.parse(partes[4]);
                citas.add(new Cita(id, partes[1], idMedico, fecha, hora, partes[5], partes[6]));
            } catch (Exception e) {
                System.out.println("Linea mala en citas.txt: " + linea);
            }
        }
        return citas;
    }

    public static void guardarCitas(List<Cita> citas) {
        List<String> lineas = new ArrayList<>();
        for (Cita c : citas) {
            lineas.add(c.toLinea());
        }
        escribirLineas(ARCHIVO_CITAS, lineas);
    }

    public static int siguienteIdCita() {
        int mayor = 0;
        for (Cita c : leerCitas()) {
            if (c.getId() > mayor) {
                mayor = c.getId();
            }
        }
        return mayor + 1;
    }

    // ---------------- METODOS DE APOYO ----------------

    private static List<String> leerLineas(String ruta) {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            // si el archivo no existe se devuelve la lista vacia
            System.out.println("No se pudo leer " + ruta + ": " + e.getMessage());
        }
        return lineas;
    }

    private static void escribirLineas(String ruta, List<String> lineas) {
        new File(CARPETA).mkdirs();
        try (PrintWriter escritor = new PrintWriter(new FileWriter(ruta, StandardCharsets.UTF_8))) {
            for (String linea : lineas) {
                escritor.println(linea);
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar " + ruta + ": " + e.getMessage());
        }
    }

    // ---------------- DATOS DE EJEMPLO ----------------

    // se llama al iniciar la app, si no existen los archivos los crea con datos de prueba
    public static void crearDatosDeEjemplo() {
        if (!new File(ARCHIVO_MEDICOS).exists()) {
            List<Medico> medicos = new ArrayList<>();
            medicos.add(new Medico(1, "Ana María Restrepo", "Medicina general"));
            medicos.add(new Medico(2, "Carlos Gómez", "Pediatría"));
            medicos.add(new Medico(3, "Laura Jiménez", "Odontología"));
            medicos.add(new Medico(4, "Jorge Ramírez", "Medicina general"));
            medicos.add(new Medico(5, "Sofía Herrera", "Dermatología"));
            guardarMedicos(medicos);
        }

        if (!new File(ARCHIVO_PACIENTES).exists()) {
            List<Paciente> pacientes = new ArrayList<>();
            pacientes.add(new Paciente("1001234567", "Juan Pérez", "3001234567", LocalDate.of(1990, 5, 12)));
            pacientes.add(new Paciente("1019876543", "María López", "3109876543", LocalDate.of(1985, 11, 3)));
            pacientes.add(new Paciente("1032456789", "Andrés Castro", "3204567890", LocalDate.of(2001, 2, 20)));
            pacientes.add(new Paciente("1045678912", "Valentina Ruiz", "3157894561", LocalDate.of(2015, 8, 9)));
            pacientes.add(new Paciente("71234567", "Luis Fernando Ortiz", "3012345678", LocalDate.of(1962, 1, 30)));
            pacientes.add(new Paciente("43567890", "Gloria Patiño", "3126549870", LocalDate.of(1970, 7, 15)));
            pacientes.add(new Paciente("1098765432", "Samuel Torres", "3185552233", LocalDate.of(2019, 4, 2)));
            guardarPacientes(pacientes);
        }

        if (!new File(ARCHIVO_CITAS).exists()) {
            // las fechas se ponen a partir de hoy para que siempre haya citas de hoy y proximas
            LocalDate hoy = LocalDate.now();
            List<Cita> citas = new ArrayList<>();
            citas.add(new Cita(1, "1001234567", 1, hoy, LocalTime.of(8, 0), "Control general", Cita.PROGRAMADA));
            citas.add(new Cita(2, "1045678912", 2, hoy, LocalTime.of(10, 30), "Fiebre", Cita.PROGRAMADA));
            citas.add(new Cita(3, "43567890", 3, hoy, LocalTime.of(15, 0), "Limpieza", Cita.PROGRAMADA));
            citas.add(new Cita(4, "1019876543", 5, hoy.plusDays(1), LocalTime.of(9, 0), "Revisión de lunar", Cita.PROGRAMADA));
            citas.add(new Cita(5, "71234567", 4, hoy.plusDays(2), LocalTime.of(7, 30), "Exámenes de sangre", Cita.PROGRAMADA));
            citas.add(new Cita(6, "1098765432", 2, hoy.plusDays(3), LocalTime.of(11, 0), "Vacunas", Cita.PROGRAMADA));
            citas.add(new Cita(7, "1032456789", 3, hoy.plusDays(5), LocalTime.of(14, 30), "Dolor de muela", Cita.PROGRAMADA));
            citas.add(new Cita(8, "1001234567", 1, hoy.minusDays(7), LocalTime.of(9, 30), "Dolor de cabeza", Cita.PROGRAMADA));
            citas.add(new Cita(9, "1019876543", 4, hoy.minusDays(3), LocalTime.of(16, 0), "Gripa", Cita.PROGRAMADA));
            citas.add(new Cita(10, "43567890", 1, hoy.minusDays(1), LocalTime.of(8, 30), "Control de presión", Cita.CANCELADA));
            guardarCitas(citas);
        }
    }
}
