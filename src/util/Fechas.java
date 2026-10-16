package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import excepciones.DatoInvalidoException;

// En pantalla las fechas se escriben como dd/mm/aaaa
public class Fechas {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static LocalDate parsear(String texto) throws DatoInvalidoException {
        try {
            return LocalDate.parse(texto.trim(), FORMATO);
        } catch (DateTimeParseException e) {
            throw new DatoInvalidoException("La fecha \"" + texto + "\" no es válida. Usá el formato dd/mm/aaaa.");
        }
    }

    public static String formatear(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO);
    }
}
