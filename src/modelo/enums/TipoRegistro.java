package modelo.enums;

// Tipos de registro que se cargan en el día a día de una estadía (la bitácora).
// Enum con atributos: cada tipo tiene el nombre que se muestra y sabe si hay que avisarle a alguien.
public enum TipoRegistro {
    COMIDA("Comida", false),
    MEDICACION("Medicación", false),
    PASEO("Paseo", false),
    SERVICIO("Servicio realizado", false),
    OBSERVACION("Observación", false),
    INCIDENTE("Incidente", true);

    private String nombreVisible;
    private boolean requiereAviso;

    private TipoRegistro(String nombreVisible, boolean requiereAviso) {
        this.nombreVisible = nombreVisible;
        this.requiereAviso = requiereAviso;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    // true si al cargar este tipo de registro hay que notificar al cliente y al encargado
    public boolean requiereAviso() {
        return requiereAviso;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
