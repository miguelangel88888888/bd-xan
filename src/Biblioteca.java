import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * Esta carpeta contiene el codigo fuente del sistema de biblioteca.
 * Aqui estan las clases que representan los libros, la ventana grafica,
 * la conexion con MySQL y la logica que administra el catalogo.
 *
 * CLASE: Biblioteca
 * ------------------------------------------------------------
 * Esta clase funciona como capa de logica del sistema.
 *
 * Su trabajo es recibir ordenes desde la interfaz grafica y convertirlas en
 * operaciones sobre la base de datos. Por ejemplo: agregar, consultar,
 * buscar, actualizar, eliminar y contar libros.
 *
 * Tambien mantiene indices en memoria para hacer mas facil la busqueda por
 * autor y la validacion de codigos repetidos.
 */
public class Biblioteca {
    // ============================================================
    // ATRIBUTOS E INDICES EN MEMORIA
    // ============================================================
    // Guarda los libros agrupados por autor normalizado.
    // Ejemplo de clave: "gabriel garcia marquez".
    private final HashMap<String, ArrayList<Libro>> librosPorAutor;

    // Guarda los codigos ya registrados para detectar duplicados rapidamente.
    private final HashSet<String> codigosRegistrados;

    // Guarda el ultimo error para que la interfaz pueda mostrarlo al usuario.
    private String ultimoError;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    /**
     * Constructor de Biblioteca.
     *
     * 1. Crea las colecciones en memoria.
     * 2. Inicializa la base de datos.
     * 3. Reconstruye los indices a partir de los libros guardados en MySQL.
     *
     * Si ocurre un error de conexion, se guarda en ultimoError para que la
     * ventana pueda avisarle al usuario.
     */
    public Biblioteca() {
        librosPorAutor = new HashMap<>();
        codigosRegistrados = new HashSet<>();
        ultimoError = "";

        try {
            ConexionBD.inicializarBaseDatos();
            reconstruirIndices();
        } catch (SQLException e) {
            ultimoError = "No se pudo iniciar la base de datos: " + e.getMessage();
        }
    }

