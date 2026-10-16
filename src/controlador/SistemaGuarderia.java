package controlador;

import java.time.LocalDate;
import java.util.ArrayList;

import excepciones.AlojamientoNoDisponibleException;
import excepciones.DatoInvalidoException;
import excepciones.VacunacionVencidaException;
import modelo.Alojamiento;
import modelo.Dueno;
import modelo.Empleado;
import modelo.EntradaBitacora;
import modelo.Especie;
import modelo.Estadia;
import modelo.EstadoReserva;
import modelo.Mascota;
import modelo.MedioPago;
import modelo.Pago;
import modelo.RegistroMedicacion;
import modelo.Reserva;
import modelo.ReservaGuarderiaDiurna;
import modelo.ReservaHospedaje;
import modelo.Sucursal;
import persistencia.RepositorioAlojamientos;
import persistencia.RepositorioDuenos;
import persistencia.RepositorioEmpleados;
import persistencia.RepositorioEstadias;
import persistencia.RepositorioMascotas;
import persistencia.RepositorioReservas;

/*
 * Controlador del sistema (patrón GRASP Controlador).
 * Las pantallas no tocan las listas ni los archivos directamente:
 * le piden todo a esta clase.
 *
 * También es un Singleton: hay un solo objeto SistemaGuarderia en todo el programa,
 * y todas las pantallas lo obtienen con SistemaGuarderia.getInstancia().
 */
public class SistemaGuarderia {
    private static SistemaGuarderia instancia; // la única instancia

    private Sucursal sucursal;
    private ArrayList<Dueno> duenos;
    private ArrayList<Empleado> empleados;
    private ArrayList<Mascota> mascotas;
    private ArrayList<Reserva> reservas;
    private ArrayList<Estadia> estadias;

    private RepositorioDuenos repoDuenos;
    private RepositorioEmpleados repoEmpleados;
    private RepositorioMascotas repoMascotas;
    private RepositorioAlojamientos repoAlojamientos;
    private RepositorioReservas repoReservas;
    private RepositorioEstadias repoEstadias;

    // Constructor privado: desde afuera no se puede hacer "new SistemaGuarderia()"
    private SistemaGuarderia() {
        sucursal = new Sucursal(1, "Patitas Felices - Belgrano", "Av. Cabildo 1234, CABA", "11-4785-0000");
        cargarDatos();
    }

    public static SistemaGuarderia getInstancia() {
        if (instancia == null) {
            instancia = new SistemaGuarderia();
        }
        return instancia;
    }

    // ===================== Carga y guardado de archivos =====================

    // El orden importa: las mascotas necesitan a los dueños, las reservas a las mascotas, etc.
    private void cargarDatos() {
        repoDuenos = new RepositorioDuenos("datos/duenos.txt");
        duenos = repoDuenos.cargarTodos();

        repoEmpleados = new RepositorioEmpleados("datos/empleados.txt");
        empleados = repoEmpleados.cargarTodos();

        repoMascotas = new RepositorioMascotas("datos/mascotas.txt", duenos);
        mascotas = repoMascotas.cargarTodos();
        for (Mascota m : mascotas) {
            m.getDueno().agregarMascota(m);
        }

        repoAlojamientos = new RepositorioAlojamientos("datos/alojamientos.txt");
        for (Alojamiento a : repoAlojamientos.cargarTodos()) {
            sucursal.agregarAlojamiento(a);
        }

        repoReservas = new RepositorioReservas("datos/reservas.txt", mascotas, sucursal.getAlojamientos());
        reservas = repoReservas.cargarTodos();
        for (Reserva r : reservas) {
            r.getAlojamiento().agregarReserva(r);
        }

        repoEstadias = new RepositorioEstadias("datos/estadias.txt", reservas, empleados);
        estadias = repoEstadias.cargarTodos();
    }

    public void guardarTodo() {
        repoDuenos.guardarTodos(duenos);
        repoEmpleados.guardarTodos(empleados);
        repoMascotas.guardarTodos(mascotas);
        repoAlojamientos.guardarTodos(sucursal.getAlojamientos());
        repoReservas.guardarTodos(reservas);
        repoEstadias.guardarTodos(estadias);
    }

    // ===================== ABM de dueños =====================

    public void agregarDueno(Dueno d) throws DatoInvalidoException {
        if (buscarDueno(d.getDni()) != null) {
            throw new DatoInvalidoException("Ya existe un dueño con DNI " + d.getDni());
        }
        duenos.add(d);
        guardarTodo();
    }

