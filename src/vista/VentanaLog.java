package vista;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import modelo.enums.AccionLog;
import modelo.log.EntradaLog;

// Consulta del log del sistema: quién hizo qué y cuándo. Se puede filtrar por acción y por texto, y ordenar.
// El log se lee de la base con un SwingWorker, para no congelar la ventana mientras llega la respuesta.
public class VentanaLog extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String TODAS = "Todas las acciones";
    private static final String[] ORDENES = { "Más nuevos primero", "Más viejos primero", "Por usuario" };

    private SistemaGuarderia sistema;
    private List<EntradaLog> logs; // todo lo que vino de la base

    private JComboBox<Object> cmbAccion;
    private JComboBox<String> cmbOrden;
    private JTextField txtBuscar;
    private JLabel lblEstado;
    private JButton btnActualizar;
    private DefaultTableModel modeloTabla;

    public VentanaLog() {
        sistema = SistemaGuarderia.getInstancia();
        logs = new ArrayList<EntradaLog>();

        setTitle("Log del sistema");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel lblAccion = new JLabel("Acción:");
        lblAccion.setBounds(20, 15, 60, 25);
        getContentPane().add(lblAccion);
        cmbAccion = new JComboBox<Object>();
        cmbAccion.addItem(TODAS);
        for (AccionLog a : AccionLog.values()) {
            cmbAccion.addItem(a);
        }
        cmbAccion.setBounds(80, 15, 200, 25);
        getContentPane().add(cmbAccion);

        JLabel lblBuscar = new JLabel("Contiene:");
        lblBuscar.setBounds(300, 15, 70, 25);
        getContentPane().add(lblBuscar);
        txtBuscar = new JTextField();
        txtBuscar.setBounds(370, 15, 220, 25);
        getContentPane().add(txtBuscar);

        JLabel lblOrden = new JLabel("Orden:");
        lblOrden.setBounds(610, 15, 50, 25);
        getContentPane().add(lblOrden);
        cmbOrden = new JComboBox<String>(ORDENES);
        cmbOrden.setBounds(660, 15, 180, 25);
        getContentPane().add(cmbOrden);

        btnActualizar = new JButton("Actualizar");
        btnActualizar.setBounds(940, 15, 130, 25);
        getContentPane().add(btnActualizar);

        modeloTabla = new DefaultTableModel(new String[] { "Fecha y hora", "Usuario", "Acción", "Detalle" }, 0);
        JTable tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(600);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 55, 1050, 470);
        getContentPane().add(scroll);

        lblEstado = new JLabel();
        lblEstado.setBounds(20, 530, 1050, 20);
        getContentPane().add(lblEstado);

        // Eventos con lambdas: filtrar no vuelve a ir a la base, solo cambia lo que se muestra
        cmbAccion.addActionListener(e -> mostrar());
        cmbOrden.addActionListener(e -> mostrar());
        txtBuscar.addActionListener(e -> mostrar());
        btnActualizar.addActionListener(e -> cargarDesdeLaBase());

        cargarDesdeLaBase();
    }

    // SwingWorker: la consulta a la base corre en otro hilo (doInBackground) y la tabla se llena
    // en el EDT (done), así la ventana no se congela mientras espera.
    private void cargarDesdeLaBase() {
        btnActualizar.setEnabled(false);
        lblEstado.setText("Cargando el log desde la base de datos...");

        SwingWorker<List<EntradaLog>, Void> worker = new SwingWorker<List<EntradaLog>, Void>() {
            @Override
            protected List<EntradaLog> doInBackground() {
                // Corre FUERA del EDT: acá no se tocan componentes
                return sistema.getLogs();
            }

            @Override
            protected void done() {
                // Corre en el EDT: acá sí se actualiza la ventana
                try {
                    logs = get();
                    mostrar();
                } catch (Exception ex) { // get() puede tirar InterruptedException o ExecutionException
                    lblEstado.setText("No se pudo cargar el log.");
                    JOptionPane.showMessageDialog(VentanaLog.this, ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                btnActualizar.setEnabled(true);
            }
        };
        worker.execute();
    }

    // Aplica los filtros y el orden elegidos y llena la tabla
    private void mostrar() {
        Object accionElegida = cmbAccion.getSelectedItem();
        String buscado = txtBuscar.getText().trim().toLowerCase();

        List<EntradaLog> filtrados = new ArrayList<EntradaLog>();
        for (EntradaLog log : logs) {
            boolean coincideAccion = accionElegida == TODAS || log.getAccion() == accionElegida;
            boolean coincideTexto = buscado.isEmpty() || log.getDetalle().toLowerCase().contains(buscado)
                    || log.getUsuario().toLowerCase().contains(buscado);
            if (coincideAccion && coincideTexto) {
                filtrados.add(log);
            }
        }

        // Comparator escrito con lambdas: cada criterio de orden es un Comparator distinto
        int orden = cmbOrden.getSelectedIndex();
        if (orden == 0) {
            filtrados.sort((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()));
        } else if (orden == 1) {
            filtrados.sort((a, b) -> a.getFechaHora().compareTo(b.getFechaHora()));
        } else {
            filtrados.sort((a, b) -> a.getUsuario().compareTo(b.getUsuario()));
        }

        modeloTabla.setRowCount(0);
        for (EntradaLog log : filtrados) {
            modeloTabla.addRow(new Object[] { log.getFechaHoraTexto(), log.getUsuario(), log.getAccion(),
                    log.getDetalle() });
        }
        lblEstado.setText("Mostrando " + filtrados.size() + " de " + logs.size() + " líneas del log.");
    }
}
