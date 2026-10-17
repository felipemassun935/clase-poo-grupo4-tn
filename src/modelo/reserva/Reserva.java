package modelo.reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import excepciones.IngresoRechazadoException;
import modelo.enums.EstadoReserva;
import modelo.enums.TipoRegistro;
import modelo.interfaces.Facturable;
import modelo.interfaces.Notificable;
import modelo.mascota.Mascota;
import modelo.persona.Empleado;
import modelo.sucursal.Alojamiento;

// Reserva de un alojamiento para una mascota entre dos fechas.
// Interfaz: es Facturable (se le puede cobrar al cliente).
// Guarda los pagos y los registros del día a día de la estadía.
public class Reserva implements Facturable {
    private Integer id;
    private Mascota mascota;
    private Alojamiento alojamiento;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private EstadoReserva estado;
    private List<Pago> pagos;
    private List<RegistroDiario> registros;

    // Constructor para una reserva nueva: siempre arranca PENDIENTE
    public Reserva(Integer id, Mascota mascota, Alojamiento alojamiento, LocalDate fechaDesde, LocalDate fechaHasta) {
        this(id, mascota, alojamiento, fechaDesde, fechaHasta, EstadoReserva.PENDIENTE);
    }

    // Constructor para cargar una reserva desde archivo, con el estado que tenía guardado
    public Reserva(Integer id, Mascota mascota, Alojamiento alojamiento, LocalDate fechaDesde, LocalDate fechaHasta,
            EstadoReserva estado) {
        this.id = id;
        this.mascota = mascota;
        this.alojamiento = alojamiento;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.estado = estado;
        this.pagos = new ArrayList<Pago>();
        this.registros = new ArrayList<RegistroDiario>();
    }

    // Cantidad de noches entre las dos fechas. Como mínimo se cobra 1 noche.
    public long getCantidadNoches() {
        long noches = ChronoUnit.DAYS.between(fechaDesde, fechaHasta);
        if (noches < 1) {
            noches = 1;
        }
        return noches;
    }

    // Interfaz Facturable: total = noches x precio por noche del alojamiento
    @Override
    public double calcularTotal() {
        return getCantidadNoches() * alojamiento.getPrecioNoche();
    }

    // Interfaz Facturable: suma todos los pagos hechos
    @Override
    public double getTotalPagado() {
        double total = 0;
        for (Pago p : pagos) {
            total = total + p.getMonto();
        }
        return total;
    }

    // Interfaz Facturable: lo que todavía falta pagar
    @Override
    public double getSaldoPendiente() {
        return calcularTotal() - getTotalPagado();
    }

    public void confirmar() {
        cambiarEstado(EstadoReserva.CONFIRMADA);
    }

    // Check-in: primero se revisa que la mascota pueda entrar (vacunas o control veterinario).
    // Si hay problemas se tira una excepción propia con la lista.
    public void realizarCheckIn(LocalDate fecha) throws IngresoRechazadoException {
        List<String> problemas = mascota.validarIngreso(fecha);
        if (!problemas.isEmpty()) {
            throw new IngresoRechazadoException(problemas);
        }
        cambiarEstado(EstadoReserva.EN_CURSO);
    }

    // Check-out: solo se puede si la reserva está totalmente pagada
    public void realizarCheckOut() {
        if (getSaldoPendiente() > 0) {
            throw new IllegalStateException("No se puede hacer el check-out: falta pagar $"
                    + String.format("%.2f", getSaldoPendiente()));
        }
        cambiarEstado(EstadoReserva.FINALIZADA);
    }

    public void cancelar() {
        cambiarEstado(EstadoReserva.CANCELADA);
    }

    // Cambia el estado solo si la transición está permitida por EstadoReserva
    private void cambiarEstado(EstadoReserva nuevo) {
        if (!estado.puedePasarA(nuevo)) {
            throw new IllegalStateException("La reserva no puede pasar de " + estado + " a " + nuevo);
        }
        estado = nuevo;
    }

    // Patrón Creador: la Reserva crea sus propios pagos porque es la que los guarda
    public void registrarPago(double monto, String medio) {
        Pago pago = new Pago(LocalDate.now(), monto, medio);
        pagos.add(pago);
    }

