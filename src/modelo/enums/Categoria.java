package modelo.enums;

// Categoría de un alojamiento.
// Enum con atributos: cada categoría guarda el recargo (en %) que se suma al precio base.
public enum Categoria {
    ESTANDAR(0),
    PREMIUM(30);

    private double recargoPorcentaje;

    private Categoria(double recargoPorcentaje) {
        this.recargoPorcentaje = recargoPorcentaje;
    }

    public double getRecargoPorcentaje() {
        return recargoPorcentaje;
    }

    // Devuelve el precio con el recargo de la categoría ya sumado
    public double aplicarRecargo(double precio) {
        return precio + precio * recargoPorcentaje / 100;
    }
}
