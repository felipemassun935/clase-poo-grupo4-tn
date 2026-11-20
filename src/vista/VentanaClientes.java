package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.persona.Cliente;

// ABM 1: alta, baja y modificación de clientes
public class VentanaClientes extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    private SistemaGuarderia sistema;
    private Cliente seleccionado; // null = se está dando de alta uno nuevo

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDni;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtDireccion;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaClientes() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Clientes");
        setSize(1000, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // cierra solo esta ventana
        getContentPane().setLayout(null);

        txtId = agregarCampo("Id:", 20);
        txtId.setEditable(false); // el id lo asigna el sistema
        txtNombre = agregarCampo("Nombre:", 55);
        txtApellido = agregarCampo("Apellido:", 90);
        txtDni = agregarCampo("DNI:", 125);
        txtTelefono = agregarCampo("Teléfono:", 160);
        txtEmail = agregarCampo("Email:", 195);
        txtDireccion = agregarCampo("Dirección:", 230);

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

        modeloTabla = new DefaultTableModel(new String[] { "Id", "Nombre", "Apellido", "DNI", "Teléfono", "Email",
                "Dirección", "Mascotas" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null); // la tabla es solo para mirar, no se edita
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(320, 20, 650, 360);
        getContentPane().add(scroll);

        // Eventos (listeners con clases anónimas)
        btnNuevo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });
        btnGuardar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                guardar();
            }
        });
        btnEliminar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eliminar();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                seleccionarFila();
            }
        });

        cargarTabla();
    }

    // Agrega una etiqueta y un campo de texto en la fila "y"
    private JTextField agregarCampo(String etiqueta, int y) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setBounds(20, y, 80, 25);
        getContentPane().add(lbl);
        JTextField campo = new JTextField();
        campo.setBounds(100, y, 200, 25);
        getContentPane().add(campo);
        return campo;
    }

    // Las filas de la tabla están en el mismo orden que la lista de clientes del sistema
    private void cargarTabla() {
        modeloTabla.setRowCount(0); // borra las filas viejas
        for (Cliente c : sistema.getClientes()) {
            modeloTabla.addRow(new Object[] { c.getId(), c.getNombre(), c.getApellido(), c.getDni(),
                    c.getTelefono(), c.getEmail(), c.getDireccion(), c.getMascotas().size() });
        }
    }

    // Pasa los datos de la fila elegida a los campos para poder modificarlos
    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionado = sistema.getClientes().get(fila);
        txtId.setText("" + seleccionado.getId());
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtDni.setText(seleccionado.getDni());
        txtTelefono.setText(seleccionado.getTelefono());
        txtEmail.setText(seleccionado.getEmail());
        txtDireccion.setText(seleccionado.getDireccion());
    }

    private void limpiar() {
        seleccionado = null;
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDni.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        tabla.clearSelection();
    }

    // Si no hay ningún cliente elegido es un alta; si hay uno, es una modificación
    private void guardar() {
        try {
            String nombre = Validaciones.texto(txtNombre.getText(), "Nombre");
            String apellido = Validaciones.texto(txtApellido.getText(), "Apellido");
            String dni = Validaciones.texto(txtDni.getText(), "DNI");
            String telefono = Validaciones.texto(txtTelefono.getText(), "Teléfono");
            String email = txtEmail.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (seleccionado == null) {
                sistema.altaCliente(nombre, apellido, dni, telefono, email, direccion);
                JOptionPane.showMessageDialog(this, "Cliente agregado.");
            } else {
                sistema.modificarCliente(seleccionado, nombre, apellido, dni, telefono, email, direccion);
                JOptionPane.showMessageDialog(this, "Cliente modificado.");
            }
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí un cliente de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar a " + seleccionado.getNombreCompleto() + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.bajaCliente(seleccionado);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
