package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import excepciones.IngresoRechazadoException;
import modelo.enums.EstadoReserva;
import modelo.enums.TipoRegistro;
import modelo.persona.Empleado;
import modelo.reserva.Reserva;

// Pantalla de negocio 2: check-in, registros del día a día (bitácora) y check-out con cobro
public class VentanaEstadias extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    // Medios de pago que se ofrecen en la lista del showInputDialog
    private static final String[] MEDIOS_DE_PAGO = { "Efectivo", "Débito", "Crédito", "Transferencia" };

    private SistemaGuarderia sistema;
    private List<Reserva> confirmadas; // filas de la tabla de check-in, en el mismo orden
    private List<Reserva> enCurso;     // filas de la tabla de estadías, en el mismo orden

    private JComboBox<Empleado> cmbEmpleado;
    private JTable tablaConfirmadas;
    private DefaultTableModel modeloConfirmadas;
    private JTable tablaEnCurso;
    private DefaultTableModel modeloEnCurso;
    private JTextArea txtBitacora;

    public VentanaEstadias() {
        sistema = SistemaGuarderia.getInstancia();
        confirmadas = new ArrayList<Reserva>();
        enCurso = new ArrayList<Reserva>();

        setTitle("Check-in y estadías");
        setSize(1100, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel lblEmpleado = new JLabel("Empleado que opera:");
        lblEmpleado.setBounds(20, 10, 150, 25);
        getContentPane().add(lblEmpleado);
        cmbEmpleado = new JComboBox<Empleado>();
        for (Empleado e : sistema.getEmpleados()) {
            cmbEmpleado.addItem(e);
        }
        cmbEmpleado.setBounds(170, 10, 300, 25);
        getContentPane().add(cmbEmpleado);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setBounds(930, 10, 140, 25);
        getContentPane().add(btnActualizar);

        // ----- Check-in -----
        JLabel lblCheckIn = new JLabel("Reservas confirmadas (esperando check-in)");
        lblCheckIn.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCheckIn.setBounds(20, 45, 500, 25);
        getContentPane().add(lblCheckIn);

        modeloConfirmadas = new DefaultTableModel(new String[] { "Id", "Mascota", "Especie", "Cliente", "Sucursal",
                "Alojamiento", "Desde", "Hasta" }, 0);
        tablaConfirmadas = new JTable(modeloConfirmadas);
        tablaConfirmadas.setDefaultEditor(Object.class, null);
        tablaConfirmadas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollConfirmadas = new JScrollPane(tablaConfirmadas);
        scrollConfirmadas.setBounds(20, 75, 880, 150);
        getContentPane().add(scrollConfirmadas);

        JButton btnCheckIn = new JButton("Hacer check-in");
        btnCheckIn.setBounds(910, 75, 160, 30);
        getContentPane().add(btnCheckIn);

        // ----- Estadías en curso -----
        JLabel lblEnCurso = new JLabel("Estadías en curso");
        lblEnCurso.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblEnCurso.setBounds(20, 240, 500, 25);
        getContentPane().add(lblEnCurso);

        modeloEnCurso = new DefaultTableModel(new String[] { "Id", "Mascota", "Cliente", "Sucursal", "Alojamiento",
                "Desde", "Hasta", "Total", "Pagado", "Saldo" }, 0);
        tablaEnCurso = new JTable(modeloEnCurso);
        tablaEnCurso.setDefaultEditor(Object.class, null);
        tablaEnCurso.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollEnCurso = new JScrollPane(tablaEnCurso);
        scrollEnCurso.setBounds(20, 270, 880, 180);
        getContentPane().add(scrollEnCurso);

        JButton btnRegistro = new JButton("Agregar registro");
        btnRegistro.setBounds(910, 270, 160, 30);
        getContentPane().add(btnRegistro);

        JButton btnCheckOut = new JButton("Check-out y cobro");
        btnCheckOut.setBounds(910, 310, 160, 30);
        getContentPane().add(btnCheckOut);

        JLabel lblBitacora = new JLabel("Bitácora de la estadía elegida");
        lblBitacora.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblBitacora.setBounds(20, 465, 500, 25);
        getContentPane().add(lblBitacora);

        txtBitacora = new JTextArea();
        txtBitacora.setEditable(false);
        JScrollPane scrollBitacora = new JScrollPane(txtBitacora);
        scrollBitacora.setBounds(20, 495, 1050, 210);
        getContentPane().add(scrollBitacora);

        // Eventos (listeners con clases anónimas)
        btnActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cargarTablas();
            }
        });
        btnCheckIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hacerCheckIn();
            }
        });
        btnRegistro.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregarRegistro();
            }
        });
        btnCheckOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hacerCheckOut();
            }
        });
        tablaEnCurso.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                mostrarBitacora();
            }
        });

        cargarTablas();
    }

    // Arma las dos tablas: reservas CONFIRMADAS y EN_CURSO
    private void cargarTablas() {
        confirmadas = new ArrayList<Reserva>();
        enCurso = new ArrayList<Reserva>();
        for (Reserva r : sistema.getReservas()) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA) {
                confirmadas.add(r);
            } else if (r.getEstado() == EstadoReserva.EN_CURSO) {
                enCurso.add(r);
            }
        }

        modeloConfirmadas.setRowCount(0);
        for (Reserva r : confirmadas) {
            modeloConfirmadas.addRow(new Object[] { r.getId(), r.getMascota().getNombre(),
                    r.getMascota().getEspecie().getNombreVisible(), r.getMascota().getCliente().getNombreCompleto(),
                    r.getAlojamiento().getSucursal().getNombre(), r.getAlojamiento().getCodigo(),
                    Validaciones.formatearFecha(r.getFechaDesde()), Validaciones.formatearFecha(r.getFechaHasta()) });
        }

        modeloEnCurso.setRowCount(0);
        for (Reserva r : enCurso) {
            modeloEnCurso.addRow(new Object[] { r.getId(), r.getMascota().getNombre(),
                    r.getMascota().getCliente().getNombreCompleto(), r.getAlojamiento().getSucursal().getNombre(),
                    r.getAlojamiento().getCodigo(), Validaciones.formatearFecha(r.getFechaDesde()),
                    Validaciones.formatearFecha(r.getFechaHasta()), Validaciones.plata(r.calcularTotal()),
                    Validaciones.plata(r.getTotalPagado()), Validaciones.plata(r.getSaldoPendiente()) });
        }
        txtBitacora.setText("");
    }

    // Devuelve la estadía elegida en la tabla de abajo, o null si no hay ninguna (y avisa)
    private Reserva getEstadiaElegida() {
        int fila = tablaEnCurso.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero elegí una estadía en curso.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return enCurso.get(fila);
    }

    // Muestra todos los registros de la estadía elegida, uno por línea
    private void mostrarBitacora() {
        int fila = tablaEnCurso.getSelectedRow();
        if (fila == -1) {
            txtBitacora.setText("");
            return;
        }
        Reserva r = enCurso.get(fila);
        String texto = "";
        for (Reserva.RegistroDiario reg : r.getRegistros()) {
            texto = texto + reg.getResumen() + "\n";
        }
        if (texto.isEmpty()) {
            texto = "Todavía no hay registros para " + r.getMascota().getNombre() + ".";
        }
        txtBitacora.setText(texto);
    }

    private void hacerCheckIn() {
        int fila = tablaConfirmadas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero elegí una reserva confirmada.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Reserva r = confirmadas.get(fila);
        // try/catch: si la mascota no cumple los requisitos se atrapa la excepción propia
        // IngresoRechazadoException, que trae la lista de problemas en el mensaje
        try {
            sistema.checkIn(r);
            JOptionPane.showMessageDialog(this, "Check-in hecho: " + r.getMascota().getNombre() + " ya está en "
                    + r.getAlojamiento().getCodigo() + ".");
            cargarTablas();
        } catch (IngresoRechazadoException ex) {
            JOptionPane.showMessageDialog(this, r.getMascota().getNombre() + " no puede ingresar:\n\n"
                    + ex.getMessage(), "Ingreso rechazado", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede hacer el check-in",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarRegistro() {
        Reserva r = getEstadiaElegida();
        if (r == null) {
            return;
        }
        Empleado empleado = (Empleado) cmbEmpleado.getSelectedItem();

        // showInputDialog con una lista: muestra los valores del enum TipoRegistro
        TipoRegistro[] tipos = TipoRegistro.values();
        Object elegido = JOptionPane.showInputDialog(this, "Tipo de registro:", "Nuevo registro",
                JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
        if (elegido == null) {
            return; // el usuario apretó "Cancelar"
        }
        TipoRegistro tipo = (TipoRegistro) elegido;

        String descripcion = JOptionPane.showInputDialog(this, "Descripción:");
        if (descripcion == null) {
            return;
        }
        try {
            sistema.agregarRegistro(r, tipo, descripcion, empleado);
            if (tipo.requiereAviso()) {
                JOptionPane.showMessageDialog(this, "Se avisó al cliente y al encargado de la sucursal.", "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            mostrarBitacora();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Muestra total, pagado y saldo; si hay saldo lo cobra; después hace el check-out y muestra el comprobante
    private void hacerCheckOut() {
        Reserva r = getEstadiaElegida();
        if (r == null) {
            return;
        }
        String resumen = "Total: " + Validaciones.plata(r.calcularTotal()) + "\nPagado: "
                + Validaciones.plata(r.getTotalPagado()) + "\nSaldo: " + Validaciones.plata(r.getSaldoPendiente());

        try {
            if (r.getSaldoPendiente() > 0) {
                Object medio = JOptionPane.showInputDialog(this, resumen + "\n\nMedio de pago para cobrar el saldo:",
                        "Check-out y cobro", JOptionPane.QUESTION_MESSAGE, null, MEDIOS_DE_PAGO, MEDIOS_DE_PAGO[0]);
                if (medio == null) {
                    return; // el usuario apretó "Cancelar"
                }
                sistema.registrarPago(r, r.getSaldoPendiente(), medio.toString());
            } else {
                int respuesta = JOptionPane.showConfirmDialog(this, resumen + "\n\n¿Hacer el check-out?",
                        "Check-out", JOptionPane.YES_NO_OPTION);
                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // try/catch: si quedara saldo o el estado no lo permite, la Reserva tira IllegalStateException
            sistema.checkOut(r);
            JOptionPane.showMessageDialog(this, armarComprobante(r), "Comprobante", JOptionPane.INFORMATION_MESSAGE);
            cargarTablas();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede hacer el check-out",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Arma el texto del comprobante con todos los pagos de la reserva
    private String armarComprobante(Reserva r) {
        String texto = "COMPROBANTE - Reserva " + r.getId() + "\n\n";
        texto = texto + "Cliente: " + r.getMascota().getCliente().getNombreCompleto() + "\n";
        texto = texto + "Mascota: " + r.getMascota().getNombre() + "\n";
        texto = texto + "Sucursal: " + r.getAlojamiento().getSucursal().getNombre() + "\n";
        texto = texto + "Alojamiento: " + r.getAlojamiento().getCodigo() + " (" + r.getAlojamiento().getCategoria()
                + ")\n";
        texto = texto + "Estadía: " + Validaciones.formatearFecha(r.getFechaDesde()) + " al "
                + Validaciones.formatearFecha(r.getFechaHasta()) + "\n";
        texto = texto + r.getCantidadNoches() + " noches x " + Validaciones.plata(r.getAlojamiento().getPrecioNoche())
                + "\n\n";
        texto = texto + "Pagos:\n";
        for (Reserva.Pago p : r.getPagos()) {
            texto = texto + "   " + Validaciones.formatearFecha(p.getFecha()) + "  " + Validaciones.plata(p.getMonto())
                    + "  (" + p.getMedio() + ")\n";
        }
        texto = texto + "\nTotal: " + Validaciones.plata(r.calcularTotal());
        texto = texto + "\nPagado: " + Validaciones.plata(r.getTotalPagado());
        texto = texto + "\nEstado: " + r.getEstado();
        return texto;
    }
}
