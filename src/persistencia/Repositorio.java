package persistencia;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import excepciones.ErrorBaseDeDatosException;
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

/*
 * Lee y escribe los datos del sistema en la base de datos SQL Server, con JDBC
 * (DriverManager, Statement, executeQuery / executeUpdate y ResultSet, como en la clase 14).
 *
 * Patrón GRASP Fabricación pura: "Repositorio" no es un concepto de la guardería; se inventa para que
 * el modelo no sepa nada de SQL (alta cohesión y bajo acoplamiento).
 *
 * Al arrancar, cargar() lee todas las tablas y arma los objetos con sus referencias.
 * Después, cada cambio que hace el controlador se guarda enseguida con un INSERT, UPDATE o DELETE.
 */
public class Repositorio {
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ConexionBD conexion;

    public Repositorio(ConexionBD conexion) {
        this.conexion = conexion;
    }

    // Prueba la conexión al arrancar el programa, para avisar enseguida si algo está mal configurado
    public void probarConexion() {
        try (Connection con = conexion.conectar()) {
            System.out.println("Conectado a " + conexion.getUrl());
        } catch (SQLException e) {
            throw new ErrorBaseDeDatosException("No se pudo conectar a la base de datos.\nURL: " + conexion.getUrl(), e);
        }
    }

    // ===================== Cargar todo =====================

    // Llena las listas que recibe con lo que hay en la base.
    // El orden importa: por ejemplo, para armar una mascota ya tiene que existir su cliente.
    public void cargar(List<Sucursal> sucursales, List<Empleado> empleados, List<Cliente> clientes,
            List<Mascota> mascotas, List<Reserva> reservas) {

        // Mapas para encontrar rápido cada objeto por su id
        Map<Integer, Sucursal> sucursalesPorId = new HashMap<Integer, Sucursal>();
        Map<Integer, Integer> encargadoPorSucursal = new HashMap<Integer, Integer>();
        Map<Integer, Empleado> empleadosPorId = new HashMap<Integer, Empleado>();
        Map<Integer, Cliente> clientesPorId = new HashMap<Integer, Cliente>();
        Map<Integer, Alojamiento> alojamientosPorId = new HashMap<Integer, Alojamiento>();
        Map<Integer, Mascota> mascotasPorId = new HashMap<Integer, Mascota>();
        Map<Integer, Reserva> reservasPorId = new HashMap<Integer, Reserva>();
        Map<Integer, Indicacion> indicacionesPorId = new HashMap<Integer, Indicacion>();

        // try-with-resources: la conexión y el Statement se cierran solos al terminar
        try (Connection con = conexion.conectar(); Statement st = con.createStatement()) {

            // 1. Sucursales
            try (ResultSet rs = st.executeQuery("SELECT * FROM sucursal ORDER BY id")) {
                while (rs.next()) {
                    Sucursal s = new Sucursal(rs.getInt("id"), rs.getString("nombre"), rs.getString("direccion"),
                            rs.getString("telefono"));
                    sucursales.add(s);
                    sucursalesPorId.put(s.getId(), s);
                    // El encargado se asigna en el paso 3, cuando ya están cargados los empleados
                    int idEncargado = rs.getInt("id_encargado");
                    if (!rs.wasNull()) {
                        encargadoPorSucursal.put(s.getId(), idEncargado);
                    }
                }
            }

            // 2. Empleados
            try (ResultSet rs = st.executeQuery("SELECT * FROM empleado ORDER BY id")) {
                while (rs.next()) {
                    Empleado e = new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getString("apellido"),
                            rs.getString("dni"), rs.getString("telefono"), rs.getString("email"),
                            rs.getString("legajo"), leerFecha(rs, "fecha_ingreso"));
                    sucursalesPorId.get(rs.getInt("id_sucursal")).agregarEmpleado(e);
                    empleados.add(e);
                    empleadosPorId.put(e.getId(), e);
                }
            }

            // 3. El encargado de cada sucursal
            for (Sucursal s : sucursales) {
                Integer idEncargado = encargadoPorSucursal.get(s.getId());
                if (idEncargado != null) {
                    s.setEncargado(empleadosPorId.get(idEncargado));
                }
            }

            // 4. Clientes
            try (ResultSet rs = st.executeQuery("SELECT * FROM cliente ORDER BY id")) {
                while (rs.next()) {
                    Cliente c = new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("apellido"),
                            rs.getString("dni"), rs.getString("telefono"), rs.getString("email"),
                            rs.getString("direccion"));
                    clientes.add(c);
                    clientesPorId.put(c.getId(), c);
                }
            }

