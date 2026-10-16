package vista;

import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.Mascota;
import modelo.Vacuna;
import util.Fechas;

// Diálogo modal para ver, agregar y quitar las vacunas de una mascota
public class DialogoVacunas extends JDialog {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private Mascota mascota;

    private JComboBox<String> cmbNombre;
    private JTextField txtAplicada;
    private JTextField txtVence;
    private JTextField txtVeterinario;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public DialogoVacunas(JFrame padre, Mascota mascota) {
        super(padre, "Vacunas de " + mascota.getNombre(), true); // true = modal
        this.sistema = SistemaGuarderia.getInstancia();
        this.mascota = mascota;

        setSize(720, 420);
        setLocationRelativeTo(padre);
        getContentPane().setLayout(null);

        String obligatorias = String.join(", ", mascota.getVacunasObligatorias());
        if (obligatorias.isEmpty()) {
            obligatorias = "ninguna";
        }
        JLabel lblObligatorias = new JLabel("Obligatorias para " + mascota.getEspecie() + ": " + obligatorias);
        lblObligatorias.setBounds(20, 10, 680, 25);
        getContentPane().add(lblObligatorias);

        modeloTabla = new DefaultTableModel(new String[] { "Vacuna", "Aplicada", "Vence", "Veterinario", "Vigente" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 40, 670, 170);
        getContentPane().add(scroll);

        JLabel lblNombre = new JLabel("Vacuna:");
        lblNombre.setBounds(20, 225, 90, 25);
        getContentPane().add(lblNombre);
        // Combo editable: sugiere las obligatorias pero se puede escribir otra
        cmbNombre = new JComboBox<>();
        for (String v : mascota.getVacunasObligatorias()) {
            cmbNombre.addItem(v);
        }
        cmbNombre.setEditable(true);
        cmbNombre.setBounds(110, 225, 200, 25);
        getContentPane().add(cmbNombre);

        JLabel lblVet = new JLabel("Veterinario:");
        lblVet.setBounds(340, 225, 90, 25);
        getContentPane().add(lblVet);
        txtVeterinario = new JTextField();
        txtVeterinario.setBounds(430, 225, 200, 25);
        getContentPane().add(txtVeterinario);

        JLabel lblAplicada = new JLabel("Aplicada:");
        lblAplicada.setBounds(20, 260, 90, 25);
        getContentPane().add(lblAplicada);
        txtAplicada = new JTextField(Fechas.formatear(LocalDate.now()));
        txtAplicada.setBounds(110, 260, 200, 25);
        getContentPane().add(txtAplicada);

        JLabel lblVence = new JLabel("Vence:");
        lblVence.setBounds(340, 260, 90, 25);
        getContentPane().add(lblVence);
        txtVence = new JTextField(Fechas.formatear(LocalDate.now().plusYears(1)));
        txtVence.setBounds(430, 260, 200, 25);
        getContentPane().add(txtVence);

        JButton btnAgregar = new JButton("Agregar vacuna");
        btnAgregar.setBounds(20, 310, 160, 30);
        getContentPane().add(btnAgregar);

        JButton btnQuitar = new JButton("Quitar seleccionada");
        btnQuitar.setBounds(190, 310, 170, 30);
        getContentPane().add(btnQuitar);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(590, 310, 100, 30);
        getContentPane().add(btnCerrar);

        btnAgregar.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitar());
        btnCerrar.addActionListener(e -> dispose());

        cargarTabla();
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Vacuna v : mascota.getVacunas()) {
            String vigente = "No";
            if (v.estaVigente(LocalDate.now())) {
                vigente = "Sí";
            }
            modeloTabla.addRow(new Object[] { v.getNombre(), Fechas.formatear(v.getFechaAplicacion()),
                    Fechas.formatear(v.getFechaVencimiento()), v.getVeterinario(), vigente });
        }
    }

    private void agregar() {
        try {
            Object elegido = cmbNombre.getSelectedItem();
            if (elegido == null || elegido.toString().trim().isEmpty()) {
                throw new DatoInvalidoException("Escribí o elegí el nombre de la vacuna.");
            }
            LocalDate aplicada = Fechas.parsear(Validaciones.texto(txtAplicada, "Aplicada"));
            LocalDate vence = Fechas.parsear(Validaciones.texto(txtVence, "Vence"));
            if (!vence.isAfter(aplicada)) {
                throw new DatoInvalidoException("El vencimiento tiene que ser posterior a la aplicación.");
            }
            String veterinario = Validaciones.texto(txtVeterinario, "Veterinario");

            mascota.agregarVacuna(new Vacuna(elegido.toString().trim(), aplicada, vence, veterinario));
            sistema.guardarTodo();
            cargarTabla();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Elegí una vacuna de la tabla.");
            return;
        }
        mascota.quitarVacuna(mascota.getVacunas().get(fila));
        sistema.guardarTodo();
        cargarTabla();
    }
}
