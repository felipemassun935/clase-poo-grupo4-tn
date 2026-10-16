package persistencia;

import modelo.Empleado;
import modelo.RolEmpleado;

// Formato: dni;nombre;apellido;telefono;email;legajo;rol
public class RepositorioEmpleados extends RepositorioArchivo<Empleado> {

    public RepositorioEmpleados(String rutaArchivo) {
        super(rutaArchivo);
    }

    @Override
    protected String aLinea(Empleado e) {
        return limpiar(e.getDni()) + SEPARADOR + limpiar(e.getNombre()) + SEPARADOR + limpiar(e.getApellido())
                + SEPARADOR + limpiar(e.getTelefono()) + SEPARADOR + limpiar(e.getEmail())
                + SEPARADOR + e.getLegajo() + SEPARADOR + e.getRol();
    }

    @Override
    protected Empleado desdeLinea(String[] c) {
        return new Empleado(c[0], c[1], c[2], c[3], c[4], Integer.parseInt(c[5]), RolEmpleado.valueOf(c[6]));
    }
}
