package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

public class Conejo extends Mascota {
    private String raza;
    private String dieta;

    public Conejo(int idMascota, String nombre, LocalDate fechaNacimiento, double peso, Dueno dueno,
                  String raza, String dieta) {
        super(idMascota, nombre, fechaNacimiento, peso, dueno);
        this.raza = raza;
        this.dieta = dieta;
    }

    @Override
    public Especie getEspecie() {
        return Especie.CONEJO;
    }

    @Override
    public ArrayList<String> getVacunasObligatorias() {
        ArrayList<String> lista = new ArrayList<>();
        lista.add("Mixomatosis");
        lista.add("Hemorrágica viral");
        return lista;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getDieta() {
        return dieta;
    }

    public void setDieta(String dieta) {
        this.dieta = dieta;
    }
}