            // 5. Alojamientos
            try (ResultSet rs = st.executeQuery("SELECT * FROM alojamiento ORDER BY id")) {
                while (rs.next()) {
                    Alojamiento a = new Alojamiento(rs.getInt("id"), rs.getString("codigo"),
                            Especie.valueOf(rs.getString("especie")), Categoria.valueOf(rs.getString("categoria")),
                            rs.getDouble("precio_base_noche"), rs.getBoolean("activo"));
                    sucursalesPorId.get(rs.getInt("id_sucursal")).agregarAlojamiento(a);
                    alojamientosPorId.put(a.getId(), a);
                }
            }

            // 6. Mascotas: todas las especies están en la misma tabla; la columna "especie" dice qué subclase crear
            try (ResultSet rs = st.executeQuery("SELECT * FROM mascota ORDER BY id")) {
                while (rs.next()) {
                    Especie especie = Especie.valueOf(rs.getString("especie"));
                    Integer id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    LocalDate nacimiento = leerFecha(rs, "fecha_nacimiento");
                    double peso = rs.getDouble("peso_kg");
                    String observaciones = rs.getString("observaciones");
                    LocalDate ultimoControl = leerFecha(rs, "fecha_ultimo_control");
                    String raza = rs.getString("raza"); // para el ave guarda el tipo de ave
                    boolean castrado = rs.getBoolean("castrado");

                    Mascota m = null;
                    switch (especie) {
                        case PERRO:
                            m = new Perro(id, nombre, nacimiento, peso, observaciones, ultimoControl, raza, castrado);
                            break;
                        case GATO:
                            m = new Gato(id, nombre, nacimiento, peso, observaciones, ultimoControl, raza, castrado);
                            break;
                        case CONEJO:
                            m = new Conejo(id, nombre, nacimiento, peso, observaciones, ultimoControl, raza);
                            break;
                        case AVE:
                            m = new Ave(id, nombre, nacimiento, peso, observaciones, ultimoControl, raza);
                            break;
                    }
                    clientesPorId.get(rs.getInt("id_cliente")).agregarMascota(m);
                    mascotas.add(m);
                    mascotasPorId.put(id, m);
                }
            }

            // 7. Vacunas (Mascota.Vacuna es una clase anidada estática: se crea sin necesitar una Mascota)
            try (ResultSet rs = st.executeQuery("SELECT * FROM vacuna ORDER BY id_mascota, fecha_aplicacion")) {
                while (rs.next()) {
                    Mascota.Vacuna v = new Mascota.Vacuna(rs.getString("nombre"), leerFecha(rs, "fecha_aplicacion"),
                            leerFecha(rs, "fecha_vencimiento"));
                    mascotasPorId.get(rs.getInt("id_mascota")).agregarVacuna(v);
                }
            }

            // 8. Reservas, con el estado que tenían guardado
            try (ResultSet rs = st.executeQuery("SELECT * FROM reserva ORDER BY id")) {
                while (rs.next()) {
                    Reserva r = new Reserva(rs.getInt("id"), mascotasPorId.get(rs.getInt("id_mascota")),
                            alojamientosPorId.get(rs.getInt("id_alojamiento")), leerFecha(rs, "fecha_desde"),
                            leerFecha(rs, "fecha_hasta"), EstadoReserva.valueOf(rs.getString("estado")));
                    reservas.add(r);
                    reservasPorId.put(r.getId(), r);
                }
            }

