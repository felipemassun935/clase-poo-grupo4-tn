package persistencia;

import modelo.Dueno;

// Formato: dni;nombre;apellido;telefono;email;direccion
public class RepositorioDuenos extends RepositorioArchivo<Dueno> {

    public RepositorioDuenos(String rutaArchivo) {
        super(rutaArchivo);
    }

    @Override
    protected String aLinea(Dueno d) {
        return limpiar(d.getDni()) + SEPARADOR + limpiar(d.getNombre()) + SEPARADOR + limpiar(d.getApellido())
                + SEPARADOR + limpiar(d.getTelefono()) + SEPARADOR + limpiar(d.getEmail())
                + SEPARADOR + limpiar(d.getDireccion());
    }

    @Override
    protected Dueno desdeLinea(String[] c) {
        return new Dueno(c[0], c[1], c[2], c[3], c[4], c[5]);
    }
}
