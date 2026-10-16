package modelo;

import java.time.LocalDate;

import util.Fechas;

// Clase abstracta: el costo se calcula distinto si es hospedaje o guardería de día
public abstract class Reserva implements Cobrable {
    protected int idReserva;
    protected LocalDate fechaCreacion;
    protected LocalDate fechaDesde;
    protected LocalDate fechaHasta;
    protected EstadoReserva estado;
    protected Mascota mascota;
    protected Alojamiento alojamiento;

    public Reserva(int idReserva, Mascota mascota, Alojamiento alojamiento, LocalDate desde, LocalDate hasta) {
        this.idReserva = idReserva;
        this.mascota = mascota;
        this.alojamiento = alojamiento;
        this.fechaDesde = desde;
        this.fechaHasta = hasta;
        this.fechaCreacion = LocalDate.now();
        this.estado = EstadoReserva.CONFIRMADA;
    }

    // Cada subclase calcula su costo (polimorfismo)
    @Override
    public abstract double calcularCosto();

    // Texto corto para mostrar en tablas: "Hospedaje" o "Guardería diurna"
    public abstract String getTipo();

    public void cancelar() {
        estado = EstadoReserva.CANCELADA;
    }

    public void iniciar() {
        estado = EstadoReserva.EN_CURSO;
    }

    public void finalizar() {
        estado = EstadoReserva.FINALIZADA;
    }

    // Una reserva "ocupa" el alojamiento mientras está confirmada o en curso
    public boolean estaActiva() {
        return estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_CURSO;
    }

    // true si el rango [desde, hasta] se pisa con el de esta reserva
    public boolean seSuperpone(LocalDate desde, LocalDate hasta) {
        return !desde.isAfter(fechaHasta) && !hasta.isBefore(fechaDesde);
    }

    // Getters y setters
    public int getIdReserva() {
        return idReserva;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public Alojamiento getAlojamiento() {
        return alojamiento;
    }

    @Override
    public String toString() {
        return "#" + idReserva + " " + mascota.getNombre() + " - " + alojamiento.getCodigo()
                + " (" + Fechas.formatear(fechaDesde) + " al " + Fechas.formatear(fechaHasta) + ")";
    }
}
