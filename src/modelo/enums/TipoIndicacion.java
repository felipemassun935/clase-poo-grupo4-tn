package modelo.enums;

// Tipos de indicaciones que deja el dueño al reservar (comida puntual, medicación, cuidados especiales).
// Enum con atributos: cada tipo sabe con qué tipo de registro de la bitácora se cumple.
public enum TipoIndicacion {
    ALIMENTACION("Alimentación", TipoRegistro.COMIDA),
    MEDICACION("Medicación", TipoRegistro.MEDICACION),
    CUIDADO_ESPECIAL("Cuidado especial", TipoRegistro.OBSERVACION);

    private String nombreVisible;
    private TipoRegistro tipoRegistro;

    private TipoIndicacion(String nombreVisible, TipoRegistro tipoRegistro) {
        this.nombreVisible = nombreVisible;
        this.tipoRegistro = tipoRegistro;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    // Tipo de registro con el que el empleado deja asentado que cumplió la indicación
    public TipoRegistro getTipoRegistro() {
        return tipoRegistro;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
