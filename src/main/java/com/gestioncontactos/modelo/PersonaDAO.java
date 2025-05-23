package com.gestioncontactos.modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.gestioncontactos.util.JsonUtil;

//Definición de la clase pública "PersonaDAO"
public class PersonaDAO {
    
	//Declaración de atributos privada de la clase "PersonaDAO"
    private File archivo; 
    private Persona persona; 
    private List<Persona> contactos;
       
    //Constructor público de la clase "PersonaDAO" que recibe un objeto "Persona" como parámetro
    public PersonaDAO(Persona persona) {
        this.persona = persona;
        archivo = new File("c:/gestionContactos/datosContactos.csv");
        contactos = new ArrayList<>();
        prepararArchivo();
    }
    
    //Método privado para gestionar el archivo utilizando la clase File
    private void prepararArchivo() {
        try {
            if(!archivo.getParentFile().exists()) archivo.getParentFile().mkdirs();
            
            if(!archivo.exists()) {
                archivo.createNewFile();
                escribir("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void escribir(String texto) {
    	//Prepara el archivo para escribir en la última línea
        FileWriter escribir;
        try {
            escribir = new FileWriter(archivo.getAbsolutePath(), true);
            escribir.write(texto + "\n"); //Escribe los datos del contacto en el archivo
            escribir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }
    
    //Método publico para escribir en el archivo
    public boolean escribirArchivo() {
        try {        
            escribir(persona.datosContacto());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
     
    //Método público para leer los datos del archivo
    public List<Persona> leerArchivo() throws IOException {
        List<Persona> personas = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo.getAbsolutePath()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    String[] partes = linea.split(";");
                    if (partes.length >= 5) {
                        Persona p = new Persona();
                        p.setNombre(partes[0]);
                        p.setTelefono(partes[1]);
                        p.setEmail(partes[2]);
                        p.setCategoria(partes[3]);
                        p.setFavorito(Boolean.parseBoolean(partes[4]));
                        personas.add(p);
                    }
                }
            }
        }
        
        return personas;
    }
    //Método público para guardar los contactos modificados o eliminados
    public void actualizarContactos(List<Persona> personas) throws IOException {
        archivo.delete();
        prepararArchivo();
        for (Persona p : personas) {
            if (!p.getNombre().equals("NOMBRE")) {  // Skip header
                new PersonaDAO(p).escribirArchivo();
            }
        }
    }
    
    //Método que exporta los contactos a un archivo CSV en la ruta especificada
    public boolean exportarCSV(String ruta) {
        try {
            // Verificar que el directorio padre exista
            File exportFile = new File(ruta);
            File parentDir = exportFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            List<Persona> contactos = leerArchivo();
            FileWriter fw = new FileWriter(exportFile);
            
            // Escribe cabecera
            fw.write("NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO\n");
            
            // Escribe datos
            for (Persona p : contactos) {
                if (!p.getNombre().equals("NOMBRE")) {  
                    fw.write(p.datosContacto() + "\n");
                }
            }
            
            fw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Cuenta los contactos pertenecientes a una categoría específica, excluyendo registros con nombre "NOMBRE".
    public int getCantidadPorCategoria(String categoria) {
        try {
            List<Persona> contactos = leerArchivo();
            int count = 0;
            for (Persona p : contactos) {
                if (!p.getNombre().equals("NOMBRE") && p.getCategoria().equals(categoria)) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    //Cuenta la cantidad de contactos marcados como favoritos, excluyendo registros con nombre "NOMBRE".
    public int getCantidadFavoritos() {
        try {
            List<Persona> contactos = leerArchivo();
            int count = 0;
            for (Persona p : contactos) {
                if (!p.getNombre().equals("NOMBRE") && p.isFavorito()) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public boolean guardarContactosJson(List<Persona> contactos, String rutaArchivo) {
        try {
            JsonUtil.guardarContactosEnArchivo(contactos, rutaArchivo);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Persona> leerContactosJson(String rutaArchivo) {
        try {
            return JsonUtil.leerContactosDesdeArchivo(rutaArchivo);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}