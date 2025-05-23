package com.gestioncontactos.vista;

import javax.swing.Icon;
//Clase simple para representar un elemento de una lista o combo que contiene tanto un texto xomo un ícono
public class IconComboItem {
  private String text; //Texto que se mostrará en el combo/lista
  private Icon icon; //Ïcono asociado a este elemento
  
  public IconComboItem(String text, Icon icon) {
      this.text = text;
      this.icon = icon;
  }
  
  public String getText() {
      return text;
  }
  
  public Icon getIcon() {
      return icon;
  }
  
  @Override
  public String toString() {
      return text;
  }
}