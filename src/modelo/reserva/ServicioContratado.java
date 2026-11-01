package modelo.reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import modelo.enums.TipoServicio;
import modelo.persona.Empleado;

// Un servicio extra (baño, corte de pelo, etc.) que el cliente contrató para una reserva.
// Se suma a la cuenta de la reserva cuando se contrata, y un empleado lo marca como realizado.
public class ServicioContratado {
    private Integer id;
    private TipoServicio tipo;
    private double precio;               // se copia al contratar: si después cambia la lista de precios, no cambia
    private LocalDate fechaSolicitud;
    private boolean realizado;
    private LocalDateTime fechaHoraRealizado;
    private Empleado realizadoPor;

    // Constructor para un servicio recién contratado: toma el precio actual del tipo de servicio
    public ServicioContratado(Integer id, TipoServicio tipo, LocalDate fechaSolicitud) {
        this(id, tipo, tipo.getPrecio(), fechaSolicitud, false, null, null);
    }

    // Constructor para cargarlo desde la base de datos, con todos sus datos
    public ServicioContratado(Integer id, TipoServicio tipo, double precio, LocalDate fechaSolicitud, boolean realizado,
            LocalDateTime fechaHoraRealizado, Empleado realizadoPor) {
        this.id = id;
        this.tipo = tipo;
        this.precio = precio;
        this.fechaSolicitud = fechaSolicitud;
        this.realizado = realizado;
        this.fechaHoraRealizado = fechaHoraRealizado;
        this.realizadoPor = realizadoPor;
    }

    // Deja anotado quién hizo el servicio y cuándo. Un servicio no se puede hacer dos veces.
    public void marcarRealizado(Empleado empleado, LocalDateTime fechaHora) {
        if (realizado) {
            throw new IllegalStateException("El servicio " + tipo + " ya fue realizado por "
                    + realizadoPor.getNombreCompleto() + ".");
        }
        this.realizado = true;
        this.realizadoPor = empleado;
        this.fechaHoraRealizado = fechaHora;
    }

    // "Pendiente" o "Realizado el dd/MM/yyyy HH:mm por Nombre Apellido"
    public String getEstadoTexto() {
        if (!realizado) {
            return "Pendiente";
        }
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "Realizado el " + fechaHoraRealizado.format(formato) + " por " + realizadoPor.getNombreCompleto();
    }

    public Integer getId() {
        return id;
    }

    public TipoServicio getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public boolean isRealizado() {
        return realizado;
    }

    public LocalDateTime getFechaHoraRealizado() {
        return fechaHoraRealizado;
    }

    public Empleado getRealizadoPor() {
        return realizadoPor;
    }

    @Override
    public String toString() {
        return tipo + " - " + getEstadoTexto();
    }
}
