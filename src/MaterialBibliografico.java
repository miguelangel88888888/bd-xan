/**
 * ============================================================
 * CARPETA: src
 * ============================================================
 * Contiene las clases Java del sistema: modelo, logica, conexion a base de
 * datos, interfaz grafica y clases de prueba.
 *
 * CLASE: MaterialBibliografico
 * ------------------------------------------------------------
 * Esta clase representa la informacion comun que tendria cualquier material
 * de una biblioteca, por ejemplo un libro, revista o documento.
 *
 * Es abstracta porque no se usa directamente para crear objetos. Sirve como
 * base para que otras clases hereden sus atributos y completen su tipo de
 * material mediante el metodo getTipoMaterial().
 */
public abstract class MaterialBibliografico {
    // ============================================================
    // ATRIBUTOS
    // ============================================================
    // Son privados para aplicar encapsulamiento: otras clases no los cambian
    // directamente, sino usando los metodos get y set.
    private String titulo;
    private String autor;
    private String codigo;
    private String genero;
    private int anioPublicacion;

    /**
     * Constructor de la clase base.
     *
     * Recibe los datos comunes del material y los guarda en los atributos.
     * La palabra this indica que se esta asignando el valor recibido al
     * atributo del objeto actual.
     */
    public MaterialBibliografico(String titulo, String autor, String codigo, String genero, int anioPublicacion) {
        this.titulo = titulo;
        this.autor = autor;
        this.codigo = codigo;
        this.genero = genero;
        this.anioPublicacion = anioPublicacion;
    }

    /**
     * Metodo abstracto.
     *
     * No tiene cuerpo en esta clase porque cada clase hija debe decidir que
     * texto devolver. Por ejemplo, Libro devuelve "Libro".
     */
    public abstract String getTipoMaterial();

    // ============================================================
    // GETTERS Y SETTERS
    // ============================================================
    // Los getters devuelven el valor de un atributo.
    // Los setters cambian el valor de un atributo.
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }
}
