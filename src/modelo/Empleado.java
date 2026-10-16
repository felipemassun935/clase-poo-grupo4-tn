package modelo;

public class Empleado extends Persona {
    private int legajo;
    private RolEmpleado rol;

    public Empleado(String dni, String nombre, String apellido, String telefono, String email,
                    int legajo, RolEmpleado rol) {
        super(dni, nombre, apellido, telefono, email);
        this.legajo = legajo;
        this.rol = rol;
    }

    // Solo cuidadores y veterinarios pueden dar medicación
    public boolean puedeAdministrarMedicacion() {
        return rol == RolEmpleado.CUIDADOR || rol == RolEmpleado.VETERINARIO;
    }

    public int getLegajo() {
        return legajo;
    }

    public void setLegajo(int legajo) {
        this.legajo = legajo;
    }

    public RolEmpleado getRol() {
        return rol;
    }

    public void setRol(RolEmpleado rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + rol;
    }
}
