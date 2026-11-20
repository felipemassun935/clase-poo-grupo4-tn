package modelo.mascota;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.enums.Especie;

// Herencia: un Ave es una Mascota.
// No tiene vacunas obligatorias: para entrar necesita un control veterinario del último año.
public class Ave extends Mascota {
    private static final int DIAS_CONTROL = 365;

    private String tipoAve;

    public Ave(Integer id, String nombre, LocalDate fechaNacimiento, double pesoKg, String observaciones,
            LocalDate fechaUltimoControl, String tipoAve) {
        super(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl);
        this.tipoAve = tipoAve;
    }

    @Override
    public Especie getEspecie() {
        return Especie.AVE;
    }

    // Polimorfismo: el ave se valida revisando que tenga un control de los últimos 365 días
    @Override
    public List<String> validarIngreso(LocalDate fecha) {
        List<String> problemas = new ArrayList<String>();
        if (!tieneControlVigente(fecha, DIAS_CONTROL)) {
            problemas.add("Necesita un control veterinario de los últimos " + DIAS_CONTROL + " días");
        }
        return problemas;
    }

    public String getTipoAve() {
        return tipoAve;
    }

    public void setTipoAve(String tipoAve) {
        this.tipoAve = tipoAve;
    }
}
