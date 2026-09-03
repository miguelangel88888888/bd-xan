/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * Contiene el codigo fuente del proyecto de biblioteca.
 *
 * CLASE: Libro
 * ------------------------------------------------------------
 * Representa un libro del catalogo.
 *
 * Hereda de MaterialBibliografico, por eso ya tiene titulo, autor, codigo,
 * genero y anio de publicacion. Esta clase agrega las copias disponibles,
 * que son propias de los libros dentro del sistema.
 */
public class Libro extends MaterialBibliografico {
    // ============================================================
    // ATRIBUTOS
    // ============================================================
    // Guarda cuantos ejemplares del libro estan disponibles.
    private int copiasDisponibles;

    /**
     * Constructor de Libro.
     *
     * super(...) envia a la clase padre los datos comunes del material.
     * Despues se guarda copiasDisponibles, que pertenece especificamente
     * a esta clase.
     */
    public Libro(String titulo, String autor, String codigo, String genero, int anioPublicacion, int copiasDisponibles) {
        super(titulo, autor, codigo, genero, anioPublicacion);
        this.copiasDisponibles = copiasDisponibles;
    }

    // ============================================================
    // METODOS DE CONSULTA
    // ============================================================
    /**
     * Devuelve el codigo del libro usando el nombre ISBN.
     *
     * En este proyecto codigo e ISBN se manejan como el mismo dato, por eso
     * este metodo llama a getCodigo().
     */
    public String getIsbn() {
        return getCodigo();
    }

    /**
     * Devuelve el anio usando un nombre corto para facilitar el uso desde otras clases.
     */
    public int getAnio() {
        return getAnioPublicacion();
    }

    /**
     * Devuelve la cantidad de copias disponibles usando un nombre corto.
     */
    public int getCopias() {
        return copiasDisponibles;
    }

    /**
     * Devuelve la cantidad de copias disponibles del libro.
     * Este nombre es mas descriptivo y se usa en la tabla y en los calculos.
     */
    public int getCopiasDisponibles() {
        return copiasDisponibles;
    }

    // ============================================================
    // METODOS DE MODIFICACION
    // ============================================================
    /**
     * Actualiza la cantidad de copias disponibles.
     */
    public void setCopiasDisponibles(int copiasDisponibles) {
        this.copiasDisponibles = copiasDisponibles;
    }

    // ============================================================
    // POLIMORFISMO Y REPRESENTACION
    // ============================================================
    /**
     * Implementa el metodo abstracto de MaterialBibliografico.
     *
     * Gracias a esto, si en el futuro existen otros materiales, cada uno
     * podra devolver su propio tipo.
     */
    @Override
    public String getTipoMaterial() {
        return "Libro";
    }

    /**
     * Genera un texto resumido del libro para mostrarlo facilmente en consola o depuracion.
     */
    @Override
    public String toString() {
        return getTitulo() + " - " + getAutor() + " (" + getAnioPublicacion() + ")";
    }
}
