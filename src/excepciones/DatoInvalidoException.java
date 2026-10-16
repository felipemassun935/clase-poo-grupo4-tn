package excepciones;

// Se lanza cuando un dato cargado en una pantalla no es válido
// (campo vacío, fecha mal escrita, DNI repetido, etc.)
public class DatoInvalidoException extends Exception {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
