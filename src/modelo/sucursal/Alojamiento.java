package modelo.sucursal;

import modelo.enums.Categoria;
import modelo.enums.Especie;
import modelo.mascota.Mascota;

// Lugar donde se queda una mascota (jaula, cucha, habitación).
// Cada alojamiento es para una sola especie y tiene una categoría que cambia el precio.
public class Alojamiento {
    private Integer id;
    private String codigo;
    private Especie especie;
    private Categoria categoria;
    private double precioBaseNoche;
    private boolean activo;
    private Sucursal sucursal;

    public Alojamiento(Integer id, String codigo, Especie especie, Categoria categoria, double precioBaseNoche,
            boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.especie = especie;
        this.categoria = categoria;
        this.precioBaseNoche = precioBaseNoche;
        this.activo = activo;
    }

    // true si el alojamiento está activo y es para la misma especie que la mascota
    public boolean admite(Mascota m) {
        return activo && m.getEspecie() == especie;
    }

    // Precio final por noche: el precio base con el recargo de la categoría
    public double getPrecioNoche() {
        return categoria.aplicarRecargo(precioBaseNoche);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public double getPrecioBaseNoche() {
        return precioBaseNoche;
    }

    public void setPrecioBaseNoche(double precioBaseNoche) {
        this.precioBaseNoche = precioBaseNoche;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    @Override
    public String toString() {
        return codigo + " - " + especie.getNombreVisible() + " " + categoria;
    }
}