    public void eliminarDueno(Dueno d) throws DatoInvalidoException {
        if (!d.getMascotas().isEmpty()) {
            throw new DatoInvalidoException("No se puede eliminar a " + d.getNombreCompleto()
                    + " porque tiene mascotas cargadas. Primero eliminá sus mascotas.");
        }
        duenos.remove(d);
        guardarTodo();
    }

    public Dueno buscarDueno(String dni) {
        for (Dueno d : duenos) {
            if (d.getDni().equals(dni)) {
                return d;
            }
        }
        return null;
    }

    // ===================== ABM de empleados =====================

    public void agregarEmpleado(Empleado e) throws DatoInvalidoException {
        if (buscarEmpleado(e.getLegajo()) != null) {
            throw new DatoInvalidoException("Ya existe un empleado con legajo " + e.getLegajo());
        }
        empleados.add(e);
        guardarTodo();
    }

    public void eliminarEmpleado(Empleado e) throws DatoInvalidoException {
        // Si ya atendió estadías no se borra, para no perder el historial
        for (Estadia est : estadias) {
            if (est.getRecibidaPor() == e) {
                throw new DatoInvalidoException("No se puede eliminar a " + e.getNombreCompleto()
                        + " porque tiene estadías registradas.");
            }
            for (EntradaBitacora entrada : est.getBitacora()) {
                if (entrada.getRegistradaPor() == e) {
                    throw new DatoInvalidoException("No se puede eliminar a " + e.getNombreCompleto()
                            + " porque tiene registros en bitácoras.");
                }
            }
        }
        empleados.remove(e);
        guardarTodo();
    }

    public Empleado buscarEmpleado(int legajo) {
        for (Empleado e : empleados) {
            if (e.getLegajo() == legajo) {
                return e;
            }
        }
        return null;
    }

    // ===================== ABM de mascotas =====================

    public int proximoIdMascota() {
        int max = 0;
        for (Mascota m : mascotas) {
            if (m.getIdMascota() > max) {
                max = m.getIdMascota();
            }
        }
        return max + 1;
    }

    public void agregarMascota(Mascota m) {
        mascotas.add(m);
        m.getDueno().agregarMascota(m);
        guardarTodo();
    }

    // Cambia el dueño de una mascota (se usa al modificar)
    public void cambiarDueno(Mascota m, Dueno nuevo) {
        m.getDueno().quitarMascota(m);
        m.setDueno(nuevo);
        nuevo.agregarMascota(m);
    }

    public void eliminarMascota(Mascota m) throws DatoInvalidoException {
        for (Reserva r : reservas) {
            if (r.getMascota() == m) {
                throw new DatoInvalidoException("No se puede eliminar a " + m.getNombre()
                        + " porque tiene reservas en el historial.");
            }
        }
        mascotas.remove(m);
        m.getDueno().quitarMascota(m);
        guardarTodo();
    }

    // ===================== ABM de alojamientos =====================

    public int proximoIdAlojamiento() {
        int max = 0;
        for (Alojamiento a : sucursal.getAlojamientos()) {
            if (a.getIdAlojamiento() > max) {
                max = a.getIdAlojamiento();
            }
        }
        return max + 1;
    }

    public void agregarAlojamiento(Alojamiento a) throws DatoInvalidoException {
        if (buscarAlojamiento(a.getCodigo()) != null) {
            throw new DatoInvalidoException("Ya existe un alojamiento con código " + a.getCodigo());
        }
        sucursal.agregarAlojamiento(a);
        guardarTodo();
    }

    public void eliminarAlojamiento(Alojamiento a) throws DatoInvalidoException {
        if (!a.getReservas().isEmpty()) {
            throw new DatoInvalidoException("No se puede eliminar " + a.getCodigo()
                    + " porque tiene reservas en el historial.");
        }
        sucursal.eliminarAlojamiento(a);
        guardarTodo();
    }

    public Alojamiento buscarAlojamiento(String codigo) {
        for (Alojamiento a : sucursal.getAlojamientos()) {
            if (a.getCodigo().equalsIgnoreCase(codigo)) {
                return a;
            }
        }
        return null;
    }

    // ===================== Reservas =====================

    public ArrayList<Alojamiento> buscarAlojamientos(Especie especie, LocalDate desde, LocalDate hasta) {
        return sucursal.buscarDisponibles(especie, desde, hasta);
    }

