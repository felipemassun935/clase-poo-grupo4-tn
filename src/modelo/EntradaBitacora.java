package modelo;

import java.time.LocalDate;

import util.Fechas;

// Una anotación del día en la estadía de la mascota.
// Clase abstracta: puede ser una comida, una medicación o un incidente.
public abstract class EntradaBitacora {
    protected LocalDate fecha;
    protected Empleado registradaPor;

    public EntradaBitacora(LocalDate fecha, Empleado registradaPor) {
        this.fecha = fecha;
        this.registradaPor = registradaPor;
    }

    // Cada tipo de registro arma su propio resumen (polimorfismo)
    public abstract String getResumen();

    public LocalDate getFecha() {
        return fecha;
    }

    public Empleado getRegistradaPor() {
        return registradaPor;
    }

    @Override
    public String toString() {
        return Fechas.formatear(fecha) + " | " + getResumen() + " | registró: " + registradaPor.getNombreCompleto();
    }
}
