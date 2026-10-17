package modelo.enums;

// Estados por los que pasa una reserva:
// PENDIENTE -> CONFIRMADA -> EN_CURSO (check-in) -> FINALIZADA (check-out)
// PENDIENTE o CONFIRMADA -> CANCELADA
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA;

    // Dice si desde este estado se puede pasar al estado "nuevo"
    public boolean puedePasarA(EstadoReserva nuevo) {
        boolean puede = false;
        switch (this) {
            case PENDIENTE:
                if (nuevo == CONFIRMADA || nuevo == CANCELADA) {
                    puede = true;
                }
                break;
            case CONFIRMADA:
                if (nuevo == EN_CURSO || nuevo == CANCELADA) {
                    puede = true;
                }
                break;
            case EN_CURSO:
                if (nuevo == FINALIZADA) {
                    puede = true;
                }
                break;
            default:
                // FINALIZADA y CANCELADA son estados finales: no pasan a ningún otro
                puede = false;
                break;
        }
        return puede;
    }
}
