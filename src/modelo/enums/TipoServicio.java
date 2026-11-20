package modelo.enums;

// Servicios extra que el cliente puede contratar durante la estadía (se suman a la cuenta).
// Enum con atributos: cada servicio tiene su nombre y su precio.
public enum TipoServicio {
    BANIO("Baño", 9000),
    CORTE_PELO("Corte de pelo", 14000),
    CORTE_UNIAS("Corte de uñas", 5000),
    PASEO_EXTRA("Paseo extra", 6000);

    private String nombreVisible;
    private double precio;

    private TipoServicio(String nombreVisible, double precio) {
        this.nombreVisible = nombreVisible;
        this.precio = precio;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public double getPrecio() {
        return precio;
    }

    // Dice si el servicio se le puede hacer a esa especie (por ejemplo, a un ave no se le corta el pelo)
    public boolean seAplicaA(Especie especie) {
        boolean aplica = false;
        switch (this) {
            case BANIO:
                aplica = especie == Especie.PERRO || especie == Especie.GATO || especie == Especie.CONEJO;
                break;
            case CORTE_PELO:
                aplica = especie == Especie.PERRO || especie == Especie.GATO;
                break;
            case CORTE_UNIAS:
                aplica = true;
                break;
            case PASEO_EXTRA:
                aplica = especie == Especie.PERRO;
                break;
        }
        return aplica;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
