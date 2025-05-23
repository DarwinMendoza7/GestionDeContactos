package com.gestioncontactos.vista;

import javax.swing.*;

import java.awt.Color;
import java.awt.Component;
//Renderizador personalizado para listas y combos que permite mostrar tanto un texto como un ícono en cada elemento
public class IconListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        //Llama al método base para obtener el JLabel configurado por defecto
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        //Si el valor es un IconComboItem, personaliza el texto y el ícono
        if (value instanceof IconComboItem) {
            IconComboItem item = (IconComboItem) value;
            label.setText(item.getText());
            label.setIcon(item.getIcon());
        }
        
        return label;
    }
}