    // Crea un registro del día (comida, medicación o incidente), lo guarda y avisa si hace falta
    public void agregarRegistro(TipoRegistro tipo, String descripcion, Empleado empleado) {
        RegistroDiario registro = new RegistroDiario(LocalDateTime.now(), tipo, descripcion, empleado);
        registros.add(registro);
        registro.notificarSiCorresponde();
    }

    // true si esta reserva se pisa con el período [desde, hasta).
    // El día de salida de una reserva puede ser el día de entrada de otra.
    public boolean seSuperponeCon(LocalDate desde, LocalDate hasta) {
        return fechaDesde.isBefore(hasta) && desde.isBefore(fechaHasta);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public Alojamiento getAlojamiento() {
        return alojamiento;
    }

    public void setAlojamiento(Alojamiento alojamiento) {
        this.alojamiento = alojamiento;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public List<RegistroDiario> getRegistros() {
        return registros;
    }

    @Override
    public String toString() {
        return "Reserva " + id + " - " + mascota.getNombre() + " en " + alojamiento.getCodigo() + " (" + estado + ")";
    }

    // Clase anidada estática: un pago no necesita acceder a los datos de la Reserva,
    // solo vive adentro de ella porque es parte de la reserva. Desde afuera se usa como Reserva.Pago.
    public static class Pago {
        private LocalDate fecha;
        private double monto;
        private String medio;

        public Pago(LocalDate fecha, double monto, String medio) {
            this.fecha = fecha;
            this.monto = monto;
            this.medio = medio;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public double getMonto() {
            return monto;
        }

        public void setMonto(double monto) {
            this.monto = monto;
        }

        public String getMedio() {
            return medio;
        }

        public void setMedio(String medio) {
            this.medio = medio;
        }

        @Override
        public String toString() {
            return fecha + " - $" + String.format("%.2f", monto) + " (" + medio + ")";
        }
    }

    // Clase interna (no static): necesita acceder a los datos de la Reserva que la contiene
    // (la mascota y el alojamiento) para saber a quién avisar.
    // Cada RegistroDiario pertenece a una Reserva en particular.
    public class RegistroDiario {
        private LocalDateTime fechaHora;
        private TipoRegistro tipo;
        private String descripcion;
        private Empleado empleado;

        public RegistroDiario(LocalDateTime fechaHora, TipoRegistro tipo, String descripcion, Empleado empleado) {
            this.fechaHora = fechaHora;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.empleado = empleado;
        }

        // Arma una línea del tipo "dd/MM/yyyy HH:mm | TIPO: descripción | cargó: nombre"
        public String getResumen() {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaHora.format(formato) + " | " + tipo + ": " + descripcion + " | cargó: "
                    + empleado.getNombreCompleto();
        }

        // Si el tipo de registro lo pide (por ejemplo un incidente), avisa al cliente y al encargado.
        // Usa "mascota" y "alojamiento" de la Reserva externa: por eso es clase interna.
        public void notificarSiCorresponde() {
            if (!tipo.requiereAviso()) {
                return;
            }
            String mensaje = tipo + " con " + mascota.getNombre() + ": " + descripcion;

            List<Notificable> destinatarios = new ArrayList<Notificable>();
            if (mascota.getCliente() != null) {
                destinatarios.add(mascota.getCliente());
            }
            if (alojamiento.getSucursal() != null && alojamiento.getSucursal().getEncargado() != null) {
                destinatarios.add(alojamiento.getSucursal().getEncargado());
            }

            // Polimorfismo: a cada uno se le llama notificar() sin preguntar si es Cliente o Empleado
            for (Notificable n : destinatarios) {
                n.notificar(mensaje);
            }
        }

        public LocalDateTime getFechaHora() {
            return fechaHora;
        }

        public void setFechaHora(LocalDateTime fechaHora) {
            this.fechaHora = fechaHora;
        }

        public TipoRegistro getTipo() {
            return tipo;
        }

        public void setTipo(TipoRegistro tipo) {
            this.tipo = tipo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Empleado getEmpleado() {
            return empleado;
        }

        public void setEmpleado(Empleado empleado) {
            this.empleado = empleado;
        }

        @Override
        public String toString() {
            return getResumen();
        }
    }
}
