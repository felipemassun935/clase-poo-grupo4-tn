package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

// Se crea cuando la mascota hace el check-in y se cierra con el check-out
public class Estadia {
    private int idEstadia;
    private LocalDate ingreso;
    private LocalDate egreso; // null mientras la mascota sigue en la guardería
    private Reserva reserva;
    private Empleado recibidaPor;
    private ArrayList<EntradaBitacora> bitacora; // composición
    private Pago pago; // null hasta el check-out

    public Estadia(int idEstadia, Reserva reserva, Empleado recibidaPor, LocalDate ingreso) {
        this.idEstadia = idEstadia;
        this.reserva = reserva;
        this.recibidaPor = recibidaPor;
        this.ingreso = ingreso;
        this.egreso = null;
        this.pago = null;
        this.bitacora = new ArrayList<>();
    }

    public void registrar(EntradaBitacora entrada) {
        bitacora.add(entrada);
    }

    public boolean estaActiva() {
        return egreso == null;
    }

    public void cerrar(LocalDate egreso, Pago pago) {
        this.egreso = egreso;
        this.pago = pago;
    }

    public double calcularTotal() {
        return reserva.calcularCosto();
    }

    public int getIdEstadia() {
        return idEstadia;
    }

    public LocalDate getIngreso() {
        return ingreso;
    }

    public LocalDate getEgreso() {
        return egreso;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Empleado getRecibidaPor() {
        return recibidaPor;
    }

    public ArrayList<EntradaBitacora> getBitacora() {
        return bitacora;
    }

    public Pago getPago() {
        return pago;
    }

    @Override
    public String toString() {
        return "Estadía #" + idEstadia + " - " + reserva.getMascota().getNombre()
                + " en " + reserva.getAlojamiento().getCodigo();
    }
}
