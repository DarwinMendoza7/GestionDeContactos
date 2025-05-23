package com.gestioncontactos.util;

import javax.swing.*;
import java.util.concurrent.*;
//Clase singleton encargada de mostrar notificaciones en la interfaz gráfica de forma segura y ordenada
public class Notificador {
    private static Notificador instancia; //Instancia única
    private final BlockingQueue<String> colaMensajes = new LinkedBlockingQueue<>(); //Cola de mensajes para notificar (hilo seguro)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); //Scheduler para cerra notificaciones automáticamente

    //Constructor privado. Inicia el hilo consumidor de mensajes
    private Notificador() {
        // Hilo consumidor de mensajes
        new Thread(() -> {
            while (true) {
                try {
                    String mensaje = colaMensajes.take(); //Espera hasta que haya un mensaje
                    mostrarNotificacion(mensaje);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                    break; //Sale del bucle si el hilo es interrumpido
                }
            }
        }).start();
    }
    // Obtiene la instancia única del Notificador
    public static synchronized Notificador getInstancia() {
        if (instancia == null) {
            instancia = new Notificador();
        }
        return instancia;
    }
    //Muestra una notificación en la interfaz gráfica
    private void mostrarNotificacion(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.INFORMATION_MESSAGE);
            JDialog dialog = optionPane.createDialog("Notificación");
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
            
            // Cerrar automáticamente después de 3 segundos
            scheduler.schedule(() -> SwingUtilities.invokeLater(dialog::dispose), 3, TimeUnit.SECONDS);
        });
    }
    //Agrega un mensaje a la cola para ser mostrado como notificación
    public void encolarMensaje(String mensaje) {
        colaMensajes.offer(mensaje);
    }
    //Agrega el scheduler de cierre automático de notificaciones
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
