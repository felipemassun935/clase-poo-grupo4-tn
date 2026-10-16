package vista;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.Empleado;
import modelo.RolEmpleado;

// ABM de empleados
public class VentanaEmpleados extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private Empleado seleccionado;

    private JTextField txtLegajo;
    private JTextField txtDni;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JComboBox<RolEmpleado> cmbRol;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaEmpleados() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Empleados");
        setSize(960, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        txtLegajo = agregarCampo("Legajo:", 20);
        txtDni = agregarCampo("DNI:", 60);
        txtNombre = agregarCampo("Nombre:", 100);
        txtApellido = agregarCampo("Apellido:", 140);
        txtTelefono = agregarCampo("Teléfono:", 180);
        txtEmail = agregarCampo("Email:", 220);

        JLabel lblRol = new JLabel("Rol:");
        lblRol.setBounds(20, 260, 80, 25);
        getContentPane().add(lblRol);
        cmbRol = new JComboBox<>(RolEmpleado.values());
        cmbRol.setBounds(100, 260, 200, 25);
        getContentPane().add(cmbRol);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 310, 90, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(115, 310, 90, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(210, 310, 90, 30);
        getContentPane().add(btnEliminar);

        modeloTabla = new DefaultTableModel(
                new String[] { "Legajo", "DNI", "Nombre", "Apellido", "Teléfono", "Email", "Rol" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(320, 20, 610, 380);
        getContentPane().add(scroll);

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> seleccionarFila());

        cargarTabla();
    }

    private JTextField agregarCampo(String etiqueta, int y) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setBounds(20, y, 80, 25);
        getContentPane().add(lbl);
        JTextField campo = new JTextField();
        campo.setBounds(100, y, 200, 25);
        getContentPane().add(campo);
        return campo;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Empleado e : sistema.getEmpleados()) {
            modeloTabla.addRow(new Object[] { e.getLegajo(), e.getDni(), e.getNombre(), e.getApellido(),
                    e.getTelefono(), e.getEmail(), e.getRol() });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionado = sistema.getEmpleados().get(fila);
        txtLegajo.setText(String.valueOf(seleccionado.getLegajo()));
        txtLegajo.setEnabled(false); // el legajo no se cambia
        txtDni.setText(seleccionado.getDni());
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtTelefono.setText(seleccionado.getTelefono());
        txtEmail.setText(seleccionado.getEmail());
        cmbRol.setSelectedItem(seleccionado.getRol());
    }

    private void limpiar() {
        seleccionado = null;
        txtLegajo.setEnabled(true);
        txtLegajo.setText("");
        txtDni.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        cmbRol.setSelectedIndex(0);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            int legajo = Validaciones.entero(txtLegajo, "Legajo");
            String dni = Validaciones.texto(txtDni, "DNI");
            String nombre = Validaciones.texto(txtNombre, "Nombre");
            String apellido = Validaciones.texto(txtApellido, "Apellido");
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            RolEmpleado rol = (RolEmpleado) cmbRol.getSelectedItem();

            if (seleccionado == null) {
                sistema.agregarEmpleado(new Empleado(dni, nombre, apellido, telefono, email, legajo, rol));
                JOptionPane.showMessageDialog(this, "Empleado agregado.");
            } else {
                seleccionado.setDni(dni);
                seleccionado.setNombre(nombre);
                seleccionado.setApellido(apellido);
                seleccionado.setTelefono(telefono);
                seleccionado.setEmail(email);
                seleccionado.setRol(rol);
                sistema.guardarTodo();
                JOptionPane.showMessageDialog(this, "Empleado modificado.");
            }
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí un empleado de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + seleccionado.getNombreCompleto() + "?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.eliminarEmpleado(seleccionado);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
