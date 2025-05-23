package com.gestioncontactos.util;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;

import java.awt.Color;
import java.io.InputStream;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Gestiona los temas de FlatLaf disponibles en la aplicación
 */
public class ThemeManager {
    
    private static Theme currentTheme = Theme.ORIGINAL; // Tema original por defecto
    
    public enum Theme {
        ORIGINAL("Original", null),  // Tema personalizado original
        LIGHT("Light", FlatLightLaf.class),
        DARK("Dark", FlatDarkLaf.class),
        INTELLIJ("IntelliJ", FlatIntelliJLaf.class),
        DARCULA("Darcula", FlatDarculaLaf.class);
        
        private final String name;
        private final Class<?> lafClass;
        
        Theme(String name, Class<?> lafClass) {
            this.name = name;
            this.lafClass = lafClass;
        }
        
        public String getName() {
            return name;
        }
        
        public Class<?> getLafClass() {
            return lafClass;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Aplica el tema FlatLaf seleccionado
     * 
     * @param theme El tema a aplicar
     * @return true si el tema se aplicó correctamente, false en caso contrario
     */
    public static boolean applyTheme(Theme theme) {
        try {
            //Actualizar el tema actual
            currentTheme = theme;
            
            if (theme == Theme.ORIGINAL) {
                return applyOriginalTheme();
            }
            
            // Crear una instancia del Look and Feel
            UIManager.LookAndFeelInfo info = new UIManager.LookAndFeelInfo(
                    theme.getName(), theme.getLafClass().getName());
            
            // Instanciar y establecer el Look and Feel
            UIManager.setLookAndFeel(theme.getLafClass().getName());
            
            // Ajustar colores específicos según el tema
            adjustSpecificColors(theme);
            
            return true;
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Método de conveniencia para aplicar directamente un tema de FlatLaf
     * 
     * @param theme El tema FlatLaf a aplicar
     * @return true si tuvo éxito, false si falló
     */
    public static boolean setFlatLafTheme(Theme theme) {
        try {
            //Actualizar el tema actual
            currentTheme = theme;
            
            // Si es el tema original, usar configuración específica
            if (theme == Theme.ORIGINAL) {
                return applyOriginalTheme();
            }
            
            // Establecer el Look and Feel utilizando los métodos propios de FlatLaf
            switch (theme) {
                case LIGHT:
                    FlatLightLaf.setup();
                    break;
                case DARK:
                    FlatDarkLaf.setup();
                    break;
                case INTELLIJ:
                    FlatIntelliJLaf.setup();
                    break;
                case DARCULA:
                    FlatDarculaLaf.setup();
                    break;
            }
            
            // Ajustar colores específicos según el tema
            adjustSpecificColors(theme);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Aplica el tema original personalizado de la aplicación
     */
    private static boolean applyOriginalTheme() {
        try {
            // Configurar el tema base de FlatLightLaf como punto de partida
            FlatLightLaf.setup();
            
            // Personalizar los colores para el tema original
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            
            // Colores básicos del tema original
            defaults.put("Panel.background", new Color(245, 245, 245));
            defaults.put("Label.foreground", new Color(50, 50, 50));
            defaults.put("Button.default.focusColor", new Color(70, 130, 180)); // Azul más oscuro
            defaults.put("TabbedPane.selectedBackground", new Color(70, 130, 180));
            defaults.put("TabbedPane.selectedForeground", Color.WHITE);
            defaults.put("TabbedPane.foreground", Color.BLACK);
            
            // Colores para componentes específicos
            defaults.put("TextField.background", Color.WHITE);
            defaults.put("TextField.foreground", Color.BLACK);
            defaults.put("ComboBox.background", new Color(70, 130, 180));
            defaults.put("ComboBox.foreground", Color.WHITE);
            defaults.put("Button.background", new Color(70, 130, 180));
            defaults.put("Button.foreground", Color.WHITE);
            
            // Colores para los bordes
            defaults.put("Component.borderColor", new Color(200, 200, 200));
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ajusta colores específicos según el tema seleccionado
     */
    private static void adjustSpecificColors(Theme theme) {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        
        if (theme == Theme.ORIGINAL) {
            // Configuración específica para el tema ORIGINAL
            defaults.put("TabbedPane.foreground", Color.BLACK);
            defaults.put("TabbedPane.selectedForeground", Color.WHITE);
            defaults.put("Label.searchForeground", Color.WHITE);
        } else if (isLightTheme(theme)) {
            // Ajustes para otros temas claros (LIGHT, INTELLIJ)
            defaults.put("TabbedPane.foreground", Color.BLACK);
            defaults.put("TabbedPane.selectedForeground", Color.BLACK);
            defaults.put("Label.searchForeground", Color.BLACK);
        } else {
            // Ajustes para temas oscuros (DARK, DARCULA)
            defaults.put("TabbedPane.foreground", Color.WHITE);
            defaults.put("TabbedPane.selectedForeground", Color.WHITE);
            defaults.put("Label.searchForeground", Color.WHITE);
        }
    }
    
    /**
     * Devuelve el tema actual que está en uso
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Determina si el tema específico es un tema claro o oscuro
     * 
     * @param theme El tema a comprobar
     * @return true si el tema es claro (LIGHT, INTELLIJ o ORIGINAL), false si es oscuro
     */
    public static boolean isLightTheme(Theme theme) {
        return theme == Theme.LIGHT || theme == Theme.INTELLIJ || theme == Theme.ORIGINAL;
    }
    
    /**
     * Determina si el tema actual es un tema claro o oscuro
     * 
     * @return true si el tema es claro (LIGHT, INTELLIJ o ORIGINAL), false si es oscuro
     */
    public static boolean isLightTheme() {
        return isLightTheme(getCurrentTheme());
    }
}