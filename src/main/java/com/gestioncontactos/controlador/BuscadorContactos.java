package com.gestioncontactos.controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.swing.SwingWorker;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import com.gestioncontactos.modelo.Persona;
import com.gestioncontactos.util.InternationalizationManager;

//Clase que realiza la búsqueda de contactos en segundo plano y actualiza la tabla de resultados
public class BuscadorContactos extends SwingWorker<List<Persona>, Integer> {
    private final List<Persona> contactos; //Lista completa de contactos a filtrar
    private final String textoBusqueda; //Texto de búsqueda en minúsculas
    private final JTable tablaContactos; //Referencia a la tabla de la UI para mostrar resultados
    private final DefaultTableModel modeloTabla; //Modelo de la tabla para manipular filas
    private final JProgressBar progressBar; // Barra de progreso para mostrar avance de la búsqueda

    public BuscadorContactos(List<Persona> contactos, String textoBusqueda, 
                           JTable tablaContactos, DefaultTableModel modeloTabla,
                           JProgressBar progressBar) { // Nuevo parámetro
        this.contactos = contactos;
        this.textoBusqueda = textoBusqueda.toLowerCase();
        this.tablaContactos = tablaContactos;
        this.modeloTabla = modeloTabla;
        this.progressBar = progressBar; // Inicializamos la barra de progreso
    }

    //Método principal que ejecuta la búsqueda en segundo plano
    @Override
    protected List<Persona> doInBackground() throws Exception {
        List<Persona> resultados = new ArrayList<>();
        int total = contactos.size();
        int progreso = 0;
        //Si el texto de búsqueda está vacío, devolver todos los contactos
        if (textoBusqueda.trim().isEmpty()) {
            publish(100);
            return contactos;
        }

        //Buscar coincidencias y actualizar progreso
        for (Persona contacto : contactos) {
            if (isCancelled()) {
                break; //Detener búsqueda si fue cancelada
            }
            
            progreso = (int)((++progreso * 100.0) / total);
            publish(progreso); //Actualizar barra de progreso
            //Comparar nombre, teléfono, email y categoría (ignorando mayúsculas/minúsculas)
            if (contacto.getNombre().toLowerCase().contains(textoBusqueda) ||
                contacto.getTelefono().contains(textoBusqueda) ||
                contacto.getEmail().toLowerCase().contains(textoBusqueda) ||
                contacto.getCategoria().toLowerCase().contains(textoBusqueda)) {
                resultados.add(contacto);
            }
        }
        
        publish(100); //Al finalizar, asegurar que la barra marque 100%
        return resultados;
    }
    //Actualiza la barra de progreso en la interfaz gráfica
    @Override
    protected void process(List<Integer> chunks) {
        if (!chunks.isEmpty() && progressBar != null) {
            int progreso = chunks.get(chunks.size() - 1);
            progressBar.setValue(progreso);
            progressBar.setString(progreso + "%");
        }
    }
    //Seejecuta cuando la búsqueda termina (éxito, cancelación o error)
    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                List<Persona> resultados = get();
                
                if (progressBar != null) {
                    progressBar.setValue(100);
                    progressBar.setString("100%");
                }
                //Limpiar y actualizar la tabla con los resultados de la búsqueda
                modeloTabla.setRowCount(0);
                for (Persona contacto : resultados) {
                    modeloTabla.addRow(new Object[]{
                        contacto.getNombre(),
                        contacto.getTelefono(),
                        contacto.getEmail(),
                        InternationalizationManager.getString("category." + contacto.getCategoria().toLowerCase()),
                        contacto.isFavorito()
                    });
                }
            } else if (progressBar != null) {
                progressBar.setString("Búsqueda cancelada");
            }
        } catch (CancellationException e) {
            if (progressBar != null) {
                progressBar.setString("Búsqueda cancelada");
            }
        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setString("Error en búsqueda");
            }
            e.printStackTrace();
        }
    }
}