package com.gestioncontactos.vista;

import java.awt.Color;

//Clase que define los colores personalizados para la interfaz gráfica
public class Colores {
    // Colores básicos
    public static Color FONDO = new Color(245, 245, 245);
    public static Color FONDO_GENERAL = new Color(235, 235, 235);
    public static Color PANEL_FORMULARIO = new Color(240, 240, 240);
    public static Color TEXTO_PRINCIPAL = new Color(50, 50, 50);
    public static Color TEXTO_SECUNDARIO = new Color(255, 255, 255);
    public static Color BORDE = new Color(200, 200, 200);
    
    // Colores para componentes específicos
    public static Color BOTON_PRIMARIO = new Color(70, 130, 180); // Steel Blue
    public static Color BOTON_TEXTO = new Color(255, 255, 255);
    public static Color BARRA_TITULO = new Color(100, 150, 200);
    public static Color CABECERA_TABLA = new Color(70, 130, 180);
    public static Color TEXTO_CABECERA = new Color(255, 255, 255);
    
    /**
     * Actualiza los colores según el tema actual
     * @param isDarkTheme true si el tema es oscuro, false si es claro
     */
    public static void actualizarColoresTema(boolean isDarkTheme) {
        if (isDarkTheme) {
            // Tema oscuro
            FONDO = new Color(40, 40, 40);
            FONDO_GENERAL = new Color(50, 50, 50);
            PANEL_FORMULARIO = new Color(60, 60, 60);
            TEXTO_PRINCIPAL = new Color(220, 220, 220);
            TEXTO_SECUNDARIO = new Color(220, 220, 220);
            BORDE = new Color(100, 100, 100);
            
            BOTON_PRIMARIO = new Color(60, 100, 140); // Versión más oscura del azul
            BOTON_TEXTO = new Color(255, 255, 255);
            BARRA_TITULO = new Color(70, 70, 70);
            CABECERA_TABLA = new Color(60, 100, 140);
            TEXTO_CABECERA = new Color(255, 255, 255);
        } else {
            // Tema claro (original)
            FONDO = new Color(245, 245, 245);
            FONDO_GENERAL = new Color(235, 235, 235);
            PANEL_FORMULARIO = new Color(240, 240, 240);
            TEXTO_PRINCIPAL = new Color(50, 50, 50);
            TEXTO_SECUNDARIO = new Color(255, 255, 255);
            BORDE = new Color(200, 200, 200);
            
            BOTON_PRIMARIO = new Color(70, 130, 180); // Steel Blue original
            BOTON_TEXTO = new Color(255, 255, 255);
            BARRA_TITULO = new Color(100, 150, 200);
            CABECERA_TABLA = new Color(70, 130, 180);
            TEXTO_CABECERA = new Color(255, 255, 255);
        }
    }
}