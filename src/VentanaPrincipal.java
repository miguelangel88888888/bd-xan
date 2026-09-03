import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Year;
import java.util.ArrayList;

/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * Esta carpeta contiene el codigo fuente de la aplicacion.
 * En ella estan las clases que se compilan para ejecutar el sistema:
 * modelo de datos, conexion a MySQL, logica de biblioteca e interfaz Swing.
 *
 * CLASE: VentanaPrincipal
 * ------------------------------------------------------------
 * Esta clase crea la ventana de escritorio del sistema de biblioteca.
 *
 * Sus responsabilidades son:
 * 1. Mostrar una tabla con los libros registrados.
 * 2. Mostrar un formulario para agregar o editar libros.
 * 3. Crear los botones de accion.
 * 4. Escuchar los eventos del usuario.
 * 5. Enviar las operaciones a la clase Biblioteca.
 * 6. Mostrar mensajes de exito, error o confirmacion.
 */
public class VentanaPrincipal extends JFrame {
    // ============================================================
    // CONSTANTES DE LA INTERFAZ
    // ============================================================
    // Nombres de las columnas que aparecen en la JTable.
    private static final String[] COLUMNAS_TABLA = {"Titulo", "Autor", "Codigo", "Genero", "Anio", "Copias"};

    // Opciones que aparecen en el JComboBox de genero.
    private static final String[] GENEROS = {"Novela", "Ciencia", "Historia", "Infantil", "Tecnico", "Otro"};

    // ============================================================
    // OBJETOS PRINCIPALES
    // ============================================================
    // Biblioteca contiene la logica del catalogo y el acceso a la base de datos.
    private final Biblioteca biblioteca;

    // JTable muestra los datos. DefaultTableModel permite agregar y limpiar filas.
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    // ============================================================
    // CAMPOS DEL FORMULARIO
    // ============================================================
    // Estos campos capturan los datos para crear o actualizar un libro.
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtCodigo;
    private JComboBox<String> cmbGenero;
    private JTextField txtAnio;
    private JTextField txtCopias;

