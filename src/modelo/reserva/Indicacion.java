package modelo.reserva;

import modelo.enums.TipoIndicacion;

// Indicación que deja el dueño para la estadía de su mascota.
// Ejemplos: "Balanceado light, 200 g" (ALIMENTACION, horario "08:00 y 20:00"),
// "Antibiótico, 1/2 comprimido" (MEDICACION, horario "cada 12 horas"),
// "No juntarlo con otros perros" (CUIDADO_ESPECIAL).
public class Indicacion {
    private Integer id;
    private TipoIndicacion tipo;
    private String descripcion;
    private String horario;

    public Indicacion(Integer id, TipoIndicacion tipo, String descripcion, String horario) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.horario = horario;
    }

    public Integer getId() {
        return id;
    }

    public TipoIndicacion getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getHorario() {
        return horario;
    }

    // Se usa en los combos y en la bitácora: "Medicación: Antibiótico, 1/2 comprimido (cada 12 horas)"
    @Override
    public String toString() {
        String texto = tipo + ": " + descripcion;
        if (horario != null && !horario.isEmpty()) {
            texto = texto + " (" + horario + ")";
        }
        return texto;
    }
}
