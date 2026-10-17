package vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
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
import modelo.enums.Especie;
import modelo.interfaces.Vacunable;
import modelo.mascota.Ave;
import modelo.mascota.Conejo;
import modelo.mascota.Gato;
import modelo.mascota.Mascota;
import modelo.mascota.Perro;
import modelo.persona.Cliente;

// ABM 2: alta, baja y modificación de mascotas
public class VentanaMascotas extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    private SistemaGuarderia sistema;
    private Mascota seleccionada; // null = se está dando de alta una nueva

    private JTextField txtId;
    // El combo muestra los nombres visibles en el mismo orden que Especie.values()
    private JComboBox<String> cmbEspecie;
    private JTextField txtNombre;
    private JTextField txtNacimiento;
    private JTextField txtPeso;
    private JComboBox<Cliente> cmbCliente;
    private JTextField txtObservaciones;
    private JTextField txtUltimoControl;
    private JTextField txtRaza;
    private JCheckBox chkCastrado;
    private JTextField txtTipoAve;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaMascotas() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("ABM de Mascotas");
        setSize(1150, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        txtId = agregarCampo("Id:", 20);
        txtId.setEditable(false); // el id lo asigna el sistema

        agregarEtiqueta("Especie:", 55);
        cmbEspecie = new JComboBox<String>();
        for (Especie e : Especie.values()) {
            cmbEspecie.addItem(e.getNombreVisible());
        }
        cmbEspecie.setBounds(140, 55, 200, 25);
        getContentPane().add(cmbEspecie);

        txtNombre = agregarCampo("Nombre:", 90);
        txtNacimiento = agregarCampo("Nacimiento:", 125);
        txtPeso = agregarCampo("Peso (kg):", 160);

        agregarEtiqueta("Cliente:", 195);
        cmbCliente = new JComboBox<Cliente>();
        for (Cliente c : sistema.getClientes()) {
            cmbCliente.addItem(c);
        }
        cmbCliente.setBounds(140, 195, 200, 25);
        getContentPane().add(cmbCliente);

        txtObservaciones = agregarCampo("Observaciones:", 230);
        txtUltimoControl = agregarCampo("Último control:", 265);
        txtRaza = agregarCampo("Raza:", 300);

        chkCastrado = new JCheckBox("Castrado");
        chkCastrado.setBounds(140, 335, 200, 25);
        getContentPane().add(chkCastrado);

        txtTipoAve = agregarCampo("Tipo de ave:", 370);

        JLabel lblFechas = new JLabel("Fechas en formato dd/mm/aaaa");
        lblFechas.setBounds(20, 400, 320, 20);
        getContentPane().add(lblFechas);

        JButton btnNuevo = new JButton("Nueva");
        btnNuevo.setBounds(20, 430, 100, 30);
        getContentPane().add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(130, 430, 100, 30);
        getContentPane().add(btnGuardar);

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.setBounds(240, 430, 100, 30);
        getContentPane().add(btnEliminar);

        JButton btnVacunas = new JButton("Vacunas...");
        btnVacunas.setBounds(20, 465, 320, 30);
        getContentPane().add(btnVacunas);

        modeloTabla = new DefaultTableModel(new String[] { "Id", "Nombre", "Especie", "Cliente", "Edad (meses)",
                "Peso", "Raza / tipo", "Último control", "Puede ingresar hoy" }, 0);
        tabla = new JTable(modeloTabla);
        tabla.setDefaultEditor(Object.class, null); // la tabla es solo para mirar, no se edita
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(360, 20, 760, 475);
        getContentPane().add(scroll);

        // Eventos (listeners con clases anónimas)
        cmbEspecie.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                habilitarCamposDeEspecie();
            }
        });
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
        btnVacunas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirVacunas();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                seleccionarFila();
            }
        });

        habilitarCamposDeEspecie();
        cargarTabla();
    }

    private void agregarEtiqueta(String texto, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setBounds(20, y, 120, 25);
        getContentPane().add(lbl);
    }

    private JTextField agregarCampo(String etiqueta, int y) {
        agregarEtiqueta(etiqueta, y);
        JTextField campo = new JTextField();
        campo.setBounds(140, y, 200, 25);
        getContentPane().add(campo);
        return campo;
    }

    // La posición elegida en el combo coincide con la posición en Especie.values()
    private Especie getEspecieElegida() {
        return Especie.values()[cmbEspecie.getSelectedIndex()];
    }

    // Habilita solo los campos propios de la especie elegida
    private void habilitarCamposDeEspecie() {
        Especie especie = getEspecieElegida();
        switch (especie) {
            case PERRO:
            case GATO:
                txtRaza.setEnabled(true);
                chkCastrado.setEnabled(true);
                txtTipoAve.setEnabled(false);
                break;
            case CONEJO:
                txtRaza.setEnabled(true);
                chkCastrado.setEnabled(false);
                txtTipoAve.setEnabled(false);
                break;
            case AVE:
                txtRaza.setEnabled(false);
                chkCastrado.setEnabled(false);
                txtTipoAve.setEnabled(true);
                break;
        }
    }

    // Devuelve la raza o el tipo de ave según la subclase (instanceof + cast)
    private String getDatoPropio(Mascota m) {
        String dato = "";
        if (m instanceof Perro) {
            Perro p = (Perro) m;
            dato = p.getRaza();
        } else if (m instanceof Gato) {
            Gato g = (Gato) m;
            dato = g.getRaza();
        } else if (m instanceof Conejo) {
            Conejo c = (Conejo) m;
            dato = c.getRaza();
        } else if (m instanceof Ave) {
            Ave a = (Ave) m;
            dato = a.getTipoAve();
        }
        return dato;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        LocalDate hoy = LocalDate.now();
        for (Mascota m : sistema.getMascotas()) {
            // Polimorfismo: cada especie valida su ingreso a su manera
            List<String> problemas = m.validarIngreso(hoy);
            String puedeIngresar = "No";
            if (problemas.isEmpty()) {
                puedeIngresar = "Sí";
            }
            modeloTabla.addRow(new Object[] { m.getId(), m.getNombre(), m.getEspecie().getNombreVisible(),
                    m.getCliente().getNombreCompleto(), m.getEdadEnMeses(), m.getPesoKg(), getDatoPropio(m),
                    Validaciones.formatearFecha(m.getFechaUltimoControl()), puedeIngresar });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        seleccionada = sistema.getMascotas().get(fila);
        txtId.setText("" + seleccionada.getId());
        cmbEspecie.setSelectedItem(seleccionada.getEspecie().getNombreVisible());
        txtNombre.setText(seleccionada.getNombre());
        txtNacimiento.setText(Validaciones.formatearFecha(seleccionada.getFechaNacimiento()));
        txtPeso.setText("" + seleccionada.getPesoKg());
        cmbCliente.setSelectedItem(seleccionada.getCliente());
        txtObservaciones.setText(seleccionada.getObservaciones());
        txtUltimoControl.setText(Validaciones.formatearFecha(seleccionada.getFechaUltimoControl()));

        txtRaza.setText("");
        txtTipoAve.setText("");
        chkCastrado.setSelected(false);
        if (seleccionada instanceof Ave) {
            txtTipoAve.setText(getDatoPropio(seleccionada));
        } else {
            txtRaza.setText(getDatoPropio(seleccionada));
        }
        if (seleccionada instanceof Perro) {
            Perro p = (Perro) seleccionada;
            chkCastrado.setSelected(p.isCastrado());
        } else if (seleccionada instanceof Gato) {
            Gato g = (Gato) seleccionada;
            chkCastrado.setSelected(g.isCastrado());
        }

        // La especie y el cliente no se pueden cambiar en una modificación
        cmbEspecie.setEnabled(false);
        cmbCliente.setEnabled(false);
    }

    private void limpiar() {
        seleccionada = null;
        txtId.setText("");
        txtNombre.setText("");
        txtNacimiento.setText("");
        txtPeso.setText("");
        txtObservaciones.setText("");
        txtUltimoControl.setText("");
        txtRaza.setText("");
        txtTipoAve.setText("");
        chkCastrado.setSelected(false);
        cmbEspecie.setEnabled(true);
        cmbCliente.setEnabled(true);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            Especie especie = getEspecieElegida();
            String nombre = Validaciones.texto(txtNombre.getText(), "Nombre");
            LocalDate nacimiento = Validaciones.parsearFecha(txtNacimiento.getText(), "Nacimiento");
            double peso = Validaciones.numero(txtPeso.getText(), "Peso");
            Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
            String observaciones = txtObservaciones.getText().trim();

            // El último control es opcional: si está vacío queda sin fecha
            LocalDate ultimoControl = null;
            if (!txtUltimoControl.getText().trim().isEmpty()) {
                ultimoControl = Validaciones.parsearFecha(txtUltimoControl.getText(), "Último control");
            }

            // Para el ave el dato propio es el tipo de ave; para las demás, la raza
            String datoPropio;
            if (especie == Especie.AVE) {
                datoPropio = Validaciones.texto(txtTipoAve.getText(), "Tipo de ave");
            } else {
                datoPropio = Validaciones.texto(txtRaza.getText(), "Raza");
            }
            boolean castrado = chkCastrado.isSelected();

            if (seleccionada == null) {
                sistema.altaMascota(especie, nombre, nacimiento, peso, observaciones, ultimoControl, cliente,
                        datoPropio, castrado);
                JOptionPane.showMessageDialog(this, "Mascota agregada.");
            } else {
                sistema.modificarMascota(seleccionada, nombre, nacimiento, peso, observaciones, ultimoControl,
                        datoPropio, castrado);
                JOptionPane.showMessageDialog(this, "Mascota modificada.");
            }
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dato inválido", JOptionPane.ERROR_MESSAGE);
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
            sistema.bajaMascota(seleccionada);
            cargarTabla();
            limpiar();
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Abre el diálogo modal de vacunas. Como es modal, el código sigue recién cuando se cierra.
    private void abrirVacunas() {
        if (seleccionada == null) {
            JOptionPane.showMessageDialog(this, "Primero elegí una mascota de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Interfaz: solo perros y gatos son Vacunable
        if (!(seleccionada instanceof Vacunable)) {
            JOptionPane.showMessageDialog(this, "Los animales de especie " + seleccionada.getEspecie().getNombreVisible()
                    + " no tienen vacunas obligatorias:\npara ingresar necesitan un control veterinario reciente.",
                    "Vacunas", JOptionPane.INFORMATION_MESSAGE);
        }
        DialogoVacunas dialogo = new DialogoVacunas(this, seleccionada);
        dialogo.setVisible(true);
        cargarTabla(); // la columna "Puede ingresar hoy" puede haber cambiado
        limpiar();
    }
}