            // 9. Pagos (Reserva.Pago es una clase anidada estática)
            try (ResultSet rs = st.executeQuery("SELECT * FROM pago ORDER BY id")) {
                while (rs.next()) {
                    Reserva.Pago p = new Reserva.Pago(leerFecha(rs, "fecha"), rs.getDouble("monto"),
                            rs.getString("medio"));
                    reservasPorId.get(rs.getInt("id_reserva")).getPagos().add(p);
                }
            }

            // 10. Servicios contratados
            try (ResultSet rs = st.executeQuery("SELECT * FROM servicio_contratado ORDER BY id")) {
                while (rs.next()) {
                    Empleado hizo = null;
                    int idEmpleado = rs.getInt("id_empleado");
                    if (!rs.wasNull()) {
                        hizo = empleadosPorId.get(idEmpleado);
                    }
                    ServicioContratado s = new ServicioContratado(rs.getInt("id"),
                            TipoServicio.valueOf(rs.getString("tipo")), rs.getDouble("precio"),
                            leerFecha(rs, "fecha_solicitud"), rs.getBoolean("realizado"),
                            leerFechaHora(rs, "fecha_hora_realizado"), hizo);
                    reservasPorId.get(rs.getInt("id_reserva")).getServicios().add(s);
                }
            }

            // 11. Indicaciones del dueño
            try (ResultSet rs = st.executeQuery("SELECT * FROM indicacion ORDER BY id")) {
                while (rs.next()) {
                    Indicacion i = new Indicacion(rs.getInt("id"), TipoIndicacion.valueOf(rs.getString("tipo")),
                            rs.getString("descripcion"), rs.getString("horario"));
                    reservasPorId.get(rs.getInt("id_reserva")).getIndicaciones().add(i);
                    indicacionesPorId.put(i.getId(), i);
                }
            }

