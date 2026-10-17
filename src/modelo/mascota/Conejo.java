package modelo.mascota;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.enums.Especie;

// Herencia: un Conejo es una Mascota.
// No tiene vacunas obligatorias: para entrar necesita un control veterinario reciente.
public class Conejo extends Mascota {
    private static final int DIAS_CONTROL = 180;

    private String raza;

    public Conejo(Integer id, String nombre, LocalDate fechaNacimiento, double pesoKg, String observaciones,
            LocalDate fechaUltimoControl, String raza) {
        super(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl);
        this.raza = raza;
    }

    @Override
    public Especie getEspecie() {
        return Especie.CONEJO;
    }

    // Polimorfismo: el conejo se valida revisando que tenga un control de los últimos 180 días
    @Override
    public List<String> validarIngreso(LocalDate fecha) {
        List<String> problemas = new ArrayList<String>();
        if (!tieneControlVigente(fecha, DIAS_CONTROL)) {
            problemas.add("Necesita un control veterinario de los últimos " + DIAS_CONTROL + " días");
        }
        return problemas;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }
}
