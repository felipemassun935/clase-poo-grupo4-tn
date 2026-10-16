package excepciones;

import java.util.ArrayList;

import modelo.Mascota;

// Se lanza en el check-in si la mascota no tiene las vacunas obligatorias al día
public class VacunacionVencidaException extends Exception {
    private static final long serialVersionUID = 1L; // lo pide Swing/Exception, Eclipse avisa si falta
    private ArrayList<String> vacunasFaltantes;

    public VacunacionVencidaException(Mascota m, ArrayList<String> faltantes) {
        super("No se puede hacer el check-in de " + m.getNombre()
                + ". Le faltan o tiene vencidas estas vacunas: " + String.join(", ", faltantes));
        this.vacunasFaltantes = faltantes;
    }

    public ArrayList<String> getVacunasFaltantes() {
        return vacunasFaltantes;
    }
}
