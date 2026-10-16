package vista;

import java.time.LocalDate;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import excepciones.VacunacionVencidaException;
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
import util.Fechas;

// Pantalla de negocio 2: check-in, bitácora diaria y check-out con cobro
public class VentanaEstadias extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private ArrayList<Estadia> activas = new ArrayList<>(); // lo que se ve en la tabla

    private JComboBox<Empleado> cmbEmpleado;
    private JComboBox<Reserva> cmbReserva;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextArea txtBitacora;

    public VentanaEstadias() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("Check-in / Estadías");
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel lblEmpleado = new JLabel("Empleado que opera:");
        lblEmpleado.setBounds(15, 10, 150, 25);
        getContentPane().add(lblEmpleado);
        cmbEmpleado = new JComboBox<>();
        for (Empleado e : sistema.getEmpleados()) {
            cmbEmpleado.addItem(e);
        }
        cmbEmpleado.setBounds(165, 10, 300, 25);
        getContentPane().add(cmbEmpleado);

        // ----- Check-in -----
        JPanel panelCheckIn = new JPanel();
        panelCheckIn.setLayout(null);
        panelCheckIn.setBorder(BorderFactory.createTitledBorder("Check-in (se controlan las vacunas)"));
        panelCheckIn.setBounds(10, 45, 965, 70);
        getContentPane().add(panelCheckIn);

        cmbReserva = new JComboBox<>();
        cmbReserva.setBounds(15, 27, 600, 28);
        panelCheckIn.add(cmbReserva);

        JButton btnCheckIn = new JButton("Hacer check-in");
        btnCheckIn.setBounds(630, 27, 180, 28);
        panelCheckIn.add(btnCheckIn);

        // ----- Estadías activas -----
        JLabel lblActivas = new JLabel("Mascotas en la guardería ahora (elegí una para ver su bitácora)");
        lblActivas.setBounds(15, 125, 600, 25);
        getContentPane().add(lblActivas);

        modeloTabla = new DefaultTableModel(new String[] { "N°", "Mascota", "Especie", "Lugar", "Ingresó",
                "Se va", "A cobrar" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(10, 150, 600, 380);
        getContentPane().add(scroll);

        JLabel lblBitacora = new JLabel("Bitácora diaria");
        lblBitacora.setBounds(625, 125, 300, 25);
        getContentPane().add(lblBitacora);

        txtBitacora = new JTextArea();
        txtBitacora.setEditable(false);
        txtBitacora.setLineWrap(true);
        txtBitacora.setWrapStyleWord(true); // corta por palabra, no por letra
        JScrollPane scrollBitacora = new JScrollPane(txtBitacora);
        scrollBitacora.setBounds(625, 150, 350, 230);
        getContentPane().add(scrollBitacora);

        JButton btnComida = new JButton("Registrar comida");
        btnComida.setBounds(625, 390, 170, 30);
        getContentPane().add(btnComida);

        JButton btnMedicacion = new JButton("Registrar medicación");
        btnMedicacion.setBounds(805, 390, 170, 30);
        getContentPane().add(btnMedicacion);

        JButton btnIncidente = new JButton("Registrar incidente");
        btnIncidente.setBounds(625, 430, 170, 30);
        getContentPane().add(btnIncidente);

        JButton btnCheckOut = new JButton("Check-out y cobro");
        btnCheckOut.setBounds(625, 490, 350, 40);
        getContentPane().add(btnCheckOut);

        btnCheckIn.addActionListener(e -> hacerCheckIn());
        btnComida.addActionListener(e -> registrarComida());
        btnMedicacion.addActionListener(e -> registrarMedicacion());
        btnIncidente.addActionListener(e -> registrarIncidente());
        btnCheckOut.addActionListener(e -> hacerCheckOut());
        tabla.getSelectionModel().addListSelectionListener(e -> mostrarBitacora());

        cargarDatos();
    }

    private void cargarDatos() {
        cmbReserva.removeAllItems();
        for (Reserva r : sistema.getReservasConfirmadas()) {
            cmbReserva.addItem(r);
        }

        activas = sistema.getEstadiasActivas();
        modeloTabla.setRowCount(0);
        for (Estadia e : activas) {
            Reserva r = e.getReserva();
            modeloTabla.addRow(new Object[] { e.getIdEstadia(), r.getMascota().getNombre(),
                    r.getMascota().getEspecie(), r.getAlojamiento().getCodigo(), Fechas.formatear(e.getIngreso()),
                    Fechas.formatear(r.getFechaHasta()), "$" + String.format("%.2f", e.calcularTotal()) });
        }
        txtBitacora.setText("");
    }

    // Devuelve la estadía elegida en la tabla, o null (y avisa) si no hay ninguna
    private Estadia getEstadiaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero elegí una mascota de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return activas.get(fila);
    }

    private Empleado getEmpleado() {
        Empleado e = (Empleado) cmbEmpleado.getSelectedItem();
        if (e == null) {
            JOptionPane.showMessageDialog(this, "No hay empleados cargados.", "Atención", JOptionPane.WARNING_MESSAGE);
        }
        return e;
    }

    private void hacerCheckIn() {
        Reserva r = (Reserva) cmbReserva.getSelectedItem();
        Empleado empleado = getEmpleado();
        if (r == null || empleado == null) {
            return;
        }
        try {
            Estadia est = sistema.realizarCheckIn(r, empleado);
            JOptionPane.showMessageDialog(this, r.getMascota().getNombre() + " ingresó a "
                    + r.getAlojamiento().getCodigo() + ". Estadía N° " + est.getIdEstadia(),
                    "Check-in realizado", JOptionPane.INFORMATION_MESSAGE);
            cargarDatos();
        } catch (VacunacionVencidaException ex) {
            // Nuestra excepción personalizada: la mascota no puede entrar
            JOptionPane.showMessageDialog(this, ex.getMessage()
                    + "\n\nCargá las vacunas en el ABM de Mascotas antes de recibirla.",
                    "Vacunas vencidas", JOptionPane.ERROR_MESSAGE);
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarBitacora() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        Estadia est = activas.get(fila);
        String texto = "";
        for (EntradaBitacora entrada : est.getBitacora()) {
            texto = texto + entrada.toString() + "\n\n"; // cada tipo de entrada arma su propio resumen
        }
        if (texto.isEmpty()) {
            texto = "Todavía no hay registros.";
        }
        txtBitacora.setText(texto);
    }

    private void registrarComida() {
        Estadia est = getEstadiaSeleccionada();
        Empleado empleado = getEmpleado();
        if (est == null || empleado == null) {
            return;
        }
        String alimento = JOptionPane.showInputDialog(this, "¿Qué comió?", "Registrar comida",
                JOptionPane.QUESTION_MESSAGE);
        if (alimento == null || alimento.trim().isEmpty()) {
            return; // apretó Cancelar
        }
        String textoGramos = JOptionPane.showInputDialog(this, "¿Cuántos gramos?", "Registrar comida",
                JOptionPane.QUESTION_MESSAGE);
        if (textoGramos == null) {
            return;
        }
        try {
            double gramos = Double.parseDouble(textoGramos.replace(",", "."));
            int comioTodo = JOptionPane.showConfirmDialog(this, "¿Comió todo?", "Registrar comida",
                    JOptionPane.YES_NO_OPTION);
            RegistroComida registro = new RegistroComida(LocalDate.now(), empleado, alimento.trim(), gramos,
                    comioTodo == JOptionPane.YES_OPTION);
            sistema.registrarEnBitacora(est, registro);
            mostrarBitacora();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los gramos tienen que ser un número.", "Dato inválido",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarMedicacion() {
        Estadia est = getEstadiaSeleccionada();
        Empleado empleado = getEmpleado();
        if (est == null || empleado == null) {
            return;
        }
        String medicamento = JOptionPane.showInputDialog(this, "Medicamento:", "Registrar medicación",
                JOptionPane.QUESTION_MESSAGE);
        if (medicamento == null || medicamento.trim().isEmpty()) {
            return;
        }
        String dosis = JOptionPane.showInputDialog(this, "Dosis:", "Registrar medicación",
                JOptionPane.QUESTION_MESSAGE);
        if (dosis == null || dosis.trim().isEmpty()) {
            return;
        }
        try {
            sistema.registrarEnBitacora(est,
                    new RegistroMedicacion(LocalDate.now(), empleado, medicamento.trim(), dosis.trim()));
            mostrarBitacora();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No permitido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarIncidente() {
        Estadia est = getEstadiaSeleccionada();
        Empleado empleado = getEmpleado();
        if (est == null || empleado == null) {
            return;
        }
        String descripcion = JOptionPane.showInputDialog(this, "¿Qué pasó?", "Registrar incidente",
                JOptionPane.QUESTION_MESSAGE);
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return;
        }
        // Diálogo con una lista desplegable para elegir la gravedad
        Gravedad gravedad = (Gravedad) JOptionPane.showInputDialog(this, "Gravedad:", "Registrar incidente",
                JOptionPane.QUESTION_MESSAGE, null, Gravedad.values(), Gravedad.LEVE);
        if (gravedad == null) {
            return;
        }
        try {
            RegistroIncidente incidente = new RegistroIncidente(LocalDate.now(), empleado, descripcion.trim(), gravedad);
            sistema.registrarEnBitacora(est, incidente);
            mostrarBitacora();
            if (incidente.hayQueAvisarAlDueno()) {
                JOptionPane.showMessageDialog(this, "Incidente GRAVE: avisar al dueño.\n"
                        + est.getReserva().getMascota().getDueno().getNombreCompleto() + " - Tel: "
                        + est.getReserva().getMascota().getDueno().getTelefono(),
                        "Avisar al dueño", JOptionPane.WARNING_MESSAGE);
            }
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hacerCheckOut() {
        Estadia est = getEstadiaSeleccionada();
        if (est == null) {
            return;
        }
        MedioPago medio = (MedioPago) JOptionPane.showInputDialog(this,
                "Total a cobrar: $" + String.format("%.2f", est.calcularTotal()) + "\n¿Con qué paga?",
                "Check-out", JOptionPane.QUESTION_MESSAGE, null, MedioPago.values(), MedioPago.EFECTIVO);
        if (medio == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Confirmar el cobro y la salida de "
                + est.getReserva().getMascota().getNombre() + "?", "Check-out", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        Pago pago = sistema.realizarCheckOut(est, medio);
        JOptionPane.showMessageDialog(this, pago.generarComprobante(est), "Comprobante",
                JOptionPane.INFORMATION_MESSAGE);
        cargarDatos();
    }
}
