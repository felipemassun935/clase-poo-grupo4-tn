package controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import excepciones.DatoInvalidoException;
import excepciones.IngresoRechazadoException;
import modelo.enums.AccionLog;
import modelo.enums.Categoria;
import modelo.enums.Especie;
import modelo.enums.EstadoReserva;
import modelo.enums.TipoIndicacion;
import modelo.enums.TipoRegistro;
import modelo.enums.TipoServicio;
import modelo.log.EntradaLog;
import modelo.mascota.Ave;
import modelo.mascota.Conejo;
import modelo.mascota.Gato;
import modelo.mascota.Mascota;
import modelo.mascota.Perro;
import modelo.persona.Cliente;
import modelo.persona.Empleado;
import modelo.reserva.Indicacion;
import modelo.reserva.Reserva;
import modelo.reserva.ServicioContratado;
import modelo.sucursal.Alojamiento;
import modelo.sucursal.Sucursal;
import persistencia.ConexionBD;
import persistencia.Repositorio;

/*
 * Controlador del sistema (patrón GRASP Controlador).
 * Las pantallas no tocan las listas ni los archivos directamente: le piden todo a esta clase.
 * Acá se hacen las validaciones de los formularios y, después de cada cambio, se guarda en la base de datos
 * (a través del Repositorio) y se deja una línea en el log con el empleado que está operando.
 *
 * Singleton: hay un solo objeto SistemaGuarderia en todo el programa
 * y todas las pantallas lo obtienen con SistemaGuarderia.getInstancia().
 */
public class SistemaGuarderia {
    private static SistemaGuarderia instancia; // la única instancia (Singleton)

    private List<Sucursal> sucursales;
    private List<Empleado> empleados;
    private List<Cliente> clientes;
    private List<Mascota> mascotas;
    private List<Reserva> reservas;
    // Los alojamientos no tienen lista propia: se acceden por cada sucursal

    private Repositorio repositorio;
    private Empleado operador; // el empleado que ingresó al sistema: queda en el log de cada acción

    // Singleton: el constructor es privado, desde afuera no se puede hacer "new SistemaGuarderia()".
    // Si la base de datos no responde, sale una ErrorBaseDeDatosException (la atrapa Main).
    private SistemaGuarderia() {
        sucursales = new ArrayList<Sucursal>();
        empleados = new ArrayList<Empleado>();
        clientes = new ArrayList<Cliente>();
        mascotas = new ArrayList<Mascota>();
        reservas = new ArrayList<Reserva>();
        repositorio = new Repositorio(new ConexionBD());
        repositorio.probarConexion();
        repositorio.cargar(sucursales, empleados, clientes, mascotas, reservas);
    }

    // Singleton: la primera vez crea el objeto, las siguientes devuelve siempre el mismo
    public static SistemaGuarderia getInstancia() {
        if (instancia == null) {
            instancia = new SistemaGuarderia();
        }
        return instancia;
    }

    // ===================== Ingreso y log =====================

    // El empleado ingresa con su legajo. Desde ahí, todo lo que hace queda a su nombre en el log.
    public Empleado iniciarSesion(String legajo) throws DatoInvalidoException {
        validarTexto(legajo, "el legajo");
        for (Empleado e : empleados) {
            if (e.getLegajo().equals(legajo.trim())) {
                operador = e;
                registrarLog(AccionLog.INGRESO, e.getNombreCompleto() + " ingresó como " + e.getRol() + " de "
                        + e.getSucursal().getNombre());
                return e;
            }
        }
        throw new DatoInvalidoException("No existe un empleado con el legajo " + legajo + ".");
    }

    public Empleado getOperador() {
        return operador;
    }

    // Deja una línea en el log con la fecha, el operador y lo que pasó
    private void registrarLog(AccionLog accion, String detalle) {
        String usuario = "Sin usuario";
        if (operador != null) {
            usuario = operador.getNombreCompleto() + " (legajo " + operador.getLegajo() + ")";
        }
        EntradaLog entrada = new EntradaLog(LocalDateTime.now().withNano(0), usuario, accion, detalle);
        repositorio.insertarLog(entrada);
        System.out.println("[LOG] " + entrada); // también se ve en la consola
    }

    // Todo el log, del más nuevo al más viejo (lo lee de la base cada vez)
    public List<EntradaLog> getLogs() {
        return repositorio.listarLogs();
    }

