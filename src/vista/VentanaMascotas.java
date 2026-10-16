package vista;

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
import javax.swing.table.DefaultTableModel;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.Ave;
import modelo.Conejo;
import modelo.Dueno;
import modelo.Especie;
import modelo.Gato;
import modelo.Mascota;
import modelo.MascotaFactory;
import modelo.Perro;
import util.Fechas;

// ABM de mascotas. Desde acá también se abre el diálogo para cargar las vacunas.
public class VentanaMascotas extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;
    private Mascota seleccionada;

    private JComboBox<Especie> cmbEspecie;
    private JTextField txtNombre;
    private JTextField txtFechaNac;
    private JTextField txtPeso;
    private JComboBox<Dueno> cmbDueno;
    private JLabel lblDato1;
    private JTextField txtDato1;
    private JLabel lblDato2;
    private JTextField txtDato2;
    private JCheckBox chkMarca;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaMascotas() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Mascotas");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel lblEspecie = new JLabel("Especie:");
        lblEspecie.setBounds(20, 20, 90, 25);
        getContentPane().add(lblEspecie);
        cmbEspecie = new JComboBox<>(Especie.values());
        cmbEspecie.setBounds(110, 20, 200, 25);
        getContentPane().add(cmbEspecie);

        txtNombre = agregarCampo("Nombre:", 60);
        txtFechaNac = agregarCampo("Nacimiento:", 100);
        txtFechaNac.setToolTipText("dd/mm/aaaa");
        txtPeso = agregarCampo("Peso (kg):", 140);

        JLabel lblDueno = new JLabel("Dueño:");
        lblDueno.setBounds(20, 180, 90, 25);
        getContentPane().add(lblDueno);
        cmbDueno = new JComboBox<>();
        for (Dueno d : sistema.getDuenos()) {
            cmbDueno.addItem(d);
        }
        cmbDueno.setBounds(110, 180, 200, 25);
        getContentPane().add(cmbDueno);

        // Estos campos cambian de nombre según la especie
        lblDato1 = new JLabel("Raza:");
        lblDato1.setBounds(20, 220, 90, 25);
        getContentPane().add(lblDato1);
        txtDato1 = new JTextField();
        txtDato1.setBounds(110, 220, 200, 25);
        getContentPane().add(txtDato1);

        lblDato2 = new JLabel("Tamaño:");
        lblDato2.setBounds(20, 260, 90, 25);
        getContentPane().add(lblDato2);
        txtDato2 = new JTextField();
        txtDato2.setBounds(110, 260, 200, 25);
        getContentPane().add(txtDato2);

        chkMarca = new JCheckBox("Castrado");
        chkMarca.setBounds(110, 295, 200, 25);
        getContentPane().add(chkMarca);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 340, 90, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(115, 340, 90, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(210, 340, 100, 30);
        getContentPane().add(btnEliminar);

        JButton btnVacunas = new JButton("Vacunas de la mascota...");
        btnVacunas.setBounds(20, 385, 290, 30);
        getContentPane().add(btnVacunas);

        modeloTabla = new DefaultTableModel(new String[] { "ID", "Nombre", "Especie", "Dueño", "Nacimiento",
                "Peso", "Vacunas al día" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(330, 20, 640, 420);
        getContentPane().add(scroll);

        cmbEspecie.addActionListener(e -> actualizarCamposEspecie());
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnVacunas.addActionListener(e -> abrirVacunas());
        tabla.getSelectionModel().addListSelectionListener(e -> seleccionarFila());

        actualizarCamposEspecie();
        cargarTabla();
    }

    private JTextField agregarCampo(String etiqueta, int y) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setBounds(20, y, 90, 25);
        getContentPane().add(lbl);
        JTextField campo = new JTextField();
        campo.setBounds(110, y, 200, 25);
        getContentPane().add(campo);
        return campo;
    }

    // Cambia los textos y habilita/deshabilita campos según la especie elegida
    private void actualizarCamposEspecie() {
        Especie especie = (Especie) cmbEspecie.getSelectedItem();
        switch (especie) {
            case PERRO:
                lblDato1.setText("Raza:");
                lblDato2.setText("Tamaño:");
                txtDato2.setEnabled(true);
                chkMarca.setEnabled(false);
                chkMarca.setText("-");
                break;
            case GATO:
                lblDato1.setText("Raza:");
                lblDato2.setText("-");
                txtDato2.setEnabled(false);
                chkMarca.setEnabled(true);
                chkMarca.setText("Castrado");
                break;
            case CONEJO:
                lblDato1.setText("Raza:");
                lblDato2.setText("Dieta:");
                txtDato2.setEnabled(true);
                chkMarca.setEnabled(false);
                chkMarca.setText("-");
                break;
            case AVE:
                lblDato1.setText("Tipo de ave:");
                lblDato2.setText("-");
                txtDato2.setEnabled(false);
                chkMarca.setEnabled(true);
                chkMarca.setText("Puede volar");
                break;
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Mascota m : sistema.getMascotas()) {
            String alDia = "No";
            if (m.tieneVacunasAlDia(LocalDate.now())) {
                alDia = "Sí";
            }
            modeloTabla.addRow(new Object[] { m.getIdMascota(), m.getNombre(), m.getEspecie(),
                    m.getDueno().getNombreCompleto(), Fechas.formatear(m.getFechaNacimiento()),
                    m.getPeso() + " kg", alDia });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionada = sistema.getMascotas().get(fila);
        cmbEspecie.setSelectedItem(seleccionada.getEspecie());
        cmbEspecie.setEnabled(false); // un perro no se convierte en gato
        txtNombre.setText(seleccionada.getNombre());
        txtFechaNac.setText(Fechas.formatear(seleccionada.getFechaNacimiento()));
        txtPeso.setText(String.valueOf(seleccionada.getPeso()));
        cmbDueno.setSelectedItem(seleccionada.getDueno());
        txtDato2.setText("");
        chkMarca.setSelected(false);

        // Datos propios de cada subclase
        if (seleccionada instanceof Perro) {
            txtDato1.setText(((Perro) seleccionada).getRaza());
            txtDato2.setText(((Perro) seleccionada).getTamanio());
        } else if (seleccionada instanceof Gato) {
            txtDato1.setText(((Gato) seleccionada).getRaza());
            chkMarca.setSelected(((Gato) seleccionada).isEsCastrado());
        } else if (seleccionada instanceof Conejo) {
            txtDato1.setText(((Conejo) seleccionada).getRaza());
            txtDato2.setText(((Conejo) seleccionada).getDieta());
        } else if (seleccionada instanceof Ave) {
            txtDato1.setText(((Ave) seleccionada).getTipoAve());
            chkMarca.setSelected(((Ave) seleccionada).isPuedeVolar());
        }
    }

    private void limpiar() {
        seleccionada = null;
        cmbEspecie.setEnabled(true);
        txtNombre.setText("");
        txtFechaNac.setText("");
        txtPeso.setText("");
        txtDato1.setText("");
        txtDato2.setText("");
        chkMarca.setSelected(false);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            String nombre = Validaciones.texto(txtNombre, "Nombre");
            LocalDate fechaNac = Fechas.parsear(Validaciones.texto(txtFechaNac, "Nacimiento"));
            if (fechaNac.isAfter(LocalDate.now())) {
                throw new DatoInvalidoException("La fecha de nacimiento no puede ser futura.");
            }
            double peso = Validaciones.numero(txtPeso, "Peso");
            Dueno dueno = (Dueno) cmbDueno.getSelectedItem();
            if (dueno == null) {
                throw new DatoInvalidoException("Primero hay que cargar al dueño en el ABM de Dueños.");
            }
            String dato1 = txtDato1.getText().trim();
            String dato2 = txtDato2.getText().trim();
            boolean marca = chkMarca.isSelected();

            if (seleccionada == null) {
                Especie especie = (Especie) cmbEspecie.getSelectedItem();
                Mascota nueva = MascotaFactory.crear(especie, sistema.proximoIdMascota(), nombre, fechaNac,
                        peso, dueno, dato1, dato2, marca);
                sistema.agregarMascota(nueva);
                JOptionPane.showMessageDialog(this, nombre + " fue agregado. Acordate de cargarle las vacunas.");
            } else {
                seleccionada.setNombre(nombre);
                seleccionada.setFechaNacimiento(fechaNac);
                seleccionada.setPeso(peso);
                if (seleccionada.getDueno() != dueno) {
                    sistema.cambiarDueno(seleccionada, dueno);
                }
                actualizarDatosEspecie(seleccionada, dato1, dato2, marca);
                sistema.guardarTodo();
                JOptionPane.showMessageDialog(this, "Mascota modificada.");
            }
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarDatosEspecie(Mascota m, String dato1, String dato2, boolean marca) {
        if (m instanceof Perro) {
            ((Perro) m).setRaza(dato1);
            ((Perro) m).setTamanio(dato2);
        } else if (m instanceof Gato) {
            ((Gato) m).setRaza(dato1);
            ((Gato) m).setEsCastrado(marca);
        } else if (m instanceof Conejo) {
            ((Conejo) m).setRaza(dato1);
            ((Conejo) m).setDieta(dato2);
        } else if (m instanceof Ave) {
            ((Ave) m).setTipoAve(dato1);
            ((Ave) m).setPuedeVolar(marca);
        }
    }

    private void eliminar() {
        if (seleccionada == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí una mascota de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar a " + seleccionada.getNombre() + "?",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.eliminarMascota(seleccionada);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirVacunas() {
        if (seleccionada == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí una mascota de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        DialogoVacunas dialogo = new DialogoVacunas(this, seleccionada);
        dialogo.setVisible(true); // es modal: el código sigue cuando se cierra el diálogo
        cargarTabla();
        limpiar();
    }
}
