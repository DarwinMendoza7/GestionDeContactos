package com.gestioncontactos.vista;

import javax.swing.*;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.gestioncontactos.controlador.Logica_ventana;
import com.gestioncontactos.modelo.Persona;
import com.gestioncontactos.util.ContactLockManager;
import com.gestioncontactos.util.IconManager;
import com.gestioncontactos.util.InternationalizationManager;
import com.gestioncontactos.util.ThemeManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Locale;

public class Ventana extends JFrame {

    public JPanel contentPane;  //Panel principal que contiene todos los elementos de la ventana
    public JTextField txt_nombres; //Campo para el nombre del contacto
    public JTextField txt_telefono; //Campo para el teléfono del contacto
    public JTextField txt_email; //Campo para el email del contacto
    public JTextField txt_buscar; //Campo para buscar contactos por texto
    public JCheckBox chb_favorito; //Chexkbox para marcar un contacto como favorito
    public JComboBox<IconComboItem> cmb_categoria; // ComboBox para seleccionar la categoría del contacto (familia, amigos, trabajo, etc)
    public JButton btn_add; //Botón para agregar un nuevo contacto
    public JButton btn_modificar; //Botón para modificar el contacto seleccionado
    public JButton btn_eliminar; //Botón para eliminar el contacto seleccionado
    public JButton btn_exportar; //Botón para exportar los contactos a CSV
    public JButton btn_importarJson; //Botón para importar los contactos en archivo JSON
    public JButton btn_exportarJson; //Botón para exportar los contactos en archivo JSON
    public JList<String> lst_contactos; //Lista visual de contactos
    public JScrollPane scrLista; //ScrollPane que contiene la lista de contactos
    public JTabbedPane tabbedPane; //Pestañas principales (Contactos, Estadísticas)
    public JPanel panelContactos; //Panel para la gestión de contactos
    public JPanel panelEstadisticas; //Panel para mostrar estadísticas
    public JPanel panelFormulario;
    public JPanel panelBusqueda;
    public JPanel panelInferior;
    public JPanel panelTablaLista;
    public JTable tablaContactos; //Tabla para mostrar los contactos en formato tabular
    public DefaultTableModel modeloTabla; //Modelo de datos para la tabla de contactos
    public JProgressBar progressBar; //Barra de progreso para mostrar operaciones en curso
    public JPopupMenu popupMenu; //Menú contextual (clic derecho) sobre la tabla de contactos
    public JMenuItem menuItemEditar; //Opción para editar contacto desde el menú contextual
    public JMenuItem menuItemEliminar; //Opción para eliminar contacto desde el menú contextual
    public JPanel panelGraficas; //Panel para mostrar gráficos
    //Etiquetas para los campos y la lista
    public JLabel lbl_nombres;  //Etiqueta para el campo de nombre del contacto
    public JLabel lbl_telefono; //Etiqueta para el campo de teléfono del contacto
    public JLabel lbl_email; //Etiqueta para el campo de email del contacto
    public JLabel lbl_buscar; //Etiqueta para el campo de búsqueda de contactos
    public JLabel lbl_listaContactos; //Etiqueta para la lista visual de contactos
    private JLabel lblSearch;
    private JLabel iconLabel;
    
    // Componentes para estadísticas
    public JLabel lblTotalContactos; //Total de contactos registrados
    public JLabel lblFavoritos; //Total de contactos marcados como favoritos
    public JLabel lblFamilia; //Total de contactos en la categoría familia
    public JLabel lblAmigos; //Total de contactos en la categoría amigos
    public JLabel lblTrabajo; //Total de contactos en la categoría trabajo
    
    public JMenuBar menuBar; //Barra de menú principal de la aplicación
    
    private Logica_ventana logica; //Referencia a la lógica/controlador principal que maneja los eventos y operaciones
    
