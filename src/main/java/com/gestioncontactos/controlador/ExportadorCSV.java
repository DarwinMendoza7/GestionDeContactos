package com.gestioncontactos.controlador;

import com.gestioncontactos.modelo.Persona;
import javax.swing.SwingWorker;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/*Clase que exporta la lista de contactos a un archivo CSV en segundo plano
 * Esta clase extiende SwingWorker para realizar la exportación sin bloquear la interfaz gráfica, y utiliza un sistema de doble 
 * bloqueo para evitar conflictos de concurrencia*/
public class ExportadorCSV extends SwingWorker<Boolean, Integer> {
    private final List<Persona> contactos; //Lista de contactos a exportar
    private final Path archivoDestino; //Ruta del archivo CSV destino
    private final ReentrantLock lock; //Bloqueo para evitar múltiples exportaciones simultáneas
    private static final ReentrantLock fileLock = new ReentrantLock(); //Bloqueo a nivel de clase para el acceso al archivo
    
    //Constructor que recibe los contactos, la ruta destino y el lock de exportación
    public ExportadorCSV(List<Persona> contactos, Path archivoDestino, ReentrantLock lock) {
        this.contactos = contactos;
        this.archivoDestino = archivoDestino;
        this.lock = lock;
    }
    //Método principal que se ejecuta en segundo plano
    @Override
    protected Boolean doInBackground() throws Exception {
        lock.lock(); //Bloquea la opeeración de exportación
        fileLock.lock(); //Bloquea el acceso al archivo
        try {
            Files.createDirectories(archivoDestino.getParent());
            //Filtrar y convertir contactos a formato CSV
            List<String> lineas = contactos.stream()
                .filter(p -> !p.getNombre().equals("NOMBRE"))
                .map(this::convertirACSV)
                .collect(Collectors.toList());
            //Escribir al archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoDestino.toFile()))) {
                writer.write("Nombre,Telefono,Email,Categoria,Favorito\n");
                
                int total = lineas.size();
                for (int i = 0; i < total; i++) {
                    if (isCancelled()) return false; //Verificar si se canceló
                    
                    writer.write(lineas.get(i) + "\n");
                    
                    // Calcular y reportar progreso
                    int progreso = (i == total - 1) ? 100 : (i * 100) / total;
                    setProgress(progreso);
                }
            }
            return true;
        } finally {
        	//Liberar locks en orden inverso a su adquisición
            fileLock.unlock();
            lock.unlock();
        }
    }
    //Convierte un objeto Persona a una lista de formato CSV
    private String convertirACSV(Persona persona) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
            persona.getNombre(),
            persona.getTelefono(),
            persona.getEmail(),
            persona.getCategoria(),
            persona.isFavorito() ? "Sí" : "No");
    }
    //Procesa las actualizaciones de progreso.
    @Override
    protected void process(List<Integer> chunks) {
       
        if (!chunks.isEmpty()) {
            int lastProgress = chunks.get(chunks.size() - 1);
            
        }
    }

}