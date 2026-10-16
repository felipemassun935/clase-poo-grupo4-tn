package persistencia;

import java.time.LocalDate;
import java.util.ArrayList;

import modelo.Empleado;
import modelo.EntradaBitacora;
import modelo.Estadia;
import modelo.Gravedad;
import modelo.MedioPago;
import modelo.Pago;
import modelo.RegistroComida;
import modelo.RegistroIncidente;
import modelo.RegistroMedicacion;
import modelo.Reserva;

// Formato: id;idReserva;legajoQueRecibio;ingreso;egreso;pago;bitacora
//   egreso vacío = la mascota sigue en la guardería
//   pago    = idPago#fecha#monto#medio (vacío si todavía no pagó)
//   bitacora = entradas separadas por |, cada una empieza con su tipo:
//     C#fecha#legajo#alimento#gramos#comioTodo
//     M#fecha#legajo#medicamento#dosis
//     I#fecha#legajo#descripcion#gravedad
public class RepositorioEstadias extends RepositorioArchivo<Estadia> {
    private ArrayList<Reserva> reservas;
    private ArrayList<Empleado> empleados;

    public RepositorioEstadias(String rutaArchivo, ArrayList<Reserva> reservas, ArrayList<Empleado> empleados) {
        super(rutaArchivo);
        this.reservas = reservas;
        this.empleados = empleados;
    }

    @Override
    protected String aLinea(Estadia e) {
        String egreso = "";
        if (e.getEgreso() != null) {
            egreso = e.getEgreso().toString();
        }

        String pago = "";
        Pago p = e.getPago();
        if (p != null) {
            pago = p.getIdPago() + "#" + p.getFecha() + "#" + p.getMonto() + "#" + p.getMedio();
        }

        String bitacora = "";
        for (EntradaBitacora entrada : e.getBitacora()) {
            if (!bitacora.isEmpty()) {
                bitacora = bitacora + "|";
            }
            bitacora = bitacora + entradaATexto(entrada);
        }

        return e.getIdEstadia() + SEPARADOR + e.getReserva().getIdReserva() + SEPARADOR
                + e.getRecibidaPor().getLegajo() + SEPARADOR + e.getIngreso() + SEPARADOR + egreso
                + SEPARADOR + pago + SEPARADOR + bitacora;
    }

    private String entradaATexto(EntradaBitacora entrada) {
        String inicio = entrada.getFecha() + "#" + entrada.getRegistradaPor().getLegajo() + "#";
        if (entrada instanceof RegistroComida) {
            RegistroComida c = (RegistroComida) entrada;
            return "C#" + inicio + limpiar(c.getAlimento()) + "#" + c.getCantidadGramos() + "#" + c.isComioTodo();
        } else if (entrada instanceof RegistroMedicacion) {
            RegistroMedicacion m = (RegistroMedicacion) entrada;
            return "M#" + inicio + limpiar(m.getMedicamento()) + "#" + limpiar(m.getDosis());
        } else {
            RegistroIncidente i = (RegistroIncidente) entrada;
            return "I#" + inicio + limpiar(i.getDescripcion()) + "#" + i.getGravedad();
        }
    }

    @Override
    protected Estadia desdeLinea(String[] c) {
        Reserva reserva = buscarReserva(Integer.parseInt(c[1]));
        Empleado recibio = buscarEmpleado(Integer.parseInt(c[2]));
        if (reserva == null || recibio == null) {
            return null;
        }
        Estadia e = new Estadia(Integer.parseInt(c[0]), reserva, recibio, LocalDate.parse(c[3]));

        if (!c[6].isEmpty()) {
            String[] entradas = c[6].split("\\|");
            for (String texto : entradas) {
                EntradaBitacora entrada = textoAEntrada(texto.split("#", -1));
                if (entrada != null) {
                    e.registrar(entrada);
                }
            }
        }

        if (!c[4].isEmpty()) {
            Pago pago = null;
            if (!c[5].isEmpty()) {
                String[] p = c[5].split("#");
                pago = new Pago(Integer.parseInt(p[0]), LocalDate.parse(p[1]), Double.parseDouble(p[2]),
                        MedioPago.valueOf(p[3]));
            }
            e.cerrar(LocalDate.parse(c[4]), pago);
        }
        return e;
    }

    private EntradaBitacora textoAEntrada(String[] t) {
        LocalDate fecha = LocalDate.parse(t[1]);
        Empleado empleado = buscarEmpleado(Integer.parseInt(t[2]));
        if (empleado == null) {
            return null;
        }
        if (t[0].equals("C")) {
            return new RegistroComida(fecha, empleado, t[3], Double.parseDouble(t[4]), Boolean.parseBoolean(t[5]));
        } else if (t[0].equals("M")) {
            return new RegistroMedicacion(fecha, empleado, t[3], t[4]);
        } else {
            return new RegistroIncidente(fecha, empleado, t[3], Gravedad.valueOf(t[4]));
        }
    }

    private Reserva buscarReserva(int id) {
        for (Reserva r : reservas) {
            if (r.getIdReserva() == id) {
                return r;
            }
        }
        return null;
    }

    private Empleado buscarEmpleado(int legajo) {
        for (Empleado e : empleados) {
            if (e.getLegajo() == legajo) {
                return e;
            }
        }
        return null;
    }
}
