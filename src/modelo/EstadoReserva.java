package modelo;

// Ciclo de vida de una reserva:
// CONFIRMADA -> EN_CURSO (check-in) -> FINALIZADA (check-out)
// CONFIRMADA -> CANCELADA
public enum EstadoReserva {
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA
}
