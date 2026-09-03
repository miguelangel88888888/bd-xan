import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * En esta carpeta estan las clases Java que forman la aplicacion.
 * Aqui se encuentra el codigo de la interfaz, la logica del catalogo,
 * el modelo de datos y la conexion con MySQL.
 *
 * CLASE: ConexionBD
 * ------------------------------------------------------------
 * Esta clase se encarga de todo lo relacionado con la base de datos:
 *
 * 1. Define los datos de conexion a MySQL.
 * 2. Crea la base de datos biblioteca si no existe.
 * 3. Crea la tabla libros si no existe.
 * 4. Migra nombres antiguos de columnas si el proyecto ya tenia una tabla previa.
 * 5. Inserta libros iniciales cuando la tabla esta vacia.
 *
 * Separar esta responsabilidad en una clase evita repetir codigo de conexion
 * en las demas partes del programa.
 */
public class ConexionBD {
    // ============================================================
    // CONSTANTES DE CONEXION
    // ============================================================
    // SERVIDOR_URL se usa para conectarse al servidor MySQL antes de elegir
    // una base de datos. Es necesario para poder crear la base biblioteca.
    private static final String SERVIDOR_URL = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";

    // BASE_DATOS_URL ya apunta directamente a la base biblioteca.
    // Se usa cuando la base de datos ya existe y se van a consultar libros.
    private static final String BASE_DATOS_URL = "jdbc:mysql://localhost:3306/biblioteca?serverTimezone=UTC";

    // Credenciales locales de MySQL usadas por el proyecto.
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    // ============================================================
    // CONEXION
    // ============================================================
    /**
     * Abre una conexion directa a la base de datos biblioteca.
     *
     * Devuelve un objeto Connection que otras clases usan para ejecutar
     * consultas SQL mediante Statement o PreparedStatement.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(BASE_DATOS_URL, USUARIO, CLAVE);
    }

    // ============================================================
    // INICIALIZACION DE LA BASE DE DATOS
    // ============================================================
    /**
     * Prepara todo lo necesario para que la aplicacion pueda trabajar.
     *
     * Este metodo se ejecuta al iniciar Biblioteca. Si la base de datos o la
     * tabla no existen, las crea automaticamente. Si ya existen, no las borra
     * ni elimina datos del usuario.
     */
    public static void inicializarBaseDatos() throws SQLException {
        // Primero se conecta al servidor para crear la base de datos si todavia no existe.
        try (Connection conn = DriverManager.getConnection(SERVIDOR_URL, USUARIO, CLAVE);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS biblioteca");
        }

        // Luego se conecta a la base creada para preparar la tabla de libros.
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS libros (
                        codigo VARCHAR(50) PRIMARY KEY,
                        titulo VARCHAR(150) NOT NULL,
                        autor VARCHAR(150) NOT NULL,
                        genero VARCHAR(80) NOT NULL,
                        anio_publicacion INT NOT NULL,
                        copias_disponibles INT NOT NULL
                    )
                    """);
            migrarTablaAnterior(stmt);
            insertarDatosInicialesSiEstaVacia(conn);
        }
    }

    // ============================================================
    // MIGRACION DE ESTRUCTURA
    // ============================================================
    /**
     * Ajusta nombres antiguos de columnas para que bases creadas antes sigan funcionando.
     *
     * Por ejemplo, si una version anterior usaba isbn, anio o copias, este
     * metodo intenta renombrar esas columnas al formato actual.
     */
    private static void migrarTablaAnterior(Statement stmt) {
        ejecutarSiEsNecesario(stmt, "ALTER TABLE libros CHANGE COLUMN isbn codigo VARCHAR(50) NOT NULL");
        ejecutarSiEsNecesario(stmt, "ALTER TABLE libros CHANGE COLUMN anio anio_publicacion INT NOT NULL");
        ejecutarSiEsNecesario(stmt, "ALTER TABLE libros CHANGE COLUMN copias copias_disponibles INT NOT NULL");
        ejecutarSiEsNecesario(stmt, "ALTER TABLE libros ADD PRIMARY KEY (codigo)");
    }

    /**
     * Ejecuta una sentencia de migracion y continua si esa modificacion ya estaba aplicada.
     *
     * Las migraciones pueden fallar si la columna ya fue cambiada antes.
     * En ese caso se ignora el error porque no afecta el funcionamiento actual.
     */
    private static void ejecutarSiEsNecesario(Statement stmt, String sql) {
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // Si la columna o llave ya existe, no hay nada que migrar.
        }
    }

    // ============================================================
    // DATOS INICIALES
    // ============================================================
    /**
     * Inserta libros de ejemplo solo cuando la tabla no tiene registros.
     *
     * Esto permite probar la aplicacion apenas se ejecuta por primera vez.
     * Si la tabla ya contiene datos, el metodo termina y no duplica registros.
     */
    private static void insertarDatosInicialesSiEstaVacia(Connection conn) throws SQLException {
        // Verifica si ya hay datos para no duplicar los libros iniciales.
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM libros")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        // Datos base para probar la interfaz inmediatamente despues de iniciar el proyecto.
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                    INSERT INTO libros (codigo, titulo, autor, genero, anio_publicacion, copias_disponibles)
                    VALUES
                    ('ISBN-001', 'Cien anios de soledad', 'Gabriel Garcia Marquez', 'Novela', 1967, 4),
                    ('ISBN-002', 'El principito', 'Antoine de Saint-Exupery', 'Infantil', 1943, 2),
                    ('ISBN-003', 'Clean Code', 'Robert C. Martin', 'Tecnico', 2008, 1)
                    """);
        }
    }
}
