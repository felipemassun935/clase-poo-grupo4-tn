package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

public class Ave extends Mascota {
    private String tipoAve; // loro, canario, cotorra...
    private boolean puedeVolar;

    public Ave(int idMascota, String nombre, LocalDate fechaNacimiento, double peso, Dueno dueno,
               String tipoAve, boolean puedeVolar) {
        super(idMascota, nombre, fechaNacimiento, peso, dueno);
        this.tipoAve = tipoAve;
        this.puedeVolar = puedeVolar;
    }

    @Override
    public Especie getEspecie() {
        return Especie.AVE;
    }

    // Las aves de compañía no tienen vacunas obligatorias: la lista vuelve vacía
    // y entonces tieneVacunasAlDia() siempre da true.
    @Override
    public ArrayList<String> getVacunasObligatorias() {
        return new ArrayList<>();
    }

    public String getTipoAve() {
        return tipoAve;
    }

    public void setTipoAve(String tipoAve) {
        this.tipoAve = tipoAve;
    }

    public boolean isPuedeVolar() {
        return puedeVolar;
    }

    public void setPuedeVolar(boolean puedeVolar) {
        this.puedeVolar = puedeVolar;
    }
}