    // Solo los encargados de sucursal pueden ver el log completo
    public boolean puedeVerLog() {
        return operador != null && operador.esEncargado();
    }

    // ===================== Consultas =====================

    public List<Sucursal> getSucursales() {
        return sucursales;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public List<Mascota> getMascotas() {
        return mascotas;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    // Junta los alojamientos de todas las sucursales en una sola lista
    public List<Alojamiento> getAlojamientos() {
        List<Alojamiento> todos = new ArrayList<Alojamiento>();
        for (Sucursal s : sucursales) {
            for (Alojamiento a : s.getAlojamientos()) {
                todos.add(a);
            }
        }
        return todos;
    }

    // ===================== Clientes =====================

    public Cliente altaCliente(String nombre, String apellido, String dni, String telefono, String email,
            String direccion) throws DatoInvalidoException {
        validarPersona(nombre, apellido, dni);
        if (buscarClientePorDni(dni) != null) {
            throw new DatoInvalidoException("Ya existe un cliente con el DNI " + dni + ".");
        }
        Cliente c = new Cliente(siguienteIdCliente(), nombre, apellido, dni, telefono, email, direccion);
        repositorio.insertarCliente(c);
        clientes.add(c);
        registrarLog(AccionLog.ALTA, "Cliente " + c.getNombreCompleto() + " (DNI " + dni + ")");
        return c;
    }

    public void modificarCliente(Cliente c, String nombre, String apellido, String dni, String telefono,
            String email, String direccion) throws DatoInvalidoException {
        validarPersona(nombre, apellido, dni);
        Cliente otro = buscarClientePorDni(dni);
        if (otro != null && otro != c) {
            throw new DatoInvalidoException("Ya existe otro cliente con el DNI " + dni + ".");
        }
        c.setNombre(nombre);
        c.setApellido(apellido);
        c.setDni(dni);
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setDireccion(direccion);
        repositorio.actualizarCliente(c);
        registrarLog(AccionLog.MODIFICACION, "Cliente " + c.getNombreCompleto() + " (id " + c.getId() + ")");
    }

    // No se puede borrar un cliente que todavía tiene mascotas
    public void bajaCliente(Cliente c) throws DatoInvalidoException {
        if (!c.getMascotas().isEmpty()) {
            throw new DatoInvalidoException("No se puede borrar a " + c.getNombreCompleto()
                    + " porque tiene mascotas cargadas. Primero borrá sus mascotas.");
        }
        repositorio.borrarCliente(c);
        clientes.remove(c);
        registrarLog(AccionLog.BAJA, "Cliente " + c.getNombreCompleto() + " (DNI " + c.getDni() + ")");
    }

    private Cliente buscarClientePorDni(String dni) {
        for (Cliente c : clientes) {
            if (c.getDni().equals(dni)) {
                return c;
            }
        }
        return null;
    }

    // ===================== Mascotas =====================

    // "raza" es la raza para perro, gato y conejo, y el tipo de ave para el ave.
    // "castrado" solo se usa para perro y gato.
    public Mascota altaMascota(Especie especie, String nombre, LocalDate fechaNacimiento, double pesoKg,
            String observaciones, LocalDate fechaUltimoControl, Cliente cliente, String raza, boolean castrado)
            throws DatoInvalidoException {
        if (especie == null) {
            throw new DatoInvalidoException("Elegí la especie.");
        }
        if (cliente == null) {
            throw new DatoInvalidoException("Elegí el cliente dueño de la mascota.");
        }
        validarMascota(nombre, fechaNacimiento, pesoKg, fechaUltimoControl);

        Integer id = siguienteIdMascota();
        Mascota m = null;
        switch (especie) {
            case PERRO:
                m = new Perro(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl, raza, castrado);
                break;
            case GATO:
                m = new Gato(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl, raza, castrado);
                break;
            case CONEJO:
                m = new Conejo(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl, raza);
                break;
            case AVE:
                m = new Ave(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl, raza);
                break;
        }
        // agregarMascota también le deja anotado a la mascota quién es su cliente
        cliente.agregarMascota(m);
        repositorio.insertarMascota(m);
        mascotas.add(m);
        registrarLog(AccionLog.ALTA, "Mascota " + m.getNombre() + " (" + m.getCodigoCollar() + ") de "
                + cliente.getNombreCompleto());
        return m;
    }

    // La especie y el cliente no se cambian: para eso se borra y se vuelve a cargar
    public void modificarMascota(Mascota m, String nombre, LocalDate fechaNacimiento, double pesoKg,
            String observaciones, LocalDate fechaUltimoControl, String raza, boolean castrado)
            throws DatoInvalidoException {
        validarMascota(nombre, fechaNacimiento, pesoKg, fechaUltimoControl);
        m.setNombre(nombre);
        m.setFechaNacimiento(fechaNacimiento);
        m.setPesoKg(pesoKg);
        m.setObservaciones(observaciones);
        m.setFechaUltimoControl(fechaUltimoControl);

        // Los datos propios de cada especie se cambian con instanceof + cast
        if (m instanceof Perro) {
            Perro p = (Perro) m;
            p.setRaza(raza);
            p.setCastrado(castrado);
        } else if (m instanceof Gato) {
            Gato g = (Gato) m;
            g.setRaza(raza);
            g.setCastrado(castrado);
        } else if (m instanceof Conejo) {
            Conejo c = (Conejo) m;
            c.setRaza(raza);
        } else if (m instanceof Ave) {
            Ave a = (Ave) m;
            a.setTipoAve(raza);
        }
        repositorio.actualizarMascota(m);
        registrarLog(AccionLog.MODIFICACION, "Mascota " + m.getNombre() + " (" + m.getCodigoCollar() + ")");
    }

    // No se puede borrar una mascota que tiene reservas (aunque sean viejas)
    public void bajaMascota(Mascota m) throws DatoInvalidoException {
        for (Reserva r : reservas) {
            if (r.getMascota() == m) {
                throw new DatoInvalidoException("No se puede borrar a " + m.getNombre() + " porque tiene reservas.");
            }
        }
        repositorio.borrarMascota(m);
        m.getCliente().quitarMascota(m);
        mascotas.remove(m);
        registrarLog(AccionLog.BAJA, "Mascota " + m.getNombre() + " (" + m.getCodigoCollar() + ")");
    }

    public void agregarVacuna(Mascota m, String nombre, LocalDate fechaAplicacion, LocalDate fechaVencimiento)
            throws DatoInvalidoException {
        validarTexto(nombre, "el nombre de la vacuna");
        if (fechaAplicacion == null || fechaVencimiento == null) {
            throw new DatoInvalidoException("Completá la fecha de aplicación y la de vencimiento.");
        }
        if (!fechaVencimiento.isAfter(fechaAplicacion)) {
            throw new DatoInvalidoException("El vencimiento tiene que ser posterior a la aplicación.");
        }
        m.agregarVacuna(new Mascota.Vacuna(nombre, fechaAplicacion, fechaVencimiento));
        repositorio.guardarVacunas(m);
        registrarLog(AccionLog.ALTA, "Vacuna " + nombre + " a " + m.getNombre() + " (" + m.getCodigoCollar()
                + "), vence " + fechaVencimiento);
    }

    public void quitarVacuna(Mascota m, Mascota.Vacuna v) {
        m.getVacunas().remove(v);
        repositorio.guardarVacunas(m);
        registrarLog(AccionLog.BAJA, "Vacuna " + v.getNombre() + " de " + m.getNombre() + " (" + m.getCodigoCollar()
                + ")");
    }

    // Busca una mascota por el código de su collar (no importan mayúsculas ni espacios).
    // También acepta solo el número: "7" encuentra a "COL-00007".
    public Mascota buscarPorCollar(String codigo) throws DatoInvalidoException {
        validarTexto(codigo, "el código del collar");
        String buscado = codigo.trim().toUpperCase();
        boolean soloNumeros = true;
        for (int i = 0; i < buscado.length(); i++) {
            if (!Character.isDigit(buscado.charAt(i))) {
                soloNumeros = false;
            }
        }
        if (soloNumeros) {
            buscado = Mascota.generarCodigoCollar(Integer.parseInt(buscado));
        }
        for (Mascota m : mascotas) {
            if (m.getCodigoCollar().equals(buscado)) {
                return m;
            }
        }
        throw new DatoInvalidoException("No hay ninguna mascota con el collar " + codigo + ".");
    }

    // ===================== Empleados =====================

    public Empleado altaEmpleado(String nombre, String apellido, String dni, String telefono, String email,
            String legajo, LocalDate fechaIngreso, Sucursal sucursal) throws DatoInvalidoException {
        validarEmpleado(nombre, apellido, dni, legajo, fechaIngreso, null);
        if (sucursal == null) {
            throw new DatoInvalidoException("Elegí la sucursal del empleado.");
        }
        Empleado e = new Empleado(siguienteIdEmpleado(), nombre, apellido, dni, telefono, email, legajo,
                fechaIngreso);
        // agregarEmpleado también le deja anotada la sucursal al empleado
        sucursal.agregarEmpleado(e);
        repositorio.insertarEmpleado(e);
        empleados.add(e);
        registrarLog(AccionLog.ALTA, "Empleado " + e.getNombreCompleto() + " (legajo " + legajo + ") en "
                + sucursal.getNombre());
        return e;
    }

    // La sucursal no se cambia: para eso se borra y se vuelve a cargar
    public void modificarEmpleado(Empleado e, String nombre, String apellido, String dni, String telefono,
            String email, String legajo, LocalDate fechaIngreso) throws DatoInvalidoException {
        validarEmpleado(nombre, apellido, dni, legajo, fechaIngreso, e);
        e.setNombre(nombre);
        e.setApellido(apellido);
        e.setDni(dni);
        e.setTelefono(telefono);
        e.setEmail(email);
        e.setLegajo(legajo);
        e.setFechaIngreso(fechaIngreso);
        repositorio.actualizarEmpleado(e);
        registrarLog(AccionLog.MODIFICACION, "Empleado " + e.getNombreCompleto() + " (legajo " + legajo + ")");
    }

    // No se puede borrar al encargado de una sucursal ni a alguien que cargó registros
    public void bajaEmpleado(Empleado e) throws DatoInvalidoException {
        for (Sucursal s : sucursales) {
            if (s.getEncargado() == e) {
                throw new DatoInvalidoException(e.getNombreCompleto() + " es el encargado de " + s.getNombre()
                        + ". Primero asigná otro encargado.");
            }
        }
        for (Reserva r : reservas) {
            for (Reserva.RegistroDiario reg : r.getRegistros()) {
                if (reg.getEmpleado() == e) {
                    throw new DatoInvalidoException("No se puede borrar a " + e.getNombreCompleto()
                            + " porque cargó registros de estadías.");
                }
            }
            for (ServicioContratado s : r.getServicios()) {
                if (s.getRealizadoPor() == e) {
                    throw new DatoInvalidoException("No se puede borrar a " + e.getNombreCompleto()
                            + " porque realizó servicios.");
                }
            }
        }
        if (e == operador) {
            throw new DatoInvalidoException("No te podés borrar a vos mismo mientras estás operando.");
        }
        repositorio.borrarEmpleado(e);
        e.getSucursal().getEmpleados().remove(e);
        empleados.remove(e);
        registrarLog(AccionLog.BAJA, "Empleado " + e.getNombreCompleto() + " (legajo " + e.getLegajo() + ")");
    }

    // El encargado tiene que trabajar en esa sucursal
    public void setEncargado(Sucursal s, Empleado e) throws DatoInvalidoException {
        if (e.getSucursal() != s) {
            throw new DatoInvalidoException(e.getNombreCompleto() + " no trabaja en " + s.getNombre() + ".");
        }
        s.setEncargado(e);
        repositorio.actualizarEncargado(s);
        registrarLog(AccionLog.MODIFICACION, e.getNombreCompleto() + " es el nuevo encargado de " + s.getNombre());
    }

    // "actual" es el empleado que se está modificando (null si es un alta), para no compararlo consigo mismo
    private void validarEmpleado(String nombre, String apellido, String dni, String legajo, LocalDate fechaIngreso,
            Empleado actual) throws DatoInvalidoException {
        validarPersona(nombre, apellido, dni);
        validarTexto(legajo, "el legajo");
        if (fechaIngreso == null) {
            throw new DatoInvalidoException("Completá la fecha de ingreso.");
        }
        for (Empleado otro : empleados) {
            if (otro != actual) {
                if (otro.getDni().equals(dni)) {
                    throw new DatoInvalidoException("Ya existe un empleado con el DNI " + dni + ".");
                }
                if (otro.getLegajo().equals(legajo)) {
                    throw new DatoInvalidoException("Ya existe un empleado con el legajo " + legajo + ".");
                }
            }
        }
    }

    // ===================== Alojamientos =====================

    public Alojamiento altaAlojamiento(Sucursal sucursal, String codigo, Especie especie, Categoria categoria,
            double precioBaseNoche, boolean activo) throws DatoInvalidoException {
        if (sucursal == null) {
            throw new DatoInvalidoException("Elegí la sucursal.");
        }
        validarAlojamiento(sucursal, codigo, especie, categoria, precioBaseNoche, null);
        Alojamiento a = new Alojamiento(siguienteIdAlojamiento(), codigo, especie, categoria, precioBaseNoche, activo);
        // agregarAlojamiento también le deja anotada la sucursal al alojamiento
        sucursal.agregarAlojamiento(a);
        repositorio.insertarAlojamiento(a);
        registrarLog(AccionLog.ALTA, "Alojamiento " + codigo + " en " + sucursal.getNombre());
        return a;
    }

    public void modificarAlojamiento(Alojamiento a, String codigo, Especie especie, Categoria categoria,
            double precioBaseNoche, boolean activo) throws DatoInvalidoException {
        validarAlojamiento(a.getSucursal(), codigo, especie, categoria, precioBaseNoche, a);
        if (especie != a.getEspecie() && tieneReservas(a)) {
            throw new DatoInvalidoException("No se puede cambiar la especie de un alojamiento que tiene reservas.");
        }
        a.setCodigo(codigo);
        a.setEspecie(especie);
        a.setCategoria(categoria);
        a.setPrecioBaseNoche(precioBaseNoche);
        a.setActivo(activo);
        repositorio.actualizarAlojamiento(a);
        registrarLog(AccionLog.MODIFICACION, "Alojamiento " + codigo + " de " + a.getSucursal().getNombre());
    }

    // No se puede borrar un alojamiento que tiene reservas (aunque sean viejas)
    public void bajaAlojamiento(Alojamiento a) throws DatoInvalidoException {
        if (tieneReservas(a)) {
            throw new DatoInvalidoException("No se puede borrar el alojamiento " + a.getCodigo()
                    + " porque tiene reservas. Si no se usa más, marcalo como inactivo.");
        }
        repositorio.borrarAlojamiento(a);
        a.getSucursal().getAlojamientos().remove(a);
        registrarLog(AccionLog.BAJA, "Alojamiento " + a.getCodigo() + " de " + a.getSucursal().getNombre());
    }

    // El código no se puede repetir dentro de la misma sucursal.
    // "actual" es el alojamiento que se está modificando (null si es un alta).
    private void validarAlojamiento(Sucursal sucursal, String codigo, Especie especie, Categoria categoria,
            double precioBaseNoche, Alojamiento actual) throws DatoInvalidoException {
        validarTexto(codigo, "el código");
        if (especie == null || categoria == null) {
            throw new DatoInvalidoException("Elegí la especie y la categoría.");
        }
        if (precioBaseNoche <= 0) {
            throw new DatoInvalidoException("El precio por noche tiene que ser mayor a 0.");
        }
        for (Alojamiento otro : sucursal.getAlojamientos()) {
            if (otro != actual && otro.getCodigo().equalsIgnoreCase(codigo)) {
                throw new DatoInvalidoException("Ya existe el código " + codigo + " en " + sucursal.getNombre() + ".");
            }
        }
    }

    private boolean tieneReservas(Alojamiento a) {
        for (Reserva r : reservas) {
            if (r.getAlojamiento() == a) {
                return true;
            }
        }
        return false;
    }

    // ===================== Reservas =====================

    // Alojamientos de la sucursal que sirven para la mascota y están libres entre esas fechas
    public List<Alojamiento> buscarDisponibles(Sucursal s, Mascota m, LocalDate desde, LocalDate hasta) {
        List<Alojamiento> disponibles = new ArrayList<Alojamiento>();
        for (Alojamiento a : s.getAlojamientos()) {
            if (a.admite(m) && estaLibre(a, desde, hasta)) {
                disponibles.add(a);
            }
        }
        return disponibles;
    }

    // Crea una reserva nueva (arranca PENDIENTE)
    public Reserva reservar(Mascota m, Alojamiento a, LocalDate desde, LocalDate hasta) throws DatoInvalidoException {
        if (m == null || a == null) {
            throw new DatoInvalidoException("Elegí la mascota y el alojamiento.");
        }
        if (desde == null || hasta == null) {
            throw new DatoInvalidoException("Completá las dos fechas.");
        }
        if (!hasta.isAfter(desde)) {
            throw new DatoInvalidoException("La fecha de salida tiene que ser posterior a la de entrada.");
        }
        if (!a.admite(m)) {
            throw new DatoInvalidoException("El alojamiento " + a.getCodigo() + " no admite a " + m.getNombre()
                    + " (está inactivo o es para otra especie).");
        }
        if (!estaLibre(a, desde, hasta)) {
            throw new DatoInvalidoException("El alojamiento " + a.getCodigo() + " ya está reservado en esas fechas.");
        }
        Reserva r = new Reserva(siguienteIdReserva(), m, a, desde, hasta);
        repositorio.insertarReserva(r);
        reservas.add(r);
        registrarLog(AccionLog.RESERVA, "Reserva " + r.getId() + " creada: " + m.getNombre() + " ("
                + m.getCodigoCollar() + ") en " + a.getCodigo() + " del " + desde + " al " + hasta);
        return r;
    }

    public void confirmar(Reserva r) {
        r.confirmar();
        repositorio.actualizarEstado(r);
        registrarLog(AccionLog.RESERVA, "Reserva " + r.getId() + " confirmada");
    }

    public void cancelar(Reserva r) {
        r.cancelar();
        repositorio.actualizarEstado(r);
        registrarLog(AccionLog.RESERVA, "Reserva " + r.getId() + " cancelada");
    }

    // Si la mascota no cumple los requisitos, se deja en el log y la excepción sube hasta la pantalla
    public void checkIn(Reserva r) throws IngresoRechazadoException {
        try {
            r.realizarCheckIn(LocalDate.now());
        } catch (IngresoRechazadoException e) {
            registrarLog(AccionLog.RECHAZO, "Check-in rechazado de " + r.getMascota().getNombre() + " ("
                    + r.getMascota().getCodigoCollar() + "): " + e.getMessage().replace("\n", " ").trim());
            throw e; // se vuelve a lanzar para que la pantalla muestre el problema
        }
        repositorio.actualizarEstado(r);
        registrarLog(AccionLog.CHECK_IN, "Check-in de la reserva " + r.getId() + " (" + r.getMascota().getNombre()
                + ", " + r.getMascota().getCodigoCollar() + ") en " + r.getAlojamiento().getCodigo());
    }

    // "empleado" es quien lo hizo; "indicacion" es la indicación del dueño que cumple (puede ser null)
    public void agregarRegistro(Reserva r, TipoRegistro tipo, String descripcion, Empleado empleado,
            Indicacion indicacion) throws DatoInvalidoException {
        if (tipo == null || empleado == null) {
            throw new DatoInvalidoException("Elegí el tipo de registro y el empleado.");
        }
        validarTexto(descripcion, "la descripción");
        validarEmpleadoDeLaSucursal(r, empleado);
        Reserva.RegistroDiario reg = r.agregarRegistro(tipo, descripcion, empleado, indicacion);
        repositorio.insertarRegistro(r, reg);
        registrarLog(AccionLog.REGISTRO, "Reserva " + r.getId() + ": " + tipo + " - " + descripcion + " (hizo: "
                + empleado.getNombreCompleto() + ")");
    }

    // No se puede pagar más de lo que se debe
    public void registrarPago(Reserva r, double monto, String medio) throws DatoInvalidoException {
        if (monto <= 0) {
            throw new DatoInvalidoException("El monto tiene que ser mayor a 0.");
        }
        if (monto > r.getSaldoPendiente() + 0.001) {
            throw new DatoInvalidoException("El monto supera el saldo pendiente (" + r.getSaldoPendiente() + ").");
        }
        validarTexto(medio, "el medio de pago");
        Reserva.Pago pago = r.registrarPago(monto, medio);
        repositorio.insertarPago(r, pago);
        registrarLog(AccionLog.PAGO, "Reserva " + r.getId() + ": pago de $" + String.format("%.2f", monto) + " ("
                + medio + "). Saldo: $" + String.format("%.2f", r.getSaldoPendiente()));
    }

    public void checkOut(Reserva r) {
        r.realizarCheckOut();
        repositorio.actualizarEstado(r);
        registrarLog(AccionLog.CHECK_OUT, "Check-out de la reserva " + r.getId() + " (" + r.getMascota().getNombre()
                + ", " + r.getMascota().getCodigoCollar() + "). Total cobrado: $"
                + String.format("%.2f", r.getTotalPagado()));
    }

    // ===================== Servicios extra =====================

    public ServicioContratado contratarServicio(Reserva r, TipoServicio tipo) throws DatoInvalidoException {
        if (tipo == null) {
            throw new DatoInvalidoException("Elegí el servicio.");
        }
        ServicioContratado s = r.contratarServicio(siguienteIdServicio(), tipo);
        repositorio.insertarServicio(r, s);
        registrarLog(AccionLog.SERVICIO, "Reserva " + r.getId() + ": se contrató " + tipo + " ($"
                + String.format("%.2f", s.getPrecio()) + ")");
        return s;
    }

    // El empleado que lo hizo tiene que trabajar en la sucursal de la reserva
    public void realizarServicio(Reserva r, ServicioContratado s, Empleado empleado) throws DatoInvalidoException {
        if (empleado == null) {
            throw new DatoInvalidoException("Elegí el empleado que hizo el servicio.");
        }
        validarEmpleadoDeLaSucursal(r, empleado);
        Reserva.RegistroDiario reg = r.realizarServicio(s, empleado);
        repositorio.actualizarServicio(s);
        repositorio.insertarRegistro(r, reg);
        registrarLog(AccionLog.SERVICIO, "Reserva " + r.getId() + ": " + s.getTipo() + " realizado por "
                + empleado.getNombreCompleto());
    }

    public void quitarServicio(Reserva r, ServicioContratado s) {
        r.quitarServicio(s);
        repositorio.borrarServicio(s);
        registrarLog(AccionLog.SERVICIO, "Reserva " + r.getId() + ": se quitó " + s.getTipo());
    }

    // ===================== Indicaciones del dueño =====================

    public Indicacion agregarIndicacion(Reserva r, TipoIndicacion tipo, String descripcion, String horario)
            throws DatoInvalidoException {
        if (tipo == null) {
            throw new DatoInvalidoException("Elegí el tipo de indicación.");
        }
        validarTexto(descripcion, "la descripción de la indicación");
        if (horario == null) {
            horario = "";
        }
        Indicacion i = r.agregarIndicacion(siguienteIdIndicacion(), tipo, descripcion.trim(), horario.trim());
        repositorio.insertarIndicacion(r, i);
        registrarLog(AccionLog.INDICACION, "Reserva " + r.getId() + ": nueva indicación " + i);
        return i;
    }

    public void quitarIndicacion(Reserva r, Indicacion i) {
        r.quitarIndicacion(i);
        repositorio.borrarIndicacion(i);
        registrarLog(AccionLog.INDICACION, "Reserva " + r.getId() + ": se quitó la indicación " + i);
    }

    // Los empleados de la sucursal donde está la mascota (para elegir quién hizo algo)
    public List<Empleado> getEmpleadosDe(Sucursal s) {
        return s.getEmpleados();
    }

    private void validarEmpleadoDeLaSucursal(Reserva r, Empleado empleado) throws DatoInvalidoException {
        Sucursal s = r.getAlojamiento().getSucursal();
        if (empleado.getSucursal() != s) {
            throw new DatoInvalidoException(empleado.getNombreCompleto() + " no trabaja en " + s.getNombre() + ".");
        }
    }

    // Una reserva "ocupa" el alojamiento si está PENDIENTE, CONFIRMADA o EN_CURSO
    private boolean ocupaAlojamiento(Reserva r) {
        EstadoReserva e = r.getEstado();
        return e == EstadoReserva.PENDIENTE || e == EstadoReserva.CONFIRMADA || e == EstadoReserva.EN_CURSO;
    }

    // true si ninguna reserva que ocupa el alojamiento se superpone con esas fechas
    private boolean estaLibre(Alojamiento a, LocalDate desde, LocalDate hasta) {
        for (Reserva r : reservas) {
            if (r.getAlojamiento() == a && ocupaAlojamiento(r) && r.seSuperponeCon(desde, hasta)) {
                return false;
            }
        }
        return true;
    }

    // ===================== Control de ocupación =====================

    // Para cada especie devuelve un texto "ocupados/capacidad".
    // Ocupados = reservas EN_CURSO en la sucursal. Capacidad = alojamientos activos.
    public Map<Especie, String> getOcupacion(Sucursal s) {
        Map<Especie, Integer> capacidad = s.getCapacidadPorEspecie();

        Map<Especie, Integer> ocupados = new HashMap<Especie, Integer>();
        for (Especie e : Especie.values()) {
            ocupados.put(e, 0);
        }
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.EN_CURSO && r.getAlojamiento().getSucursal() == s) {
                Especie e = r.getAlojamiento().getEspecie();
                ocupados.put(e, ocupados.get(e) + 1);
            }
        }

        Map<Especie, String> resultado = new HashMap<Especie, String>();
        for (Especie e : Especie.values()) {
            resultado.put(e, ocupados.get(e) + "/" + capacidad.get(e));
        }
        return resultado;
    }

