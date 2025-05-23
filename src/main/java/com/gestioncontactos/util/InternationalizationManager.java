package com.gestioncontactos.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class InternationalizationManager {
    private static ResourceBundle bundle;
    private static Locale currentLocale;
    static {
        try {
            // 1. Verifica que los recursos existen
            String resourceName = "/messages_es.properties";
            if (InternationalizationManager.class.getResource(resourceName) == null) {
                throw new RuntimeException("Archivo de recursos no encontrado: " + resourceName);
            }
            
            // 2. Configuración segura
            currentLocale = new Locale("es");
            bundle = ResourceBundle.getBundle("messages", currentLocale,
                InternationalizationManager.class.getClassLoader());
            
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO: " + e.getMessage());
            // Fallback seguro
            bundle = new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key) {
                    return "[" + key + "]"; // Marcador visible para keys faltantes
                }
                @Override
                public java.util.Enumeration<String> getKeys() {
                    return java.util.Collections.emptyEnumeration();
                }
            };
        }
    }
    
    //Cambia el idioma de las traducciones en tiempo de ejecución
    public static void setLocale(Locale locale) {
        try {
            currentLocale = locale;  
            bundle = ResourceBundle.getBundle("messages", locale,
                InternationalizationManager.class.getClassLoader());
        } catch (Exception e) {
            System.err.println("Error cambiando idioma: " + e.getMessage());
        }
    }

    //obtiene la traducción para la key especificada o devuelve marcador si no existe
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            System.err.println("Traducción faltante: " + key);
            return "{" + key + "}"; // Marcador visible
        }
    }

    //Obtiene el locale actualmente configurado
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}