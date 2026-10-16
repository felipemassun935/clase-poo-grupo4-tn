package modelo;

import java.time.LocalDate;

public class RegistroIncidente extends EntradaBitacora {
    private String descripcion;
    private Gravedad gravedad;

    public RegistroIncidente(LocalDate fecha, Empleado registradaPor, String descripcion, Gravedad gravedad) {
        super(fecha, registradaPor);
        this.descripcion = descripcion;
        this.gravedad = gravedad;
    }

    @Override
    public String getResumen() {
        return "INCIDENTE " + gravedad + ": " + descripcion;
    }

    // Los incidentes graves hay que avisárselos al dueño
    public boolean hayQueAvisarAlDueno() {
        return gravedad == Gravedad.GRAVE;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Gravedad getGravedad() {
        return gravedad;
    }
}
