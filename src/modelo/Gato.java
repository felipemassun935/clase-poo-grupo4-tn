package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

public class Gato extends Mascota {
    private String raza;
    private boolean esCastrado;

    public Gato(int idMascota, String nombre, LocalDate fechaNacimiento, double peso, Dueno dueno,
                String raza, boolean esCastrado) {
        super(idMascota, nombre, fechaNacimiento, peso, dueno);
        this.raza = raza;
        this.esCastrado = esCastrado;
    }

    @Override
    public Especie getEspecie() {
        return Especie.GATO;
    }

    @Override
    public ArrayList<String> getVacunasObligatorias() {
        ArrayList<String> lista = new ArrayList<>();
        lista.add("Antirrábica");
        lista.add("Triple felina");
        return lista;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public boolean isEsCastrado() {
        return esCastrado;
    }

    public void setEsCastrado(boolean esCastrado) {
        this.esCastrado = esCastrado;
    }
}
