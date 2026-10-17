package modelo.interfaces;

import java.util.List;

// Interfaz: la implementan las mascotas que tienen vacunas obligatorias (Perro y Gato)
public interface Vacunable {

    // Nombres de las vacunas que la mascota tiene que tener vigentes para entrar
    List<String> getVacunasObligatorias();
}
