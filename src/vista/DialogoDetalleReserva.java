package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.enums.TipoIndicacion;
import modelo.enums.TipoServicio;
import modelo.persona.Empleado;
import modelo.reserva.Indicacion;
import modelo.reserva.Reserva;
import modelo.reserva.ServicioContratado;

// Diálogo modal con el detalle de una reserva:
// - las indicaciones que dejó el dueño (comida, medicación, cuidados) y cuántas veces se cumplió cada una;
// - los servicios extra contratados (baño, corte de pelo...), quién los hizo y cuándo;
// - la cuenta: alojamiento + servicios, lo pagado y el saldo.
public class DialogoDetalleReserva extends JDialog {
    private static final long serialVersionUID = 1L;

    private SistemaGuarderia sistema;
    private Reserva reserva;

    private JLabel lblCuenta;
    private DefaultTableModel modeloIndicaciones;
    private JTable tablaIndicaciones;
    private JComboBox<TipoIndicacion> cmbTipoIndicacion;
    private JTextField txtDescripcion;
    private JTextField txtHorario;
    private DefaultTableModel modeloServicios;
    private JTable tablaServicios;
    private JComboBox<TipoServicio> cmbServicio;
    private JComboBox<Empleado> cmbEmpleado;

    public DialogoDetalleReserva(JFrame padre, Reserva reserva) {
        super(padre, "Reserva " + reserva.getId() + " - servicios e indicaciones", true); // true = modal
        this.sistema = SistemaGuarderia.getInstancia();
        this.reserva = reserva;

        setSize(940, 690);
        setLocationRelativeTo(padre);
        getContentPane().setLayout(null);

        JLabel lblTitulo = new JLabel(reserva.getMascota().getNombre() + " (" + reserva.getMascota().getCodigoCollar()
                + ") - " + reserva.getAlojamiento().getCodigo() + ", " + reserva.getAlojamiento().getSucursal()
                + " - del " + Validaciones.formatearFecha(reserva.getFechaDesde()) + " al "
                + Validaciones.formatearFecha(reserva.getFechaHasta()) + " - " + reserva.getEstado());
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setBounds(20, 10, 900, 25);
        getContentPane().add(lblTitulo);

        // ----- Indicaciones del dueño -----
        JLabel lblInd = new JLabel("Indicaciones del dueño");
        lblInd.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblInd.setBounds(20, 45, 400, 20);
        getContentPane().add(lblInd);

        modeloIndicaciones = new DefaultTableModel(
                new String[] { "Tipo", "Indicación", "Horario", "Veces cumplida", "Última vez" }, 0);
        tablaIndicaciones = new JTable(modeloIndicaciones);
        tablaIndicaciones.setDefaultEditor(Object.class, null);
        tablaIndicaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaIndicaciones.getColumnModel().getColumn(1).setPreferredWidth(300);
        tablaIndicaciones.getColumnModel().getColumn(4).setPreferredWidth(240);
        JScrollPane scrollInd = new JScrollPane(tablaIndicaciones);
        scrollInd.setBounds(20, 70, 890, 140);
        getContentPane().add(scrollInd);

        cmbTipoIndicacion = new JComboBox<TipoIndicacion>(TipoIndicacion.values());
        cmbTipoIndicacion.setBounds(20, 220, 160, 25);
        getContentPane().add(cmbTipoIndicacion);
        JLabel lblDesc = new JLabel("Qué:");
        lblDesc.setBounds(190, 220, 40, 25);
        getContentPane().add(lblDesc);
        txtDescripcion = new JTextField();
        txtDescripcion.setBounds(225, 220, 300, 25);
        getContentPane().add(txtDescripcion);
        JLabel lblHorario = new JLabel("Horario:");
        lblHorario.setBounds(535, 220, 60, 25);
        getContentPane().add(lblHorario);
        txtHorario = new JTextField();
        txtHorario.setBounds(595, 220, 140, 25);
        getContentPane().add(txtHorario);

        JButton btnAgregarInd = new JButton("Agregar");
        btnAgregarInd.setBounds(745, 220, 80, 25);
        getContentPane().add(btnAgregarInd);
        JButton btnQuitarInd = new JButton("Quitar");
        btnQuitarInd.setBounds(830, 220, 80, 25);
        getContentPane().add(btnQuitarInd);

        // ----- Servicios extra -----
        JLabel lblServ = new JLabel("Servicios contratados");
        lblServ.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblServ.setBounds(20, 265, 400, 20);
        getContentPane().add(lblServ);

        modeloServicios = new DefaultTableModel(new String[] { "Servicio", "Precio", "Contratado", "Estado" }, 0);
        tablaServicios = new JTable(modeloServicios);
        tablaServicios.setDefaultEditor(Object.class, null);
        tablaServicios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaServicios.getColumnModel().getColumn(3).setPreferredWidth(360);
        JScrollPane scrollServ = new JScrollPane(tablaServicios);
        scrollServ.setBounds(20, 290, 890, 140);
        getContentPane().add(scrollServ);

        // Solo se ofrecen los servicios que se le pueden hacer a esta especie
        cmbServicio = new JComboBox<TipoServicio>();
        for (TipoServicio t : TipoServicio.values()) {
            if (t.seAplicaA(reserva.getMascota().getEspecie())) {
                cmbServicio.addItem(t);
            }
        }
        cmbServicio.setBounds(20, 440, 200, 25);
        getContentPane().add(cmbServicio);
        JButton btnContratar = new JButton("Contratar");
        btnContratar.setBounds(230, 440, 110, 25);
        getContentPane().add(btnContratar);
        JButton btnQuitarServ = new JButton("Quitar");
        btnQuitarServ.setBounds(350, 440, 90, 25);
        getContentPane().add(btnQuitarServ);

        JLabel lblHizo = new JLabel("Lo hizo:");
        lblHizo.setBounds(500, 440, 60, 25);
        getContentPane().add(lblHizo);
        // Solo los empleados de la sucursal donde está la mascota
        cmbEmpleado = new JComboBox<Empleado>();
        for (Empleado e : reserva.getAlojamiento().getSucursal().getEmpleados()) {
            cmbEmpleado.addItem(e);
        }
        if (sistema.getOperador().getSucursal() == reserva.getAlojamiento().getSucursal()) {
            cmbEmpleado.setSelectedItem(sistema.getOperador());
        }
        cmbEmpleado.setBounds(560, 440, 200, 25);
        getContentPane().add(cmbEmpleado);
        JButton btnRealizado = new JButton("Marcar realizado");
        btnRealizado.setBounds(770, 440, 140, 25);
        getContentPane().add(btnRealizado);

        // ----- Cuenta -----
        JLabel lblCuentaTitulo = new JLabel("Cuenta");
        lblCuentaTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCuentaTitulo.setBounds(20, 485, 400, 20);
        getContentPane().add(lblCuentaTitulo);
        lblCuenta = new JLabel();
        lblCuenta.setVerticalAlignment(JLabel.TOP);
        lblCuenta.setBounds(20, 510, 890, 90);
        getContentPane().add(lblCuenta);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(810, 610, 100, 30);
        getContentPane().add(btnCerrar);

        // Eventos (listeners con clases anónimas)
        btnAgregarInd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregarIndicacion();
            }
        });
        btnQuitarInd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitarIndicacion();
            }
        });
        btnContratar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                contratar();
            }
        });
        btnQuitarServ.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitarServicio();
            }
        });
        btnRealizado.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                marcarRealizado();
            }
        });
        btnCerrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        cargar();
    }

    // Llena las dos tablas y la cuenta con los datos actuales de la reserva
    private void cargar() {
        modeloIndicaciones.setRowCount(0);
        for (Indicacion i : reserva.getIndicaciones()) {
            Reserva.RegistroDiario ultimo = reserva.getUltimoCumplimiento(i);
            String ultimaVez = "Todavía no";
            if (ultimo != null) {
                ultimaVez = ultimo.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                        + " por " + ultimo.getEmpleado().getNombreCompleto();
            }
            modeloIndicaciones.addRow(new Object[] { i.getTipo(), i.getDescripcion(), i.getHorario(),
                    reserva.getCantidadCumplimientos(i), ultimaVez });
        }

        modeloServicios.setRowCount(0);
        for (ServicioContratado s : reserva.getServicios()) {
            modeloServicios.addRow(new Object[] { s.getTipo(), Validaciones.plata(s.getPrecio()),
                    Validaciones.formatearFecha(s.getFechaSolicitud()), s.getEstadoTexto() });
        }

        // JLabel acepta HTML simple para mostrar varias líneas
        lblCuenta.setText("<html>Alojamiento: " + reserva.getCantidadNoches() + " noches x "
                + Validaciones.plata(reserva.getAlojamiento().getPrecioNoche()) + " = "
                + Validaciones.plata(reserva.getTotalAlojamiento()) + "<br>Servicios: "
                + reserva.getServicios().size() + " contratados (" + reserva.getCantidadServiciosRealizados()
                + " realizados) = " + Validaciones.plata(reserva.getTotalServicios()) + "<br><b>Total: "
                + Validaciones.plata(reserva.calcularTotal()) + "</b> &nbsp; Pagado: "
                + Validaciones.plata(reserva.getTotalPagado()) + " (" + reserva.getPagos().size()
                + " pagos) &nbsp; <b>Saldo: " + Validaciones.plata(reserva.getSaldoPendiente()) + "</b></html>");
    }

    private void agregarIndicacion() {
        try {
            sistema.agregarIndicacion(reserva, (TipoIndicacion) cmbTipoIndicacion.getSelectedItem(),
                    txtDescripcion.getText(), txtHorario.getText());
            txtDescripcion.setText("");
            txtHorario.setText("");
            cargar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarIndicacion() {
        int fila = tablaIndicaciones.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Elegí una indicación de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Indicacion i = reserva.getIndicaciones().get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Quitar la indicación \"" + i.getDescripcion() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.quitarIndicacion(reserva, i);
            cargar();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede quitar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void contratar() {
        TipoServicio tipo = (TipoServicio) cmbServicio.getSelectedItem();
        if (tipo == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Contratar " + tipo + " por "
                + Validaciones.plata(tipo.getPrecio()) + "? Se suma a la cuenta.", "Contratar servicio",
                JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.contratarServicio(reserva, tipo);
            cargar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede contratar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Devuelve el servicio elegido en la tabla, o null si no hay ninguno (y avisa)
    private ServicioContratado getServicioElegido() {
        int fila = tablaServicios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Elegí un servicio de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return reserva.getServicios().get(fila);
    }

    private void quitarServicio() {
        ServicioContratado s = getServicioElegido();
        if (s == null) {
            return;
        }
        try {
            sistema.quitarServicio(reserva, s);
            cargar();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede quitar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarRealizado() {
        ServicioContratado s = getServicioElegido();
        if (s == null) {
            return;
        }
        try {
            sistema.realizarServicio(reserva, s, (Empleado) cmbEmpleado.getSelectedItem());
            cargar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede", JOptionPane.ERROR_MESSAGE);
        }
    }
}
