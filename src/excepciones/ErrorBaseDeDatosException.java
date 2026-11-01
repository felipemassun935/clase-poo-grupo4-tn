package excepciones;

// Excepción propia NO chequeada (unchecked, hereda de RuntimeException): se lanza cuando falla la base de datos
// (no se puede conectar, falta el driver, una consulta SQL dio error).
// Envuelve a la SQLException original, que es chequeada, para no obligar a todas las pantallas a atraparla.
public class ErrorBaseDeDatosException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ErrorBaseDeDatosException(String mensaje, Throwable causa) {
        super(mensaje + "\n\nDetalle: " + causa.getMessage(), causa);
    }
}
