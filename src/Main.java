import javax.swing.SwingUtilities;

/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * En esta carpeta esta todo el codigo fuente del sistema de biblioteca.
 * Desde aqui se compila la aplicacion, se abre la interfaz grafica y se
 * conectan las clases del modelo con la base de datos.
 *
 * CLASE: Main
 * ------------------------------------------------------------
 * Esta clase es el punto de entrada principal del programa.
 * Su unica responsabilidad es iniciar la ventana principal de la aplicacion.
 */
public class Main {
    /**
     * Metodo principal que Java ejecuta al iniciar el programa.
     *
     * SwingUtilities.invokeLater se usa para abrir la interfaz grafica en el
     * hilo de eventos de Swing. Esto evita problemas al crear o actualizar
     * componentes visuales como ventanas, botones y tablas.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
