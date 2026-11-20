package persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.enums.Categoria;
import modelo.enums.Especie;
import modelo.enums.EstadoReserva;
import modelo.enums.TipoRegistro;
import modelo.mascota.Ave;
import modelo.mascota.Conejo;
import modelo.mascota.Gato;
import modelo.mascota.Mascota;
import modelo.mascota.Perro;
import modelo.persona.Cliente;
import modelo.persona.Empleado;
import modelo.reserva.Reserva;
import modelo.sucursal.Alojamiento;
import modelo.sucursal.Sucursal;

// Lee y escribe todos los datos del sistema en archivos de texto de la carpeta datos/.
// Cada archivo es una "tabla": cada línea es un objeto y los campos van separados por ";".
// Las referencias entre objetos se guardan por id (o legajo) y se reconstruyen al cargar.
public class ArchivoDatos {
    private static final String CARPETA = "datos/";

    private static final String SUCURSALES = CARPETA + "sucursales.txt";
    private static final String EMPLEADOS = CARPETA + "empleados.txt";
    private static final String CLIENTES = CARPETA + "clientes.txt";
    private static final String ALOJAMIENTOS = CARPETA + "alojamientos.txt";
    private static final String MASCOTAS = CARPETA + "mascotas.txt";
    private static final String VACUNAS = CARPETA + "vacunas.txt";
    private static final String RESERVAS = CARPETA + "reservas.txt";
    private static final String PAGOS = CARPETA + "pagos.txt";
    private static final String REGISTROS = CARPETA + "registros.txt";

    // ===================== Cargar =====================

    // Llena las listas que recibe con lo que hay en los archivos.
    // El orden importa: por ejemplo, para armar una mascota ya tiene que existir su cliente.
    public void cargar(List<Sucursal> sucursales, List<Empleado> empleados, List<Cliente> clientes,
            List<Mascota> mascotas, List<Reserva> reservas) {

        // Mapas para encontrar rápido cada objeto por su id o legajo
        Map<Integer, Sucursal> sucursalesPorId = new HashMap<Integer, Sucursal>();
        Map<Integer, String> legajoEncargadoPorSucursal = new HashMap<Integer, String>();
        Map<String, Empleado> empleadosPorLegajo = new HashMap<String, Empleado>();
        Map<Integer, Cliente> clientesPorId = new HashMap<Integer, Cliente>();
        Map<Integer, Alojamiento> alojamientosPorId = new HashMap<Integer, Alojamiento>();
        Map<Integer, Mascota> mascotasPorId = new HashMap<Integer, Mascota>();
        Map<Integer, Reserva> reservasPorId = new HashMap<Integer, Reserva>();

        // 1. Sucursales: id;nombre;direccion;telefono;legajoEncargado
        for (String linea : leerLineas(SUCURSALES)) {
            String[] c = linea.split(";", -1);
            Integer id = Integer.parseInt(c[0]);
            Sucursal s = new Sucursal(id, c[1], c[2], c[3]);
            sucursales.add(s);
            sucursalesPorId.put(id, s);
            // El encargado se asigna en el paso 3, cuando ya están cargados los empleados
            legajoEncargadoPorSucursal.put(id, c[4]);
        }

        // 2. Empleados: id;nombre;apellido;dni;telefono;email;legajo;fechaIngreso;idSucursal
        for (String linea : leerLineas(EMPLEADOS)) {
            String[] c = linea.split(";", -1);
            Empleado e = new Empleado(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], c[5], c[6],
                    leerFecha(c[7]));
            Sucursal s = sucursalesPorId.get(Integer.parseInt(c[8]));
            s.agregarEmpleado(e);
            empleados.add(e);
            empleadosPorLegajo.put(e.getLegajo(), e);
        }

        // 3. El encargado de cada sucursal (se buscó por el legajo guardado en sucursales.txt)
        for (Sucursal s : sucursales) {
            String legajo = legajoEncargadoPorSucursal.get(s.getId());
            if (!legajo.isEmpty()) {
                s.setEncargado(empleadosPorLegajo.get(legajo));
            }
        }

