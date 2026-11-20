package vista;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import excepciones.DatoInvalidoException;

// Métodos de ayuda para convertir lo que escribe el usuario en las pantallas.
// Si algo está mal escrito tiran DatoInvalidoException con un mensaje claro.
public class Validaciones {
    // Formato de fecha que se usa en todas las pantallas
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Devuelve el texto sin espacios de más, o tira la excepción si está vacío
    public static String texto(String valor, String nombreCampo) throws DatoInvalidoException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" es obligatorio.");
        }
        return valor.trim();
    }

    // Convierte el texto a número con decimales (acepta coma o punto)
    public static double numero(String valor, String nombreCampo) throws DatoInvalidoException {
        String limpio = texto(valor, nombreCampo).replace(",", ".");
        // try/catch: si el texto no es un número, parseDouble tira NumberFormatException
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" tiene que ser un número.");
        }
    }

    // Convierte el texto a número entero
    public static int entero(String valor, String nombreCampo) throws DatoInvalidoException {
        String limpio = texto(valor, nombreCampo);
        // try/catch: si el texto no es un entero, parseInt tira NumberFormatException
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" tiene que ser un número entero.");
        }
    }

    // Convierte un texto dd/MM/yyyy a LocalDate
    public static LocalDate parsearFecha(String valor, String nombreCampo) throws DatoInvalidoException {
        String limpio = texto(valor, nombreCampo);
        // try/catch: si la fecha está mal escrita, parse tira DateTimeParseException
        try {
            return LocalDate.parse(limpio, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" tiene que ser una fecha dd/mm/aaaa.");
        }
    }

    // Muestra una fecha como dd/MM/yyyy. Si no hay fecha devuelve un texto vacío.
    public static String formatearFecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_FECHA);
    }

    // Muestra un importe con 2 decimales
    public static String plata(double monto) {
        return "$" + String.format("%.2f", monto);
    }
}
