package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import modelo.enums.Categoria;
import modelo.enums.Especie;
import modelo.sucursal.Alojamiento;
import modelo.sucursal.Sucursal;

// ABM 4: alta, baja y modificación de alojamientos de cada sucursal
public class VentanaAlojamientos extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    private SistemaGuarderia sistema;
    private Alojamiento seleccionado; // null = se está dando de alta uno nuevo
    private List<Alojamiento> filas;  // los alojamientos en el mismo orden que la tabla

    private JComboBox<Sucursal> cmbSucursal;
    private JTextField txtCodigo;
    // El combo muestra los nombres visibles en el mismo orden que Especie.values()
    private JComboBox<String> cmbEspecie;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField txtPrecio;
    private JCheckBox chkActivo;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaAlojamientos() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Alojamientos");
        setSize(1050, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        agregarEtiqueta("Sucursal:", 20);
        cmbSucursal = new JComboBox<Sucursal>();
        for (Sucursal s : sistema.getSucursales()) {
            cmbSucursal.addItem(s);
        }
        cmbSucursal.setBounds(120, 20, 220, 25);
        getContentPane().add(cmbSucursal);

        agregarEtiqueta("Código:", 55);
        txtCodigo = new JTextField();
        txtCodigo.setBounds(120, 55, 220, 25);
        getContentPane().add(txtCodigo);

        agregarEtiqueta("Especie:", 90);
        cmbEspecie = new JComboBox<String>();
        for (Especie e : Especie.values()) {
            cmbEspecie.addItem(e.getNombreVisible());
        }
        cmbEspecie.setBounds(120, 90, 220, 25);
        getContentPane().add(cmbEspecie);

        agregarEtiqueta("Categoría:", 125);
        cmbCategoria = new JComboBox<Categoria>();
        for (Categoria c : Categoria.values()) {
            cmbCategoria.addItem(c);
        }
        cmbCategoria.setBounds(120, 125, 220, 25);
        getContentPane().add(cmbCategoria);

        agregarEtiqueta("Precio base:", 160);
        txtPrecio = new JTextField();
        txtPrecio.setBounds(120, 160, 220, 25);
        getContentPane().add(txtPrecio);

        chkActivo = new JCheckBox("Activo");
        chkActivo.setSelected(true);
        chkActivo.setBounds(120, 195, 220, 25);
        getContentPane().add(chkActivo);

        JLabel lblAyuda = new JLabel("PREMIUM suma un 30% al precio base.");
        lblAyuda.setBounds(20, 230, 320, 20);
        getContentPane().add(lblAyuda);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 270, 100, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(130, 270, 100, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(240, 270, 100, 30);
        getContentPane().add(btnEliminar);

        modeloTabla = new DefaultTableModel(new String[] { "Id", "Sucursal", "Código", "Especie", "Categoría",
                "Precio base", "Precio final", "Activo" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null); // la tabla es solo para mirar, no se edita
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(360, 20, 660, 360);
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

    private void agregarEtiqueta(String texto, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setBounds(20, y, 100, 25);
        getContentPane().add(lbl);
    }

    // Los alojamientos están dentro de cada sucursal; el sistema los junta en una lista
    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        filas = sistema.getAlojamientos();
        for (Alojamiento a : filas) {
            String activo = "No";
            if (a.isActivo()) {
                activo = "Sí";
            }
            modeloTabla.addRow(new Object[] { a.getId(), a.getSucursal().getNombre(), a.getCodigo(),
                    a.getEspecie().getNombreVisible(), a.getCategoria(), Validaciones.plata(a.getPrecioBaseNoche()),
                    Validaciones.plata(a.getPrecioNoche()), activo });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionado = filas.get(fila);
        cmbSucursal.setSelectedItem(seleccionado.getSucursal());
        cmbSucursal.setEnabled(false); // la sucursal no se cambia en una modificación
        txtCodigo.setText(seleccionado.getCodigo());
        cmbEspecie.setSelectedItem(seleccionado.getEspecie().getNombreVisible());
        cmbCategoria.setSelectedItem(seleccionado.getCategoria());
        txtPrecio.setText("" + seleccionado.getPrecioBaseNoche());
        chkActivo.setSelected(seleccionado.isActivo());
    }

    private void limpiar() {
        seleccionado = null;
        cmbSucursal.setEnabled(true);
        txtCodigo.setText("");
        txtPrecio.setText("");
        chkActivo.setSelected(true);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            Sucursal sucursal = (Sucursal) cmbSucursal.getSelectedItem();
            String codigo = Validaciones.texto(txtCodigo.getText(), "Código");
            // La posición elegida en el combo coincide con la posición en Especie.values()
            Especie especie = Especie.values()[cmbEspecie.getSelectedIndex()];
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
            double precio = Validaciones.numero(txtPrecio.getText(), "Precio base");
            boolean activo = chkActivo.isSelected();

            if (seleccionado == null) {
                sistema.altaAlojamiento(sucursal, codigo, especie, categoria, precio, activo);
                JOptionPane.showMessageDialog(this, "Alojamiento agregado.");
            } else {
                sistema.modificarAlojamiento(seleccionado, codigo, especie, categoria, precio, activo);
                JOptionPane.showMessageDialog(this, "Alojamiento modificado.");
            }
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí un alojamiento de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar el alojamiento " + seleccionado.getCodigo()
                + "?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.bajaAlojamiento(seleccionado);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
