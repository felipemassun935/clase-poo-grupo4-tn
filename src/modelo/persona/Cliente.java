package modelo.persona;

import java.util.ArrayList;
import java.util.List;

import modelo.interfaces.Notificable;
import modelo.mascota.Mascota;

// Cliente de la guardería: el dueño de una o más mascotas.
// Herencia: es una Persona. Interfaz: es Notificable (se le puede mandar un aviso).
public class Cliente extends Persona implements Notificable {
    private String direccion;
    private List<Mascota> mascotas;

    public Cliente(Integer id, String nombre, String apellido, String dni, String telefono, String email,
            String direccion) {
        super(id, nombre, apellido, dni, telefono, email);
        this.direccion = direccion;
        this.mascotas = new ArrayList<Mascota>();
    }

    // Agrega la mascota a la lista y le deja anotado que este es su cliente
    public void agregarMascota(Mascota m) {
        mascotas.add(m);
        m.setCliente(this);
    }

    public void quitarMascota(Mascota m) {
        mascotas.remove(m);
    }

    public List<Mascota> getMascotas() {
        return mascotas;
    }

    // Polimorfismo: implementa el método abstracto de Persona
    @Override
    public String getRol() {
        return "Cliente";
    }

    // Polimorfismo: al cliente el aviso le llega por SMS (lo simulamos por consola)
    @Override
    public void notificar(String mensaje) {
        System.out.println("[SMS a " + getNombreCompleto() + " - " + getTelefono() + "] " + mensaje);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
