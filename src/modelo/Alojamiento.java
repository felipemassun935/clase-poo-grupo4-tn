package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

// Lugar físico donde se queda la mascota: canil, gatera, conejera o jaula según la especie
public class Alojamiento {
    private int idAlojamiento;
    private String codigo;
    private Especie especie;
    private Categoria categoria;
    private double precioBaseDia;
    private ArrayList<Reserva> reservas;

    public Alojamiento(int idAlojamiento, String codigo, Especie especie, Categoria categoria, double precioBaseDia) {
        this.idAlojamiento = idAlojamiento;
        this.codigo = codigo;
        this.especie = especie;
        this.categoria = categoria;
        this.precioBaseDia = precioBaseDia;
        this.reservas = new ArrayList<>();
    }

    // Experto en información: el alojamiento conoce sus reservas,
    // así que es el que sabe si está libre en un rango de fechas.
    public boolean estaDisponible(LocalDate desde, LocalDate hasta) {
        for (Reserva r : reservas) {
            if (r.estaActiva() && r.seSuperpone(desde, hasta)) {
                return false;
            }
        }
        return true;
    }

    // Está ocupado si hay una mascota adentro ahora (reserva con check-in hecho)
    public boolean estaOcupado() {
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.EN_CURSO) {
                return true;
            }
        }
        return false;
    }

    // Precio por día con el recargo de la categoría
    public double calcularPrecioDia() {
        return precioBaseDia + precioBaseDia * categoria.getRecargo();
    }

    // Nombre del tipo de lugar según la especie
    public String getTipoLugar() {
        String tipo = "";
        switch (especie) {
            case PERRO:
                tipo = "Canil";
                break;
            case GATO:
                tipo = "Gatera";
                break;
            case CONEJO:
                tipo = "Conejera";
                break;
            case AVE:
                tipo = "Jaula";
                break;
        }
        return tipo;
    }

    public void agregarReserva(Reserva r) {
        reservas.add(r);
    }

    public boolean tieneReservasActivas() {
        for (Reserva r : reservas) {
            if (r.estaActiva()) {
                return true;
            }
        }
        return false;
    }

    // Getters y setters
    public int getIdAlojamiento() {
        return idAlojamiento;
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

    public double getPrecioBaseDia() {
        return precioBaseDia;
    }

    public void setPrecioBaseDia(double precioBaseDia) {
        this.precioBaseDia = precioBaseDia;
    }

    public ArrayList<Reserva> getReservas() {
        return reservas;
    }

    @Override
    public String toString() {
        return codigo + " - " + getTipoLugar() + " " + categoria;
    }
}
