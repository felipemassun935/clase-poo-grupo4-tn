package modelo;

// Categoría del alojamiento. Cada una tiene un recargo sobre el precio base.
public enum Categoria {
    ESTANDAR(0.0),
    PREMIUM(0.30); // 30% más caro

    private double recargo;

    private Categoria(double recargo) {
        this.recargo = recargo;
    }

    public double getRecargo() {
        return recargo;
    }
}
