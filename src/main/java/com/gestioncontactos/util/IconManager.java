package com.gestioncontactos.util;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class IconManager {
    
    private static final String LIGHT_ICONS_PREFIX = "/icons/light/";
    private static final String DARK_ICONS_PREFIX = "/icons/dark/";
    
    public static ImageIcon loadThemedIcon(String iconName, int width, int height) {
        boolean isLight = ThemeManager.isLightTheme();
        String prefix = isLight ? LIGHT_ICONS_PREFIX : DARK_ICONS_PREFIX;
        
        // Intentar cargar el icono de la carpeta específica del tema
        URL url = IconManager.class.getResource(prefix + iconName);
        
        // Si no existe en la carpeta del tema específico, intentar con el icono normal
        if (url == null) {
            url = IconManager.class.getResource("/icons/" + iconName);
        }
        
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("Imagen no encontrada: " + iconName + " (tema: " + (isLight ? "claro" : "oscuro") + ")");
            return null;
        }
    }
    
    // Método para actualizar un icono según el tema actual
    public static ImageIcon updateIconForTheme(String iconName, int width, int height) {
        return loadThemedIcon(iconName, width, height);
    }
}