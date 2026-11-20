package modelo.mascota;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.enums.Especie;
import modelo.interfaces.Vacunable;

// Herencia: un Perro es una Mascota. Interfaz: es Vacunable (tiene vacunas obligatorias).
public class Perro extends Mascota implements Vacunable {
    private String raza;
    private boolean castrado;

    public Perro(Integer id, String nombre, LocalDate fechaNacimiento, double pesoKg, String observaciones,
            LocalDate fechaUltimoControl, String raza, boolean castrado) {
        super(id, nombre, fechaNacimiento, pesoKg, observaciones, fechaUltimoControl);
        this.raza = raza;
        this.castrado = castrado;
    }

    @Override
    public Especie getEspecie() {
        return Especie.PERRO;
    }

    // Vacunas que un perro tiene que tener al día para entrar
    @Override
    public List<String> getVacunasObligatorias() {
        List<String> obligatorias = new ArrayList<String>();
        obligatorias.add("Antirrábica");
        obligatorias.add("Séxtuple");
        return obligatorias;
    }

    // Polimorfismo: el perro se valida revisando sus vacunas obligatorias
    @Override
    public List<String> validarIngreso(LocalDate fecha) {
        List<String> problemas = new ArrayList<String>();
        for (String vacuna : getVacunasObligatorias()) {
            if (!tieneVacunaVigente(vacuna, fecha)) {
                problemas.add("Falta o venció la vacuna " + vacuna);
            }
        }
        return problemas;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public boolean isCastrado() {
        return castrado;
    }

    public void setCastrado(boolean castrado) {
        this.castrado = castrado;
    }
}