    // ============================================================
    // OPERACIONES CRUD
    // CRUD significa: Create, Read, Update, Delete.
    // En espanol: crear, leer, actualizar y eliminar.
    // ============================================================
    /**
     * Guarda un libro nuevo en la tabla libros.
     *
     * Antes de insertar, normaliza el codigo y revisa si ya existe en el
     * HashSet codigosRegistrados. Esto evita duplicar libros por codigo o ISBN.
     *
     * Devuelve true si el libro se agrego correctamente.
     * Devuelve false si el codigo ya existe o si hubo un error de base de datos.
     */
    public boolean agregarLibro(Libro libro) {
        String codigoNormalizado = normalizar(libro.getCodigo());
        if (codigosRegistrados.contains(codigoNormalizado)) {
            ultimoError = "Ya existe un libro con ese codigo o ISBN.";
            return false;
        }

        String sql = """
                INSERT INTO libros (codigo, titulo, autor, genero, anio_publicacion, copias_disponibles)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        // PreparedStatement permite enviar valores a la consulta de forma segura y ordenada.
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, libro.getCodigo());
            stmt.setString(2, libro.getTitulo());
            stmt.setString(3, libro.getAutor());
            stmt.setString(4, libro.getGenero());
            stmt.setInt(5, libro.getAnioPublicacion());
            stmt.setInt(6, libro.getCopiasDisponibles());
            stmt.executeUpdate();

            // Despues de modificar la base de datos se actualizan los indices en memoria.
            reconstruirIndices();
            ultimoError = "";
            return true;
        } catch (SQLException e) {
            ultimoError = "Error al agregar libro: " + e.getMessage();
            return false;
        }
    }

    /**
     * Lee todos los libros registrados.
     *
     * Usa consultarLibros(...) para ejecutar el SELECT y luego ordena el
     * resultado alfabeticamente por titulo sin distinguir mayusculas.
     */
    public ArrayList<Libro> obtenerTodos() {
        ArrayList<Libro> catalogo = consultarLibros("""
                SELECT codigo, titulo, autor, genero, anio_publicacion, copias_disponibles
                FROM libros
                """);
        catalogo.sort(Comparator.comparing(Libro::getTitulo, String.CASE_INSENSITIVE_ORDER));
        return catalogo;
    }

    /**
     * Elimina de la base de datos el libro identificado por su codigo.
     *
     * El metodo revisa cuantas filas fueron eliminadas:
     * - Si filas > 0, el libro existia y fue borrado.
     * - Si filas == 0, no habia ningun libro con ese codigo.
     */
    public boolean eliminarLibro(String codigo) {
        String sql = "DELETE FROM libros WHERE codigo = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            int filas = stmt.executeUpdate();

            if (filas > 0) {
                reconstruirIndices();
                ultimoError = "";
                return true;
            }

            ultimoError = "No existe un libro con ese codigo.";
            return false;
        } catch (SQLException e) {
            ultimoError = "Error al eliminar libro: " + e.getMessage();
            return false;
        }
    }

    /**
     * Actualiza los datos editables de un libro existente.
     *
     * El codigo se usa en el WHERE como identificador del registro.
     * Por eso se actualizan titulo, autor, genero, anio y copias, pero no se
     * cambia el codigo del libro.
     */
    public boolean actualizarLibro(Libro libro) {
        String sql = """
                UPDATE libros
                SET titulo = ?, autor = ?, genero = ?, anio_publicacion = ?, copias_disponibles = ?
                WHERE codigo = ?
                """;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setString(3, libro.getGenero());
            stmt.setInt(4, libro.getAnioPublicacion());
            stmt.setInt(5, libro.getCopiasDisponibles());
            stmt.setString(6, libro.getCodigo());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                reconstruirIndices();
                ultimoError = "";
                return true;
            }

            ultimoError = "No existe un libro con ese codigo para actualizar.";
            return false;
        } catch (SQLException e) {
            ultimoError = "Error al actualizar libro: " + e.getMessage();
            return false;
        }
    }

    // ============================================================
    // BUSQUEDAS Y FILTROS
    // ============================================================
    /**
     * Busca libros por autor usando el indice librosPorAutor.
     *
     * El texto se normaliza para que la busqueda funcione aunque el usuario
     * escriba con mayusculas, minusculas o espacios extra.
     */
    public ArrayList<Libro> filtrarPorAutor(String autor) {
        ArrayList<Libro> resultado = new ArrayList<>();
        String textoBuscado = normalizar(autor);

        // Permite coincidencias parciales, por ejemplo "garcia" encuentra "gabriel garcia marquez".
        for (String autorRegistrado : librosPorAutor.keySet()) {
            if (autorRegistrado.contains(textoBuscado)) {
                resultado.addAll(librosPorAutor.get(autorRegistrado));
            }
        }

        resultado.sort(Comparator.comparing(Libro::getTitulo, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    /**
     * Busca libros cuyo titulo contenga el texto indicado.
     *
     * A diferencia de la busqueda por autor, aqui se recorre todo el catalogo
     * porque no hay un HashMap por titulo.
     */
    public ArrayList<Libro> buscarPorTitulo(String titulo) {
        ArrayList<Libro> resultado = new ArrayList<>();
        String textoBuscado = normalizar(titulo);

        // Recorre el catalogo completo porque el titulo puede coincidir de forma parcial.
        for (Libro libro : obtenerTodos()) {
            if (normalizar(libro.getTitulo()).contains(textoBuscado)) {
                resultado.add(libro);
            }
        }

        resultado.sort(Comparator.comparing(Libro::getTitulo, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    /**
     * Indica si ya existe un libro con el codigo recibido.
     *
     * Se usa el HashSet porque permite verificar existencia de forma rapida.
     */
    public boolean existeCodigo(String codigo) {
        return codigosRegistrados.contains(normalizar(codigo));
    }

    // ============================================================
    // CONTEO Y ERRORES
    // ============================================================
    /**
     * Suma todas las copias disponibles del catalogo usando un ciclo while.
     *
     * Este metodo demuestra el uso de ciclos y permite saber cuantos ejemplares
     * hay disponibles entre todos los libros registrados.
     */
    public int contarLibrosDisponibles() {
        ArrayList<Libro> catalogo = obtenerTodos();
        int total = 0;
        int indice = 0;

        while (indice < catalogo.size()) {
            total += catalogo.get(indice).getCopiasDisponibles();
            indice++;
        }

        return total;
    }

    /**
     * Devuelve el ultimo mensaje de error producido por una operacion.
     *
     * La interfaz llama este metodo para decidir si debe mostrar un mensaje
     * de error al usuario.
     */
    public String getUltimoError() {
        return ultimoError;
    }

    // ============================================================
    // METODOS AUXILIARES INTERNOS
    // ============================================================
    /**
     * Ejecuta una consulta SQL de libros y convierte cada fila en un objeto Libro.
     *
     * Es privado porque solo Biblioteca necesita saber como transformar los
     * datos de MySQL en objetos Java.
     */
    private ArrayList<Libro> consultarLibros(String sql) {
        ArrayList<Libro> catalogo = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                catalogo.add(crearLibro(rs));
            }
            ultimoError = "";
        } catch (SQLException e) {
            ultimoError = "Error al consultar libros: " + e.getMessage();
        }

        return catalogo;
    }

    /**
     * Reconstruye los indices en memoria con la informacion actual de la base de datos.
     *
     * Se llama despues de agregar, actualizar o eliminar libros para que las
     * busquedas y validaciones siempre usen datos actualizados.
     */
    private void reconstruirIndices() {
        librosPorAutor.clear();
        codigosRegistrados.clear();

        // Cada autor normalizado apunta a la lista de libros escritos por ese autor.
        for (Libro libro : obtenerTodos()) {
            codigosRegistrados.add(normalizar(libro.getCodigo()));
            String autorNormalizado = normalizar(libro.getAutor());
            librosPorAutor.computeIfAbsent(autorNormalizado, clave -> new ArrayList<>()).add(libro);
        }
    }

    /**
     * Crea un objeto Libro a partir de la fila actual de un ResultSet.
     *
     * ResultSet representa los resultados que devuelve una consulta SQL.
     * Cada getString o getInt lee una columna de la fila actual.
     */
    private Libro crearLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("codigo"),
                rs.getString("genero"),
                rs.getInt("anio_publicacion"),
                rs.getInt("copias_disponibles")
        );
    }

    /**
     * Limpia espacios y pasa texto a minusculas para comparar sin depender de mayusculas.
     *
     * Ejemplo: "  GARCIA  " se convierte en "garcia".
     */
    private String normalizar(String texto) {
        return texto.trim().toLowerCase();
    }
}
