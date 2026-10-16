package vista;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.Dueno;

// ABM de dueños
public class VentanaDuenos extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private Dueno seleccionado; // null = estamos dando de alta uno nuevo

    private JTextField txtDni;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtDireccion;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaDuenos() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Dueños");
        setSize(960, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // cierra solo esta ventana
        getContentPane().setLayout(null);

        txtDni = agregarCampo("DNI:", 20);
        txtNombre = agregarCampo("Nombre:", 60);
        txtApellido = agregarCampo("Apellido:", 100);
        txtTelefono = agregarCampo("Teléfono:", 140);
        txtEmail = agregarCampo("Email:", 180);
        txtDireccion = agregarCampo("Dirección:", 220);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 280, 90, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(115, 280, 90, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(210, 280, 90, 30);
        getContentPane().add(btnEliminar);

        JLabel lblAyuda = new JLabel("Elegí una fila de la tabla para modificarla.");
        lblAyuda.setBounds(20, 320, 300, 20);
        getContentPane().add(lblAyuda);

        modeloTabla = new DefaultTableModel(
                new String[] { "DNI", "Nombre", "Apellido", "Teléfono", "Email", "Dirección", "Mascotas" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null); // la tabla es solo para mirar, no se edita
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(320, 20, 610, 360);
        getContentPane().add(scroll);

        // Eventos
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
        modeloTabla.setRowCount(0); // borra las filas viejas
        for (Dueno d : sistema.getDuenos()) {
            modeloTabla.addRow(new Object[] { d.getDni(), d.getNombre(), d.getApellido(), d.getTelefono(),
                    d.getEmail(), d.getDireccion(), d.getMascotas().size() });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionado = sistema.getDuenos().get(fila);
        txtDni.setText(seleccionado.getDni());
        txtDni.setEnabled(false); // el DNI identifica al dueño, no se cambia
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtTelefono.setText(seleccionado.getTelefono());
        txtEmail.setText(seleccionado.getEmail());
        txtDireccion.setText(seleccionado.getDireccion());
    }

    private void limpiar() {
        seleccionado = null;
        txtDni.setEnabled(true);
        txtDni.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            String dni = Validaciones.texto(txtDni, "DNI");
            String nombre = Validaciones.texto(txtNombre, "Nombre");
            String apellido = Validaciones.texto(txtApellido, "Apellido");
            String telefono = Validaciones.texto(txtTelefono, "Teléfono");
            String email = txtEmail.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (seleccionado == null) {
                // Alta
                sistema.agregarDueno(new Dueno(dni, nombre, apellido, telefono, email, direccion));
                JOptionPane.showMessageDialog(this, "Dueño agregado.");
            } else {
                // Modificación
                seleccionado.setNombre(nombre);
                seleccionado.setApellido(apellido);
                seleccionado.setTelefono(telefono);
                seleccionado.setEmail(email);
                seleccionado.setDireccion(direccion);
                sistema.guardarTodo();
                JOptionPane.showMessageDialog(this, "Dueño modificado.");
            }
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí un dueño de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + seleccionado.getNombreCompleto() + "?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.eliminarDueno(seleccionado);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