    // ===================== Ids nuevos =====================
    // Cada alta usa el id más alto que haya en la lista + 1

    private Integer siguienteIdCliente() {
        int maximo = 0;
        for (Cliente c : clientes) {
            if (c.getId() > maximo) {
                maximo = c.getId();
            }
        }
        return maximo + 1;
    }

    private Integer siguienteIdMascota() {
        int maximo = 0;
        for (Mascota m : mascotas) {
            if (m.getId() > maximo) {
                maximo = m.getId();
            }
        }
        return maximo + 1;
    }

    private Integer siguienteIdEmpleado() {
        int maximo = 0;
        for (Empleado e : empleados) {
            if (e.getId() > maximo) {
                maximo = e.getId();
            }
        }
        return maximo + 1;
    }

    // Los alojamientos están repartidos en las sucursales, por eso se recorren todas
    private Integer siguienteIdAlojamiento() {
        int maximo = 0;
        for (Sucursal s : sucursales) {
            for (Alojamiento a : s.getAlojamientos()) {
                if (a.getId() > maximo) {
                    maximo = a.getId();
                }
            }
        }
        return maximo + 1;
    }

    // Los servicios y las indicaciones están repartidos en las reservas, por eso se recorren todas
    private Integer siguienteIdServicio() {
        int maximo = 0;
        for (Reserva r : reservas) {
            for (ServicioContratado s : r.getServicios()) {
                if (s.getId() > maximo) {
                    maximo = s.getId();
                }
            }
        }
        return maximo + 1;
    }

