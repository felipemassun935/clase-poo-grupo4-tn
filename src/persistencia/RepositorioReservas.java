package persistencia;

import java.time.LocalDate;
import java.util.ArrayList;

import modelo.Alojamiento;
import modelo.EstadoReserva;
import modelo.Mascota;
import modelo.Reserva;
import modelo.ReservaGuarderiaDiurna;
import modelo.ReservaHospedaje;

// Formato: tipo;id;idMascota;idAlojamiento;desde;hasta;fechaCreacion;estado;extra
// tipo = H (hospedaje) o D (guardería diurna)
// extra = incluye baño (hospedaje) o incluye paseo (diurna)
public class RepositorioReservas extends RepositorioArchivo<Reserva> {
    private ArrayList<Mascota> mascotas;
    private ArrayList<Alojamiento> alojamientos;

    public RepositorioReservas(String rutaArchivo, ArrayList<Mascota> mascotas, ArrayList<Alojamiento> alojamientos) {
        super(rutaArchivo);
        this.mascotas = mascotas;
        this.alojamientos = alojamientos;
    }

    @Override
    protected String aLinea(Reserva r) {
        String tipo;
        boolean extra;
        if (r instanceof ReservaHospedaje) {
            tipo = "H";
            extra = ((ReservaHospedaje) r).isIncluyeBanio();
        } else {
            tipo = "D";
            extra = ((ReservaGuarderiaDiurna) r).isIncluyePaseo();
        }
        return tipo + SEPARADOR + r.getIdReserva() + SEPARADOR + r.getMascota().getIdMascota()
                + SEPARADOR + r.getAlojamiento().getIdAlojamiento() + SEPARADOR + r.getFechaDesde()
                + SEPARADOR + r.getFechaHasta() + SEPARADOR + r.getFechaCreacion()
                + SEPARADOR + r.getEstado() + SEPARADOR + extra;
    }

    @Override
    protected Reserva desdeLinea(String[] c) {
        int id = Integer.parseInt(c[1]);
        Mascota mascota = buscarMascota(Integer.parseInt(c[2]));
        Alojamiento alojamiento = buscarAlojamiento(Integer.parseInt(c[3]));
        if (mascota == null || alojamiento == null) {
            return null;
        }
        LocalDate desde = LocalDate.parse(c[4]);
        LocalDate hasta = LocalDate.parse(c[5]);
        boolean extra = Boolean.parseBoolean(c[8]);

        Reserva r;
        if (c[0].equals("H")) {
            r = new ReservaHospedaje(id, mascota, alojamiento, desde, hasta, extra);
        } else {
            r = new ReservaGuarderiaDiurna(id, mascota, alojamiento, desde, hasta, extra);
        }
        r.setFechaCreacion(LocalDate.parse(c[6]));
        r.setEstado(EstadoReserva.valueOf(c[7]));
        return r;
    }

    private Mascota buscarMascota(int id) {
        for (Mascota m : mascotas) {
            if (m.getIdMascota() == id) {
                return m;
            }
        }
        return null;
    }

    private Alojamiento buscarAlojamiento(int id) {
        for (Alojamiento a : alojamientos) {
            if (a.getIdAlojamiento() == id) {
                return a;
            }
        }
        return null;
    }
}
