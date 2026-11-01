package modelo.reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import excepciones.IngresoRechazadoException;
import modelo.enums.EstadoReserva;
import modelo.enums.TipoIndicacion;
import modelo.enums.TipoRegistro;
import modelo.enums.TipoServicio;
import modelo.interfaces.Facturable;
import modelo.interfaces.Notificable;
import modelo.mascota.Mascota;
import modelo.persona.Empleado;
import modelo.sucursal.Alojamiento;

// Reserva de un alojamiento para una mascota entre dos fechas.
// Interfaz: es Facturable (se le puede cobrar al cliente).
// Guarda los pagos, los registros del día a día de la estadía (bitácora),
// los servicios extra que contrató el cliente y las indicaciones que dejó el dueño.
public class Reserva implements Facturable {
    private Integer id;
    private Mascota mascota;
    private Alojamiento alojamiento;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private EstadoReserva estado;
    private List<Pago> pagos;
    private List<RegistroDiario> registros;
    private List<ServicioContratado> servicios;
    private List<Indicacion> indicaciones;

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
        this.servicios = new ArrayList<ServicioContratado>();
        this.indicaciones = new ArrayList<Indicacion>();
    }

    // Cantidad de noches entre las dos fechas. Como mínimo se cobra 1 noche.
    public long getCantidadNoches() {
        long noches = ChronoUnit.DAYS.between(fechaDesde, fechaHasta);
        if (noches < 1) {
            noches = 1;
        }
        return noches;
    }

    // Lo que cuesta solo el alojamiento: noches x precio por noche
    public double getTotalAlojamiento() {
        return getCantidadNoches() * alojamiento.getPrecioNoche();
    }

    // Suma de los servicios contratados (estén hechos o no: se cobran al contratarlos)
    public double getTotalServicios() {
        double total = 0;
        for (ServicioContratado s : servicios) {
            total = total + s.getPrecio();
        }
        return total;
    }

    // Cuántos servicios contratados ya se hicieron
    public int getCantidadServiciosRealizados() {
        int cantidad = 0;
        for (ServicioContratado s : servicios) {
            if (s.isRealizado()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // true si hay algún servicio contratado que todavía no se hizo
    public boolean tieneServiciosPendientes() {
        return getCantidadServiciosRealizados() < servicios.size();
    }

    // Interfaz Facturable: total = alojamiento + servicios contratados
    @Override
    public double calcularTotal() {
        return getTotalAlojamiento() + getTotalServicios();
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

    // Check-in: primero se revisa la fecha y después que la mascota pueda entrar (vacunas o control veterinario).
    // Si hay problemas se tira una excepción propia con la lista.
    public void realizarCheckIn(LocalDate fecha) throws IngresoRechazadoException {
        if (fecha.isBefore(fechaDesde)) {
            throw new IllegalStateException("La reserva empieza el " + fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ": todavía no se puede hacer el check-in.");
        }
        if (!fecha.isBefore(fechaHasta)) {
            throw new IllegalStateException("La reserva terminaba el " + fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ": ya no se puede hacer el check-in.");
        }
        List<String> problemas = mascota.validarIngreso(fecha);
        if (!problemas.isEmpty()) {
            throw new IngresoRechazadoException(problemas);
        }
        cambiarEstado(EstadoReserva.EN_CURSO);
    }

    // Check-out: no puede quedar ningún servicio sin hacer y la reserva tiene que estar totalmente pagada
    public void realizarCheckOut() {
        if (tieneServiciosPendientes()) {
            int pendientes = servicios.size() - getCantidadServiciosRealizados();
            throw new IllegalStateException("No se puede hacer el check-out: hay " + pendientes
                    + " servicio(s) contratado(s) sin realizar. Realizalos o quitalos.");
        }
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

    // true mientras la reserva no terminó ni se canceló: se le pueden sumar servicios e indicaciones
    private boolean admiteCambios() {
        return estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.EN_CURSO;
    }

    // Patrón Creador: la Reserva crea sus propios pagos porque es la que los guarda
    public Pago registrarPago(double monto, String medio) {
        Pago pago = new Pago(LocalDate.now(), monto, medio);
        pagos.add(pago);
        return pago;
    }

    // Crea un registro del día (comida, medicación, paseo, incidente...), lo guarda y avisa si hace falta
    public RegistroDiario agregarRegistro(TipoRegistro tipo, String descripcion, Empleado empleado) {
        return agregarRegistro(tipo, descripcion, empleado, null);
    }

    // Sobrecarga: igual que el anterior, pero dejando anotado qué indicación del dueño se cumplió
    public RegistroDiario agregarRegistro(TipoRegistro tipo, String descripcion, Empleado empleado,
            Indicacion indicacion) {
        if (estado != EstadoReserva.EN_CURSO) {
            throw new IllegalStateException("Solo se cargan registros en estadías en curso.");
        }
        RegistroDiario registro = new RegistroDiario(LocalDateTime.now(), tipo, descripcion, empleado, indicacion);
        registros.add(registro);
        registro.notificarSiCorresponde();
        return registro;
    }

    // ----- Servicios extra -----

    // Patrón Creador: la Reserva crea los servicios contratados porque es la que los guarda
    public ServicioContratado contratarServicio(Integer idServicio, TipoServicio tipo) {
        if (!admiteCambios()) {
            throw new IllegalStateException("No se pueden contratar servicios en una reserva " + estado + ".");
        }
        if (!tipo.seAplicaA(mascota.getEspecie())) {
            throw new IllegalStateException("El servicio " + tipo + " no se le puede hacer a un/a "
                    + mascota.getEspecie().getNombreVisible() + ".");
        }
        ServicioContratado servicio = new ServicioContratado(idServicio, tipo, LocalDate.now());
        servicios.add(servicio);
        return servicio;
    }

    // Marca el servicio como hecho y lo deja asentado en la bitácora con el empleado que lo hizo
    public RegistroDiario realizarServicio(ServicioContratado servicio, Empleado empleado) {
        if (estado != EstadoReserva.EN_CURSO) {
            throw new IllegalStateException("Los servicios se realizan durante la estadía (después del check-in).");
        }
        servicio.marcarRealizado(empleado, LocalDateTime.now());
        return agregarRegistro(TipoRegistro.SERVICIO, "Se realizó: " + servicio.getTipo(), empleado);
    }

    // Un servicio se puede quitar si no se hizo y si al quitarlo no queda pagado de más
    public void quitarServicio(ServicioContratado servicio) {
        if (servicio.isRealizado()) {
            throw new IllegalStateException("El servicio ya se realizó: no se puede quitar.");
        }
        if (calcularTotal() - servicio.getPrecio() < getTotalPagado()) {
            throw new IllegalStateException("El servicio ya está pago: no se puede quitar.");
        }
        servicios.remove(servicio);
    }

    // ----- Indicaciones del dueño -----

    // Patrón Creador: la Reserva crea las indicaciones porque es la que las guarda
    public Indicacion agregarIndicacion(Integer idIndicacion, TipoIndicacion tipo, String descripcion, String horario) {
        if (!admiteCambios()) {
            throw new IllegalStateException("No se pueden agregar indicaciones en una reserva " + estado + ".");
        }
        Indicacion indicacion = new Indicacion(idIndicacion, tipo, descripcion, horario);
        indicaciones.add(indicacion);
        return indicacion;
    }

    // No se puede quitar una indicación que ya figura cumplida en la bitácora (se perdería el seguimiento)
    public void quitarIndicacion(Indicacion indicacion) {
        if (getCantidadCumplimientos(indicacion) > 0) {
            throw new IllegalStateException("La indicación ya tiene registros en la bitácora: no se puede quitar.");
        }
        indicaciones.remove(indicacion);
    }

    // Cuántas veces se cumplió la indicación (por ejemplo, cuántas dosis se dieron)
    public int getCantidadCumplimientos(Indicacion indicacion) {
        int cantidad = 0;
        for (RegistroDiario r : registros) {
            if (r.getIndicacion() == indicacion) {
                cantidad++;
            }
        }
        return cantidad;
    }

    // El último registro que cumplió la indicación, o null si todavía no se cumplió nunca
    public RegistroDiario getUltimoCumplimiento(Indicacion indicacion) {
        RegistroDiario ultimo = null;
        for (RegistroDiario r : registros) {
            if (r.getIndicacion() == indicacion) {
                if (ultimo == null || r.getFechaHora().isAfter(ultimo.getFechaHora())) {
                    ultimo = r;
                }
            }
        }
        return ultimo;
    }

    // Las indicaciones que se cumplen con ese tipo de registro (para ofrecerlas al cargar uno)
    public List<Indicacion> getIndicacionesPara(TipoRegistro tipo) {
        List<Indicacion> resultado = new ArrayList<Indicacion>();
        for (Indicacion i : indicaciones) {
            if (i.getTipo().getTipoRegistro() == tipo) {
                resultado.add(i);
            }
        }
        return resultado;
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

    public List<ServicioContratado> getServicios() {
        return servicios;
    }

    public List<Indicacion> getIndicaciones() {
        return indicaciones;
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
        private Empleado empleado;       // quién lo hizo
        private Indicacion indicacion;   // indicación del dueño que se cumplió (puede ser null)

        public RegistroDiario(LocalDateTime fechaHora, TipoRegistro tipo, String descripcion, Empleado empleado) {
            this(fechaHora, tipo, descripcion, empleado, null);
        }

        // Sobrecarga del constructor: con la indicación que cumple. El de arriba la llama con this(...)
        public RegistroDiario(LocalDateTime fechaHora, TipoRegistro tipo, String descripcion, Empleado empleado,
                Indicacion indicacion) {
            this.fechaHora = fechaHora;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.empleado = empleado;
            this.indicacion = indicacion;
        }

        // Arma una línea del tipo "dd/MM/yyyy HH:mm | Tipo: descripción | hizo: nombre [cumple: indicación]"
        public String getResumen() {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String texto = fechaHora.format(formato) + " | " + tipo + ": " + descripcion + " | hizo: "
                    + empleado.getNombreCompleto();
            if (indicacion != null) {
                texto = texto + " [cumple: " + indicacion + "]";
            }
            return texto;
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

        public Indicacion getIndicacion() {
            return indicacion;
        }

        @Override
        public String toString() {
            return getResumen();
        }
    }
}
