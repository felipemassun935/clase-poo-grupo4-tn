package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

// Clase abstracta: cada especie dice cuáles son sus vacunas obligatorias
public abstract class Mascota {
    protected int idMascota;
    protected String nombre;
    protected LocalDate fechaNacimiento;
    protected double peso;
    protected Dueno dueno;
    protected ArrayList<Vacuna> vacunas; // composición: las vacunas son de esta mascota

    public Mascota(int idMascota, String nombre, LocalDate fechaNacimiento, double peso, Dueno dueno) {
        this.idMascota = idMascota;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.peso = peso;
        this.dueno = dueno;
        this.vacunas = new ArrayList<>();
    }

    // Métodos abstractos: cada subclase los implementa a su manera (polimorfismo)
    public abstract Especie getEspecie();

    public abstract ArrayList<String> getVacunasObligatorias();

    public void agregarVacuna(Vacuna v) {
        vacunas.add(v);
    }

    public void quitarVacuna(Vacuna v) {
        vacunas.remove(v);
    }

    // Devuelve los nombres de las vacunas obligatorias que no tiene vigentes en esa fecha
    public ArrayList<String> getVacunasFaltantes(LocalDate fecha) {
        ArrayList<String> faltantes = new ArrayList<>();
        for (String obligatoria : getVacunasObligatorias()) {
            boolean laTiene = false;
            for (Vacuna v : vacunas) {
                if (v.getNombre().equalsIgnoreCase(obligatoria) && v.estaVigente(fecha)) {
                    laTiene = true;
                }
            }
            if (!laTiene) {
                faltantes.add(obligatoria);
            }
        }
        return faltantes;
    }

    public boolean tieneVacunasAlDia(LocalDate fecha) {
        return getVacunasFaltantes(fecha).isEmpty();
    }

    // Getters y setters
    public int getIdMascota() {
        return idMascota;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public Dueno getDueno() {
        return dueno;
    }

    public void setDueno(Dueno dueno) {
        this.dueno = dueno;
    }

    public ArrayList<Vacuna> getVacunas() {
        return vacunas;
    }

    @Override
    public String toString() {
        return nombre + " (" + getEspecie() + ") - " + dueno.getNombreCompleto();
    }
}
