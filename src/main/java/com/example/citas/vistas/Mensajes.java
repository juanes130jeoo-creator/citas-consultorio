package com.example.citas.vistas;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

// para no repetir el codigo de las notificaciones en todas las vistas
public class Mensajes {

    public static void exito(String texto) {
        Notification n = Notification.show(texto, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String texto) {
        Notification n = Notification.show(texto, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
