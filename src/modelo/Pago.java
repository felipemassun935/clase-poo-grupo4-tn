package modelo;

import java.time.LocalDate;

import util.Fechas;

public class Pago {
    private int idPago;
    private LocalDate fecha;
    private double monto;
    private MedioPago medio;

    public Pago(int idPago, LocalDate fecha, double monto, MedioPago medio) {
        this.idPago = idPago;
        this.fecha = fecha;
        this.monto = monto;
        this.medio = medio;
    }

    public String generarComprobante(Estadia estadia) {
        Reserva r = estadia.getReserva();
        String texto = "COMPROBANTE DE PAGO N° " + idPago + "\n"
                + "Fecha: " + Fechas.formatear(fecha) + "\n"
                + "Mascota: " + r.getMascota().getNombre() + "\n"
                + "Dueño: " + r.getMascota().getDueno().getNombreCompleto() + "\n"
                + "Servicio: " + r.getTipo() + " en " + r.getAlojamiento().getCodigo() + "\n"
                + "Período: " + Fechas.formatear(r.getFechaDesde()) + " al " + Fechas.formatear(r.getFechaHasta()) + "\n"
                + "Medio de pago: " + medio + "\n"
                + "TOTAL: $" + String.format("%.2f", monto);
        return texto;
    }

    public int getIdPago() {
        return idPago;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }

    public MedioPago getMedio() {
        return medio;
    }
}