    public Reserva reservar(Mascota m, Alojamiento a, LocalDate desde, LocalDate hasta,
                            boolean esHospedaje, boolean conExtra)
            throws AlojamientoNoDisponibleException, DatoInvalidoException {
        if (hasta.isBefore(desde)) {
            throw new DatoInvalidoException("La fecha hasta no puede ser anterior a la fecha desde.");
        }
        if (esHospedaje && hasta.equals(desde)) {
            throw new DatoInvalidoException("Un hospedaje tiene que ser de al menos una noche.");
        }
        if (a.getEspecie() != m.getEspecie()) {
            throw new DatoInvalidoException("El alojamiento " + a.getCodigo() + " es para "
                    + a.getEspecie() + " y la mascota es " + m.getEspecie() + ".");
        }
        if (!a.estaDisponible(desde, hasta)) {
            throw new AlojamientoNoDisponibleException(a);
        }

        int id = reservas.size() + 1;
        Reserva r;
        if (esHospedaje) {
            r = new ReservaHospedaje(id, m, a, desde, hasta, conExtra);
        } else {
            r = new ReservaGuarderiaDiurna(id, m, a, desde, hasta, conExtra);
        }
        reservas.add(r);
        a.agregarReserva(r);
        guardarTodo();
        return r;
    }

    public void cancelarReserva(Reserva r) throws DatoInvalidoException {
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new DatoInvalidoException("Solo se pueden cancelar reservas confirmadas. Esta está " + r.getEstado());
        }
        r.cancelar();
        guardarTodo();
    }

    public ArrayList<Reserva> getReservasConfirmadas() {
        ArrayList<Reserva> lista = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA) {
                lista.add(r);
            }
        }
        return lista;
    }

    // ===================== Estadías: check-in, bitácora y check-out =====================

    public Estadia realizarCheckIn(Reserva r, Empleado e) throws VacunacionVencidaException, DatoInvalidoException {
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new DatoInvalidoException("La reserva está " + r.getEstado() + ", no se puede hacer el check-in.");
        }
        // Las vacunas tienen que estar vigentes hasta el último día de la estadía
        Mascota m = r.getMascota();
        if (!m.tieneVacunasAlDia(r.getFechaHasta())) {
            throw new VacunacionVencidaException(m, m.getVacunasFaltantes(r.getFechaHasta()));
        }

        Estadia est = new Estadia(estadias.size() + 1, r, e, LocalDate.now());
        estadias.add(est);
        r.iniciar();
        guardarTodo();
        return est;
    }

    public void registrarEnBitacora(Estadia est, EntradaBitacora entrada) throws DatoInvalidoException {
        if (entrada instanceof RegistroMedicacion && !entrada.getRegistradaPor().puedeAdministrarMedicacion()) {
            throw new DatoInvalidoException(entrada.getRegistradaPor().getNombreCompleto()
                    + " es " + entrada.getRegistradaPor().getRol()
                    + " y no puede administrar medicación. Tiene que hacerlo un cuidador o veterinario.");
        }
        est.registrar(entrada);
        guardarTodo();
    }

    public Pago realizarCheckOut(Estadia est, MedioPago medio) {
        int idPago = 1;
        for (Estadia e : estadias) {
            if (e.getPago() != null) {
                idPago++;
            }
        }
        Pago pago = new Pago(idPago, LocalDate.now(), est.calcularTotal(), medio);
        est.cerrar(LocalDate.now(), pago);
        est.getReserva().finalizar();
        guardarTodo();
        return pago;
    }

    public ArrayList<Estadia> getEstadiasActivas() {
        ArrayList<Estadia> lista = new ArrayList<>();
        for (Estadia e : estadias) {
            if (e.estaActiva()) {
                lista.add(e);
            }
        }
        return lista;
    }

    // ===================== Getters =====================

    public Sucursal getSucursal() {
        return sucursal;
    }

    public ArrayList<Dueno> getDuenos() {
        return duenos;
    }

    public ArrayList<Empleado> getEmpleados() {
        return empleados;
    }

    public ArrayList<Mascota> getMascotas() {
        return mascotas;
    }

    public ArrayList<Alojamiento> getAlojamientos() {
        return sucursal.getAlojamientos();
    }

    public ArrayList<Reserva> getReservas() {
        return reservas;
    }

    public ArrayList<Estadia> getEstadias() {
        return estadias;
    }
}