    private Integer siguienteIdIndicacion() {
        int maximo = 0;
        for (Reserva r : reservas) {
            for (Indicacion i : r.getIndicaciones()) {
                if (i.getId() > maximo) {
                    maximo = i.getId();
                }
            }
        }
        return maximo + 1;
    }

    private Integer siguienteIdReserva() {
        int maximo = 0;
        for (Reserva r : reservas) {
            if (r.getId() > maximo) {
                maximo = r.getId();
            }
        }
        return maximo + 1;
    }

    // ===================== Validaciones comunes =====================

    // Tira una excepción si el texto está vacío. "campo" se usa para armar el mensaje.
    private void validarTexto(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatoInvalidoException("Completá " + campo + ".");
        }
    }

    private void validarPersona(String nombre, String apellido, String dni) throws DatoInvalidoException {
        validarTexto(nombre, "el nombre");
        validarTexto(apellido, "el apellido");
        validarTexto(dni, "el DNI");
        for (int i = 0; i < dni.length(); i++) {
            if (!Character.isDigit(dni.charAt(i))) {
                throw new DatoInvalidoException("El DNI tiene que tener solo números.");
            }
        }
    }

    private void validarMascota(String nombre, LocalDate fechaNacimiento, double pesoKg, LocalDate fechaUltimoControl)
            throws DatoInvalidoException {
        validarTexto(nombre, "el nombre de la mascota");
        if (fechaNacimiento == null) {
            throw new DatoInvalidoException("Completá la fecha de nacimiento.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new DatoInvalidoException("La fecha de nacimiento no puede ser futura.");
        }
        if (fechaUltimoControl != null && fechaUltimoControl.isAfter(LocalDate.now())) {
            throw new DatoInvalidoException("La fecha del último control no puede ser futura.");
        }
        if (pesoKg <= 0) {
            throw new DatoInvalidoException("El peso tiene que ser mayor a 0.");
        }
    }
}
