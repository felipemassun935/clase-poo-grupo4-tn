package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import modelo.persona.Empleado;
import modelo.sucursal.Sucursal;

// ABM 3: alta, baja y modificación de empleados
public class VentanaEmpleados extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    private SistemaGuarderia sistema;
    private Empleado seleccionado; // null = se está dando de alta uno nuevo

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDni;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtLegajo;
    private JTextField txtIngreso;
    private JComboBox<Sucursal> cmbSucursal;
    private JCheckBox chkEncargado;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaEmpleados() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Empleados");
        setSize(1100, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        txtId = agregarCampo("Id:", 20);
        txtId.setEditable(false); // el id lo asigna el sistema
        txtNombre = agregarCampo("Nombre:", 55);
        txtApellido = agregarCampo("Apellido:", 90);
        txtDni = agregarCampo("DNI:", 125);
        txtTelefono = agregarCampo("Teléfono:", 160);
        txtEmail = agregarCampo("Email:", 195);
        txtLegajo = agregarCampo("Legajo:", 230);
        txtIngreso = agregarCampo("Ingreso:", 265);

        JLabel lblSucursal = new JLabel("Sucursal:");
        lblSucursal.setBounds(20, 300, 100, 25);
        getContentPane().add(lblSucursal);
        cmbSucursal = new JComboBox<Sucursal>();
        for (Sucursal s : sistema.getSucursales()) {
            cmbSucursal.addItem(s);
        }
        cmbSucursal.setBounds(120, 300, 220, 25);
        getContentPane().add(cmbSucursal);

        chkEncargado = new JCheckBox("Encargado de la sucursal");
        chkEncargado.setBounds(120, 335, 220, 25);
        getContentPane().add(chkEncargado);

        JLabel lblFechas = new JLabel("Fecha de ingreso en formato dd/mm/aaaa");
        lblFechas.setBounds(20, 365, 320, 20);
        getContentPane().add(lblFechas);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 400, 100, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(130, 400, 100, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(240, 400, 100, 30);
        getContentPane().add(btnEliminar);

        modeloTabla = new DefaultTableModel(new String[] { "Id", "Nombre", "Apellido", "DNI", "Teléfono", "Legajo",
                "Ingreso", "Sucursal", "Rol" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null); // la tabla es solo para mirar, no se edita
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(360, 20, 710, 440);
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

    private JTextField agregarCampo(String etiqueta, int y) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setBounds(20, y, 100, 25);
        getContentPane().add(lbl);
        JTextField campo = new JTextField();
        campo.setBounds(120, y, 220, 25);
        getContentPane().add(campo);
        return campo;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Empleado e : sistema.getEmpleados()) {
            // Polimorfismo: getRol() dice "Encargado" o "Empleado" según el caso
            modeloTabla.addRow(new Object[] { e.getId(), e.getNombre(), e.getApellido(), e.getDni(),
                    e.getTelefono(), e.getLegajo(), Validaciones.formatearFecha(e.getFechaIngreso()),
                    e.getSucursal().getNombre(), e.getRol() });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionado = sistema.getEmpleados().get(fila);
        txtId.setText("" + seleccionado.getId());
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtDni.setText(seleccionado.getDni());
        txtTelefono.setText(seleccionado.getTelefono());
        txtEmail.setText(seleccionado.getEmail());
        txtLegajo.setText(seleccionado.getLegajo());
        txtIngreso.setText(Validaciones.formatearFecha(seleccionado.getFechaIngreso()));
        cmbSucursal.setSelectedItem(seleccionado.getSucursal());
        cmbSucursal.setEnabled(false); // la sucursal no se cambia en una modificación
        chkEncargado.setSelected(seleccionado.getSucursal().getEncargado() == seleccionado);
    }

    private void limpiar() {
        seleccionado = null;
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDni.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtLegajo.setText("");
        txtIngreso.setText("");
        cmbSucursal.setEnabled(true);
        chkEncargado.setSelected(false);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            String nombre = Validaciones.texto(txtNombre.getText(), "Nombre");
            String apellido = Validaciones.texto(txtApellido.getText(), "Apellido");
            String dni = Validaciones.texto(txtDni.getText(), "DNI");
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            String legajo = Validaciones.texto(txtLegajo.getText(), "Legajo");
            LocalDate ingreso = Validaciones.parsearFecha(txtIngreso.getText(), "Ingreso");
            Sucursal sucursal = (Sucursal) cmbSucursal.getSelectedItem();

            Empleado empleado;
            if (seleccionado == null) {
                empleado = sistema.altaEmpleado(nombre, apellido, dni, telefono, email, legajo, ingreso, sucursal);
            } else {
                // Una sucursal siempre tiene que tener un encargado: no se puede "desmarcar"
                boolean eraEncargado = seleccionado.getSucursal().getEncargado() == seleccionado;
                if (eraEncargado && !chkEncargado.isSelected()) {
                    throw new DatoInvalidoException(
                            "Para cambiar el encargado, marcá a otro empleado de la sucursal como encargado.");
                }
                sistema.modificarEmpleado(seleccionado, nombre, apellido, dni, telefono, email, legajo, ingreso);
                empleado = seleccionado;
            }

            if (chkEncargado.isSelected()) {
                sistema.setEncargado(empleado.getSucursal(), empleado);
            }
            JOptionPane.showMessageDialog(this, "Empleado guardado.");
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
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar a " + seleccionado.getNombreCompleto() + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.bajaEmpleado(seleccionado);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
