package com.gestioncontactos.modelo;

//Definición de la clase pública Persona
public class Persona {
	//Declaración de variables privadas de la clase Persona
  private String nombre, telefono, email, categoria;
  private boolean favorito;
  
  //Constructor público de la clase Persona
  public Persona() {
      super();   
      //Inicializa las variables
      this.nombre = "";
      this.telefono = "";
      this.email = "";
      this.categoria = "";
      this.favorito = false;
  }    
  
  //Constructor público de la clase "Persona" que inicializa todos los campos
  public Persona(String nombre, String telefono, String email, String categoria, boolean favorito) {
      super();  
      //Inicializa las variables con los valores enviados por los argumentos
      this.nombre = nombre;
      this.telefono = telefono;
      this.email = email;
      this.categoria = categoria;
      this.favorito = favorito;
  }
      
  //Getters y setters
  public String getNombre() {
      return nombre; 
  }
  
  public void setNombre(String nombre) {
      this.nombre = nombre; 
  }
  
  public String getTelefono() {
      return telefono; 
  }
      
  public void setTelefono(String telefono) {
      this.telefono = telefono; 
  }
      
  public String getEmail() {
      return email; 
  }
      
  public void setEmail(String email) {
      this.email = email; 
  }
      
  public String getCategoria() {
      return categoria; 
  }
          
  public void setCategoria(String categoria) {
      this.categoria = categoria;
  }
      
  public boolean isFavorito() {
      return favorito; 
  }
      
  public void setFavorito(boolean favorito) {
      this.favorito = favorito; 
  }
    
  // Método para proveer un formato para almacenar en un archivo
  public String datosContacto() {
  	
  	// Estructurar el siguiente formato: nombre;telefono;email;categoria;favorito
  	// Por ejemplo: Daniela Poma;097145478;dpoma2024@gmail.com;amigo;true
      String contacto = String.format("%s;%s;%s;%s;%s", nombre, telefono, email, categoria, favorito);
      return contacto; //Retorna la cadena formateada
  }
  
  //Método para proveer el formato de los campos que se van a imprimir en la lista
  public String formatoLista() {
      String contacto = String.format("%-40s%-40s%-40s%-40s", nombre, telefono, email, categoria);
      return contacto;
  }
  
  //Convierte los atributos del objeto actual en un arreglo de objetos compatible con JTable
  public Object[] toTableRow() {
      return new Object[]{nombre, telefono, email, categoria, favorito};
  }
  
  public String toCSVLine() {
      return String.format("%s,%s,%s,%s,%s", nombre, telefono, email, categoria, favorito);
  }
}