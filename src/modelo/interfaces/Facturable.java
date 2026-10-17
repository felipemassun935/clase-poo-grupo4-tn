package modelo.interfaces;

// Interfaz: la implementa todo lo que se le puede cobrar al cliente (por ahora, la Reserva)
public interface Facturable {

    // Total a cobrar
    double calcularTotal();

    // Suma de lo que ya se pagó
    double getTotalPagado();

    // Lo que falta pagar
    double getSaldoPendiente();
}
