package modelo;

// Junta en un solo lugar los precios de la guardería.
// Métodos static: no hace falta crear un objeto Tarifario para usarlos.
public class Tarifario {
    public static final double PRECIO_BANIO = 8000;
    public static final double PRECIO_PASEO_DIA = 4000;
    public static final double PORCENTAJE_DIURNA = 0.6; // el día de guardería sale 60% de una noche

    public static double calcularHospedaje(Alojamiento a, int noches, boolean conBanio) {
        double total = a.calcularPrecioDia() * noches;
        if (conBanio) {
            total = total + PRECIO_BANIO;
        }
        return total;
    }

    public static double calcularGuarderiaDiurna(Alojamiento a, int dias, boolean conPaseo) {
        double total = a.calcularPrecioDia() * PORCENTAJE_DIURNA * dias;
        if (conPaseo) {
            total = total + PRECIO_PASEO_DIA * dias;
        }
        return total;
    }
}
