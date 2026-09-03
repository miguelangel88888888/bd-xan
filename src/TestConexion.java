/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * Contiene el codigo fuente que se compila para ejecutar el proyecto.
 *
 * CLASE: TestConexion
 * ------------------------------------------------------------
 * Esta clase no abre la interfaz grafica.
 * Sirve para probar desde consola si MySQL esta funcionando, si la base de
 * datos biblioteca se puede crear y si la tabla libros esta disponible.
 */
public class TestConexion {
    /**
     * Metodo principal de prueba.
     *
     * 1. Inicializa la base de datos.
     * 2. Abre una conexion a MySQL.
     * 3. Crea un objeto Biblioteca.
     * 4. Muestra cuantos libros hay registrados.
     */
    public static void main(String[] args) {
        try {
            ConexionBD.inicializarBaseDatos();
            try (java.sql.Connection conn = ConexionBD.getConnection()) {
                System.out.println("Conexion exitosa a MySQL y base de datos biblioteca lista.");
                Biblioteca biblioteca = new Biblioteca();
                System.out.println("Libros registrados: " + biblioteca.obtenerTodos().size());
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }
}
