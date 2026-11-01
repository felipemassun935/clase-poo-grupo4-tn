package modelo.enums;

// Tipos de acción que quedan guardados en el log del sistema (quién hizo qué y cuándo)
public enum AccionLog {
    INGRESO("Ingreso al sistema"),
    ALTA("Alta"),
    MODIFICACION("Modificación"),
    BAJA("Baja"),
    RESERVA("Reserva"),
    PAGO("Pago"),
    CHECK_IN("Check-in"),
    CHECK_OUT("Check-out"),
    SERVICIO("Servicio"),
    INDICACION("Indicación"),
    REGISTRO("Registro de estadía"),
    RECHAZO("Operación rechazada");

    private String nombreVisible;

    private AccionLog(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
