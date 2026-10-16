package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// La mascota se queda a dormir (por ejemplo, mientras el dueño viaja)
public class ReservaHospedaje extends Reserva {
    private boolean incluyeBanio;

    public ReservaHospedaje(int idReserva, Mascota mascota, Alojamiento alojamiento,
                            LocalDate desde, LocalDate hasta, boolean incluyeBanio) {
        super(idReserva, mascota, alojamiento, desde, hasta);
        this.incluyeBanio = incluyeBanio;
    }

    public int getCantidadNoches() {
        int noches = (int) ChronoUnit.DAYS.between(fechaDesde, fechaHasta);
        if (noches < 1) {
            noches = 1; // como mínimo se cobra una noche
        }
        return noches;
    }

    @Override
    public double calcularCosto() {
        return Tarifario.calcularHospedaje(alojamiento, getCantidadNoches(), incluyeBanio);
    }

    @Override
    public String getTipo() {
        return "Hospedaje";
    }

    public boolean isIncluyeBanio() {
        return incluyeBanio;
    }
}
