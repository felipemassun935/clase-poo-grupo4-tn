package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controlador.SistemaGuarderia;
import modelo.mascota.Mascota;
import modelo.persona.Empleado;

// Pantalla de consulta: busca en la bitácora por mascota o por empleado.
// Ejemplo: "¿qué empleado lavó a este perro?" o "¿a qué mascotas lavó este empleado?"
public class VentanaHistorico extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana

    private SistemaGuarderia sistema;
    private JComboBox<Mascota> cmbMascota;
    private JComboBox<Empleado> cmbEmpleado;
    private JTextArea txtResultado;

    public VentanaHistorico() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("Histórico de registros");
        setSize(1000, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(null);

        // ----- Búsqueda por mascota -----
        JLabel lblMascota = new JLabel("Mascota:");
        lblMascota.setBounds(20, 15, 80, 25);
        getContentPane().add(lblMascota);

        cmbMascota = new JComboBox<Mascota>();

        for (Mascota m : sistema.getMascotas()) {
            cmbMascota.addItem(m);
        }
        cmbMascota.setBounds(100, 15, 300, 25);
        getContentPane().add(cmbMascota);

        JButton btnPorMascota = new JButton("Ver registros de la mascota");
        btnPorMascota.setBounds(420, 15, 230, 25);
        getContentPane().add(btnPorMascota);

        // ----- Búsqueda por empleado -----
        JLabel lblEmpleado = new JLabel("Empleado:");
        lblEmpleado.setBounds(20, 50, 80, 25);
        getContentPane().add(lblEmpleado);

        cmbEmpleado = new JComboBox<Empleado>();

        for (Empleado e : sistema.getEmpleados()) {
            cmbEmpleado.addItem(e);
        }
        cmbEmpleado.setBounds(100, 50, 300, 25);
        getContentPane().add(cmbEmpleado);

        JButton btnPorEmpleado = new JButton("Ver registros del empleado");
        btnPorEmpleado.setBounds(420, 50, 230, 25);
        getContentPane().add(btnPorEmpleado);

        // ----- Resultado -----
        JLabel lblResultado = new JLabel("Resultado (un registro por línea)");
        lblResultado.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResultado.setBounds(20, 90, 500, 25);
        getContentPane().add(lblResultado);

        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setBounds(20, 120, 950, 380);
        getContentPane().add(scrollResultado);

        // Eventos (listeners con clases anónimas)
        btnPorMascota.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarPorMascota();
            }
        });
        btnPorEmpleado.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarPorEmpleado();
            }
        });
    }

    // Busca los registros de la mascota elegida
    private void buscarPorMascota() {
        Mascota m = (Mascota) cmbMascota.getSelectedItem();

        if (m == null) {
            txtResultado.setText("No hay mascotas cargadas.");
            return;
        }
        List<String> lineas = sistema.historicoDeMascota(m);
        mostrar(lineas, "Todavía no hay registros para " + m.getNombre() + ".");
    }

    // Busca los registros que cargó el empleado elegido
    private void buscarPorEmpleado() {
        Empleado e = (Empleado) cmbEmpleado.getSelectedItem();

        if (e == null) {
            txtResultado.setText("No hay empleados cargados.");
            return;
        }
        List<String> lineas = sistema.historicoDeEmpleado(e);
        mostrar(lineas, e.getNombreCompleto() + " todavía no cargó registros.");
    }

    // Arma el texto con una línea por registro. Si la lista está vacía, muestra el mensaje.
    private void mostrar(List<String> lineas, String mensajeVacio) {
        if (lineas.isEmpty()) {
            txtResultado.setText(mensajeVacio);
            return;
        }
        String texto = "";

        for (String linea : lineas) {
            texto = texto + linea + "\n";
        }
        txtResultado.setText(texto);
    }
}
