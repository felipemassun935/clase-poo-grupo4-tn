package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.enums.EstadoReserva;
import modelo.mascota.Mascota;
import modelo.reserva.Reserva;
import modelo.sucursal.Alojamiento;
import modelo.sucursal.Sucursal;

// Pantalla de negocio 1: buscar alojamientos libres, reservar, confirmar, registrar señas y cancelar
public class VentanaReservas extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    // Medios de pago que se ofrecen en la lista del showInputDialog
    private static final String[] MEDIOS_DE_PAGO = { "Efectivo", "Débito", "Crédito", "Transferencia" };

    private SistemaGuarderia sistema;
    private List<Alojamiento> disponibles; // lo que muestra la tabla de disponibles, en el mismo orden

    private JComboBox<Sucursal> cmbSucursal;
    private JComboBox<Mascota> cmbMascota;
    private JTextField txtDesde;
    private JTextField txtHasta;
    private JTable tablaDisponibles;
    private DefaultTableModel modeloDisponibles;
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;

    public VentanaReservas() {
        sistema = SistemaGuarderia.getInstancia();
        disponibles = new ArrayList<Alojamiento>();

        setTitle("Reservas");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        // ----- Parte de arriba: nueva reserva -----
        JLabel lblNueva = new JLabel("Nueva reserva");
        lblNueva.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNueva.setBounds(20, 10, 300, 25);
        getContentPane().add(lblNueva);

        agregarEtiqueta("Sucursal:", 20, 45);
        cmbSucursal = new JComboBox<Sucursal>();
        cmbSucursal.setBounds(100, 45, 240, 25);
        getContentPane().add(cmbSucursal);

        agregarEtiqueta("Mascota:", 20, 80);
        cmbMascota = new JComboBox<Mascota>();
        cmbMascota.setBounds(100, 80, 240, 25);
        getContentPane().add(cmbMascota);

        agregarEtiqueta("Desde:", 20, 115);
        txtDesde = new JTextField(Validaciones.formatearFecha(LocalDate.now()));
        txtDesde.setBounds(100, 115, 240, 25);
        getContentPane().add(txtDesde);

        agregarEtiqueta("Hasta:", 20, 150);
        txtHasta = new JTextField(Validaciones.formatearFecha(LocalDate.now().plusDays(3)));
        txtHasta.setBounds(100, 150, 240, 25);
        getContentPane().add(txtHasta);

        JButton btnBuscar = new JButton("Buscar disponibles");
        btnBuscar.setBounds(20, 190, 155, 30);
        getContentPane().add(btnBuscar);

        JButton btnReservar = new JButton("Reservar");
        btnReservar.setBounds(185, 190, 155, 30);
        getContentPane().add(btnReservar);

        JLabel lblAyuda = new JLabel("Fechas dd/mm/aaaa. Elegí un alojamiento de la tabla para reservar.");
        lblAyuda.setBounds(20, 225, 700, 20);
        getContentPane().add(lblAyuda);

        modeloDisponibles = new DefaultTableModel(new String[] { "Código", "Especie", "Categoría", "Precio por noche",
                "Noches", "Total estimado" }, 0);
        tablaDisponibles = new JTable(modeloDisponibles);
        tablaDisponibles.setDefaultEditor(Object.class, null);
        tablaDisponibles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollDisponibles = new JScrollPane(tablaDisponibles);
        scrollDisponibles.setBounds(360, 45, 710, 175);
        getContentPane().add(scrollDisponibles);

        // ----- Parte de abajo: reservas existentes -----
        JLabel lblReservas = new JLabel("Reservas");
        lblReservas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblReservas.setBounds(20, 260, 300, 25);
        getContentPane().add(lblReservas);

        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.setBounds(20, 290, 140, 30);
        getContentPane().add(btnConfirmar);

        JButton btnSena = new JButton("Registrar seña");
        btnSena.setBounds(170, 290, 140, 30);
        getContentPane().add(btnSena);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(320, 290, 140, 30);
        getContentPane().add(btnCancelar);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setBounds(930, 290, 140, 30);
        getContentPane().add(btnActualizar);

        modeloReservas = new DefaultTableModel(new String[] { "Id", "Mascota", "Cliente", "Sucursal", "Alojamiento",
                "Desde", "Hasta", "Total", "Pagado", "Saldo", "Estado" }, 0);
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setDefaultEditor(Object.class, null);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollReservas = new JScrollPane(tablaReservas);
        scrollReservas.setBounds(20, 330, 1050, 330);
        getContentPane().add(scrollReservas);

        // Eventos (listeners con clases anónimas)
        btnBuscar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarDisponibles();
            }
        });
        btnReservar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reservar();
            }
        });
        btnConfirmar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                confirmar();
            }
        });
        btnSena.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registrarSena();
            }
        });
        btnCancelar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelar();
            }
        });
        btnActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cargarCombos();
                cargarReservas();
            }
        });

        cargarCombos();
        cargarReservas();
    }

    private void agregarEtiqueta(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setBounds(x, y, 80, 25);
        getContentPane().add(lbl);
    }

    // Vuelve a llenar los combos (por si se cargaron mascotas en otra ventana)
    private void cargarCombos() {
        cmbSucursal.removeAllItems();
        for (Sucursal s : sistema.getSucursales()) {
            cmbSucursal.addItem(s);
        }
        cmbMascota.removeAllItems();
        for (Mascota m : sistema.getMascotas()) {
            cmbMascota.addItem(m);
        }
    }

    // Las filas de la tabla están en el mismo orden que la lista de reservas del sistema
    private void cargarReservas() {
        modeloReservas.setRowCount(0);
        for (Reserva r : sistema.getReservas()) {
            modeloReservas.addRow(new Object[] { r.getId(), r.getMascota().getNombre(),
                    r.getMascota().getCliente().getNombreCompleto(), r.getAlojamiento().getSucursal().getNombre(),
                    r.getAlojamiento().getCodigo(), Validaciones.formatearFecha(r.getFechaDesde()),
                    Validaciones.formatearFecha(r.getFechaHasta()), Validaciones.plata(r.calcularTotal()),
                    Validaciones.plata(r.getTotalPagado()), Validaciones.plata(r.getSaldoPendiente()),
                    r.getEstado() });
        }
    }

    private void buscarDisponibles() {
        try {
            Sucursal sucursal = (Sucursal) cmbSucursal.getSelectedItem();
            Mascota mascota = (Mascota) cmbMascota.getSelectedItem();
            if (sucursal == null || mascota == null) {
                throw new DatoInvalidoException("Elegí la sucursal y la mascota.");
            }
            LocalDate desde = Validaciones.parsearFecha(txtDesde.getText(), "Desde");
            LocalDate hasta = Validaciones.parsearFecha(txtHasta.getText(), "Hasta");
            if (!hasta.isAfter(desde)) {
                throw new DatoInvalidoException("La fecha \"Hasta\" tiene que ser posterior a \"Desde\".");
            }

            long noches = ChronoUnit.DAYS.between(desde, hasta);
            disponibles = sistema.buscarDisponibles(sucursal, mascota, desde, hasta);
            modeloDisponibles.setRowCount(0);
            for (Alojamiento a : disponibles) {
                modeloDisponibles.addRow(new Object[] { a.getCodigo(), a.getEspecie().getNombreVisible(),
                        a.getCategoria(), Validaciones.plata(a.getPrecioNoche()), noches,
                        Validaciones.plata(noches * a.getPrecioNoche()) });
            }
            if (disponibles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay alojamientos libres para " + mascota.getNombre()
                        + " en esas fechas.", "Sin disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reservar() {
        int fila = tablaDisponibles.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero buscá y elegí un alojamiento disponible.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Mascota mascota = (Mascota) cmbMascota.getSelectedItem();
            LocalDate desde = Validaciones.parsearFecha(txtDesde.getText(), "Desde");
            LocalDate hasta = Validaciones.parsearFecha(txtHasta.getText(), "Hasta");
            Reserva r = sistema.reservar(mascota, disponibles.get(fila), desde, hasta);
            JOptionPane.showMessageDialog(this, "Reserva " + r.getId() + " creada en estado " + r.getEstado()
                    + ".\nTotal: " + Validaciones.plata(r.calcularTotal()));
            modeloDisponibles.setRowCount(0);
            disponibles = new ArrayList<Alojamiento>();
            cargarReservas();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo reservar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Devuelve la reserva elegida en la tabla de abajo, o null si no hay ninguna (y avisa)
    private Reserva getReservaElegida() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero elegí una reserva de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return sistema.getReservas().get(fila);
    }

    private void confirmar() {
        Reserva r = getReservaElegida();
        if (r == null) {
            return;
        }
        // try/catch: si el estado actual no permite confirmar, la Reserva tira IllegalStateException
        try {
            sistema.confirmar(r);
            cargarReservas();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede confirmar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Pide el monto y el medio de pago con dos showInputDialog
    private void registrarSena() {
        Reserva r = getReservaElegida();
        if (r == null) {
            return;
        }
        if (r.getEstado() != EstadoReserva.PENDIENTE && r.getEstado() != EstadoReserva.CONFIRMADA) {
            JOptionPane.showMessageDialog(this, "Solo se registran señas en reservas PENDIENTES o CONFIRMADAS.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String textoMonto = JOptionPane.showInputDialog(this, "Total: " + Validaciones.plata(r.calcularTotal())
                + "\nSaldo: " + Validaciones.plata(r.getSaldoPendiente()) + "\n\nMonto de la seña:");
        if (textoMonto == null) {
            return; // el usuario apretó "Cancelar"
        }
        try {
            double monto = Validaciones.numero(textoMonto, "Monto");
            if (monto > r.getSaldoPendiente()) {
                throw new DatoInvalidoException("La seña no puede superar el saldo pendiente.");
            }
            Object medio = JOptionPane.showInputDialog(this, "Medio de pago:", "Registrar seña",
                    JOptionPane.QUESTION_MESSAGE, null, MEDIOS_DE_PAGO, MEDIOS_DE_PAGO[0]);
            if (medio == null) {
                return;
            }
            sistema.registrarPago(r, monto, medio.toString());
            JOptionPane.showMessageDialog(this, "Seña registrada. Saldo: " + Validaciones.plata(r.getSaldoPendiente()));
            cargarReservas();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        Reserva r = getReservaElegida();
        if (r == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Cancelar la reserva " + r.getId() + " de "
                + r.getMascota().getNombre() + "?", "Confirmar cancelación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        // try/catch: si el estado actual no permite cancelar, la Reserva tira IllegalStateException
        try {
            sistema.cancelar(r);
            cargarReservas();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede cancelar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
