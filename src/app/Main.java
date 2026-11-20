package app;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controlador.SistemaGuarderia;
import excepciones.DatoInvalidoException;
import excepciones.ErrorBaseDeDatosException;
import vista.MenuPrincipal;

// Punto de entrada del programa: aplica el estilo visual, se conecta a la base, pide el legajo y abre el menú
public class Main {
    public static void main(String[] args) {
        // try/catch: si el estilo "Nimbus" no está disponible, se sigue con el de por defecto
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el estilo Nimbus, se usa el de por defecto.");
        }

        // Regla del EDT: la interfaz se crea dentro del hilo de eventos de Swing, con invokeLater.
        // Runnable tiene un solo método, por eso se puede pasar una lambda.
        SwingUtilities.invokeLater(() -> iniciar());
    }

    private static void iniciar() {
        SistemaGuarderia sistema;
        // try/catch: si no se puede conectar a SQL Server (servidor apagado, usuario mal, falta el driver),
        // se avisa con un cartel y el programa termina
        try {
            sistema = SistemaGuarderia.getInstancia();
        } catch (ErrorBaseDeDatosException e) {
            JOptionPane.showMessageDialog(null, e.getMessage()
                    + "\n\nRevisá que SQL Server esté encendido, los datos de conexion.txt y que el driver"
                    + "\nmssql-jdbc esté en la carpeta lib (ver LEEME).", "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (pedirLegajo(sistema)) {
            new MenuPrincipal().setVisible(true);
        }
    }

    // Pide el legajo hasta que sea válido. Devuelve false si el usuario apretó "Cancelar".
    // Es public static para usarlo también desde el menú "Cambiar de usuario".
    public static boolean pedirLegajo(SistemaGuarderia sistema) {
        while (true) {
            String legajo = JOptionPane.showInputDialog(null, "Ingresá tu legajo de empleado:",
                    "Ingreso al sistema", JOptionPane.QUESTION_MESSAGE);
            if (legajo == null) {
                return false;
            }
            try {
                sistema.iniciarSesion(legajo);
                return true;
            } catch (DatoInvalidoException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Legajo inválido", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
