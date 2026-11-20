package excepciones;

import java.util.List;

// Excepción propia (checked): se lanza en el check-in cuando la mascota no cumple
// los requisitos para entrar (vacunas vencidas o falta de control veterinario).
public class IngresoRechazadoException extends Exception {
    private static final long serialVersionUID = 1L; // Eclipse avisa si falta en una Exception

    private List<String> problemas;

    public IngresoRechazadoException(List<String> problemas) {
        super(armarMensaje(problemas));
        this.problemas = problemas;
    }

    // Junta todos los problemas en un solo texto, uno por línea.
    // Es static porque se usa dentro de super(...), antes de que exista el objeto.
    private static String armarMensaje(List<String> problemas) {
        String mensaje = "";
        for (String problema : problemas) {
            mensaje = mensaje + problema + "\n";
        }
        return mensaje;
    }

    public List<String> getProblemas() {
        return problemas;
    }
}
