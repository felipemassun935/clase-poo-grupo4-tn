package excepciones;

import modelo.Alojamiento;

// Se lanza al reservar si el alojamiento ya está tomado en esas fechas
public class AlojamientoNoDisponibleException extends Exception {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta

    public AlojamientoNoDisponibleException(Alojamiento a) {
        super("El alojamiento " + a.getCodigo() + " ya está reservado en esas fechas.");
    }
}