    // Estos campos se usan solo para buscar por titulo o filtrar por autor.
    private JTextField txtBuscarTitulo;
    private JTextField txtBuscarAutor;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    /**
     * Constructor de la ventana.
     *
     * Aqui se configura el tamano, titulo, cierre de ventana, ubicacion y
     * distribucion general. Despues se crean la tabla, el panel inferior y se
     * cargan los libros que ya existen en la base de datos.
     */
    public VentanaPrincipal() {
        biblioteca = new Biblioteca();

        setTitle("Sistema de Gestion de Biblioteca");
        setSize(980, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        configurarTabla();
        configurarPanelInferior();
        mostrarTodos();
    }

    // ============================================================
    // CREACION DE COMPONENTES VISUALES
    // ============================================================
    /**
     * Crea la tabla donde se muestran los libros.
     *
     * El modelo de la tabla define las columnas y bloquea la edicion directa
     * de las celdas. Para editar un libro se selecciona una fila, se cargan sus
     * datos en el formulario y luego se presiona Actualizar.
     */
    private void configurarTabla() {
        modeloTabla = new DefaultTableModel(COLUMNAS_TABLA, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Arial", Font.PLAIN, 14));
        // Cuando el usuario selecciona un libro, sus datos pasan al formulario para editarlo.
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    /**
     * Une el formulario y los botones en la parte inferior de la ventana.
     *
     * BorderLayout.CENTER contiene los campos del libro.
     * BorderLayout.SOUTH contiene los botones y campos de busqueda.
     */
    private void configurarPanelInferior() {
        JPanel panelForm = crearPanelFormulario();
        JPanel panelBotones = crearPanelBotones();

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelForm, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);
    }

    /**
     * Construye el formulario usado para registrar o editar los datos de un libro.
     *
     * GridLayout(6, 2) organiza los componentes en seis filas y dos columnas:
     * una etiqueta a la izquierda y el campo correspondiente a la derecha.
     */
    private JPanel crearPanelFormulario() {
        JPanel panelForm = new JPanel(new GridLayout(6, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Libro"));

        txtTitulo = new JTextField();
        txtAutor = new JTextField();
        txtCodigo = new JTextField();
        cmbGenero = new JComboBox<>(GENEROS);
        txtAnio = new JTextField();
        txtCopias = new JTextField();

        panelForm.add(new JLabel("Titulo:"));
        panelForm.add(txtTitulo);
        panelForm.add(new JLabel("Autor:"));
        panelForm.add(txtAutor);
        panelForm.add(new JLabel("Codigo / ISBN:"));
        panelForm.add(txtCodigo);
        panelForm.add(new JLabel("Genero:"));
        panelForm.add(cmbGenero);
        panelForm.add(new JLabel("Anio:"));
        panelForm.add(txtAnio);
        panelForm.add(new JLabel("Copias:"));
        panelForm.add(txtCopias);

        return panelForm;
    }

    /**
     * Crea los botones de accion y conecta cada boton con su metodo correspondiente.
     *
     * Cada addActionListener indica que debe pasar cuando el usuario hace clic
     * en un boton. Por ejemplo, btnAgregar llama al metodo agregarLibro().
     */
    private JPanel crearPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        panelBotones.setBorder(BorderFactory.createTitledBorder("Acciones"));

        JButton btnAgregar = new JButton("Agregar");
        JButton btnMostrar = new JButton("Mostrar Todos");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnLimpiar = new JButton("Limpiar Campos");
        JButton btnBuscarTitulo = new JButton("Buscar Titulo");
        JButton btnBuscarAutor = new JButton("Filtrar Autor");
        JButton btnContar = new JButton("Contar Copias");

        txtBuscarTitulo = new JTextField(14);
        txtBuscarAutor = new JTextField(14);

        // Eventos de los botones: cada accion de la interfaz llama a un metodo de esta clase.
        btnAgregar.addActionListener(e -> agregarLibro());
        btnMostrar.addActionListener(e -> mostrarTodos());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnActualizar.addActionListener(e -> actualizarLibro());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnBuscarTitulo.addActionListener(e -> buscarPorTitulo());
        btnBuscarAutor.addActionListener(e -> filtrarPorAutor());
        btnContar.addActionListener(e -> mostrarTotalCopias());

        panelBotones.add(new JLabel("Titulo:"));
        panelBotones.add(txtBuscarTitulo);
        panelBotones.add(btnBuscarTitulo);
        panelBotones.add(new JLabel("Autor:"));
        panelBotones.add(txtBuscarAutor);
        panelBotones.add(btnBuscarAutor);
        panelBotones.add(btnAgregar);
        panelBotones.add(btnMostrar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnContar);

        return panelBotones;
    }

    // ============================================================
    // ACCIONES PRINCIPALES DE LOS BOTONES
    // ============================================================
    /**
     * Agrega un libro nuevo.
     *
     * Flujo:
     * 1. Valida que los campos esten completos y sean correctos.
     * 2. Crea un objeto Libro con los datos del formulario.
     * 3. Pide a Biblioteca que lo guarde en MySQL.
     * 4. Si todo sale bien, actualiza la tabla y limpia los campos.
     */
    private void agregarLibro() {
        if (!validarCampos()) {
            return;
        }

        Libro libro = crearLibroDesdeCampos();
        if (biblioteca.agregarLibro(libro)) {
            JOptionPane.showMessageDialog(this, "Libro agregado correctamente.");
            mostrarTodos();
            limpiarCampos();
        } else {
            mostrarErrorOperacion("Ya existe un libro con ese codigo o ISBN.");
        }
    }

    /**
     * Carga todos los libros desde la base de datos y los muestra en la tabla.
     *
     * Este metodo tambien se usa para quitar filtros y volver a ver el catalogo completo.
     */
    private void mostrarTodos() {
        actualizarTabla(biblioteca.obtenerTodos());
        mostrarErrorBaseDatosSiExiste();
    }

    /**
     * Elimina el libro seleccionado en la tabla.
     *
     * Primero verifica que haya una fila seleccionada. Luego toma el codigo de
     * esa fila, pide confirmacion y llama a Biblioteca para borrar el registro.
     */
    private void eliminarLibro() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro en la tabla para eliminar.");
            return;
        }

        String codigo = (String) modeloTabla.getValueAt(fila, 2);
        // La confirmacion evita borrar un registro por accidente.
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Seguro que deseas eliminar este libro?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (biblioteca.eliminarLibro(codigo)) {
                mostrarTodos();
                limpiarCampos();
                JOptionPane.showMessageDialog(this, "Libro eliminado correctamente.");
            } else {
                mostrarErrorOperacion("No se pudo eliminar el libro.");
            }
        }
    }

