package vista;

import java.time.LocalDate;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.AlojamientoNoDisponibleException;
import excepciones.DatoInvalidoException;
import modelo.Alojamiento;
import modelo.Mascota;
import modelo.Reserva;
import util.Fechas;

// Pantalla de negocio 1: búsqueda de alojamientos libres y reservas
public class VentanaReservas extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private ArrayList<Alojamiento> disponibles = new ArrayList<>(); // resultado de la última búsqueda

    private JComboBox<Mascota> cmbMascota;
    private JComboBox<String> cmbTipo;
    private JTextField txtDesde;
    private JTextField txtHasta;
    private JCheckBox chkExtra;
    private JTable tablaDisponibles;
    private DefaultTableModel modeloDisponibles;
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;

    public VentanaReservas() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("Reservas");
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        // ----- Panel de nueva reserva -----
        JPanel panelNueva = new JPanel();
        panelNueva.setLayout(null);
        panelNueva.setBorder(BorderFactory.createTitledBorder("Nueva reserva"));
        panelNueva.setBounds(10, 10, 965, 290);
        getContentPane().add(panelNueva);

        JLabel lblMascota = new JLabel("Mascota:");
        lblMascota.setBounds(15, 30, 80, 25);
        panelNueva.add(lblMascota);
        cmbMascota = new JComboBox<>();
        for (Mascota m : sistema.getMascotas()) {
            cmbMascota.addItem(m);
        }
        cmbMascota.setBounds(95, 30, 260, 25);
        panelNueva.add(cmbMascota);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setBounds(15, 65, 80, 25);
        panelNueva.add(lblTipo);
        cmbTipo = new JComboBox<>(new String[] { "Hospedaje", "Guardería diurna" });
        cmbTipo.setBounds(95, 65, 260, 25);
        panelNueva.add(cmbTipo);

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setBounds(15, 100, 80, 25);
        panelNueva.add(lblDesde);
        txtDesde = new JTextField(Fechas.formatear(LocalDate.now()));
        txtDesde.setBounds(95, 100, 260, 25);
        panelNueva.add(txtDesde);

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setBounds(15, 135, 80, 25);
        panelNueva.add(lblHasta);
        txtHasta = new JTextField(Fechas.formatear(LocalDate.now().plusDays(3)));
        txtHasta.setBounds(95, 135, 260, 25);
        panelNueva.add(txtHasta);

        chkExtra = new JCheckBox("Incluye baño (+$8000)");
        chkExtra.setBounds(95, 170, 260, 25);
        panelNueva.add(chkExtra);

        JButton btnBuscar = new JButton("Buscar alojamientos libres");
        btnBuscar.setBounds(15, 210, 340, 30);
        panelNueva.add(btnBuscar);

        JButton btnReservar = new JButton("Reservar el alojamiento elegido");
        btnReservar.setBounds(15, 245, 340, 30);
        panelNueva.add(btnReservar);

        modeloDisponibles = new DefaultTableModel(new String[] { "Código", "Tipo", "Categoría", "Precio/día" }, 0);
        tablaDisponibles = new JTable(modeloDisponibles);
        tablaDisponibles.setDefaultEditor(Object.class, null);
        JScrollPane scrollDisp = new JScrollPane(tablaDisponibles);
        scrollDisp.setBounds(375, 30, 575, 245);
        panelNueva.add(scrollDisp);

        // ----- Listado de reservas -----
        JLabel lblListado = new JLabel("Todas las reservas");
        lblListado.setBounds(15, 310, 300, 25);
        getContentPane().add(lblListado);

        modeloReservas = new DefaultTableModel(new String[] { "N°", "Mascota", "Tipo", "Alojamiento", "Desde",
                "Hasta", "Estado", "Costo" }, 0);
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setDefaultEditor(Object.class, null);
        JScrollPane scrollRes = new JScrollPane(tablaReservas);
        scrollRes.setBounds(10, 340, 965, 240);
        getContentPane().add(scrollRes);

        JButton btnCancelar = new JButton("Cancelar reserva seleccionada");
        btnCancelar.setBounds(10, 590, 260, 30);
        getContentPane().add(btnCancelar);

        cmbTipo.addActionListener(e -> actualizarExtra());
        btnBuscar.addActionListener(e -> buscar());
        btnReservar.addActionListener(e -> reservar());
        btnCancelar.addActionListener(e -> cancelar());

        cargarReservas();
    }

    private boolean esHospedaje() {
        return cmbTipo.getSelectedIndex() == 0;
    }

    private void actualizarExtra() {
        if (esHospedaje()) {
            chkExtra.setText("Incluye baño (+$8000)");
        } else {
            chkExtra.setText("Incluye paseo (+$4000 por día)");
        }
    }

    private void buscar() {
        try {
            Mascota m = (Mascota) cmbMascota.getSelectedItem();
            if (m == null) {
                throw new DatoInvalidoException("No hay mascotas cargadas.");
            }
            LocalDate desde = Fechas.parsear(txtDesde.getText());
            LocalDate hasta = Fechas.parsear(txtHasta.getText());
            if (hasta.isBefore(desde)) {
                throw new DatoInvalidoException("La fecha hasta no puede ser anterior a la fecha desde.");
            }

            disponibles = sistema.buscarAlojamientos(m.getEspecie(), desde, hasta);
            modeloDisponibles.setRowCount(0);
            for (Alojamiento a : disponibles) {
                modeloDisponibles.addRow(new Object[] { a.getCodigo(), a.getTipoLugar(), a.getCategoria(),
                        "$" + a.calcularPrecioDia() });
            }
            if (disponibles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay lugares libres para " + m.getEspecie()
                        + " en esas fechas.", "Sin disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reservar() {
        int fila = tablaDisponibles.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero buscá y elegí un alojamiento de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Mascota m = (Mascota) cmbMascota.getSelectedItem();
            Alojamiento a = disponibles.get(fila);
            LocalDate desde = Fechas.parsear(txtDesde.getText());
            LocalDate hasta = Fechas.parsear(txtHasta.getText());

            Reserva r = sistema.reservar(m, a, desde, hasta, esHospedaje(), chkExtra.isSelected());
            JOptionPane.showMessageDialog(this, "Reserva N° " + r.getIdReserva() + " confirmada.\n"
                    + r.getTipo() + " de " + m.getNombre() + " en " + a.getCodigo() + "\n"
                    + "Costo estimado: $" + String.format("%.2f", r.calcularCosto()),
                    "Reserva confirmada", JOptionPane.INFORMATION_MESSAGE);
            modeloDisponibles.setRowCount(0);
            disponibles.clear();
            cargarReservas();
        } catch (AlojamientoNoDisponibleException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage() + "\nVolvé a buscar con las fechas actuales.",
                    "No disponible", JOptionPane.ERROR_MESSAGE);
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarReservas() {
        modeloReservas.setRowCount(0);
        for (Reserva r : sistema.getReservas()) {
            modeloReservas.addRow(new Object[] { r.getIdReserva(), r.getMascota().getNombre(), r.getTipo(),
                    r.getAlojamiento().getCodigo(), Fechas.formatear(r.getFechaDesde()),
                    Fechas.formatear(r.getFechaHasta()), r.getEstado(),
                    "$" + String.format("%.2f", r.calcularCosto()) });
        }
    }

    private void cancelar() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Elegí una reserva de la tabla de abajo.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Reserva r = sistema.getReservas().get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Cancelar la reserva N° " + r.getIdReserva()
                + " de " + r.getMascota().getNombre() + "?", "Cancelar reserva", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.cancelarReserva(r);
            cargarReservas();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede cancelar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
