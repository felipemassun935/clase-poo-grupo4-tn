package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

public class Sucursal {
    private int idSucursal;
    private String nombre;
    private String direccion;
    private String telefono;
    private ArrayList<Alojamiento> alojamientos; // composición

    public Sucursal(int idSucursal, String nombre, String direccion, String telefono) {
        this.idSucursal = idSucursal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.alojamientos = new ArrayList<>();
    }

    public void agregarAlojamiento(Alojamiento a) {
        alojamientos.add(a);
    }

    public void eliminarAlojamiento(Alojamiento a) {
        alojamientos.remove(a);
    }

    // Alojamientos de la especie pedida que están libres en esas fechas
    public ArrayList<Alojamiento> buscarDisponibles(Especie especie, LocalDate desde, LocalDate hasta) {
        ArrayList<Alojamiento> disponibles = new ArrayList<>();
        for (Alojamiento a : alojamientos) {
            if (a.getEspecie() == especie && a.estaDisponible(desde, hasta)) {
                disponibles.add(a);
            }
        }
        return disponibles;
    }

    // Porcentaje de alojamientos ocupados ahora mismo
    public double calcularOcupacion() {
        if (alojamientos.isEmpty()) {
            return 0;
        }
        int ocupados = 0;
        for (Alojamiento a : alojamientos) {
            if (a.estaOcupado()) {
                ocupados++;
            }
        }
        return ocupados * 100.0 / alojamientos.size();
    }

    // Sobrecarga: el mismo cálculo pero solo para una especie
    public double calcularOcupacion(Especie especie) {
        int total = 0;
        int ocupados = 0;
        for (Alojamiento a : alojamientos) {
            if (a.getEspecie() == especie) {
                total++;
                if (a.estaOcupado()) {
                    ocupados++;
                }
            }
        }
        if (total == 0) {
            return 0;
        }
        return ocupados * 100.0 / total;
    }

    public int getIdSucursal() {
        return idSucursal;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public ArrayList<Alojamiento> getAlojamientos() {
        return alojamientos;
    }
}
