package excepciones;

// Excepción propia (checked): se lanza cuando un dato cargado en un formulario no es válido
// (campo vacío, fecha mal escrita, DNI repetido, etc.)
public class DatoInvalidoException extends Exception {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una Exception

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