    /**
     * Actualiza en la base de datos el libro que tiene el codigo escrito en el formulario.
     *
     * Se usa normalmente despues de seleccionar una fila de la tabla, porque
     * cargarSeleccion() copia los datos al formulario.
     */
    private void actualizarLibro() {
        if (!validarCampos()) {
            return;
        }

        Libro libro = crearLibroDesdeCampos();
        if (biblioteca.actualizarLibro(libro)) {
            JOptionPane.showMessageDialog(this, "Libro actualizado correctamente.");
            mostrarTodos();
            limpiarCampos();
        } else {
            mostrarErrorOperacion("No existe un libro con ese codigo para actualizar.");
        }
    }

    /**
     * Busca libros por una parte del titulo ingresado en el campo de busqueda.
     *
     * Si el campo esta vacio, muestra un mensaje y no consulta la base de datos.
     */
    private void buscarPorTitulo() {
        String titulo = txtBuscarTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un titulo para buscar.");
            return;
        }

        mostrarResultados(biblioteca.buscarPorTitulo(titulo), "No se encontro ningun libro con ese titulo.");
    }

    /**
     * Filtra la tabla para mostrar solo libros cuyo autor coincida con el texto ingresado.
     *
     * La busqueda permite coincidencias parciales, por ejemplo escribir solo
     * una parte del nombre del autor.
     */
    private void filtrarPorAutor() {
        String autor = txtBuscarAutor.getText().trim();
        if (autor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un autor para filtrar.");
            return;
        }

        mostrarResultados(biblioteca.filtrarPorAutor(autor), "No se encontro ningun libro de ese autor.");
    }

    // ============================================================
    // ACTUALIZACION DE LA TABLA Y RESULTADOS
    // ============================================================
    /**
     * Muestra una lista de resultados y avisa si no hubo coincidencias o si ocurrio un error.
     *
     * Este metodo evita repetir la misma logica en buscarPorTitulo() y
     * filtrarPorAutor().
     */
    private void mostrarResultados(ArrayList<Libro> libros, String mensajeVacio) {
        actualizarTabla(libros);
        if (!biblioteca.getUltimoError().isEmpty()) {
            JOptionPane.showMessageDialog(this, biblioteca.getUltimoError());
        } else if (libros.isEmpty()) {
            JOptionPane.showMessageDialog(this, mensajeVacio);
        }
    }

    /**
     * Calcula y muestra la suma de todas las copias disponibles.
     *
     * La suma real se hace en Biblioteca. Esta clase solo muestra el resultado
     * en un cuadro de dialogo.
     */
    private void mostrarTotalCopias() {
        int total = biblioteca.contarLibrosDisponibles();
        if (biblioteca.getUltimoError().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Copias disponibles: " + total);
        } else {
            JOptionPane.showMessageDialog(this, biblioteca.getUltimoError());
        }
    }

    /**
     * Reemplaza el contenido de la tabla por la lista de libros recibida.
     *
     * setRowCount(0) borra las filas actuales.
     * Luego el ciclo for agrega una fila por cada libro recibido.
     */
    private void actualizarTabla(ArrayList<Libro> libros) {
        modeloTabla.setRowCount(0);
        for (Libro libro : libros) {
            modeloTabla.addRow(new Object[]{
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getCodigo(),
                    libro.getGenero(),
                    libro.getAnioPublicacion(),
                    libro.getCopiasDisponibles()
            });
        }
    }

    // ============================================================
    // MANEJO DEL FORMULARIO
    // ============================================================
    /**
     * Copia los datos de la fila seleccionada hacia los campos del formulario.
     *
     * Esto facilita editar un libro: el usuario selecciona una fila, modifica
     * los campos necesarios y presiona Actualizar.
     */
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila != -1) {
            txtTitulo.setText((String) modeloTabla.getValueAt(fila, 0));
            txtAutor.setText((String) modeloTabla.getValueAt(fila, 1));
            txtCodigo.setText((String) modeloTabla.getValueAt(fila, 2));
            cmbGenero.setSelectedItem((String) modeloTabla.getValueAt(fila, 3));
            txtAnio.setText(modeloTabla.getValueAt(fila, 4).toString());
            txtCopias.setText(modeloTabla.getValueAt(fila, 5).toString());
        }
    }

    /**
     * Deja vacios los campos del formulario, los campos de busqueda y la seleccion de la tabla.
     *
     * Tambien regresa el combo de genero a su primera opcion.
     */
    private void limpiarCampos() {
        txtTitulo.setText("");
        txtAutor.setText("");
        txtCodigo.setText("");
        cmbGenero.setSelectedIndex(0);
        txtAnio.setText("");
        txtCopias.setText("");
        txtBuscarTitulo.setText("");
        txtBuscarAutor.setText("");
        tabla.clearSelection();
    }

    // ============================================================
    // VALIDACION Y CREACION DE OBJETOS
    // ============================================================
    /**
     * Comprueba que los campos obligatorios existan y que anio y copias sean valores validos.
     *
     * Reglas:
     * - Titulo, autor, codigo, anio y copias no pueden estar vacios.
     * - Anio debe ser un numero entero mayor que 0.
     * - Anio no puede ser mayor al anio actual.
     * - Copias debe ser un numero entero mayor o igual a 0.
     */
    private boolean validarCampos() {
        if (txtTitulo.getText().trim().isEmpty()
                || txtAutor.getText().trim().isEmpty()
                || txtCodigo.getText().trim().isEmpty()
                || txtAnio.getText().trim().isEmpty()
                || txtCopias.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos obligatorios.");
            return false;
        }

        try {
            // El anio no puede ser futuro y las copias no pueden ser negativas.
            int anio = Integer.parseInt(txtAnio.getText().trim());
            int copias = Integer.parseInt(txtCopias.getText().trim());
            int anioActual = Year.now().getValue();

            if (anio <= 0 || anio > anioActual) {
                JOptionPane.showMessageDialog(this, "El anio debe ser mayor que 0 y no puede superar " + anioActual + ".");
                return false;
            }

            if (copias < 0) {
                JOptionPane.showMessageDialog(this, "Las copias disponibles deben ser mayor o igual a 0.");
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El anio y las copias deben ser numeros enteros.");
            return false;
        }

        return true;
    }

    /**
     * Convierte los valores escritos en el formulario en un objeto Libro.
     *
     * Este metodo se llama despues de validarCampos(), por eso aqui se asume
     * que anio y copias ya se pueden convertir a numeros enteros.
     */
    private Libro crearLibroDesdeCampos() {
        return new Libro(
                txtTitulo.getText().trim(),
                txtAutor.getText().trim(),
                txtCodigo.getText().trim(),
                cmbGenero.getSelectedItem().toString(),
                Integer.parseInt(txtAnio.getText().trim()),
                Integer.parseInt(txtCopias.getText().trim())
        );
    }

    // ============================================================
    // MENSAJES DE ERROR
    // ============================================================
    /**
     * Muestra el error real de Biblioteca o un mensaje por defecto si no hay detalle tecnico.
     *
     * Esto permite mostrar errores mas especificos cuando vienen de MySQL,
     * pero conservar un mensaje entendible si no hay detalle.
     */
    private void mostrarErrorOperacion(String mensajePorDefecto) {
        String error = biblioteca.getUltimoError();
        JOptionPane.showMessageDialog(this, error.isEmpty() ? mensajePorDefecto : error);
    }

    /**
     * Avisa al usuario cuando la ultima operacion dejo un error de base de datos.
     *
     * Se usa despues de cargar libros para informar problemas de conexion o consulta.
     */
    private void mostrarErrorBaseDatosSiExiste() {
        if (!biblioteca.getUltimoError().isEmpty()) {
            JOptionPane.showMessageDialog(this, biblioteca.getUltimoError());
        }
    }

    // ============================================================
    // EJECUCION DIRECTA DE ESTA CLASE
    // ============================================================
    /**
     * Permite ejecutar esta ventana directamente sin pasar por la clase Main.
     *
     * Es util si desde el IDE se quiere ejecutar VentanaPrincipal directamente.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
