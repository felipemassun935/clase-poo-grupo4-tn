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
import modelo.Alojamiento;
import modelo.Categoria;
import modelo.Especie;

// ABM de alojamientos (caniles, gateras, conejeras y jaulas) de la sucursal
public class VentanaAlojamientos extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private Alojamiento seleccionado;

    private JTextField txtCodigo;
    private JComboBox<Especie> cmbEspecie;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField txtPrecio;
    private JLabel lblOcupacion;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaAlojamientos() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Alojamientos");
        setSize(960, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel lblCodigo = new JLabel("Código:");
        lblCodigo.setBounds(20, 20, 90, 25);
        getContentPane().add(lblCodigo);
        txtCodigo = new JTextField();
        txtCodigo.setBounds(110, 20, 190, 25);
        getContentPane().add(txtCodigo);

        JLabel lblEspecie = new JLabel("Especie:");
        lblEspecie.setBounds(20, 60, 90, 25);
        getContentPane().add(lblEspecie);
        cmbEspecie = new JComboBox<>(Especie.values());
        cmbEspecie.setBounds(110, 60, 190, 25);
        getContentPane().add(cmbEspecie);

        JLabel lblCategoria = new JLabel("Categoría:");
        lblCategoria.setBounds(20, 100, 90, 25);
        getContentPane().add(lblCategoria);
        cmbCategoria = new JComboBox<>(Categoria.values());
        cmbCategoria.setBounds(110, 100, 190, 25);
        getContentPane().add(cmbCategoria);

        JLabel lblPrecio = new JLabel("Precio x día $:");
        lblPrecio.setBounds(20, 140, 90, 25);
        getContentPane().add(lblPrecio);
        txtPrecio = new JTextField();
        txtPrecio.setBounds(110, 140, 190, 25);
        getContentPane().add(txtPrecio);

        JLabel lblNota = new JLabel("PREMIUM tiene 30% de recargo.");
        lblNota.setBounds(20, 170, 280, 20);
        getContentPane().add(lblNota);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 210, 90, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(115, 210, 90, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(210, 210, 90, 30);
        getContentPane().add(btnEliminar);

        lblOcupacion = new JLabel();
        lblOcupacion.setBounds(20, 270, 290, 25);
        getContentPane().add(lblOcupacion);

        modeloTabla = new DefaultTableModel(new String[] { "Código", "Tipo", "Especie", "Categoría",
                "Precio base", "Precio final/día", "Estado" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(320, 20, 610, 340);
        getContentPane().add(scroll);

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> seleccionarFila());

        cargarTabla();
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Alojamiento a : sistema.getAlojamientos()) {
            String estado = "Libre";
            if (a.estaOcupado()) {
                estado = "Ocupado";
            }
            modeloTabla.addRow(new Object[] { a.getCodigo(), a.getTipoLugar(), a.getEspecie(), a.getCategoria(),
                    "$" + a.getPrecioBaseDia(), "$" + a.calcularPrecioDia(), estado });
        }
        lblOcupacion.setText("Ocupación actual: "
                + String.format("%.0f", sistema.getSucursal().calcularOcupacion()) + "%");
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionado = sistema.getAlojamientos().get(fila);
        txtCodigo.setText(seleccionado.getCodigo());
        txtCodigo.setEnabled(false);
        cmbEspecie.setSelectedItem(seleccionado.getEspecie());
        cmbEspecie.setEnabled(false); // no se cambia la especie de un lugar ya creado
        cmbCategoria.setSelectedItem(seleccionado.getCategoria());
        txtPrecio.setText(String.valueOf(seleccionado.getPrecioBaseDia()));
    }

    private void limpiar() {
        seleccionado = null;
        txtCodigo.setEnabled(true);
        cmbEspecie.setEnabled(true);
        txtCodigo.setText("");
        cmbEspecie.setSelectedIndex(0);
        cmbCategoria.setSelectedIndex(0);
        txtPrecio.setText("");
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            String codigo = Validaciones.texto(txtCodigo, "Código").toUpperCase();
            double precio = Validaciones.numero(txtPrecio, "Precio x día");
            Especie especie = (Especie) cmbEspecie.getSelectedItem();
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();

            if (seleccionado == null) {
                Alojamiento nuevo = new Alojamiento(sistema.proximoIdAlojamiento(), codigo, especie, categoria, precio);
                sistema.agregarAlojamiento(nuevo);
                JOptionPane.showMessageDialog(this, "Alojamiento agregado.");
            } else {
                seleccionado.setCategoria(categoria);
                seleccionado.setPrecioBaseDia(precio);
                sistema.guardarTodo();
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
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar el alojamiento " + seleccionado.getCodigo() + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.eliminarAlojamiento(seleccionado);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
