package modelo.enums;

// Tipos de registro que se cargan en el día a día de una estadía.
// Enum con atributos: cada tipo sabe si hay que avisarle a alguien cuando se carga.
public enum TipoRegistro {
    COMIDA(false),
    MEDICACION(false),
    INCIDENTE(true);

    private boolean requiereAviso;

    private TipoRegistro(boolean requiereAviso) {
        this.requiereAviso = requiereAviso;
    }

    // true si al cargar este tipo de registro hay que notificar al cliente y al encargado
    public boolean requiereAviso() {
        return requiereAviso;
    }
}
