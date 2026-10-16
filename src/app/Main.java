package app;

import javax.swing.UIManager;

import vista.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        // Estilo visual "Nimbus" de Swing. Si no está disponible, queda el de por defecto.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el estilo Nimbus, se usa el de por defecto.");
        }

        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true);
    }
}
