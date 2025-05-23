package com.gestioncontactos.controlador;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import com.gestioncontactos.modelo.*;
import com.gestioncontactos.util.*;
import com.gestioncontactos.vista.*;


public class Logica_ventana implements ActionListener, ListSelectionListener, ItemListener, KeyListener, MouseListener {
    private Ventana delegado; //Referencia a la interfaz gráfica (ventana principal) que esta clase controla.
    private String nombres, email, telefono, categoria = ""; //Variables para almacenar temporalmente los datos ingresados o seleccionados para un contacto
    private Persona persona; //Objeto que representa un contacto individual
    private List<Persona> contactos = new ArrayList<>(); //Lista en memoria que contiene todos los contactos cargados desde almacenamiento.
    private boolean favorito = false; // Indica si el contacto actual está marcado como favorito
    private int selectedIndex = -1; //Índice del contacto actualmente seleccionado en la lista o tabla
    private BuscadorContactos buscadorActual; //Referencia a la tarea de búsqueda en segundo plano que filtra contactos según texto de búsqueda
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); //permite ejecutar tareas e segundo plano (hilos) de forma controlada
    private Timer searchTimer; //Temporizador que retrasa la ejecución de la búsqueda mientras el usuario escribe, para evitar búsquedas excesivas
    private final ReentrantLock exportLock = new ReentrantLock(); //Se usa para proteger secciones críticas del código que no deben ser ejecutadas par más de un hilo al mismo tiempo
    private ExportadorCSV exportadorActual; //Referencia a la tarea de exportación de contactos a CSV que se ejecuta en segundo plano.
    
    public Logica_ventana(Ventana delegado) {
        this.delegado = delegado;
        Notificador.getInstancia(); //Inicializa el notificador
        
        // Cargar contactos al iniciar
        cargarContactosRegistrados();
        
        // Agregar listeners
        this.delegado.btn_add.addActionListener(this);
        this.delegado.btn_eliminar.addActionListener(this);
        this.delegado.btn_modificar.addActionListener(this);
        this.delegado.btn_exportar.addActionListener(this);
        this.delegado.btn_importarJson.addActionListener(this);
        this.delegado.btn_exportarJson.addActionListener(this);
        this.delegado.lst_contactos.addListSelectionListener(this);
        this.delegado.cmb_categoria.addItemListener(this);
        this.delegado.chb_favorito.addItemListener(this);
        this.delegado.txt_buscar.addKeyListener(this);
        this.delegado.tablaContactos.addMouseListener(this);
        
        // Configurar validación en tiempo real
        configurarValidacionEnTiempoReal();
        
        // Configurar menú contextual
        this.delegado.menuItemEditar.addActionListener(e -> {
            int row = delegado.tablaContactos.getSelectedRow();
            if (row >= 0) {
                row = delegado.tablaContactos.convertRowIndexToModel(row);
                
                String nombreSeleccionado = (String) delegado.tablaContactos.getModel().getValueAt(row, 0);
                for(int i = 0; i < contactos.size();i++) {
                    if(contactos.get(i).getNombre().equals(nombreSeleccionado)) {
                         cargarContacto(i);
                         break;
                    }
                }
            }
        });
        
        this.delegado.menuItemEliminar.addActionListener(e -> {
            int row = delegado.tablaContactos.getSelectedRow();
            if (row >= 0) {
                int modelRow = delegado.tablaContactos.convertRowIndexToModel(row);
                String nombreSeleccionado = (String) delegado.tablaContactos.getModel().getValueAt(modelRow, 0);
                
                Persona personaAEliminar = null;
                int indexAEliminar = -1;
                
                for (int i = 0; i < contactos.size(); i++) {
                    if (contactos.get(i).getNombre().equals(nombreSeleccionado)) {
                        personaAEliminar = contactos.get(i);
                        indexAEliminar = i;
                        break;
                    }
                }
                
                if (personaAEliminar != null) {
                    int confirmacion = JOptionPane.showConfirmDialog(delegado, 
                        InternationalizationManager.getString("message.confirm.delete") + " " + personaAEliminar.getNombre() + "?",
                        InternationalizationManager.getString("title.confirm.delete"), 
                        JOptionPane.YES_NO_OPTION);
                        
                    if (confirmacion == JOptionPane.YES_OPTION) {
                        try {
                            contactos.remove(indexAEliminar);
                            new PersonaDAO(new Persona()).actualizarContactos(contactos);
                            
                            selectedIndex = -1;
                            limpiarCampos();
                            cargarContactosRegistrados();
                            
                            JOptionPane.showMessageDialog(delegado, 
                                InternationalizationManager.getString("message.contact.deleted"));
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(delegado, 
                                InternationalizationManager.getString("message.error.delete") + ": " + ex.getMessage());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(delegado, 
                        InternationalizationManager.getString("message.error.delete"));
                }
            }
        });
        
        // Actualizar estadísticas
        actualizarEstadisticas();
        
        // Agregar listener para cambio de pestañas
        this.delegado.tabbedPane.addChangeListener(e -> {
            if (delegado.tabbedPane.getSelectedIndex() == 1) {
                actualizarEstadisticas();
            }
        });
    }

    public void actualizarDatosIdioma() {
        cargarContactosRegistrados();
        actualizarEstadisticas();
    }
    
    public List<Persona> getContactos() {
        return contactos;
    }

    //Validación en tiempo real de los diferentes campos
    private void configurarValidacionEnTiempoReal() { //Validación en tiempo real para el campo Nombre
        delegado.txt_nombres.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarCampoNombre(); }
            @Override public void removeUpdate(DocumentEvent e) { validarCampoNombre(); }
            @Override public void changedUpdate(DocumentEvent e) { validarCampoNombre(); }
            
            private void validarCampoNombre() {
                String texto = delegado.txt_nombres.getText();
                if (!texto.isEmpty() && !validarNombre(texto)) {
                    delegado.txt_nombres.setBackground(new Color(255, 200, 200));
                    delegado.txt_nombres.setToolTipText(InternationalizationManager.getString("validation.name"));
                } else {
                    delegado.txt_nombres.setBackground(Color.WHITE);
                    delegado.txt_nombres.setToolTipText(null);
                }
            }
        });
        //Validación en tiempo real para el campo teléfono
        delegado.txt_telefono.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarCampoTelefono(); }
            @Override public void removeUpdate(DocumentEvent e) { validarCampoTelefono(); }
            @Override public void changedUpdate(DocumentEvent e) { validarCampoTelefono(); }
            
            private void validarCampoTelefono() {
                String texto = delegado.txt_telefono.getText();
                if (!texto.isEmpty() && !validarTelefono(texto)) {
                    delegado.txt_telefono.setBackground(new Color(255, 200, 200));
                    delegado.txt_telefono.setToolTipText(InternationalizationManager.getString("validation.phone"));
                } else {
                    delegado.txt_telefono.setBackground(Color.WHITE);
                    delegado.txt_telefono.setToolTipText(null);
                }
            }
        });
        //validación en tiempo real para el campo email
        delegado.txt_email.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarCampoEmail(); }
            @Override public void removeUpdate(DocumentEvent e) { validarCampoEmail(); }
            @Override public void changedUpdate(DocumentEvent e) { validarCampoEmail(); }
            
            private void validarCampoEmail() {
                String texto = delegado.txt_email.getText();
                if (!texto.isEmpty() && !validarEmail(texto)) {
                    delegado.txt_email.setBackground(new Color(255, 200, 200));
                    delegado.txt_email.setToolTipText(InternationalizationManager.getString("validation.email"));
                } else {
                    delegado.txt_email.setBackground(Color.WHITE);
                    delegado.txt_email.setToolTipText(null);
                }
            }
        });
    }
    //Valida todos los campos antes de guardar o modificar un contacto.
    private boolean validarCampos() {
        // Validar nombres (solo letras y espacios)
        if (!validarNombre(delegado.txt_nombres.getText())) {
            JOptionPane.showMessageDialog(delegado, 
                InternationalizationManager.getString("validation.name.message"), 
                InternationalizationManager.getString("validation.title"), 
                JOptionPane.ERROR_MESSAGE);
            delegado.txt_nombres.requestFocus();
            return false;
        }
        
        // Validar teléfono (solo números)
        if (!validarTelefono(delegado.txt_telefono.getText())) {
            JOptionPane.showMessageDialog(delegado, 
                InternationalizationManager.getString("validation.phone.message"), 
                InternationalizationManager.getString("validation.title"), 
                JOptionPane.ERROR_MESSAGE);
            delegado.txt_telefono.requestFocus();
            return false;
        }
        
        // Validar email (formato correcto)
        if (!validarEmail(delegado.txt_email.getText())) {
            JOptionPane.showMessageDialog(delegado, 
                InternationalizationManager.getString("validation.email.message"), 
                InternationalizationManager.getString("validation.title"), 
                JOptionPane.ERROR_MESSAGE);
            delegado.txt_email.requestFocus();
            return false;
        }
        
        return true;
    }

    private boolean validarNombre(String nombre) {
        String regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$";
        return nombre.trim().matches(regex);
    }

    private boolean validarTelefono(String telefono) {
        String regex = "^[0-9]+$";
        return telefono.trim().matches(regex);
    }

    private boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.trim().matches(regex);
    }

    private void incializacionCampos() {
        nombres = delegado.txt_nombres.getText();
        email = delegado.txt_email.getText();
        telefono = delegado.txt_telefono.getText();
    }
    //Carga los contactos registrados utilizando un SwingWorker para evitar bloquear la interfaz
    private void cargarContactosRegistrados() {
        try {
            // Configuración inicial de la barra de progreso
            SwingUtilities.invokeLater(() -> {
                delegado.progressBar.setValue(0);
                delegado.progressBar.setString(InternationalizationManager.getString("progress.loading"));
                delegado.progressBar.setIndeterminate(true);
            });
            //SwingWorker ejecuta doInBackground() en un hilo separado (no bloquea el EDT)
            SwingWorker<List<Persona>, Integer> worker = new SwingWorker<List<Persona>, Integer>() {
                @Override
                protected List<Persona> doInBackground() throws Exception {
                    publish(25); // Primer progreso visible
                    List<Persona> contactosCargados = new PersonaDAO(new Persona()).leerArchivo();
                    publish(75); // Progreso después de leer archivo
                    return contactosCargados;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    // Tomamos el último valor de progreso recibido
                    int progreso = chunks.get(chunks.size() - 1);
                    delegado.progressBar.setValue(progreso);
                    delegado.progressBar.setString(progreso + "%");
                }

                @Override
                protected void done() {
                    try {
                        contactos = get(); // Obtiene el resultado del hilo en background
                        
                        // Actualizar interfaz en el EDT
                        SwingUtilities.invokeLater(() -> {
                            // Actualizar tabla
                            DefaultTableModel model = (DefaultTableModel) delegado.tablaContactos.getModel();
                            model.setRowCount(0);
                            
                            // Actualizar lista
                            DefaultListModel<String> listaModelo = new DefaultListModel<>();
                            
                            for (Persona contacto : contactos) {
                                if (!contacto.getNombre().equals("NOMBRE")) {
                                    model.addRow(new Object[]{
                                        contacto.getNombre(),
                                        contacto.getTelefono(),
                                        contacto.getEmail(),
                                        obtenerTraduccionCategoria(contacto.getCategoria()),
                                        contacto.isFavorito()
                                    });
                                    listaModelo.addElement(contacto.formatoLista());
                                }
                            }
                            
                            delegado.lst_contactos.setModel(listaModelo);
                            
                            // Actualizar estadísticas
                            actualizarEstadisticas();
                            
                            // Completar barra de progreso
                            delegado.progressBar.setIndeterminate(false);
                            delegado.progressBar.setValue(100);
                            delegado.progressBar.setString(InternationalizationManager.getString("progress.loaded"));
                        });
                        
                    } catch (Exception e) { //Manejo de errores en el EDT
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(delegado, 
                                InternationalizationManager.getString("error.load.contacts") + ": " + e.getMessage(),
                                InternationalizationManager.getString("error.title"),
                                JOptionPane.ERROR_MESSAGE);
                            delegado.progressBar.setIndeterminate(false);
                            delegado.progressBar.setString(InternationalizationManager.getString("error.loading"));
                        });
                    }
                }
            };
            
            worker.execute();  //Inicia el hilo en background
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(delegado, 
                    InternationalizationManager.getString("error.load.contacts") + ": " + e.getMessage(),
                    InternationalizationManager.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
                delegado.progressBar.setIndeterminate(false);
                delegado.progressBar.setString(InternationalizationManager.getString("error.loading"));
            });
        }
    }
    //Limpia los campos de la interfaz y libera el bloqueo sobre el contacto seleccionado
    private void limpiarCampos() {
        // Liberar el bloqueo del contacto actualmente seleccionado
        if (selectedIndex != -1 && selectedIndex < contactos.size()) {
            String nombreContacto = contactos.get(selectedIndex).getNombre();
            ContactLockManager lockManager = ContactLockManager.getInstance();
            
            if (lockManager.hasLock(nombreContacto)) {
                lockManager.unlock(nombreContacto);
            }
        }
        
        // Limpiar campos de la interfaz
        delegado.txt_nombres.setText("");
        delegado.txt_telefono.setText("");
        delegado.txt_email.setText("");
        categoria = "";
        favorito = false;
        delegado.chb_favorito.setSelected(favorito);
        delegado.cmb_categoria.setSelectedIndex(0);
        incializacionCampos();
        selectedIndex = -1;
        
        // Restaurar el color y tooltips de validación
        delegado.txt_nombres.setBackground(Color.WHITE);
        delegado.txt_telefono.setBackground(Color.WHITE);
        delegado.txt_email.setBackground(Color.WHITE);
        
        delegado.txt_nombres.setToolTipText(null);
        delegado.txt_telefono.setToolTipText(null);
        delegado.txt_email.setToolTipText(null);
        
        cargarContactosRegistrados();
    }
    //Carga los datos de un contacto en la interfaz y gestiona el bloqueo de edición
    private void cargarContacto(int index) {
        // Validación básica del índice
        if (index < 0 || index >= contactos.size()) {
            return;
        }

        // Obtener el nuevo contacto a cargar
        Persona nuevoContacto = contactos.get(index);
        String nombreNuevoContacto = nuevoContacto.getNombre();
        ContactLockManager lockManager = ContactLockManager.getInstance();

        //  Ya estamos editando este mismo contacto
        if (selectedIndex != -1 && contactos.get(selectedIndex).getNombre().equals(nombreNuevoContacto)) {
            cargarDatosContacto(index);
            return;
        }

        //  El contacto ya está bloqueado por otro usuario/hilo
        if (!lockManager.tryLock(nombreNuevoContacto)) {
            mostrarMensajeContactoBloqueado(nombreNuevoContacto);
            return;
        }

        //  Podemos bloquear el nuevo contacto
        try {
            // Liberar el bloqueo del contacto anterior (si existe)
            liberarBloqueoContactoActual();

            // Actualizar el índice seleccionado
            selectedIndex = index;

            // Cargar los datos en la interfaz
            cargarDatosContacto(index);
        } catch (Exception e) {
            // En caso de error, liberar el bloqueo
            lockManager.unlock(nombreNuevoContacto);
            selectedIndex = -1;
            JOptionPane.showMessageDialog(delegado, 
                "Error al cargar el contacto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método auxiliar para cargar datos en la UI
    private void cargarDatosContacto(int index) {
        Persona contacto = contactos.get(index);
        
        SwingUtilities.invokeLater(() -> {
            delegado.txt_nombres.setText(contacto.getNombre());
            delegado.txt_telefono.setText(contacto.getTelefono());
            delegado.txt_email.setText(contacto.getEmail());
            delegado.chb_favorito.setSelected(contacto.isFavorito());
            
            // Configurar la categoría adecuada en el ComboBox
            String categoriaAlmacenada = contacto.getCategoria();
            for (int i = 0; i < delegado.cmb_categoria.getItemCount(); i++) {
                IconComboItem item = delegado.cmb_categoria.getItemAt(i);
                if (item.getText().equals(InternationalizationManager.getString("category." + categoriaAlmacenada.toLowerCase()))) {
                    delegado.cmb_categoria.setSelectedIndex(i);
                    break;
                }
            }
        });
    }

    // Método auxiliar para liberar el bloqueo actual
    private void liberarBloqueoContactoActual() {
        if (selectedIndex != -1 && selectedIndex < contactos.size()) {
            String nombreActual = contactos.get(selectedIndex).getNombre();
            ContactLockManager.getInstance().unlock(nombreActual);
        }
    }

    // Método auxiliar para mostrar mensaje de contacto bloqueado
    private void mostrarMensajeContactoBloqueado(String nombreContacto) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(delegado, 
                InternationalizationManager.getString("message.contact.locked") + ": " + nombreContacto,
                InternationalizationManager.getString("title.contact.locked"),
                JOptionPane.WARNING_MESSAGE);
        });
    }
    //Elimina un contacto de la lista, asegurando que sólo una instancia pueda hacerlo a la vez
    private void eliminarContacto(int index) {
        if (index >= 0 && index < contactos.size()) {
            Persona contacto = contactos.get(index);
            ContactLockManager lockManager = ContactLockManager.getInstance(); // Declaración correcta
            int confirmacion = JOptionPane.NO_OPTION; 
            try {
                // Intentar adquirir el lock del contacto a eliminar
                if (lockManager.tryLock(contacto.getNombre())) {
                    try {
                            confirmacion = JOptionPane.showConfirmDialog(
                            delegado, 
                            InternationalizationManager.getString("message.confirm.delete") + " " + 
                            contacto.getNombre() + "?",
                            InternationalizationManager.getString("title.confirm.delete"), 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );

                        if (confirmacion == JOptionPane.YES_OPTION) {
                            delegado.progressBar.setIndeterminate(true);
                            delegado.progressBar.setString(InternationalizationManager.getString("progress.deleting"));
                            
                            // Ejecuta la eliminación en segundo plano
                            new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    try {
                                        String nombreContacto = contacto.getNombre();
                                        contactos.remove(index);
                                        new PersonaDAO(new Persona()).actualizarContactos(contactos);
                                        
                                        SwingUtilities.invokeLater(() -> {
                                            Notificador.getInstancia().encolarMensaje(
                                                InternationalizationManager.getString("notification.contact.deleted") + ": " + 
                                                nombreContacto
                                            );
                                        });
                                    } catch (IOException e) {
                                        SwingUtilities.invokeLater(() -> {
                                            Notificador.getInstancia().encolarMensaje(
                                                InternationalizationManager.getString("notification.error.delete") + ": " + 
                                                e.getMessage()
                                            );
                                        });
                                        throw e;
                                    }
                                    return null;
                                }

                                @Override
                                protected void done() {
                                    delegado.progressBar.setIndeterminate(false);
                                    try {
                                        get();
                                        selectedIndex = -1;
                                        limpiarCampos();
                                        cargarContactosRegistrados();
                                        delegado.progressBar.setString(InternationalizationManager.getString("progress.deleted"));
                                    } catch (Exception e) {
                                        delegado.progressBar.setString(InternationalizationManager.getString("error.deleting"));
                                    } finally {
                                        // Asegurarse de liberar el lock después de la eliminación
                                        lockManager.unlock(contacto.getNombre());
                                    }
                                }
                            }.execute();
                        }
                    } finally {
                        // Liberar el lock si no se procedió con la eliminación
                        if (confirmacion != JOptionPane.YES_OPTION) {
                            lockManager.unlock(contacto.getNombre());
                        }
                    }
                } else {
                    // Si no se puede adquirir el lock, avisar al usuario
                    JOptionPane.showMessageDialog(delegado, 
                        InternationalizationManager.getString("message.contact.locked.delete") + ": " + contacto.getNombre(),
                        InternationalizationManager.getString("title.contact.locked"),
                        JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                JOptionPane.showMessageDialog(delegado, 
                    InternationalizationManager.getString("message.operation.interrupted"),
                    InternationalizationManager.getString("title.error"),
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Notificador.getInstancia().encolarMensaje(
                InternationalizationManager.getString("notification.select.contact.delete")
            );
        }
    }
    //Actualiza las estadísticas mostradas en la interfaz, contando contactos totales, favoritos y por categoría (familia, amigos, trabajo)
    private void actualizarEstadisticas() {
    	 try {
    	        // Reiniciamos contadores
    	        int total = 0;
    	        int favoritos = 0;
    	        int familia = 0;
    	        int amigos = 0;
    	        int trabajo = 0;

    	        // Usamos directamente la lista de contactos en memoria
    	        for (Persona p : contactos) {
    	            if (!p.getNombre().equals("NOMBRE")) {
    	                total++;
    	                if (p.isFavorito()) {
    	                    favoritos++;
    	                }
    	                
    	                // Convertimos la categoría a minúsculas para comparación
    	                String categoria = p.getCategoria().toLowerCase();
    	                
    	                if (categoria.contains("family") || categoria.contains("familia")) {
    	                    familia++;
    	                } else if (categoria.contains("friends") || categoria.contains("amigos")) {
    	                    amigos++;
    	                } else if (categoria.contains("work") || categoria.contains("trabajo")) {
    	                    trabajo++;
    	                }
    	            }
    	        }

    	        // Actualizamos las etiquetas de la UI con los valores calculados
    	        delegado.lblTotalContactos.setText(InternationalizationManager.getString("stats.total") + ": " + total);
    	        delegado.lblFavoritos.setText(InternationalizationManager.getString("stats.favorites") + ": " + favoritos);
    	        delegado.lblFamilia.setText(InternationalizationManager.getString("stats.family") + ": " + familia);
    	        delegado.lblAmigos.setText(InternationalizationManager.getString("stats.friends") + ": " + amigos);
    	        delegado.lblTrabajo.setText(InternationalizationManager.getString("stats.work") + ": " + trabajo);

    	        // Preparamos datos para el gráfico de barras
    	        Map<String, Integer> datosGrafico = new LinkedHashMap<>();
    	        datosGrafico.put(InternationalizationManager.getString("category.family"), familia);
    	        datosGrafico.put(InternationalizationManager.getString("category.friends"), amigos);
    	        datosGrafico.put(InternationalizationManager.getString("category.work"), trabajo);
    	        datosGrafico.put(InternationalizationManager.getString("stats.favorites"), favoritos);

    	        // Actualizamos el gráfico con los datos calculados
    	        actualizarGrafico(datosGrafico);

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        JOptionPane.showMessageDialog(delegado, 
    	            InternationalizationManager.getString("error.stats") + ": " + e.getMessage());
    	    }
    }
    //Actualiza el panel de gráficos con un gráfico de barras basado en los datos proporcionados
	private void actualizarGrafico(Map<String, Integer> datos) {
	    GraficoBarras grafico = new GraficoBarras(datos, 
	        InternationalizationManager.getString("stats.title"));
	    delegado.panelGraficas.removeAll();
	    delegado.panelGraficas.add(grafico);
	    delegado.panelGraficas.revalidate();
	    delegado.panelGraficas.repaint();
	}
    //Filtra la lista de contactos en base al texto ingresado en el campo de búsqueda
	private void filtrarContactos() {
	    String textoBusqueda = delegado.txt_buscar.getText().trim();
	    //Cancelar búsqueda previa si está en curso
	    if (buscadorActual != null && !buscadorActual.isDone()) {
	        buscadorActual.cancel(true);
	    }
	    
	    delegado.progressBar.setValue(0);
	    delegado.progressBar.setString("0%");
	    delegado.progressBar.setStringPainted(true);
	    
	    // Pasar la barra de progreso como nuevo parámetro
	    buscadorActual = new BuscadorContactos(contactos, textoBusqueda, 
	                                         delegado.tablaContactos, delegado.modeloTabla,
	                                         delegado.progressBar); // Pasamos la barra de progreso
	    //Ejecutar la búsqueda en el ExecutorService (hilo en background)
	    executorService.execute(buscadorActual);
	}
    //Agrega el ExecutorService que maneja tareas en background
    public void shutdown() {
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
   }
    //Obtiene la traducción de la categoría para mostrar en la interfaz
    private String obtenerTraduccionCategoria(String categoria) {
        if (categoria == null || categoria.isEmpty()) {
            return InternationalizationManager.getString("category.choose");
        }
        
        // Normalizar la categoría para comparación
        String catLower = categoria.toLowerCase();
        
        if (catLower.contains("family") || catLower.contains("familia")) {
            return InternationalizationManager.getString("category.family");
        } else if (catLower.contains("friends") || catLower.contains("amigos")) {
            return InternationalizationManager.getString("category.friends");
        } else if (catLower.contains("work") || catLower.contains("trabajo")) {
            return InternationalizationManager.getString("category.work");
        }
        
        return categoria; // Si no coincide, devolver el original
    }
    //Exporta la lista de contactos a un archivo CSV de forma asíncrona y segura.
    private void exportarContactosCSV(Path archivo) {
        //Si hay una exportación en curso la cancelamos
    	if (exportadorActual != null && !exportadorActual.isDone()) {
            exportadorActual.cancel(true);
        }
        //Inicializar la barra de progreso
        delegado.progressBar.setValue(0);
        delegado.progressBar.setString("Exportando... 0%");
        delegado.progressBar.setStringPainted(true);
        //Crear el SwingWorker para exportar en background
        exportadorActual = new ExportadorCSV(contactos, archivo, exportLock);
        
        //  Listener para actualizar progreso y estado en la barra de progreso
        exportadorActual.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progreso = (Integer) evt.getNewValue();
                delegado.progressBar.setValue(progreso);
                delegado.progressBar.setString("Exportando... " + progreso + "%");
            }
            //Cuando termina la exportación (éxito o error)
            if ("state".equals(evt.getPropertyName()) && 
                SwingWorker.StateValue.DONE == evt.getNewValue()) {
                
                if (!exportadorActual.isCancelled()) {
                    try {
                        if (exportadorActual.get()) {
                        	
                        	delegado.progressBar.setValue(100);
                        	delegado.progressBar.setString("Exportación completada");
                            Notificador.getInstancia().encolarMensaje(
                                InternationalizationManager.getString("notification.export.success"));
                        }
                    } catch (Exception ex) {
                    	 delegado.progressBar.setString("Error en exportación");
                        Notificador.getInstancia().encolarMensaje(
                            InternationalizationManager.getString("notification.error"));
                       
                    }
                }
            }
        });
        //Ejecutar la exportación en background
        exportadorActual.execute();
    }
    //Muestra el estado de bloqueo para un contacto específico
    public void mostrarEstadoBloqueo(String nombreContacto) {
        ContactLockManager lockManager = ContactLockManager.getInstance(); //Obtener la instancia del gestor de locks
        boolean tieneLock = lockManager.hasLock(nombreContacto); //Verificar si esta instancia ya tiene el lock para el contacto
        boolean puedeLock = lockManager.tryLock(nombreContacto); // Intentar adquirir el lock para el contacto (sin bloquear)
        
        if (tieneLock) {
            JOptionPane.showMessageDialog(delegado,
                "Esta aplicación tiene el bloqueo para el contacto: " + nombreContacto,
                "Estado de bloqueo",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (puedeLock) {
            JOptionPane.showMessageDialog(delegado,
                "Se ha adquirido el bloqueo para el contacto: " + nombreContacto,
                "Estado de bloqueo",
                JOptionPane.INFORMATION_MESSAGE);
            lockManager.unlock(nombreContacto); //Liberar inmediatamente el lock adquirido para no bloquear innecesariamente
        } else {
            JOptionPane.showMessageDialog(delegado,
                "¡El contacto " + nombreContacto + " está siendo editado por otra instancia!",
                "Estado de bloqueo",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void cargarContactosDesdeJson(String rutaArchivo) {
        PersonaDAO dao = new PersonaDAO(new Persona());
        try {
            contactos = dao.leerContactosJson(rutaArchivo);
            actualizarListaContactos(); // Actualiza la tabla y lista en la UI
        } catch (Exception e) {
            JOptionPane.showMessageDialog(delegado, "Error al cargar contactos JSON: " + e.getMessage());
        }
    }

    public void guardarContactosEnJson(String rutaArchivo) {
       try {
    	   PersonaDAO dao = new PersonaDAO(new Persona());
    	   boolean exito = dao.guardarContactosJson(contactos, rutaArchivo);
    	   if (exito) {
    		   JOptionPane.showMessageDialog(delegado, InternationalizationManager.getString("message.export.success") );
    	   } else {
    		   JOptionPane.showMessageDialog(delegado, InternationalizationManager.getString("message.export.error.general"));
    	   }
       }catch(Exception e) {
    	   String errorMsg = MessageFormat.format(InternationalizationManager.getString("message.export.error"), e.getMessage());
    	   JOptionPane.showMessageDialog(delegado, errorMsg);
       }
    }
    
    private void actualizarListaContactos() {
        DefaultTableModel model = (DefaultTableModel) delegado.tablaContactos.getModel();
        model.setRowCount(0);

        DefaultListModel<String> listaModelo = new DefaultListModel<>();

        for (Persona contacto : contactos) {
            if (!contacto.getNombre().equals("NOMBRE")) {
                model.addRow(new Object[]{
                    contacto.getNombre(),
                    contacto.getTelefono(),
                    contacto.getEmail(),
                    obtenerTraduccionCategoria(contacto.getCategoria()),
                    contacto.isFavorito()
                });
                listaModelo.addElement(contacto.formatoLista());
            }
        }
        delegado.lst_contactos.setModel(listaModelo);

        actualizarEstadisticas();
    }
    
    public void cargarContactosDesdeJsonAsync(String rutaArchivo) {
    	SwingWorker<List<Persona>, Void> worker = new SwingWorker<List<Persona>, Void>() {
    	    @Override
    	    protected List<Persona> doInBackground() throws Exception {
    	        PersonaDAO dao = new PersonaDAO(new Persona());
    	        return dao.leerContactosJson(rutaArchivo);
    	    }

    	    @Override
    	    protected void done() {
    	        try {
    	            contactos = get();
    	            actualizarListaContactos();
    	            JOptionPane.showMessageDialog(delegado, InternationalizationManager.getString("message.import.success"));
    	        } catch (Exception e) {
    	        	String errorMsg = MessageFormat.format(InternationalizationManager.getString("message.import.error"),
    	        			e.getMessage());
    	            JOptionPane.showMessageDialog(delegado, errorMsg);
    	        }
    	    }
    	};
    	worker.execute();
    }

    //Maneja los eventos de los botones principales de la interfaz
    @Override
    public void actionPerformed(ActionEvent e) {
        incializacionCampos();
        //Agregar contacto
        if (e.getSource() == delegado.btn_add) {
            if ((!nombres.equals("")) && (!telefono.equals("")) && (!email.equals(""))) {
                if ((!categoria.equals(InternationalizationManager.getString("category.choose"))) && (!categoria.equals(""))) {
                    if (validarCampos()) {
                        // Deshabilitar botón mientras se valida
                        delegado.btn_add.setEnabled(false);
                        delegado.progressBar.setIndeterminate(true);
                        delegado.progressBar.setString(InternationalizationManager.getString("progress.validating"));
                        
                        // Iniciar validación en segundo plano
                        new ValidadorContactos(contactos, nombres, telefono, email, new ValidadorContactos.ValidadorCallback() {
                            @Override
                            public void onValidacionCompletada(boolean existeDuplicado) {
                                delegado.progressBar.setIndeterminate(false);
                                delegado.btn_add.setEnabled(true);
                                
                                if (existeDuplicado) {
                                    JOptionPane.showMessageDialog(delegado, 
                                        InternationalizationManager.getString("message.duplicate.contact"));
                                    delegado.progressBar.setString(InternationalizationManager.getString("error.duplicate"));
                                } else {
                                    // Guardar la categoría en inglés para consistencia interna
                                    String categoriaInterna = "";
                                    if (categoria.equals(InternationalizationManager.getString("category.family"))) {
                                        categoriaInterna = "family";
                                    } else if (categoria.equals(InternationalizationManager.getString("category.friends"))) {
                                        categoriaInterna = "friends";
                                    } else if (categoria.equals(InternationalizationManager.getString("category.work"))) {
                                        categoriaInterna = "work";
                                    }
                                    
                                    persona = new Persona(nombres, telefono, email, categoriaInterna, favorito);
                                    boolean exito = new PersonaDAO(persona).escribirArchivo();
                                    if (exito) {
                                        limpiarCampos();
                                        Notificador.getInstancia().encolarMensaje(
                                                InternationalizationManager.getString("notification.contact.saved"));
                                            delegado.progressBar.setString(InternationalizationManager.getString("progress.saved"));
                                    } else {
                                        JOptionPane.showMessageDialog(delegado, 
                                            InternationalizationManager.getString("message.error.add"));
                                        delegado.progressBar.setString(InternationalizationManager.getString("error.saving"));
                                    }
                                }
                            }
                        }).start();
                    }
                } else {
                    JOptionPane.showMessageDialog(delegado, 
                        InternationalizationManager.getString("message.select.category"));
                }
            } else {
                JOptionPane.showMessageDialog(delegado, 
                    InternationalizationManager.getString("message.fill.fields"));
            }
        } 
        else if (e.getSource() == delegado.btn_eliminar) { //Eliminar contacto
            if (selectedIndex != -1) {
                eliminarContacto(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(delegado, 
                    InternationalizationManager.getString("message.select.contact.delete"));
            }
        } //Modificar contacto
        else if (e.getSource() == delegado.btn_modificar) {
            if (selectedIndex != -1 && (!nombres.equals("")) && (!telefono.equals("")) && (!email.equals(""))) {
                if ((!categoria.equals(InternationalizationManager.getString("category.choose"))) && (!categoria.equals(""))) {
                    if (validarCampos()) {
                        final Persona contacto = contactos.get(selectedIndex);
                        ContactLockManager lockManager = ContactLockManager.getInstance();
                        final boolean yaBloqueado = lockManager.hasLock(contacto.getNombre());
                        
                        if (!yaBloqueado && !lockManager.tryLock(contacto.getNombre())) {
                            JOptionPane.showMessageDialog(delegado, 
                                InternationalizationManager.getString("message.contact.locked.modify") + ": " + contacto.getNombre(),
                                InternationalizationManager.getString("title.contact.locked"),
                                JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        //Modificación en segundo plano usando SwingWorker
                        new SwingWorker<Boolean, Void>() {
                            @Override
                            protected Boolean doInBackground() throws Exception {
                                try {
                                    String categoriaInterna = "";
                                    if (categoria.equals(InternationalizationManager.getString("category.family"))) {
                                        categoriaInterna = "family";
                                    } else if (categoria.equals(InternationalizationManager.getString("category.friends"))) {
                                        categoriaInterna = "friends";
                                    } else if (categoria.equals(InternationalizationManager.getString("category.work"))) {
                                        categoriaInterna = "work";
                                    }
                                    
                                    contacto.setNombre(nombres);
                                    contacto.setTelefono(telefono);
                                    contacto.setEmail(email);
                                    contacto.setCategoria(categoriaInterna);
                                    contacto.setFavorito(favorito);
                                    
                                    new PersonaDAO(new Persona()).actualizarContactos(contactos);
                                    return true;
                                } catch (IOException ex) {
                                    return false;
                                }
                            }
                            
                            @Override
                            protected void done() {
                                try {
                                    if (get()) {
                                        limpiarCampos();
                                        JOptionPane.showMessageDialog(delegado, 
                                            InternationalizationManager.getString("message.contact.modified"));
                                    } else {
                                        JOptionPane.showMessageDialog(delegado, 
                                            InternationalizationManager.getString("message.error.modify"));
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(delegado, 
                                        InternationalizationManager.getString("message.error.modify") + ": " + ex.getMessage());
                                } finally {
                                    if (!yaBloqueado) {
                                        lockManager.unlock(contacto.getNombre());
                                    }
                                }
                            }
                        }.execute();
                    }
                } else {
                    JOptionPane.showMessageDialog(delegado, 
                        InternationalizationManager.getString("message.select.category"));
                }
            } else {
                JOptionPane.showMessageDialog(delegado, 
                    InternationalizationManager.getString("message.select.contact.modify"));
            }
        } 
        else if (e.getSource() == delegado.btn_exportar) { //Exportar contactos a CSV
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(InternationalizationManager.getString("button.export"));
            fileChooser.setSelectedFile(new File("contactos_exportados.csv"));
            
            int seleccion = fileChooser.showSaveDialog(delegado);
            
            if (seleccion == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();
                
                if (!ruta.toLowerCase().endsWith(".csv")) {
                    ruta += ".csv";
                }
                
                final Path rutaFinal = Path.of(ruta);
                
                // Configurar barra de progreso
                delegado.progressBar.setValue(0);
                delegado.progressBar.setString("0%");
                delegado.progressBar.setStringPainted(true);
                
                // Cancelar exportación anterior si existe
                if (exportadorActual != null && !exportadorActual.isDone()) {
                    exportadorActual.cancel(true);
                }
                
                // Crear nuevo exportador
                exportadorActual = new ExportadorCSV(contactos, rutaFinal, exportLock);
                
                // Configurar listeners de progreso
                exportadorActual.addPropertyChangeListener(evt -> {
                    if ("progress".equals(evt.getPropertyName())) {
                        int progreso = (Integer) evt.getNewValue();
                        delegado.progressBar.setValue(progreso);
                        delegado.progressBar.setString(progreso + "%");
                    }
                    
                    if ("state".equals(evt.getPropertyName()) && 
                        SwingWorker.StateValue.DONE == evt.getNewValue()) {
                        
                        if (!exportadorActual.isCancelled()) {
                            try {
                                if (exportadorActual.get()) {
                                    delegado.progressBar.setString("Exportación completada");
                                    JOptionPane.showMessageDialog(delegado,
                                        InternationalizationManager.getString("message.export.success") + "\n" + rutaFinal.toString(),
                                        InternationalizationManager.getString("title.export.success"),
                                        JOptionPane.INFORMATION_MESSAGE);
                                }
                            } catch (Exception ex) {
                                delegado.progressBar.setString("Error en exportación");
                                JOptionPane.showMessageDialog(delegado,
                                    InternationalizationManager.getString("message.export.error") + ":\n" + ex.getMessage(),
                                    InternationalizationManager.getString("title.export.error"),
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });
                
                // Ejecutar exportación
                executorService.execute(exportadorActual);
            }
        }else if(e.getSource() == delegado.btn_importarJson) {
        	JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(InternationalizationManager.getString("button.import_json"));
            int seleccion = fileChooser.showOpenDialog(delegado);
            if (seleccion == JFileChooser.APPROVE_OPTION) {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                cargarContactosDesdeJsonAsync(ruta);
            }
        }else if (e.getSource() == delegado.btn_exportarJson) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(InternationalizationManager.getString("button.export_json"));
            fileChooser.setSelectedFile(new File("contactos_exportados.json"));
            int seleccion = fileChooser.showSaveDialog(delegado);
            if (seleccion == JFileChooser.APPROVE_OPTION) {
                String ruta = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    PersonaDAO dao = new PersonaDAO(new Persona());
                    boolean exito = dao.guardarContactosJson(contactos, ruta);
                    if (exito) {
                        JOptionPane.showMessageDialog(delegado, 
                        	InternationalizationManager.getString("message.export.success"));
                    } else {
                        JOptionPane.showMessageDialog(delegado,
                        	InternationalizationManager.getString("message.export.error.general"));
                    }
                } catch (Exception ex) {
                	String errorMsg = MessageFormat.format(InternationalizationManager.getString("message.export.error"),
                			ex.getMessage());
                    JOptionPane.showMessageDialog(delegado, errorMsg);
                }
            }
        }
    }
    //Se ejecuta cuando cambia la selección en la lista de contactos
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int index = delegado.lst_contactos.getSelectedIndex();
            if (index != -1) {
                int realIndex = -1;
                int contador = 0;
                //Buscar el índice real del contacto (ignorando "NOMBRE")
                for (int i = 0; i < contactos.size(); i++) {
                    if (!contactos.get(i).getNombre().equals("NOMBRE")) {
                        if (contador == index) {
                            realIndex = i;
                            break;
                        }
                        contador++;
                    }
                }
                
                if (realIndex != -1) {
                    cargarContacto(realIndex);  //Gestiona el bloqueo del contacto
                    //Sincronizar selecci´n en la tabla
                    for (int i = 0; i < delegado.tablaContactos.getRowCount(); i++) {
                        if (delegado.tablaContactos.getValueAt(i, 0).equals(contactos.get(realIndex).getNombre())) {
                            delegado.tablaContactos.setRowSelectionInterval(i, i);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    
    //Se ejecuta cuando cambia el estado de un ítem en la interfaz
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == delegado.cmb_categoria) {
           IconComboItem selectedItem = (IconComboItem) delegado.cmb_categoria.getSelectedItem();
           categoria = selectedItem.getText(); 
        } else if (e.getSource() == delegado.chb_favorito) {
            favorito = delegado.chb_favorito.isSelected();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == delegado.txt_buscar) {
        	 if (searchTimer != null) {
                 searchTimer.stop();
             }
             //Esperar 400 ms antes de filtrar
             searchTimer = new Timer(400, event -> {
                 filtrarContactos();
             });
             searchTimer.setRepeats(false);
             searchTimer.start();
         }
        
    }
    //Se utiliza cuando el usuario hace clic en la tabla de contactos
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == delegado.tablaContactos) {
            int row = delegado.tablaContactos.rowAtPoint(e.getPoint());
            
            if (row >= 0) {
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) { //Clic izquierdo: cargar contacto
                    int modelRow = delegado.tablaContactos.convertRowIndexToModel(row);
                    for (int i = 0; i < contactos.size(); i++) {
                        if (contactos.get(i).getNombre().equals(delegado.tablaContactos.getModel().getValueAt(modelRow, 0))) {
                            cargarContacto(i);  //Gestiona el lock
                            break;
                        }
                    }
                }
                //Clic derecho: mnstrar menú contextual
                if (e.getButton() == MouseEvent.BUTTON3) {
                    delegado.tablaContactos.setRowSelectionInterval(row, row);
                    delegado.popupMenu.show(delegado.tablaContactos, e.getX(), e.getY());
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
