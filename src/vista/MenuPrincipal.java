package vista;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import controlador.SistemaGuarderia;
import modelo.Especie;
import modelo.Sucursal;

public class MenuPrincipal extends JFrame {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private SistemaGuarderia sistema;

    public MenuPrincipal() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("Guardería y Hotel de Mascotas");
        setSize(520, 470);
        setLocationRelativeTo(null); // centrada en la pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(null);

        JLabel lblTitulo = new JLabel("Guardería y Hotel de Mascotas");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setBounds(30, 20, 460, 30);
        getContentPane().add(lblTitulo);

        JLabel lblSucursal = new JLabel(sistema.getSucursal().getNombre() + " - " + sistema.getSucursal().getDireccion());
        lblSucursal.setBounds(30, 52, 460, 20);
        getContentPane().add(lblSucursal);

        // ABM (alta, baja y modificación)
        JLabel lblAbm = new JLabel("Administración");
        lblAbm.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAbm.setBounds(30, 90, 200, 20);
        getContentPane().add(lblAbm);

        JButton btnDuenos = crearBoton("Dueños", 30, 120);
        JButton btnMascotas = crearBoton("Mascotas", 260, 120);
        JButton btnEmpleados = crearBoton("Empleados", 30, 170);
        JButton btnAlojamientos = crearBoton("Alojamientos", 260, 170);

        // Pantallas de negocio
        JLabel lblNegocio = new JLabel("Operación diaria");
        lblNegocio.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNegocio.setBounds(30, 230, 200, 20);
        getContentPane().add(lblNegocio);

        JButton btnReservas = crearBoton("Reservas", 30, 260);
        JButton btnEstadias = crearBoton("Check-in / Estadías", 260, 260);
        JButton btnOcupacion = crearBoton("Ver ocupación", 30, 310);
        JButton btnSalir = crearBoton("Salir", 260, 310);

        // Cada botón abre su ventana
        btnDuenos.addActionListener(e -> new VentanaDuenos().setVisible(true));
        btnMascotas.addActionListener(e -> new VentanaMascotas().setVisible(true));
        btnEmpleados.addActionListener(e -> new VentanaEmpleados().setVisible(true));
        btnAlojamientos.addActionListener(e -> new VentanaAlojamientos().setVisible(true));
        btnReservas.addActionListener(e -> new VentanaReservas().setVisible(true));
        btnEstadias.addActionListener(e -> new VentanaEstadias().setVisible(true));
        btnOcupacion.addActionListener(e -> mostrarOcupacion());
        btnSalir.addActionListener(e -> salir());

        JLabel lblPie = new JLabel("TPO Paradigma Orientado a Objetos - UADE 2026");
        lblPie.setBounds(30, 380, 460, 20);
        getContentPane().add(lblPie);
    }

    private JButton crearBoton(String texto, int x, int y) {
        JButton boton = new JButton(texto);
        boton.setBounds(x, y, 210, 38);
        getContentPane().add(boton);
        return boton;
    }

    // Control de ocupación: porcentaje general y por especie
    private void mostrarOcupacion() {
        Sucursal s = sistema.getSucursal();
        String texto = "Ocupación general: " + String.format("%.0f", s.calcularOcupacion()) + "%\n\n";
        for (Especie esp : Especie.values()) {
            texto = texto + esp + ": " + String.format("%.0f", s.calcularOcupacion(esp)) + "%\n";
        }
        texto = texto + "\nMascotas alojadas ahora: " + sistema.getEstadiasActivas().size();
        JOptionPane.showMessageDialog(this, texto, "Control de ocupación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que querés salir?", "Salir",
                JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            sistema.guardarTodo();
            System.exit(0);
        }
    }
}
