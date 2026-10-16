package modelo;

import java.util.ArrayList;

public class Dueno extends Persona {
    private String direccion;
    private ArrayList<Mascota> mascotas; // asociación 1 a muchos

    public Dueno(String dni, String nombre, String apellido, String telefono, String email, String direccion) {
        super(dni, nombre, apellido, telefono, email);
        this.direccion = direccion;
        this.mascotas = new ArrayList<>();
    }

    public void agregarMascota(Mascota m) {
        mascotas.add(m);
    }

    public void quitarMascota(Mascota m) {
        mascotas.remove(m);
    }

    public ArrayList<Mascota> getMascotas() {
        return mascotas;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    // Se usa para mostrar el dueño en los combos de las pantallas
    @Override
    public String toString() {
        return apellido + ", " + nombre + " (" + dni + ")";
    }
}