            // 12. Registros de la bitácora
            try (ResultSet rs = st.executeQuery("SELECT * FROM registro_diario ORDER BY fecha_hora, id")) {
                while (rs.next()) {
                    Reserva r = reservasPorId.get(rs.getInt("id_reserva"));
                    Indicacion indicacion = null;
                    int idIndicacion = rs.getInt("id_indicacion");
                    if (!rs.wasNull()) {
                        indicacion = indicacionesPorId.get(idIndicacion);
                    }
                    // Clase interna: un RegistroDiario siempre pertenece a una Reserva,
                    // por eso se crea con la sintaxis "reserva.new RegistroDiario(...)"
                    Reserva.RegistroDiario reg = r.new RegistroDiario(leerFechaHora(rs, "fecha_hora"),
                            TipoRegistro.valueOf(rs.getString("tipo")), rs.getString("descripcion"),
                            empleadosPorId.get(rs.getInt("id_empleado")), indicacion);
                    // Se agrega directo a la lista (y no con agregarRegistro) para no volver a mandar los avisos
                    r.getRegistros().add(reg);
                }
            }
        } catch (SQLException e) {
            throw new ErrorBaseDeDatosException("No se pudieron leer los datos de la base.", e);
        }
    }

    // ===================== Clientes =====================

    public void insertarCliente(Cliente c) {
        ejecutar("INSERT INTO cliente (id, nombre, apellido, dni, telefono, email, direccion) VALUES (" + c.getId()
                + ", " + texto(c.getNombre()) + ", " + texto(c.getApellido()) + ", " + texto(c.getDni()) + ", "
                + texto(c.getTelefono()) + ", " + texto(c.getEmail()) + ", " + texto(c.getDireccion()) + ")");
    }

    public void actualizarCliente(Cliente c) {
        ejecutar("UPDATE cliente SET nombre = " + texto(c.getNombre()) + ", apellido = " + texto(c.getApellido())
                + ", dni = " + texto(c.getDni()) + ", telefono = " + texto(c.getTelefono()) + ", email = "
                + texto(c.getEmail()) + ", direccion = " + texto(c.getDireccion()) + " WHERE id = " + c.getId());
    }

    public void borrarCliente(Cliente c) {
        ejecutar("DELETE FROM cliente WHERE id = " + c.getId());
    }

    // ===================== Mascotas y vacunas =====================

    public void insertarMascota(Mascota m) {
        ejecutar("INSERT INTO mascota (id, codigo_collar, especie, nombre, fecha_nacimiento, peso_kg, observaciones, "
                + "fecha_ultimo_control, id_cliente, raza, castrado) VALUES (" + m.getId() + ", "
                + texto(m.getCodigoCollar()) + ", " + texto(m.getEspecie().name()) + ", " + texto(m.getNombre())
                + ", " + fecha(m.getFechaNacimiento()) + ", " + numero(m.getPesoKg()) + ", "
                + texto(m.getObservaciones()) + ", " + fecha(m.getFechaUltimoControl()) + ", "
                + m.getCliente().getId() + ", " + texto(getRaza(m)) + ", " + bit(isCastrado(m)) + ")");
    }

    public void actualizarMascota(Mascota m) {
        ejecutar("UPDATE mascota SET nombre = " + texto(m.getNombre()) + ", fecha_nacimiento = "
                + fecha(m.getFechaNacimiento()) + ", peso_kg = " + numero(m.getPesoKg()) + ", observaciones = "
                + texto(m.getObservaciones()) + ", fecha_ultimo_control = " + fecha(m.getFechaUltimoControl())
                + ", raza = " + texto(getRaza(m)) + ", castrado = " + bit(isCastrado(m)) + " WHERE id = " + m.getId());
    }

    // Primero se borran sus vacunas, porque la tabla vacuna apunta a la mascota (clave foránea)
    public void borrarMascota(Mascota m) {
        ejecutar("DELETE FROM vacuna WHERE id_mascota = " + m.getId());
        ejecutar("DELETE FROM mascota WHERE id = " + m.getId());
    }

    // Reemplaza todas las vacunas guardadas de la mascota por las que tiene ahora en memoria
    public void guardarVacunas(Mascota m) {
        ejecutar("DELETE FROM vacuna WHERE id_mascota = " + m.getId());
        for (Mascota.Vacuna v : m.getVacunas()) {
            ejecutar("INSERT INTO vacuna (id_mascota, nombre, fecha_aplicacion, fecha_vencimiento) VALUES ("
                    + m.getId() + ", " + texto(v.getNombre()) + ", " + fecha(v.getFechaAplicacion()) + ", "
                    + fecha(v.getFechaVencimiento()) + ")");
        }
    }

    // La raza (o el tipo de ave) solo la tienen las subclases: se usa instanceof + cast
    private String getRaza(Mascota m) {
        if (m instanceof Perro) {
            return ((Perro) m).getRaza();
        } else if (m instanceof Gato) {
            return ((Gato) m).getRaza();
        } else if (m instanceof Conejo) {
            return ((Conejo) m).getRaza();
        } else if (m instanceof Ave) {
            return ((Ave) m).getTipoAve();
        }
        return null;
    }

    private boolean isCastrado(Mascota m) {
        if (m instanceof Perro) {
            return ((Perro) m).isCastrado();
        } else if (m instanceof Gato) {
            return ((Gato) m).isCastrado();
        }
        return false;
    }

    // ===================== Empleados y sucursales =====================

    public void insertarEmpleado(Empleado e) {
        ejecutar("INSERT INTO empleado (id, nombre, apellido, dni, telefono, email, legajo, fecha_ingreso, id_sucursal) "
                + "VALUES (" + e.getId() + ", " + texto(e.getNombre()) + ", " + texto(e.getApellido()) + ", "
                + texto(e.getDni()) + ", " + texto(e.getTelefono()) + ", " + texto(e.getEmail()) + ", "
                + texto(e.getLegajo()) + ", " + fecha(e.getFechaIngreso()) + ", " + e.getSucursal().getId() + ")");
    }

    public void actualizarEmpleado(Empleado e) {
        ejecutar("UPDATE empleado SET nombre = " + texto(e.getNombre()) + ", apellido = " + texto(e.getApellido())
                + ", dni = " + texto(e.getDni()) + ", telefono = " + texto(e.getTelefono()) + ", email = "
                + texto(e.getEmail()) + ", legajo = " + texto(e.getLegajo()) + ", fecha_ingreso = "
                + fecha(e.getFechaIngreso()) + " WHERE id = " + e.getId());
    }

    public void borrarEmpleado(Empleado e) {
        ejecutar("DELETE FROM empleado WHERE id = " + e.getId());
    }

    public void actualizarEncargado(Sucursal s) {
        String idEncargado = "NULL";
        if (s.getEncargado() != null) {
            idEncargado = "" + s.getEncargado().getId();
        }
        ejecutar("UPDATE sucursal SET id_encargado = " + idEncargado + " WHERE id = " + s.getId());
    }

    // ===================== Alojamientos =====================

    public void insertarAlojamiento(Alojamiento a) {
        ejecutar("INSERT INTO alojamiento (id, codigo, especie, categoria, precio_base_noche, activo, id_sucursal) "
                + "VALUES (" + a.getId() + ", " + texto(a.getCodigo()) + ", " + texto(a.getEspecie().name()) + ", "
                + texto(a.getCategoria().name()) + ", " + numero(a.getPrecioBaseNoche()) + ", " + bit(a.isActivo())
                + ", " + a.getSucursal().getId() + ")");
    }

    public void actualizarAlojamiento(Alojamiento a) {
        ejecutar("UPDATE alojamiento SET codigo = " + texto(a.getCodigo()) + ", especie = "
                + texto(a.getEspecie().name()) + ", categoria = " + texto(a.getCategoria().name())
                + ", precio_base_noche = " + numero(a.getPrecioBaseNoche()) + ", activo = " + bit(a.isActivo())
                + " WHERE id = " + a.getId());
    }

    public void borrarAlojamiento(Alojamiento a) {
        ejecutar("DELETE FROM alojamiento WHERE id = " + a.getId());
    }

    // ===================== Reservas y todo lo que cuelga de ellas =====================

    public void insertarReserva(Reserva r) {
        ejecutar("INSERT INTO reserva (id, id_mascota, id_alojamiento, fecha_desde, fecha_hasta, estado) VALUES ("
                + r.getId() + ", " + r.getMascota().getId() + ", " + r.getAlojamiento().getId() + ", "
                + fecha(r.getFechaDesde()) + ", " + fecha(r.getFechaHasta()) + ", " + texto(r.getEstado().name())
                + ")");
    }

    public void actualizarEstado(Reserva r) {
        ejecutar("UPDATE reserva SET estado = " + texto(r.getEstado().name()) + " WHERE id = " + r.getId());
    }

    public void insertarPago(Reserva r, Reserva.Pago p) {
        ejecutar("INSERT INTO pago (id_reserva, fecha, monto, medio) VALUES (" + r.getId() + ", " + fecha(p.getFecha())
                + ", " + numero(p.getMonto()) + ", " + texto(p.getMedio()) + ")");
    }

    public void insertarRegistro(Reserva r, Reserva.RegistroDiario reg) {
        String idIndicacion = "NULL";
        if (reg.getIndicacion() != null) {
            idIndicacion = "" + reg.getIndicacion().getId();
        }
        ejecutar("INSERT INTO registro_diario (id_reserva, fecha_hora, tipo, descripcion, id_empleado, id_indicacion) "
                + "VALUES (" + r.getId() + ", " + fechaHora(reg.getFechaHora()) + ", " + texto(reg.getTipo().name())
                + ", " + texto(reg.getDescripcion()) + ", " + reg.getEmpleado().getId() + ", " + idIndicacion + ")");
    }

    public void insertarServicio(Reserva r, ServicioContratado s) {
        ejecutar("INSERT INTO servicio_contratado (id, id_reserva, tipo, precio, fecha_solicitud, realizado) VALUES ("
                + s.getId() + ", " + r.getId() + ", " + texto(s.getTipo().name()) + ", " + numero(s.getPrecio()) + ", "
                + fecha(s.getFechaSolicitud()) + ", 0)");
    }

    // Se usa cuando el servicio se marca como realizado
    public void actualizarServicio(ServicioContratado s) {
        String idEmpleado = "NULL";
        if (s.getRealizadoPor() != null) {
            idEmpleado = "" + s.getRealizadoPor().getId();
        }
        ejecutar("UPDATE servicio_contratado SET realizado = " + bit(s.isRealizado()) + ", fecha_hora_realizado = "
                + fechaHora(s.getFechaHoraRealizado()) + ", id_empleado = " + idEmpleado + " WHERE id = " + s.getId());
    }

    public void borrarServicio(ServicioContratado s) {
        ejecutar("DELETE FROM servicio_contratado WHERE id = " + s.getId());
    }

    public void insertarIndicacion(Reserva r, Indicacion i) {
        ejecutar("INSERT INTO indicacion (id, id_reserva, tipo, descripcion, horario) VALUES (" + i.getId() + ", "
                + r.getId() + ", " + texto(i.getTipo().name()) + ", " + texto(i.getDescripcion()) + ", "
                + texto(i.getHorario()) + ")");
    }

    public void borrarIndicacion(Indicacion i) {
        ejecutar("DELETE FROM indicacion WHERE id = " + i.getId());
    }

    // ===================== Log del sistema =====================

    public void insertarLog(EntradaLog log) {
        ejecutar("INSERT INTO log_sistema (fecha_hora, usuario, accion, detalle) VALUES ("
                + fechaHora(log.getFechaHora()) + ", " + texto(log.getUsuario()) + ", " + texto(log.getAccion().name())
                + ", " + texto(log.getDetalle()) + ")");
    }

    // Trae todo el log, del más nuevo al más viejo
    public List<EntradaLog> listarLogs() {
        List<EntradaLog> logs = new ArrayList<EntradaLog>();
        try (Connection con = conexion.conectar();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM log_sistema ORDER BY fecha_hora DESC, id DESC")) {
            while (rs.next()) {
                logs.add(new EntradaLog(leerFechaHora(rs, "fecha_hora"), rs.getString("usuario"),
                        AccionLog.valueOf(rs.getString("accion")), rs.getString("detalle")));
            }
        } catch (SQLException e) {
            throw new ErrorBaseDeDatosException("No se pudo leer el log del sistema.", e);
        }
        return logs;
    }

    // ===================== Métodos de ayuda =====================

    // Ejecuta un INSERT, UPDATE o DELETE (executeUpdate devuelve cuántas filas cambió)
    private void ejecutar(String sql) {
        try (Connection con = conexion.conectar(); Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("SQL con error: " + sql);
            throw new ErrorBaseDeDatosException("No se pudo guardar el cambio en la base de datos.", e);
        }
    }

    // Arma un texto para el SQL: N'...' (N = admite acentos y ñ). La comilla simple se duplica para no
    // romper la sentencia (O'Brien -> 'O''Brien'). Si es null, queda NULL.
    private String texto(String valor) {
        if (valor == null) {
            return "NULL";
        }
        return "N'" + valor.replace("'", "''") + "'";
    }

    // Fecha en formato 'aaaa-mm-dd', que SQL Server entiende siempre
    private String fecha(LocalDate valor) {
        if (valor == null) {
            return "NULL";
        }
        return "'" + valor + "'";
    }

    private String fechaHora(LocalDateTime valor) {
        if (valor == null) {
            return "NULL";
        }
        return "'" + valor.format(FORMATO_FECHA_HORA) + "'";
    }

    // Número con punto decimal (Locale.ROOT), aunque la PC esté configurada en español
    private String numero(double valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }

    // En SQL Server los boolean se guardan como BIT: 1 o 0
    private String bit(boolean valor) {
        if (valor) {
            return "1";
        }
        return "0";
    }

    private LocalDate leerFecha(ResultSet rs, String columna) throws SQLException {
        java.sql.Date valor = rs.getDate(columna);
        if (valor == null) {
            return null;
        }
        return valor.toLocalDate();
    }

    private LocalDateTime leerFechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp valor = rs.getTimestamp(columna);
        if (valor == null) {
            return null;
        }
        return valor.toLocalDateTime();
    }
}
