package modelo;

import java.time.LocalDate;

public class Vacuna {
    private String nombre;
    private LocalDate fechaAplicacion;
    private LocalDate fechaVencimiento;
    private String veterinario;

    public Vacuna(String nombre, LocalDate fechaAplicacion, LocalDate fechaVencimiento, String veterinario) {
        this.nombre = nombre;
        this.fechaAplicacion = fechaAplicacion;
        this.fechaVencimiento = fechaVencimiento;
        this.veterinario = veterinario;
    }

    // La vacuna sirve si en esa fecha todavía no venció
    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isAfter(fechaVencimiento);
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getVeterinario() {
        return veterinario;
    }
}
