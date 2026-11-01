package vista;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import app.Main;
import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import modelo.enums.Especie;
import modelo.enums.EstadoReserva;
import modelo.mascota.Mascota;
import modelo.persona.Empleado;
import modelo.reserva.Reserva;
import modelo.sucursal.Sucursal;

// Ventana principal: barra de menú (JMenuBar) y botones para abrir cada pantalla del sistema
public class MenuPrincipal extends JFrame {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una ventana
    private SistemaGuarderia sistema;
    private JLabel lblOperador;

    public MenuPrincipal() {
        sistema = SistemaGuarderia.getInstancia();

        setTitle("Guardería y Hotel de Mascotas");
        setSize(520, 500);
        setLocationRelativeTo(null); // centrada en la pantalla
        // Al cerrar con la X no sale directo: lo maneja el WindowAdapter de abajo (pregunta antes)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(null);

        crearMenu();

        JLabel lblTitulo = new JLabel("Guardería y Hotel de Mascotas");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setBounds(30, 15, 460, 30);
        getContentPane().add(lblTitulo);

        lblOperador = new JLabel();
        lblOperador.setBounds(30, 47, 460, 20);
        getContentPane().add(lblOperador);
        actualizarOperador();

        // ABM (alta, baja y modificación)
        JLabel lblAbm = new JLabel("Administración");
        lblAbm.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAbm.setBounds(30, 85, 200, 20);
        getContentPane().add(lblAbm);

        JButton btnClientes = crearBoton("Clientes", 30, 115);
        JButton btnMascotas = crearBoton("Mascotas", 260, 115);
        JButton btnEmpleados = crearBoton("Empleados", 30, 165);
        JButton btnAlojamientos = crearBoton("Alojamientos", 260, 165);

        // Pantallas de negocio
        JLabel lblNegocio = new JLabel("Operación diaria");
        lblNegocio.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNegocio.setBounds(30, 225, 200, 20);
        getContentPane().add(lblNegocio);

        JButton btnReservas = crearBoton("Reservas", 30, 255);
        JButton btnEstadias = crearBoton("Check-in / Estadías", 260, 255);
        JButton btnOcupacion = crearBoton("Ver ocupación", 30, 305);
        JButton btnLog = crearBoton("Log del sistema", 260, 305);
        JButton btnCollar = crearBoton("Buscar por collar", 30, 355);
        JButton btnSalir = crearBoton("Salir", 260, 355);

        JLabel lblPie = new JLabel("TPO Paradigma Orientado a Objetos - UADE 2026");
        lblPie.setBounds(30, 410, 460, 20);
        getContentPane().add(lblPie);

        // Eventos de los botones: listeners con clases anónimas
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
        btnLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirLog();
            }
        });
        btnCollar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarPorCollar();
            }
        });
        btnSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                salir();
            }
        });

        // WindowAdapter: de WindowListener solo interesa windowClosing, el Adapter deja los otros vacíos
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
    }

    // Barra de menú: JMenuBar -> JMenu -> JMenuItem. Cada JMenuItem genera un ActionEvent.
    // Acá los listeners se escriben con lambdas (la tercera forma de escribir un oyente).
    private void crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemCambiarUsuario = new JMenuItem("Cambiar de usuario");
        JMenuItem itemSalir = new JMenuItem("Salir");
        // Atajo de teclado: Ctrl + Q
        itemSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemCambiarUsuario.addActionListener(e -> cambiarUsuario());
        itemSalir.addActionListener(e -> salir());
        menuArchivo.add(itemCambiarUsuario);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        JMenu menuAdmin = new JMenu("Administración");
        JMenuItem itemClientes = new JMenuItem("Clientes");
        JMenuItem itemMascotas = new JMenuItem("Mascotas");
        JMenuItem itemEmpleados = new JMenuItem("Empleados");
        JMenuItem itemAlojamientos = new JMenuItem("Alojamientos");
        itemClientes.addActionListener(e -> new VentanaClientes().setVisible(true));
        itemMascotas.addActionListener(e -> new VentanaMascotas().setVisible(true));
        itemEmpleados.addActionListener(e -> new VentanaEmpleados().setVisible(true));
        itemAlojamientos.addActionListener(e -> new VentanaAlojamientos().setVisible(true));
        menuAdmin.add(itemClientes);
        menuAdmin.add(itemMascotas);
        menuAdmin.add(itemEmpleados);
        menuAdmin.add(itemAlojamientos);

        JMenu menuOperacion = new JMenu("Operación");
        JMenuItem itemReservas = new JMenuItem("Reservas");
        JMenuItem itemEstadias = new JMenuItem("Check-in / Estadías");
        JMenuItem itemOcupacion = new JMenuItem("Ver ocupación");
        JMenuItem itemCollar = new JMenuItem("Buscar mascota por collar...");
        itemReservas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        itemEstadias.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        itemCollar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        itemReservas.addActionListener(e -> new VentanaReservas().setVisible(true));
        itemEstadias.addActionListener(e -> new VentanaEstadias().setVisible(true));
        itemOcupacion.addActionListener(e -> mostrarOcupacion());
        itemCollar.addActionListener(e -> buscarPorCollar());
        menuOperacion.add(itemReservas);
        menuOperacion.add(itemEstadias);
        menuOperacion.add(itemOcupacion);
        menuOperacion.addSeparator();
        menuOperacion.add(itemCollar);

        JMenu menuConsultas = new JMenu("Consultas");
        JMenuItem itemLog = new JMenuItem("Log del sistema");
        itemLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        itemLog.addActionListener(e -> abrirLog());
        menuConsultas.add(itemLog);

        barra.add(menuArchivo);
        barra.add(menuAdmin);
        barra.add(menuOperacion);
        barra.add(menuConsultas);
        setJMenuBar(barra);
    }

    private JButton crearBoton(String texto, int x, int y) {
        JButton boton = new JButton(texto);
        boton.setBounds(x, y, 210, 38);
        getContentPane().add(boton);
        return boton;
    }

    private void actualizarOperador() {
        Empleado op = sistema.getOperador();
        lblOperador.setText("Operando: " + op.getNombreCompleto() + " (" + op.getRol() + ", "
                + op.getSucursal().getNombre() + ")");
    }

    private void cambiarUsuario() {
        if (Main.pedirLegajo(sistema)) {
            actualizarOperador();
        }
    }

    // Solo los encargados ven el log
    private void abrirLog() {
        if (!sistema.puedeVerLog()) {
            JOptionPane.showMessageDialog(this, "Solo los encargados de sucursal pueden ver el log del sistema.",
                    "Sin permiso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new VentanaLog().setVisible(true);
    }

    // Busca por el código del collar y muestra la ficha de la mascota con su estadía actual (si tiene)
    private void buscarPorCollar() {
        String codigo = JOptionPane.showInputDialog(this, "Código del collar (por ejemplo COL-00001 o 1):",
                "Buscar por collar", JOptionPane.QUESTION_MESSAGE);
        if (codigo == null) {
            return;
        }
        try {
            Mascota m = sistema.buscarPorCollar(codigo);
            String texto = m.getCodigoCollar() + " - " + m.getNombre() + " (" + m.getEspecie().getNombreVisible()
                    + ")\nDueño: " + m.getCliente().getNombreCompleto() + " - Tel. " + m.getCliente().getTelefono()
                    + "\nObservaciones: " + m.getObservaciones() + "\n\n";
            String estadia = "No está alojada en este momento.";
            for (Reserva r : sistema.getReservas()) {
                if (r.getMascota() == m && r.getEstado() == EstadoReserva.EN_CURSO) {
                    estadia = "Alojada en " + r.getAlojamiento().getCodigo() + " ("
                            + r.getAlojamiento().getSucursal().getNombre() + ") hasta el "
                            + Validaciones.formatearFecha(r.getFechaHasta()) + ".\nIndicaciones del dueño: "
                            + r.getIndicaciones().size() + ". Servicios: " + r.getCantidadServiciosRealizados()
                            + " de " + r.getServicios().size() + " realizados.";
                }
            }
            JOptionPane.showMessageDialog(this, texto + estadia, "Mascota " + m.getCodigoCollar(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Collar no encontrado", JOptionPane.WARNING_MESSAGE);
        }
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

    // Los datos ya se guardan en la base después de cada cambio, así que solo hay que cerrar
    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que querés salir?", "Salir",
                JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
