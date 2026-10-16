package vista;

import javax.swing.JTextField;

import excepciones.DatoInvalidoException;

// Métodos de ayuda para leer los campos de texto de las pantallas
public class Validaciones {

    // Devuelve el texto del campo, o lanza la excepción si está vacío
    public static String texto(JTextField campo, String nombreCampo) throws DatoInvalidoException {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" es obligatorio.");
        }
        return valor;
    }

    public static double numero(JTextField campo, String nombreCampo) throws DatoInvalidoException {
        String valor = texto(campo, nombreCampo).replace(",", ".");
        try {
            double numero = Double.parseDouble(valor);
            if (numero <= 0) {
                throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" tiene que ser mayor a cero.");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" tiene que ser un número.");
        }
    }

    public static int entero(JTextField campo, String nombreCampo) throws DatoInvalidoException {
        String valor = texto(campo, nombreCampo);
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo \"" + nombreCampo + "\" tiene que ser un número entero.");
        }
    }
}
