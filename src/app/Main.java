package app;

import javax.swing.UIManager;

import vista.MenuPrincipal;

// Punto de entrada del programa: aplica el estilo visual y abre el menú principal
public class Main {
    public static void main(String[] args) {
        // try/catch: si el estilo "Nimbus" no está disponible, se sigue con el de por defecto
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el estilo Nimbus, se usa el de por defecto.");
        }

        MenuPrincipal menu = new MenuPrincipal();
        menu.setVisible(true);
    }
}
