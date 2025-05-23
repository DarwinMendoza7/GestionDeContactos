package com.gestioncontactos.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestioncontactos.modelo.Persona;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Guarda la lista de contactos en un archivo JSON.
     * @param contactos Lista de objetos Persona a guardar.
     * @param rutaArchivo Ruta completa del archivo JSON.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static void guardarContactosEnArchivo(List<Persona> contactos, String rutaArchivo) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(rutaArchivo), contactos);
    }

    /**
     * Lee la lista de contactos desde un archivo JSON.
     * @param rutaArchivo Ruta completa del archivo JSON.
     * @return Lista de objetos Persona leídos del archivo.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public static List<Persona> leerContactosDesdeArchivo(String rutaArchivo) throws IOException {
        return mapper.readValue(new File(rutaArchivo), new TypeReference<List<Persona>>() {});
    }
}
