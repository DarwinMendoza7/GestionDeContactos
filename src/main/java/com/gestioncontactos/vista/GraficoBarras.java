package com.gestioncontactos.vista;

import java.awt.*;
import javax.swing.JPanel;
import java.util.Map;
import java.util.LinkedHashMap;


public class GraficoBarras extends JPanel {
    private Map<String, Integer> datos;
    private String titulo;
    private Color[] colores = {
        new Color(65, 105, 225),   // Azul real (familia)
        new Color(34, 139, 34),    // Verde bosque (amigos)
        new Color(178, 34, 34),    // Rojo ladrillo (trabajo)
        new Color(255, 215, 0)     // Dorado (favoritos)
    };

    public GraficoBarras(Map<String, Integer> datos, String titulo) {
        this.datos = datos;
        this.titulo = titulo;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        int margen = 50;
        int espacioBarras = 20; // Espacio entre barras
        
        // Calcular dimensiones
        int numBarras = datos.size();
        int anchoBarra = (ancho - 2 * margen - (numBarras - 1) * espacioBarras) / numBarras;
        int maxValor = datos.values().stream().max(Integer::compare).orElse(1);
        if (maxValor == 0) maxValor = 1;

        // Dibujar título
        g2d.setFont(new Font("Tahoma", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int anchoTitulo = fm.stringWidth(titulo);
        g2d.drawString(titulo, (ancho - anchoTitulo) / 2, 30);

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(margen, alto - margen, ancho - margen, alto - margen); // Eje X
        g2d.drawLine(margen, margen, margen, alto - margen); // Eje Y

        // Dibujar barras y etiquetas
        int x = margen;
        int colorIndex = 0;
        
        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            String categoria = entry.getKey();
            int valor = entry.getValue();

            // Calcular altura de la barra
            int altoBarra = (int) (((alto - 2 * margen) * valor) / (double) maxValor);
            if (valor > 0 && altoBarra < 1) altoBarra = 1;

            // Dibujar barra
            g2d.setColor(colores[colorIndex % colores.length]);
            g2d.fillRect(x, alto - margen - altoBarra, anchoBarra, altoBarra);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, alto - margen - altoBarra, anchoBarra, altoBarra);

            // Dibujar valor encima de la barra
            g2d.setFont(new Font("Tahoma", Font.BOLD, 12));
            String valorStr = String.valueOf(valor);
            int anchoValor = g2d.getFontMetrics().stringWidth(valorStr);
            g2d.drawString(valorStr, x + (anchoBarra - anchoValor)/2, alto - margen - altoBarra - 5);

            // Dibujar etiqueta de categoría (horizontal)
            g2d.setFont(new Font("Tahoma", Font.PLAIN, 12));
            fm = g2d.getFontMetrics();
            int anchoTexto = fm.stringWidth(categoria);
            
            // Ajustar texto largo
            if (anchoTexto > anchoBarra) {
                categoria = categoria.substring(0, Math.min(5, categoria.length())) + ".";
                anchoTexto = fm.stringWidth(categoria);
            }
            
            g2d.drawString(categoria, x + (anchoBarra - anchoTexto)/2, alto - margen + 20);

            x += anchoBarra + espacioBarras;
            colorIndex++;
        }
    }
}