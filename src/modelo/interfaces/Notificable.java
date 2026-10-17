package modelo.interfaces;

// Interfaz: la implementa toda persona a la que se le puede mandar un aviso (Cliente y Empleado).
// Cada clase decide cómo le llega el aviso (polimorfismo).
public interface Notificable {

    void notificar(String mensaje);
}