    public Ventana() {
        // Configurar idioma inicial
        InternationalizationManager.setLocale(new Locale("es"));
        
        //Aplicar el tema original por defecto
        ThemeManager.setFlatLafTheme(ThemeManager.Theme.ORIGINAL);
        
           
        setTitle(InternationalizationManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setBounds(100, 100, 800, 750);
        
        // Configurar menú de idiomas
        menuBar = new JMenuBar();
        JMenu languageMenu = new JMenu();
        
        ImageIcon languageIcon = IconManager.loadThemedIcon("idioma.png", 16, 16);
        languageMenu.setText(InternationalizationManager.getString("menu.language"));
        languageMenu.setIcon(languageIcon);
              
        //Crear items de menú con iconos
        ImageIcon spanishIcon = IconManager.loadThemedIcon("español.png",16 ,16);
        ImageIcon englishIcon = IconManager.loadThemedIcon("inglés.png",16, 16);
        ImageIcon frenchIcon = IconManager.loadThemedIcon("francés.png", 16, 16);
        
        JMenuItem spanishItem = new JMenuItem("Español",spanishIcon);
        JMenuItem englishItem = new JMenuItem("English", englishIcon);
        JMenuItem frenchItem = new JMenuItem("Français", frenchIcon);

        spanishItem.addActionListener(e -> {
            InternationalizationManager.setLocale(new Locale("es"));
            updateUITexts();
            logica.actualizarDatosIdioma();
        });

        englishItem.addActionListener(e -> {
            InternationalizationManager.setLocale(new Locale("en"));
            updateUITexts();
            logica.actualizarDatosIdioma();
        });

        frenchItem.addActionListener(e -> {
            InternationalizationManager.setLocale(new Locale("fr"));
            updateUITexts();
            logica.actualizarDatosIdioma();
        });

        languageMenu.add(spanishItem);
        languageMenu.add(englishItem);
        languageMenu.add(frenchItem);
        menuBar.add(languageMenu);
        setJMenuBar(menuBar);
        
        // Configurar menú de temas
        JMenu themeMenu = new JMenu();
        themeMenu.setText(InternationalizationManager.getString("menu.theme"));
        ImageIcon themeIcon = IconManager.loadThemedIcon("theme.png", 16, 16);
        if (themeIcon != null) {
            themeMenu.setIcon(themeIcon);
        }

        // Crear los items del menú de temas
        JMenuItem originalThemeItem = new JMenuItem("Original");
        JMenuItem lightThemeItem = new JMenuItem("Light");
        JMenuItem darkThemeItem = new JMenuItem("Dark");
        JMenuItem intellijThemeItem = new JMenuItem("IntelliJ");
        JMenuItem darculaThemeItem = new JMenuItem("Darcula");

        // Añadir iconos a los items si están disponibles
        ImageIcon originalIcon = IconManager.loadThemedIcon("original_theme.png", 16, 16);
        if(originalIcon != null) {
        	originalThemeItem.setIcon(originalIcon);
        }
        
        ImageIcon lightIcon = IconManager.loadThemedIcon("light_theme.png", 16, 16);
        if (lightIcon != null) {
            lightThemeItem.setIcon(lightIcon);
        }

        ImageIcon darkIcon = IconManager.loadThemedIcon("dark_theme.png", 16, 16);
        if (darkIcon != null) {
            darkThemeItem.setIcon(darkIcon);
        }

        ImageIcon intellijIcon = IconManager.loadThemedIcon("intellij_theme.png", 16, 16);
        if (intellijIcon != null) {
            intellijThemeItem.setIcon(intellijIcon);
        }

        ImageIcon darculaIcon = IconManager.loadThemedIcon("darcula_theme.png", 16, 16);
        if (darculaIcon != null) {
            darculaThemeItem.setIcon(darculaIcon);
        }
        
        // Añadir acciones a los items del menú
        originalThemeItem.addActionListener(e -> {
        	ThemeManager.setFlatLafTheme(ThemeManager.Theme.ORIGINAL);
        	SwingUtilities.updateComponentTreeUI(this);
        	actualizarColoresTema();
        	actualizarIconosTema();
        });
        
        lightThemeItem.addActionListener(e -> {
            ThemeManager.setFlatLafTheme(ThemeManager.Theme.LIGHT);
            SwingUtilities.updateComponentTreeUI(this);
            actualizarColoresTema();
            actualizarIconosTema();
        });

        darkThemeItem.addActionListener(e -> {
            ThemeManager.setFlatLafTheme(ThemeManager.Theme.DARK);
            SwingUtilities.updateComponentTreeUI(this);
            actualizarColoresTema();
            actualizarIconosTema();
        });

        intellijThemeItem.addActionListener(e -> {
            ThemeManager.setFlatLafTheme(ThemeManager.Theme.INTELLIJ);
            SwingUtilities.updateComponentTreeUI(this);
            actualizarColoresTema();
            actualizarIconosTema();
        });

        darculaThemeItem.addActionListener(e -> {
            ThemeManager.setFlatLafTheme(ThemeManager.Theme.DARCULA);
            SwingUtilities.updateComponentTreeUI(this);
            actualizarColoresTema();
            actualizarIconosTema();
        });

        // Añadir los items al menú de temas
        themeMenu.add(originalThemeItem);
        themeMenu.add(lightThemeItem);
        themeMenu.add(darkThemeItem);
        themeMenu.add(intellijThemeItem);
        themeMenu.add(darculaThemeItem);

        // Añadir el menú de temas a la barra de menú
        menuBar.add(themeMenu);
        
        
        // Configurar atajos de teclado globales
        configurarAtajos();
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setBackground(Colores.FONDO);
        
        // Crear el JTabbedPane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
             
        // Panel de Contactos
        panelContactos = new JPanel();
        panelContactos.setBackground(Colores.FONDO_GENERAL);
        tabbedPane.addTab(InternationalizationManager.getString("menu.contacts"), null, panelContactos, null);
        panelContactos.setLayout(new BorderLayout());
        
        // Panel superior para los campos de entrada y botones
        panelFormulario = new JPanel();
        panelFormulario.setBackground(Colores.PANEL_FORMULARIO);
        panelFormulario.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        panelFormulario.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Panel para búsqueda y resultados
        panelInferior = new JPanel();
        panelInferior.setBackground(Colores.FONDO_GENERAL);
        panelInferior.setLayout(new BorderLayout(10, 10));
        
        // Configuramos los componentes con GridBagLayout para mejor alineación
        lbl_nombres = new JLabel(InternationalizationManager.getString("label.names"));
        lbl_nombres.setFont(Fuentes.TITULO);
        lbl_nombres.setForeground(Colores.TEXTO_PRINCIPAL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelFormulario.add(lbl_nombres, gbc);
        
        txt_nombres = new JTextField();
        txt_nombres.setFont(Fuentes.TEXTO_NORMAL);
        txt_nombres.setColumns(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelFormulario.add(txt_nombres, gbc);
        
        lbl_telefono = new JLabel(InternationalizationManager.getString("label.phone"));
        lbl_telefono.setFont(Fuentes.TITULO);
        lbl_telefono.setForeground(Colores.TEXTO_PRINCIPAL);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelFormulario.add(lbl_telefono, gbc);
        
        txt_telefono = new JTextField();
        txt_telefono.setFont(Fuentes.TEXTO_NORMAL);
        txt_telefono.setColumns(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelFormulario.add(txt_telefono, gbc);
        
        lbl_email = new JLabel(InternationalizationManager.getString("label.email"));
        lbl_email.setFont(Fuentes.TITULO);
        lbl_email.setForeground(Colores.TEXTO_PRINCIPAL);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panelFormulario.add(lbl_email, gbc);
        
        txt_email = new JTextField();
        txt_email.setFont(Fuentes.TEXTO_NORMAL);
        txt_email.setColumns(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelFormulario.add(txt_email, gbc);
        
        // Panel para checkbox y combobox
        JPanel panelOpciones = new JPanel();
        panelOpciones.setBackground(Colores.PANEL_FORMULARIO);
        panelOpciones.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        chb_favorito = new JCheckBox(InternationalizationManager.getString("checkbox.favorite"));
        chb_favorito.setFont(Fuentes.TEXTO_NORMAL);
        chb_favorito.setBackground(Colores.PANEL_FORMULARIO);
        chb_favorito.setForeground(Colores.TEXTO_PRINCIPAL);
        panelOpciones.add(chb_favorito);
        
        cmb_categoria = new JComboBox<>();
        cmb_categoria.setFont(Fuentes.TEXTO_NORMAL);
        cmb_categoria.setPreferredSize(new Dimension(220, 25));
        cmb_categoria.setBackground(Colores.BOTON_PRIMARIO);
        cmb_categoria.setForeground(Colores.TEXTO_SECUNDARIO);
        panelOpciones.add(cmb_categoria);
        
        actualizarComboCategorias();
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelFormulario.add(panelOpciones, gbc);
        
        // Panel para botones
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(Colores.PANEL_FORMULARIO);
        panelBotones.setLayout(new GridLayout(2, 2, 10, 10));
        
        btn_add = new JButton(InternationalizationManager.getString("button.add"));
        configurarBotonTemaOriginal(btn_add);
        panelBotones.add(btn_add);
        
        btn_modificar = new JButton(InternationalizationManager.getString("button.modify"));
        configurarBotonTemaOriginal(btn_modificar);
        panelBotones.add(btn_modificar);
        
        btn_eliminar = new JButton(InternationalizationManager.getString("button.delete"));
        configurarBotonTemaOriginal(btn_eliminar);
        panelBotones.add(btn_eliminar);
        
        btn_exportar = new JButton(InternationalizationManager.getString("button.export"));
        configurarBotonTemaOriginal(btn_exportar);
        panelBotones.add(btn_exportar);
        
        btn_importarJson = new JButton(InternationalizationManager.getString("button.import_json"));
        configurarBotonTemaOriginal(btn_importarJson);
        panelBotones.add(btn_importarJson);
        
        btn_exportarJson = new JButton(InternationalizationManager.getString("button.export_json"));
        configurarBotonTemaOriginal(btn_exportarJson);
        panelBotones.add(btn_exportarJson);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panelFormulario.add(panelBotones, gbc);
        
        // Panel de búsqueda
        panelBusqueda = new JPanel();
        panelBusqueda.setBackground(Colores.BOTON_PRIMARIO);
        panelBusqueda.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        panelBusqueda.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setPreferredSize(new Dimension(760, 40));
        
        lbl_buscar = new JLabel(InternationalizationManager.getString("label.search"));
        lbl_buscar.setFont(Fuentes.TITULO);
        lbl_buscar.setForeground(Colores.BOTON_TEXTO);
        panelBusqueda.add(lbl_buscar);
        
        lblSearch = new JLabel(); //guardamos la referencia para poder actualizar el ícono
        ImageIcon searchIcon = IconManager.loadThemedIcon("search.png",20,20);
        if(searchIcon != null) {
            lblSearch.setIcon(searchIcon);
            lblSearch.setPreferredSize(new Dimension(20, 20));
        }
        
        panelBusqueda.add(lblSearch);
        
        txt_buscar = new JTextField();
        txt_buscar.setFont(Fuentes.TEXTO_NORMAL);
        txt_buscar.setColumns(30);
        panelBusqueda.add(txt_buscar);
        
        panelInferior.add(panelBusqueda, BorderLayout.NORTH);
        
        // Panel para la tabla y lista
        panelTablaLista = new JPanel();
        panelTablaLista.setLayout(new BorderLayout(10, 0));
        panelTablaLista.setBackground(Colores.FONDO_GENERAL);
        
        // Configurando tabla
        String[] columnas = {
            InternationalizationManager.getString("table.header.name"),
            InternationalizationManager.getString("table.header.phone"),
            InternationalizationManager.getString("table.header.email"),
            InternationalizationManager.getString("table.header.category"),
            InternationalizationManager.getString("table.header.favorite")
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return Boolean.class;
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaContactos = new JTable(modeloTabla);
        tablaContactos.setFont(Fuentes.TEXTO_NORMAL);
        tablaContactos.setRowHeight(25);
        tablaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaContactos.setGridColor(Colores.BORDE);

        tablaContactos.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(Colores.BOTON_PRIMARIO);
                label.setForeground(Color.WHITE);
                label.setFont(Fuentes.TEXTO_NORMAL);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Colores.BORDE));
                return label;
            }
        });

        tablaContactos.getTableHeader().setOpaque(true);
        tablaContactos.getTableHeader().setBackground(Colores.BOTON_PRIMARIO);
        tablaContactos.getTableHeader().setForeground(Color.WHITE);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaContactos.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(tablaContactos);
        scrollPane.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        panelTablaLista.add(scrollPane, BorderLayout.CENTER);
        
        // Inicializar la lista de contactos
        lst_contactos = new JList<>();
        lst_contactos.setFont(Fuentes.TEXTO_NORMAL);
        lst_contactos.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Configurar panel para lista de contactos
        JPanel panelLista = new JPanel(new BorderLayout());
        panelLista.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        
        // Añadir cabecera para la lista
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Colores.CABECERA_TABLA);
        headerPanel.setPreferredSize(new Dimension(230, 30));
        
        JPanel headerContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5, 0));
        headerContentPanel.setBackground(Colores.CABECERA_TABLA);
        
        ImageIcon contactListIcon = IconManager.loadThemedIcon("contactos.png",24,24);
        iconLabel = new JLabel();
        if(contactListIcon != null) {
            iconLabel.setIcon(contactListIcon);
        }
        headerContentPanel.add(iconLabel);
        
        lbl_listaContactos = new JLabel(InternationalizationManager.getString("list.title"));
        lbl_listaContactos.setForeground(Colores.TEXTO_CABECERA);
        lbl_listaContactos.setFont(Fuentes.TEXTO_NORMAL);
        headerContentPanel.add(lbl_listaContactos);
       
        headerPanel.add(headerContentPanel, BorderLayout.CENTER);
        
        panelLista.add(headerPanel, BorderLayout.NORTH);
        
        scrLista = new JScrollPane(lst_contactos);
        scrLista.setBorder(null);
        panelLista.add(scrLista, BorderLayout.CENTER);
        panelLista.setPreferredSize(new Dimension(230, 0));
        
        panelTablaLista.add(panelLista, BorderLayout.EAST);
        
        panelInferior.add(panelTablaLista, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setFont(Fuentes.TEXTO_PEQUENO);
        progressBar.setStringPainted(true);
        progressBar.setValue(25);
        progressBar.setForeground(Colores.BOTON_PRIMARIO);
        progressBar.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        panelInferior.add(progressBar, BorderLayout.SOUTH);
        
        // Añadimos los paneles principales al panel de contactos
        panelContactos.add(panelFormulario, BorderLayout.NORTH);
        panelContactos.add(panelInferior, BorderLayout.CENTER);
        
        // Menú contextual para la tabla
        popupMenu = new JPopupMenu();
        menuItemEditar = new JMenuItem(InternationalizationManager.getString("popup.edit"));
        menuItemEditar.setFont(Fuentes.TEXTO_NORMAL);
        menuItemEliminar = new JMenuItem(InternationalizationManager.getString("popup.delete"));
        menuItemEliminar.setFont(Fuentes.TEXTO_NORMAL);
        popupMenu.add(menuItemEditar);
        popupMenu.add(menuItemEliminar);
        
        // Panel de Estadísticas
        panelEstadisticas = new JPanel();
        panelEstadisticas.setBackground(Colores.FONDO_GENERAL);
        tabbedPane.addTab(InternationalizationManager.getString("menu.stats"), null, panelEstadisticas, null);
        panelEstadisticas.setLayout(new BorderLayout(0, 0));
        
        // Personalizar las pestañas
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = Colores.BORDE;
                shadow = Colores.BORDE;
                darkShadow = Colores.BORDE;
                focus = Colores.BOTON_PRIMARIO;
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2D = (Graphics2D) g;
                if (isSelected) {
                    g2D.setColor(Colores.BOTON_PRIMARIO);
                } else {
                    g2D.setColor(Colores.BARRA_TITULO);
                }
                g2D.fillRect(x, y, w, h);
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(Colores.BORDE);
                g.drawRect(x, y, w, h);
            }
            
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // No pintar el borde del contenido
            }
        });
        
        actualizarColoresPestanas();
        
        JPanel panelInfoEstadisticas = new JPanel();
        panelInfoEstadisticas.setLayout(new GridLayout(5, 1, 10, 10));
        panelInfoEstadisticas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelInfoEstadisticas.setBackground(Colores.BOTON_PRIMARIO);
        
        lblTotalContactos = new JLabel(InternationalizationManager.getString("stats.total") + " 0");
        lblTotalContactos.setFont(Fuentes.TEXTO_NORMAL);
        lblTotalContactos.setForeground(Colores.TEXTO_CABECERA);
        
        lblFavoritos = new JLabel(InternationalizationManager.getString("stats.favorites") + " 0");
        lblFavoritos.setFont(Fuentes.TEXTO_NORMAL);
        lblFavoritos.setForeground(Colores.TEXTO_CABECERA);
        
        lblFamilia = new JLabel(InternationalizationManager.getString("stats.family") + " 0");
        lblFamilia.setFont(Fuentes.TEXTO_NORMAL);
        lblFamilia.setForeground(Colores.TEXTO_CABECERA);
        
        lblAmigos = new JLabel(InternationalizationManager.getString("stats.friends") + " 0");
        lblAmigos.setFont(Fuentes.TEXTO_NORMAL);
        lblAmigos.setForeground(Colores.TEXTO_CABECERA);
        
        lblTrabajo = new JLabel(InternationalizationManager.getString("stats.work") + " 0");
        lblTrabajo.setFont(Fuentes.TEXTO_NORMAL);
        lblTrabajo.setForeground(Colores.TEXTO_CABECERA);
        
        panelInfoEstadisticas.add(lblTotalContactos);
        panelInfoEstadisticas.add(lblFavoritos);
        panelInfoEstadisticas.add(lblFamilia);
        panelInfoEstadisticas.add(lblAmigos);
        panelInfoEstadisticas.add(lblTrabajo);
        
        panelEstadisticas.add(panelInfoEstadisticas, BorderLayout.NORTH);
        
        panelGraficas = new JPanel();
        panelGraficas.setLayout(new BorderLayout());
        panelGraficas.setBackground(Colores.PANEL_FORMULARIO);
        panelEstadisticas.add(panelGraficas, BorderLayout.CENTER);
        
        // Añadir íconos a los botones
        configurarIconosBotonesTemaOriginal();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Liberar todos los bloqueos al cerrar la ventana
                ContactLockManager.getInstance().releaseAllLocks();
                
                // Cerrar los hilos del ejecutor
                if (logica != null) {
                    logica.shutdown();
                }
                
                // Cierra la aplicación
                dispose();
                System.exit(0);
            }
        });
        // Inicializar el controlador
        logica = new Logica_ventana(this);
        
        SwingUtilities.invokeLater(() ->{
        	aplicarColoresMenuTemaOriginal();
        	actualizarColoresTema();
        	actualizarIconosTema();
        	actualizarColoresPestanas();
        	revalidate();
        	repaint();
        });
    }
    //Actualiza el estado de la tabla de contactos en la interfaz gráfica
    public void actualizarTablaContactos() {
        DefaultTableModel model = (DefaultTableModel) tablaContactos.getModel();
        model.setRowCount(0);
        
        for (Persona contacto : logica.getContactos()) {
            if (!contacto.getNombre().equals("NOMBRE")) {
                model.addRow(new Object[]{
                    contacto.getNombre(),
                    contacto.getTelefono(),
                    contacto.getEmail(),
                    InternationalizationManager.getString("category." + contacto.getCategoria().toLowerCase()),
                    contacto.isFavorito()
                });
            }
        }
    }
    //Actualiza todos los textos visibles en la interfaz gráfica de usuario (UI) para reflejar la configuración actual de internacionalización
    private void updateUITexts() {
        setTitle(InternationalizationManager.getString("app.title"));
        
        menuBar.getMenu(0).setText(InternationalizationManager.getString("menu.language"));
        menuBar.getMenu(1).setText(InternationalizationManager.getString("menu.theme"));
        // Actualizar pestañas
        tabbedPane.setTitleAt(0, InternationalizationManager.getString("menu.contacts"));
        tabbedPane.setTitleAt(1, InternationalizationManager.getString("menu.stats"));
        
        lbl_nombres.setText(InternationalizationManager.getString("label.names"));
        lbl_telefono.setText(InternationalizationManager.getString("label.phone"));
        lbl_email.setText(InternationalizationManager.getString("label.email"));
        lbl_buscar.setText(InternationalizationManager.getString("label.search"));
        lbl_listaContactos.setText(InternationalizationManager.getString("list.title"));
        
        // Actualizar checkbox
        chb_favorito.setText(InternationalizationManager.getString("checkbox.favorite"));
        
        // Actualizar botones
        btn_add.setText(InternationalizationManager.getString("button.add"));
        btn_modificar.setText(InternationalizationManager.getString("button.modify"));
        btn_eliminar.setText(InternationalizationManager.getString("button.delete"));
        btn_exportar.setText(InternationalizationManager.getString("button.export"));
        btn_importarJson.setText(InternationalizationManager.getString("button.import_json"));
        btn_exportarJson.setText(InternationalizationManager.getString("button.export_json"));
        
        // Actualizar encabezados de tabla
        modeloTabla.setColumnIdentifiers(new String[] {
            InternationalizationManager.getString("table.header.name"),
            InternationalizationManager.getString("table.header.phone"),
            InternationalizationManager.getString("table.header.email"),
            InternationalizationManager.getString("table.header.category"),
            InternationalizationManager.getString("table.header.favorite")
        });
        
        // Actualizar menú contextual
        menuItemEditar.setText(InternationalizationManager.getString("popup.edit"));
        menuItemEliminar.setText(InternationalizationManager.getString("popup.delete"));
        
        // Actualizar estadísticas
        actualizarTextosEstadisticas();
        
        // Actualizar combo box de categorías
        actualizarComboCategorias();
      
        // Forzar repintado
        revalidate();
        repaint();
    }
    //Actualiza los textos de las etiquetas que muestran las estadísticas de contactos, asegurando que las descripiciones estén localizadas según el idioma actual
    private void actualizarTextosEstadisticas() {
        // Obtener los valores actuales antes de actualizar los textos
        String totalText = lblTotalContactos.getText().replaceAll("[^0-9]", "");
        String favText = lblFavoritos.getText().replaceAll("[^0-9]", "");
        String famText = lblFamilia.getText().replaceAll("[^0-9]", "");
        String amigText = lblAmigos.getText().replaceAll("[^0-9]", "");
        String trabText = lblTrabajo.getText().replaceAll("[^0-9]", "");
        
        lblTotalContactos.setText(InternationalizationManager.getString("stats.total") + " " + (totalText.isEmpty() ? "0" : totalText));
        lblFavoritos.setText(InternationalizationManager.getString("stats.favorites") + " " + (favText.isEmpty() ? "0" : favText));
        lblFamilia.setText(InternationalizationManager.getString("stats.family") + " " + (famText.isEmpty() ? "0" : famText));
        lblAmigos.setText(InternationalizationManager.getString("stats.friends") + " " + (amigText.isEmpty() ? "0" : amigText));
        lblTrabajo.setText(InternationalizationManager.getString("stats.work") + " " + (trabText.isEmpty() ? "0" : trabText));
    }
    //Acrualiza el contenido y la apariencia del combo box de categorías en la interfaz gráfica
    private void actualizarComboCategorias() {
        DefaultComboBoxModel<IconComboItem> model = new DefaultComboBoxModel<>();
        ImageIcon defaultIcon = loadIcon("/icons/default.png", 16, 16);
        ImageIcon familiaIcon = loadIcon("/icons/familia.png", 26, 26);
        ImageIcon amigosIcon = loadIcon("/icons/amigos.png", 26, 26);
        ImageIcon trabajoIcon = loadIcon("/icons/trabajo.png", 26, 26);
        
        model.addElement(new IconComboItem(InternationalizationManager.getString("category.choose"), defaultIcon));
        model.addElement(new IconComboItem(InternationalizationManager.getString("category.family"), familiaIcon));
        model.addElement(new IconComboItem(InternationalizationManager.getString("category.friends"), amigosIcon));
        model.addElement(new IconComboItem(InternationalizationManager.getString("category.work"), trabajoIcon));
        
        cmb_categoria.setModel(model);
        cmb_categoria.setRenderer(new IconListRenderer());
    }
    //Verifica si la pestaña actualmente seleccionada en el tabbedPane cprresponde al índice especificado
    private boolean isSelectedIndex(int index) {
        return tabbedPane.getSelectedIndex() == index;
    }
    //Obtiene la cadena localizada a partir de una clave dada 
    private String getLocalizedString(String key, String defaultValue) {
        try {
            return InternationalizationManager.getString(key);
        } catch (java.util.MissingResourceException e) {
            return defaultValue;
        }
    }
    //Configura los atajos de teclado para las acciones principales de la aplicación
    private void configurarAtajos() {
        KeyStroke keyStrokeAdd = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke keyStrokeDelete = KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke keyStrokeModify = KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke keyStrokeExport = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke keyStrokeImportJSON = KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke keyStrokeExportJSON = KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStrokeAdd, "agregar");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStrokeDelete, "eliminar");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStrokeModify, "modificar");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStrokeExport, "exportar");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStrokeImportJSON, "importarJSON");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStrokeExportJSON, "exportarJSON");
        
        getRootPane().getActionMap().put("agregar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btn_add.doClick();
            }
        });
        
        getRootPane().getActionMap().put("eliminar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btn_eliminar.doClick();
            }
        });
        
        getRootPane().getActionMap().put("modificar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btn_modificar.doClick();
            }
        });
        
        getRootPane().getActionMap().put("exportar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btn_exportar.doClick();
            }
        });
        
        getRootPane().getActionMap().put("importarJSON", new AbstractAction() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		btn_importarJson.doClick();
        	}
        });
        
        getRootPane().getActionMap().put("exportarJSON", new AbstractAction() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		btn_exportarJson.doClick();
        	}
        });
    }
    //Carga un ícono de la imagen desde un recurso ubicado en el classpath y lo escala a las dimensiones especificadas
    private ImageIcon loadIcon(String path, int width, int height) {
        URL url = getClass().getResource(path);
        if(url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("Imagen no encontrada: " + path);
            return null;
        }
    }
    
    /* Actualiza los colores de los componentes de la interfaz cuando se cambia el tema
    */
    private void actualizarColoresTema() {
        // Obtener colores del UIManager actual (de FlatLaf)
        Color bgColor = UIManager.getColor("Panel.background");
        Color fgColor = UIManager.getColor("Label.foreground");
        Color primaryColor = UIManager.getColor("Button.default.focusColor");
        Color secondaryColor = UIManager.getColor("TabbedPane.selectedBackground");
        
        if (bgColor == null) bgColor = Colores.FONDO_GENERAL;
        if (fgColor == null) fgColor = Colores.TEXTO_PRINCIPAL;
        if (primaryColor == null) primaryColor = Colores.BOTON_PRIMARIO;
        if (secondaryColor == null) secondaryColor = Colores.BARRA_TITULO;
        
        // Actualizar colores de los componentes principales
        contentPane.setBackground(bgColor);
        panelContactos.setBackground(bgColor);
        panelEstadisticas.setBackground(bgColor);
        panelInferior.setBackground(bgColor);
        panelTablaLista.setBackground(bgColor);
        
        // Determinar el color de texto para los botones basado en el tema
        Color buttonTextColor;
        
        // Si es tema claro, usar texto oscuro
       if(ThemeManager.getCurrentTheme() == ThemeManager.Theme.ORIGINAL) {
    	// Aplicar colores específicos del tema original
           contentPane.setBackground(Colores.FONDO);
           panelContactos.setBackground(Colores.FONDO_GENERAL);
           panelEstadisticas.setBackground(Colores.FONDO_GENERAL);
           panelInferior.setBackground(Colores.FONDO_GENERAL);
           panelTablaLista.setBackground(Colores.FONDO_GENERAL);
           
           // Botones con colores del tema original
           Color buttonBg = Colores.BOTON_PRIMARIO;
           Color buttonText = Colores.BOTON_TEXTO; // Blanco
           
           btn_add.setBackground(buttonBg);
           btn_add.setForeground(buttonText);
           btn_modificar.setBackground(buttonBg);
           btn_modificar.setForeground(buttonText);
           btn_eliminar.setBackground(buttonBg);
           btn_eliminar.setForeground(buttonText);
           btn_exportar.setBackground(buttonBg);
           btn_exportar.setForeground(buttonText);
           btn_importarJson.setBackground(buttonBg);
           btn_importarJson.setForeground(buttonText);
           btn_exportarJson.setBackground(buttonBg);
           btn_exportarJson.setForeground(buttonText);
           
           // Panel de búsqueda azul con texto blanco
           panelBusqueda.setBackground(Colores.BOTON_PRIMARIO);
           lbl_buscar.setForeground(Color.WHITE);
           
           return; // Salir temprano para evitar aplicar colores de otros temas
       }
        else if (ThemeManager.isLightTheme()) {
            buttonTextColor = Color.BLACK;
        } else {
            // Para temas oscuros, usar texto blanco
            buttonTextColor = Color.WHITE;
        }
        
        // Actualizar colores de botones
        btn_add.setBackground(primaryColor);
        btn_add.setForeground(buttonTextColor);
        
        btn_modificar.setBackground(primaryColor);
        btn_modificar.setForeground(buttonTextColor);
        
        btn_eliminar.setBackground(primaryColor);
        btn_eliminar.setForeground(buttonTextColor);
        
        btn_exportar.setBackground(primaryColor);
        btn_exportar.setForeground(buttonTextColor);
        
        btn_importarJson.setBackground(primaryColor);
        btn_importarJson.setForeground(buttonTextColor);
        
        btn_exportarJson.setBackground(primaryColor);
        btn_exportarJson.setForeground(buttonTextColor);
        
        // Actualizar el color del panel de búsqueda según el tema
        if (ThemeManager.getCurrentTheme() == ThemeManager.Theme.ORIGINAL) {
            // Solo el tema ORIGINAL tiene el panel de búsqueda azul con texto blanco
            panelBusqueda.setBackground(new Color(70, 130, 180)); // Azul del tema original
            lbl_buscar.setForeground(Color.WHITE);
        } else if (ThemeManager.isLightTheme()) {
            // Para temas claros (excepto ORIGINAL), usar el color del panel normal
            panelBusqueda.setBackground(UIManager.getColor("Panel.background"));
            lbl_buscar.setForeground(Color.BLACK);
        } else {
            // Para temas oscuros, mantener el color secundario
            panelBusqueda.setBackground(secondaryColor);
            lbl_buscar.setForeground(Color.WHITE);
        }
        
        // Actualizar colores del formulario
        panelFormulario.setBackground(UIManager.getColor("Panel.background"));
        
        // Actualizar colores del texto
        lbl_nombres.setForeground(fgColor);
        lbl_telefono.setForeground(fgColor);
        lbl_email.setForeground(fgColor);
        
        // También actualizar la barra de progreso
        progressBar.setForeground(primaryColor);
        progressBar.setBackground(bgColor);
        
        actualizarColoresPestanas();
        aplicarColoresMenuTemaOriginal();
        
        // Forzar el repintado de componentes
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
        
    }
    private void actualizarIconosTema() {
    	 // Determinar si usar iconos blancos para botones (solo en tema original)
        boolean usarIconosBlancos = (ThemeManager.getCurrentTheme() == ThemeManager.Theme.ORIGINAL);
        
        // Actualizar iconos de los botones con color condicional
        if (usarIconosBlancos) {
            // Para tema ORIGINAL - usar iconos blancos
        	 btn_add.setIcon(createWhiteIcon("add.png", 24, 24));
             btn_modificar.setIcon(createWhiteIcon("edit.png", 24, 24));
             btn_eliminar.setIcon(createWhiteIcon("delete.png", 24, 24));
             btn_exportar.setIcon(createWhiteIcon("export.png", 24, 24));
             btn_importarJson.setIcon(createWhiteIcon("import.png", 24, 24));
             btn_exportarJson.setIcon(createWhiteIcon("exportarJson.png", 24, 24));
        } else {
            // Para otros temas - usar iconos normales
            btn_add.setIcon(IconManager.updateIconForTheme("add.png", 24, 24));
            btn_modificar.setIcon(IconManager.updateIconForTheme("edit.png", 24, 24));
            btn_eliminar.setIcon(IconManager.updateIconForTheme("delete.png", 24, 24));
            btn_exportar.setIcon(IconManager.updateIconForTheme("export.png", 24, 24));
            btn_importarJson.setIcon(IconManager.updateIconForTheme("import.png", 24, 24));
            btn_exportarJson.setIcon(IconManager.updateIconForTheme("exportarJson.png", 24, 24));
        }
        
        // Actualizar iconos de los menús
        if (menuBar != null && menuBar.getMenuCount() >= 2) {
            JMenu languageMenu = menuBar.getMenu(0);
            JMenu themeMenu = menuBar.getMenu(1);
            
            // Actualizar ícono del menú de idiomas
            languageMenu.setIcon(IconManager.updateIconForTheme("idioma.png", 16, 16));
            
            // Actualizar ícono del menú de temas
            themeMenu.setIcon(IconManager.updateIconForTheme("theme.png", 16, 16));
            
            // Actualizar íconos de los items del menú de idiomas
            if (languageMenu.getItemCount() >= 3) {
                languageMenu.getItem(0).setIcon(IconManager.updateIconForTheme("español.png", 16, 16));
                languageMenu.getItem(1).setIcon(IconManager.updateIconForTheme("inglés.png", 16, 16));
                languageMenu.getItem(2).setIcon(IconManager.updateIconForTheme("francés.png", 16, 16));
            }
            
            // Actualizar íconos de los items del menú de temas
            if (themeMenu.getItemCount() >= 5) {
            	themeMenu.getItem(0).setIcon(IconManager.updateIconForTheme("original_theme.png", 16, 16));
                themeMenu.getItem(1).setIcon(IconManager.updateIconForTheme("light_theme.png", 16, 16));
                themeMenu.getItem(2).setIcon(IconManager.updateIconForTheme("dark_theme.png", 16, 16));
                themeMenu.getItem(3).setIcon(IconManager.updateIconForTheme("intellij_theme.png", 16, 16));
                themeMenu.getItem(4).setIcon(IconManager.updateIconForTheme("darcula_theme.png", 16, 16));
            }
        }
        
        // Actualizar ícono de búsqueda
        if (lblSearch != null) {
            lblSearch.setIcon(IconManager.updateIconForTheme("search.png", 20, 20));
        }
        
        // Actualizar ícono de la lista de contactos
        if (iconLabel != null) {
            iconLabel.setIcon(IconManager.updateIconForTheme("contactos.png", 24, 24));
        }
        
        // Actualizar items del menú contextual si tienen iconos
        if (menuItemEditar != null) {
            menuItemEditar.setIcon(IconManager.updateIconForTheme("edit.png", 16, 16));
        }
        if (menuItemEliminar != null) {
            menuItemEliminar.setIcon(IconManager.updateIconForTheme("delete.png", 16, 16));
        }
        
        // Forzar repintado de componentes
        revalidate();
        repaint();
    }
    
    //Actualiza los colores de las pestañas según el tema actual
   private void actualizarColoresPestanas() {
	   if (ThemeManager.getCurrentTheme() == ThemeManager.Theme.ORIGINAL) {
	        // Para el tema ORIGINAL: texto negro para ambas pestañas
	        tabbedPane.setForegroundAt(0, Color.BLACK);
	        tabbedPane.setForegroundAt(1, Color.BLACK);
	    } else if (ThemeManager.isLightTheme()) {
	        // Tema claro (pero no ORIGINAL) - texto negro
	        tabbedPane.setForegroundAt(0, Color.BLACK);
	        tabbedPane.setForegroundAt(1, Color.BLACK);
	    } else {
	        // Tema oscuro - texto blanco
	        tabbedPane.setForegroundAt(0, Color.WHITE);
	        tabbedPane.setForegroundAt(1, Color.WHITE);
	    }
   }
   
   /**
    * Crea un ícono coloreado en blanco para el tema original
    * 
    * @param iconName Nombre del archivo del ícono
    * @param width Ancho deseado
    * @param height Alto deseado
    * @return ImageIcon coloreado en blanco o el ícono original si no se puede colorear
    */
   private ImageIcon createWhiteIcon(String iconName, int width, int height) {
       ImageIcon originalIcon = IconManager.loadThemedIcon(iconName, width, height);
       if (originalIcon == null) {
           return null;
       }
       
       // Crear una nueva imagen coloreada en blanco
       BufferedImage whiteImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
       Graphics2D g2d = whiteImage.createGraphics();
       
       // Habilitar antialiasing
       g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       
       // Dibujar la imagen original
       g2d.drawImage(originalIcon.getImage(), 0, 0, null);
       
       // Aplicar color blanco manteniendo la transparencia
       g2d.setComposite(AlphaComposite.SrcAtop);
       g2d.setColor(Color.WHITE);
       g2d.fillRect(0, 0, width, height);
       
       g2d.dispose();
       
       return new ImageIcon(whiteImage);
   }
	// Método auxiliar para configurar botones con el estilo del tema original
	private void configurarBotonTemaOriginal(JButton button) {
	    button.setBackground(Colores.BOTON_PRIMARIO);
	    button.setForeground(Colores.BOTON_TEXTO); // Texto blanco
	    button.setFont(Fuentes.TEXTO_NORMAL);
	    button.setPreferredSize(new Dimension(100, 50));
	    button.setOpaque(true);
	    button.setBorderPainted(false);
	    button.setFocusPainted(false);
	}
	
	// Método auxiliar para configurar iconos de botones para tema original
	private void configurarIconosBotonesTemaOriginal() {
	    // Para el tema original, usar iconos blancos
	    ImageIcon addIcon = createWhiteIcon("add.png", 24, 24);
	    if (addIcon != null) {
	        btn_add.setIcon(addIcon);
	        btn_add.setHorizontalTextPosition(SwingConstants.CENTER);
	        btn_add.setVerticalTextPosition(SwingConstants.BOTTOM);
	    }
	    
	    ImageIcon editIcon = createWhiteIcon("edit.png", 24, 24);
	    if (editIcon != null) {
	        btn_modificar.setIcon(editIcon);
	        btn_modificar.setHorizontalTextPosition(SwingConstants.CENTER);
	        btn_modificar.setVerticalTextPosition(SwingConstants.BOTTOM);
	    }
	    
	    ImageIcon deleteIcon = createWhiteIcon("delete.png", 24, 24);
	    if (deleteIcon != null) {
	        btn_eliminar.setIcon(deleteIcon);
	        btn_eliminar.setHorizontalTextPosition(SwingConstants.CENTER);
	        btn_eliminar.setVerticalTextPosition(SwingConstants.BOTTOM);
	    }
	    
	    ImageIcon exportIcon = createWhiteIcon("export.png", 24, 24);
	    if (exportIcon != null) {
	        btn_exportar.setIcon(exportIcon);
	        btn_exportar.setHorizontalTextPosition(SwingConstants.CENTER);
	        btn_exportar.setVerticalTextPosition(SwingConstants.BOTTOM);
	    }
	    
	    ImageIcon importIcon = createWhiteIcon("import.png", 24, 24);
	    if(importIcon != null) {
	        btn_importarJson.setIcon(importIcon);
	        btn_importarJson.setHorizontalTextPosition(SwingConstants.CENTER);
	        btn_importarJson.setVerticalTextPosition(SwingConstants.BOTTOM);
	    }
	    
	    ImageIcon exportJsonIcon = createWhiteIcon("exportarJson.png", 24, 24);
	    if(exportJsonIcon != null) {
	        btn_exportarJson.setIcon(exportJsonIcon);
	        btn_exportarJson.setHorizontalTextPosition(SwingConstants.CENTER);
	        btn_exportarJson.setVerticalTextPosition(SwingConstants.BOTTOM);
	    }
	}
	
	private void aplicarColoresMenuTemaOriginal() {
	    if (ThemeManager.getCurrentTheme() == ThemeManager.Theme.ORIGINAL) {
	        // Configurar colores para el menú de idiomas
	        JMenu languageMenu = menuBar.getMenu(0);
	        languageMenu.setForeground(Color.WHITE);
	        languageMenu.setOpaque(true);
	        languageMenu.setBackground(Colores.BOTON_PRIMARIO);
	        
	        // Configurar colores para items del menú de idiomas
	        for (int i = 0; i < languageMenu.getItemCount(); i++) {
	            JMenuItem item = languageMenu.getItem(i);
	            if (item != null) {
	                item.setForeground(Color.WHITE);
	                item.setBackground(Colores.BOTON_PRIMARIO);
	                item.setOpaque(true);
	            }
	        }
	        
	        // Configurar colores para el menú de temas
	        JMenu themeMenu = menuBar.getMenu(1);
	        themeMenu.setForeground(Color.WHITE);
	        themeMenu.setOpaque(true);
	        themeMenu.setBackground(Colores.BOTON_PRIMARIO);
	        
	        // Configurar colores para items del menú de temas
	        for (int i = 0; i < themeMenu.getItemCount(); i++) {
	            JMenuItem item = themeMenu.getItem(i);
	            if (item != null) {
	                item.setForeground(Color.WHITE);
	                item.setBackground(Colores.BOTON_PRIMARIO);
	                item.setOpaque(true);
	            }
	        }
	        
	        // Configurar la barra de menú principal
	        menuBar.setBackground(Colores.BOTON_PRIMARIO);
	        menuBar.setForeground(Color.WHITE);
	        menuBar.setOpaque(true);
	    }
	}
}