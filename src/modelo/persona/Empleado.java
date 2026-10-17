package modelo.persona;

import java.time.LocalDate;

import modelo.interfaces.Notificable;
import modelo.sucursal.Sucursal;

// Empleado de una sucursal de la guardería.
// Herencia: es una Persona. Interfaz: es Notificable (se le puede mandar un aviso).
public class Empleado extends Persona implements Notificable {
    private String legajo;
    private LocalDate fechaIngreso;
    private Sucursal sucursal;

    public Empleado(Integer id, String nombre, String apellido, String dni, String telefono, String email,
            String legajo, LocalDate fechaIngreso) {
        super(id, nombre, apellido, dni, telefono, email);
        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso;
    }

    // Polimorfismo: el rol depende de si es el encargado de su sucursal o no
    @Override
    public String getRol() {
        if (sucursal != null && sucursal.getEncargado() == this) {
            return "Encargado";
        }
        return "Empleado";
    }

    // Polimorfismo: al empleado el aviso le llega como aviso interno (lo simulamos por consola)
    @Override
    public void notificar(String mensaje) {
        System.out.println("[Aviso interno a " + getNombreCompleto() + " - legajo " + legajo + "] " + mensaje);
    }

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}
