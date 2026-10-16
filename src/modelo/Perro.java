package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

public class Perro extends Mascota {
    private String raza;
    private String tamanio; // chico, mediano, grande

    public Perro(int idMascota, String nombre, LocalDate fechaNacimiento, double peso, Dueno dueno,
                 String raza, String tamanio) {
        super(idMascota, nombre, fechaNacimiento, peso, dueno);
        this.raza = raza;
        this.tamanio = tamanio;
    }

    @Override
    public Especie getEspecie() {
        return Especie.PERRO;
    }

    @Override
    public ArrayList<String> getVacunasObligatorias() {
        ArrayList<String> lista = new ArrayList<>();
        lista.add("Antirrábica");
        lista.add("Séxtuple");
        return lista;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getTamanio() {
        return tamanio;
    }

    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }
}
