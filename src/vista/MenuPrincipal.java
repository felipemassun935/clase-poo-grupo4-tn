package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import controlador.SistemaGuarderia;
import modelo.enums.Especie;
import modelo.sucursal.Sucursal;

// Ventana principal: tiene un botón para abrir cada pantalla del sistema
public class MenuPrincipal extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
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

        JLabel lblSucursales = new JLabel("Sucursales: " + sistema.getSucursales().size());
        lblSucursales.setBounds(30, 52, 460, 20);
        getContentPane().add(lblSucursales);

        // ABM (alta, baja y modificación)
        JLabel lblAbm = new JLabel("Administración");
        lblAbm.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAbm.setBounds(30, 90, 200, 20);
        getContentPane().add(lblAbm);

        JButton btnClientes = crearBoton("Clientes", 30, 120);
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

        JLabel lblPie = new JLabel("TPO Paradigma Orientado a Objetos - UADE 2026");
        lblPie.setBounds(30, 380, 460, 20);
        getContentPane().add(lblPie);

        // Eventos: cada botón abre su ventana (listeners con clases anónimas)
        btnClientes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VentanaClientes().setVisible(true);
            }
        });
        btnMascotas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VentanaMascotas().setVisible(true);
            }
        });
        btnEmpleados.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VentanaEmpleados().setVisible(true);
            }
        });
        btnAlojamientos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VentanaAlojamientos().setVisible(true);
            }
        });
        btnReservas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VentanaReservas().setVisible(true);
            }
        });
        btnEstadias.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new VentanaEstadias().setVisible(true);
            }
        });
        btnOcupacion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarOcupacion();
            }
        });
        btnSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salir();
            }
        });
    }

    private JButton crearBoton(String texto, int x, int y) {
        JButton boton = new JButton(texto);
        boton.setBounds(x, y, 210, 38);
        getContentPane().add(boton);
        return boton;
    }

    // Control de ocupación: para cada sucursal, "ocupados/capacidad" de cada especie
    private void mostrarOcupacion() {
        String texto = "";
        for (Sucursal s : sistema.getSucursales()) {
            texto = texto + s.getNombre() + "\n";
            Map<Especie, String> ocupacion = sistema.getOcupacion(s);
            for (Especie e : Especie.values()) {
                texto = texto + "   " + e.getNombreVisible() + ": " + ocupacion.get(e) + "\n";
            }
            texto = texto + "\n";
        }
        texto = texto + "(alojados ahora / alojamientos activos)";
        JOptionPane.showMessageDialog(this, texto, "Control de ocupación", JOptionPane.INFORMATION_MESSAGE);
    }

    // Los datos ya se guardan después de cada cambio, así que solo hay que cerrar
    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que querés salir?", "Salir",
                JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
