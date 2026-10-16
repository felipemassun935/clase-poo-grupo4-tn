package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// La mascota viene solo de día y vuelve a su casa a la noche
public class ReservaGuarderiaDiurna extends Reserva {
    private boolean incluyePaseo;

    public ReservaGuarderiaDiurna(int idReserva, Mascota mascota, Alojamiento alojamiento,
                                  LocalDate desde, LocalDate hasta, boolean incluyePaseo) {
        super(idReserva, mascota, alojamiento, desde, hasta);
        this.incluyePaseo = incluyePaseo;
    }

    // Del 10 al 12 son 3 días (se cuentan los dos extremos)
    public int getCantidadDias() {
        return (int) ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;
    }

    @Override
    public double calcularCosto() {
        return Tarifario.calcularGuarderiaDiurna(alojamiento, getCantidadDias(), incluyePaseo);
    }

    @Override
    public String getTipo() {
        return "Guardería diurna";
    }

    public boolean isIncluyePaseo() {
        return incluyePaseo;
    }
}
