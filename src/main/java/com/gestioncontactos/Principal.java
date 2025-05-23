package com.gestioncontactos;

import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;  // Importa FlatLaf
import com.gestioncontactos.controlador.*;
import com.gestioncontactos.vista.*;

public class Principal {
    public static void main(String[] args) {
        // Opción para ejecutar solo pruebas desde línea de comandos
        if(args.length > 0 && args[0].equals("--test")) {
            ConsolePerformanceTester.runAllTests();
            System.exit(0);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Configurar look and feel con FlatLaf (tema claro)
                UIManager.setLookAndFeel(new FlatDarculaLaf());
                
                // Crear y mostrar ventana principal
                Ventana ventana = new Ventana();
                ventana.setVisible(true);
                ventana.setLocationRelativeTo(null);
                
                // Preguntar después de mostrar la interfaz (no bloquear el EDT)
                SwingUtilities.invokeLater(() -> askToRunTests());
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar la aplicación: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void askToRunTests() {
        int respuesta = JOptionPane.showConfirmDialog(null,
            "¿Desea ejecutar pruebas de rendimiento en segundo plano?\n"
            + "Los resultados aparecerán en la consola.",
            "Pruebas de Rendimiento",
            JOptionPane.YES_NO_OPTION);
        
        if(respuesta == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                ConsolePerformanceTester.runAllTests();
                JOptionPane.showMessageDialog(null,
                    "Pruebas de rendimiento completadas.\n"
                    + "Ver resultados en la consola.",
                    "Pruebas Finalizadas",
                    JOptionPane.INFORMATION_MESSAGE);
            }).start();
        }
    }
}
