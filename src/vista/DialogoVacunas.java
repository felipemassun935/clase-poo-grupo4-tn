package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

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
import modelo.interfaces.Vacunable;
import modelo.mascota.Mascota;

// Diálogo modal para ver, agregar y quitar las vacunas de una mascota.
// Modal quiere decir que mientras está abierto no se puede usar la ventana de atrás.
public class DialogoVacunas extends JDialog {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    private SistemaGuarderia sistema;
    private Mascota mascota;

    private JComboBox<String> cmbNombre;
    private JTextField txtAplicada;
    private JTextField txtVence;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public DialogoVacunas(JFrame padre, Mascota mascota) {
        super(padre, "Vacunas de " + mascota.getNombre(), true); // true = modal
        this.sistema = SistemaGuarderia.getInstancia();
        this.mascota = mascota;

        setSize(640, 400);
        setLocationRelativeTo(padre);
        getContentPane().setLayout(null);

        // Combo editable: sugiere las obligatorias pero se puede escribir otra
        cmbNombre = new JComboBox<String>();
        cmbNombre.setEditable(true);

        // Interfaz: si la mascota es Vacunable se muestran sus vacunas obligatorias (instanceof + cast)
        String textoObligatorias;
        if (mascota instanceof Vacunable) {
            Vacunable vacunable = (Vacunable) mascota;
            List<String> obligatorias = vacunable.getVacunasObligatorias();
            textoObligatorias = "Obligatorias para " + mascota.getEspecie().getNombreVisible() + ": ";
            for (int i = 0; i < obligatorias.size(); i++) {
                if (i > 0) {
                    textoObligatorias = textoObligatorias + ", ";
                }
                textoObligatorias = textoObligatorias + obligatorias.get(i);
                cmbNombre.addItem(obligatorias.get(i));
            }
        } else {
            textoObligatorias = "Esta especie no tiene vacunas obligatorias: usa control veterinario (último: "
                    + Validaciones.formatearFecha(mascota.getFechaUltimoControl()) + ").";
        }
        JLabel lblObligatorias = new JLabel(textoObligatorias);
        lblObligatorias.setBounds(20, 10, 600, 25);
        getContentPane().add(lblObligatorias);

        modeloTabla = new DefaultTableModel(new String[] { "Vacuna", "Aplicada", "Vence", "Vigente hoy" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 40, 590, 170);
        getContentPane().add(scroll);

        JLabel lblNombre = new JLabel("Vacuna:");
        lblNombre.setBounds(20, 225, 90, 25);
        getContentPane().add(lblNombre);
        cmbNombre.setBounds(110, 225, 200, 25);
        getContentPane().add(cmbNombre);

        JLabel lblAplicada = new JLabel("Aplicada:");
        lblAplicada.setBounds(20, 260, 90, 25);
        getContentPane().add(lblAplicada);
        txtAplicada = new JTextField(Validaciones.formatearFecha(LocalDate.now()));
        txtAplicada.setBounds(110, 260, 200, 25);
        getContentPane().add(txtAplicada);

        JLabel lblVence = new JLabel("Vence:");
        lblVence.setBounds(330, 260, 60, 25);
        getContentPane().add(lblVence);
        txtVence = new JTextField(Validaciones.formatearFecha(LocalDate.now().plusYears(1)));
        txtVence.setBounds(390, 260, 200, 25);
        getContentPane().add(txtVence);

        JButton btnAgregar = new JButton("Agregar vacuna");
        btnAgregar.setBounds(20, 310, 160, 30);
        getContentPane().add(btnAgregar);

        JButton btnQuitar = new JButton("Quitar seleccionada");
        btnQuitar.setBounds(190, 310, 170, 30);
        getContentPane().add(btnQuitar);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(510, 310, 100, 30);
        getContentPane().add(btnCerrar);

        // Eventos (listeners con clases anónimas)
        btnAgregar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregar();
            }
        });
        btnQuitar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitar();
            }
        });
        btnCerrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        cargarTabla();
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Mascota.Vacuna v : mascota.getVacunas()) {
            String vigente = "No";
            if (v.estaVigente(LocalDate.now())) {
                vigente = "Sí";
            }
            modeloTabla.addRow(new Object[] { v.getNombre(), Validaciones.formatearFecha(v.getFechaAplicacion()),
                    Validaciones.formatearFecha(v.getFechaVencimiento()), vigente });
        }
    }

    private void agregar() {
        try {
            Object elegido = cmbNombre.getSelectedItem();
            String nombre = "";
            if (elegido != null) {
                nombre = elegido.toString();
            }
            nombre = Validaciones.texto(nombre, "Vacuna");
            LocalDate aplicada = Validaciones.parsearFecha(txtAplicada.getText(), "Aplicada");
            LocalDate vence = Validaciones.parsearFecha(txtVence.getText(), "Vence");

            sistema.agregarVacuna(mascota, nombre, aplicada, vence);
            cargarTabla();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Elegí una vacuna de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Mascota.Vacuna v = mascota.getVacunas().get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Quitar la vacuna " + v.getNombre() + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            sistema.quitarVacuna(mascota, v);
            cargarTabla();
        }
    }
}