        // 4. Clientes: id;nombre;apellido;dni;telefono;email;direccion
        for (String linea : leerLineas(CLIENTES)) {
            String[] c = linea.split(";", -1);
            Cliente cliente = new Cliente(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], c[5], c[6]);
            clientes.add(cliente);
            clientesPorId.put(cliente.getId(), cliente);
        }

        // 5. Alojamientos: id;codigo;especie;categoria;precioBaseNoche;activo;idSucursal
        for (String linea : leerLineas(ALOJAMIENTOS)) {
            String[] c = linea.split(";", -1);
            Alojamiento a = new Alojamiento(Integer.parseInt(c[0]), c[1], Especie.valueOf(c[2]),
                    Categoria.valueOf(c[3]), Double.parseDouble(c[4]), Boolean.parseBoolean(c[5]));
            Sucursal s = sucursalesPorId.get(Integer.parseInt(c[6]));
            s.agregarAlojamiento(a);
            alojamientosPorId.put(a.getId(), a);
        }

        // 6. Mascotas: especie;id;nombre;fechaNacimiento;pesoKg;observaciones;fechaUltimoControl;idCliente;dato1;dato2
        for (String linea : leerLineas(MASCOTAS)) {
            String[] c = linea.split(";", -1);
            Especie especie = Especie.valueOf(c[0]);
            Integer id = Integer.parseInt(c[1]);
            String nombre = c[2];
            LocalDate fechaNacimiento = leerFecha(c[3]);
            double peso = Double.parseDouble(c[4]);
            String observaciones = c[5];
            LocalDate ultimoControl = leerFecha(c[6]);
            String dato1 = c[8];
            String dato2 = c[9];

            // Según la especie se crea un objeto de una subclase distinta
            Mascota m = null;
            switch (especie) {
                case PERRO:
                    m = new Perro(id, nombre, fechaNacimiento, peso, observaciones, ultimoControl, dato1,
                            Boolean.parseBoolean(dato2));
                    break;
                case GATO:
                    m = new Gato(id, nombre, fechaNacimiento, peso, observaciones, ultimoControl, dato1,
                            Boolean.parseBoolean(dato2));
                    break;
                case CONEJO:
                    m = new Conejo(id, nombre, fechaNacimiento, peso, observaciones, ultimoControl, dato1);
                    break;
                case AVE:
                    m = new Ave(id, nombre, fechaNacimiento, peso, observaciones, ultimoControl, dato1);
                    break;
            }

            Cliente cliente = clientesPorId.get(Integer.parseInt(c[7]));
            cliente.agregarMascota(m);
            mascotas.add(m);
            mascotasPorId.put(id, m);
        }

        // 7. Vacunas: idMascota;nombre;fechaAplicacion;fechaVencimiento
        for (String linea : leerLineas(VACUNAS)) {
            String[] c = linea.split(";", -1);
            Mascota m = mascotasPorId.get(Integer.parseInt(c[0]));
            // Mascota.Vacuna es una clase anidada estática: se crea sin necesitar una Mascota
            Mascota.Vacuna v = new Mascota.Vacuna(c[1], leerFecha(c[2]), leerFecha(c[3]));
            m.agregarVacuna(v);
        }

        // 8. Reservas: id;idMascota;idAlojamiento;fechaDesde;fechaHasta;estado
        for (String linea : leerLineas(RESERVAS)) {
            String[] c = linea.split(";", -1);
            Mascota m = mascotasPorId.get(Integer.parseInt(c[1]));
            Alojamiento a = alojamientosPorId.get(Integer.parseInt(c[2]));
            // Se usa el constructor que recibe el estado, para respetar el que estaba guardado
            Reserva r = new Reserva(Integer.parseInt(c[0]), m, a, leerFecha(c[3]), leerFecha(c[4]),
                    EstadoReserva.valueOf(c[5]));
            reservas.add(r);
            reservasPorId.put(r.getId(), r);
        }

        // 9. Pagos: idReserva;fecha;monto;medio
        for (String linea : leerLineas(PAGOS)) {
            String[] c = linea.split(";", -1);
            Reserva r = reservasPorId.get(Integer.parseInt(c[0]));
            // Reserva.Pago es una clase anidada estática
            Reserva.Pago pago = new Reserva.Pago(leerFecha(c[1]), Double.parseDouble(c[2]), c[3]);
            r.getPagos().add(pago);
        }

        // 10. Registros: idReserva;fechaHora;tipo;legajoEmpleado;descripcion
        for (String linea : leerLineas(REGISTROS)) {
            String[] c = linea.split(";", -1);
            Reserva r = reservasPorId.get(Integer.parseInt(c[0]));
            LocalDateTime fechaHora = LocalDateTime.parse(c[1]);
            TipoRegistro tipo = TipoRegistro.valueOf(c[2]);
            Empleado e = empleadosPorLegajo.get(c[3]);
            // Clase interna: un RegistroDiario siempre pertenece a una Reserva, por eso se crea
            // con la sintaxis "reserva.new RegistroDiario(...)".
            Reserva.RegistroDiario registro = r.new RegistroDiario(fechaHora, tipo, c[4], e);
            // Se agrega directo a la lista (y no con agregarRegistro) para no volver a mandar los avisos
            r.getRegistros().add(registro);
        }
    }

    // ===================== Guardar =====================

    // Escribe todas las listas en sus archivos, pisando lo que había
    public void guardar(List<Sucursal> sucursales, List<Empleado> empleados, List<Cliente> clientes,
            List<Mascota> mascotas, List<Reserva> reservas) {

        // Sucursales y sus alojamientos
        List<String> lineasSucursales = new ArrayList<String>();
        List<String> lineasAlojamientos = new ArrayList<String>();
        for (Sucursal s : sucursales) {
            String legajoEncargado = "";
            if (s.getEncargado() != null) {
                legajoEncargado = s.getEncargado().getLegajo();
            }
            lineasSucursales.add(s.getId() + ";" + limpiar(s.getNombre()) + ";" + limpiar(s.getDireccion()) + ";"
                    + limpiar(s.getTelefono()) + ";" + legajoEncargado);

            for (Alojamiento a : s.getAlojamientos()) {
                lineasAlojamientos.add(a.getId() + ";" + limpiar(a.getCodigo()) + ";" + a.getEspecie() + ";"
                        + a.getCategoria() + ";" + a.getPrecioBaseNoche() + ";" + a.isActivo() + ";" + s.getId());
            }
        }

        // Empleados
        List<String> lineasEmpleados = new ArrayList<String>();
        for (Empleado e : empleados) {
            lineasEmpleados.add(e.getId() + ";" + limpiar(e.getNombre()) + ";" + limpiar(e.getApellido()) + ";"
                    + limpiar(e.getDni()) + ";" + limpiar(e.getTelefono()) + ";" + limpiar(e.getEmail()) + ";"
                    + limpiar(e.getLegajo()) + ";" + escribirFecha(e.getFechaIngreso()) + ";"
                    + e.getSucursal().getId());
        }

        // Clientes
        List<String> lineasClientes = new ArrayList<String>();
        for (Cliente c : clientes) {
            lineasClientes.add(c.getId() + ";" + limpiar(c.getNombre()) + ";" + limpiar(c.getApellido()) + ";"
                    + limpiar(c.getDni()) + ";" + limpiar(c.getTelefono()) + ";" + limpiar(c.getEmail()) + ";"
                    + limpiar(c.getDireccion()));
        }

        // Mascotas y sus vacunas
        List<String> lineasMascotas = new ArrayList<String>();
        List<String> lineasVacunas = new ArrayList<String>();
        for (Mascota m : mascotas) {
            // dato1 y dato2 dependen de la especie. Se usa instanceof + cast para leer
            // los atributos que solo tiene cada subclase.
            String dato1 = "";
            String dato2 = "";
            if (m instanceof Perro) {
                Perro p = (Perro) m;
                dato1 = p.getRaza();
                dato2 = "" + p.isCastrado();
            } else if (m instanceof Gato) {
                Gato g = (Gato) m;
                dato1 = g.getRaza();
                dato2 = "" + g.isCastrado();
            } else if (m instanceof Conejo) {
                Conejo co = (Conejo) m;
                dato1 = co.getRaza();
            } else if (m instanceof Ave) {
                Ave a = (Ave) m;
                dato1 = a.getTipoAve();
            }

            lineasMascotas.add(m.getEspecie() + ";" + m.getId() + ";" + limpiar(m.getNombre()) + ";"
                    + escribirFecha(m.getFechaNacimiento()) + ";" + m.getPesoKg() + ";"
                    + limpiar(m.getObservaciones()) + ";" + escribirFecha(m.getFechaUltimoControl()) + ";"
                    + m.getCliente().getId() + ";" + limpiar(dato1) + ";" + dato2);

            for (Mascota.Vacuna v : m.getVacunas()) {
                lineasVacunas.add(m.getId() + ";" + limpiar(v.getNombre()) + ";"
                        + escribirFecha(v.getFechaAplicacion()) + ";" + escribirFecha(v.getFechaVencimiento()));
            }
        }

        // Reservas, con sus pagos y registros
        List<String> lineasReservas = new ArrayList<String>();
        List<String> lineasPagos = new ArrayList<String>();
        List<String> lineasRegistros = new ArrayList<String>();
        for (Reserva r : reservas) {
            lineasReservas.add(r.getId() + ";" + r.getMascota().getId() + ";" + r.getAlojamiento().getId() + ";"
                    + escribirFecha(r.getFechaDesde()) + ";" + escribirFecha(r.getFechaHasta()) + ";"
                    + r.getEstado());

            for (Reserva.Pago p : r.getPagos()) {
                lineasPagos.add(r.getId() + ";" + escribirFecha(p.getFecha()) + ";" + p.getMonto() + ";"
                        + limpiar(p.getMedio()));
            }

            for (Reserva.RegistroDiario reg : r.getRegistros()) {
                // withNano(0) saca las fracciones de segundo para que el archivo quede más legible
                lineasRegistros.add(r.getId() + ";" + reg.getFechaHora().withNano(0) + ";" + reg.getTipo() + ";"
                        + reg.getEmpleado().getLegajo() + ";" + limpiar(reg.getDescripcion()));
            }
        }

        escribirLineas(SUCURSALES, lineasSucursales);
        escribirLineas(EMPLEADOS, lineasEmpleados);
        escribirLineas(CLIENTES, lineasClientes);
        escribirLineas(ALOJAMIENTOS, lineasAlojamientos);
        escribirLineas(MASCOTAS, lineasMascotas);
        escribirLineas(VACUNAS, lineasVacunas);
        escribirLineas(RESERVAS, lineasReservas);
        escribirLineas(PAGOS, lineasPagos);
        escribirLineas(REGISTROS, lineasRegistros);
    }

    // ===================== Métodos de ayuda =====================

    // Devuelve todas las líneas no vacías del archivo. Si el archivo no existe, devuelve una lista vacía.
    private List<String> leerLineas(String ruta) {
        crearCarpetaSiNoExiste();
        List<String> lineas = new ArrayList<String>();
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return lineas;
        }
        // try-with-resources: el archivo se cierra solo al terminar, aunque haya un error
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea = lector.readLine();
            while (linea != null) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
                linea = lector.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error al leer " + ruta + ": " + e.getMessage());
        }
        return lineas;
    }

    // Escribe las líneas en el archivo, reemplazando su contenido anterior
    private void escribirLineas(String ruta, List<String> lineas) {
        crearCarpetaSiNoExiste();
        // try-with-resources: el archivo se cierra solo al terminar, aunque haya un error
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta, StandardCharsets.UTF_8))) {
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar " + ruta + ": " + e.getMessage());
        }
    }

    private void crearCarpetaSiNoExiste() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }

    // Saca del texto lo que rompería el formato del archivo:
    // el ";" (separa campos) pasa a "," y los saltos de línea (separan objetos) pasan a espacio
    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.replace(";", ",");
        limpio = limpio.replace("\r", " ");
        limpio = limpio.replace("\n", " ");
        return limpio;
    }

    // Un campo vacío en el archivo significa "sin fecha" (null)
    private LocalDate leerFecha(String texto) {
        if (texto.isEmpty()) {
            return null;
        }
        return LocalDate.parse(texto);
    }

    private String escribirFecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.toString();
    }
}
