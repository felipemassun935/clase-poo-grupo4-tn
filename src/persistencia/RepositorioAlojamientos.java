package persistencia;

import modelo.Alojamiento;
import modelo.Categoria;
import modelo.Especie;

// Formato: id;codigo;especie;categoria;precioBaseDia
public class RepositorioAlojamientos extends RepositorioArchivo<Alojamiento> {

    public RepositorioAlojamientos(String rutaArchivo) {
        super(rutaArchivo);
    }

    @Override
    protected String aLinea(Alojamiento a) {
        return a.getIdAlojamiento() + SEPARADOR + limpiar(a.getCodigo()) + SEPARADOR + a.getEspecie()
                + SEPARADOR + a.getCategoria() + SEPARADOR + a.getPrecioBaseDia();
    }

    @Override
    protected Alojamiento desdeLinea(String[] c) {
        return new Alojamiento(Integer.parseInt(c[0]), c[1], Especie.valueOf(c[2]),
                Categoria.valueOf(c[3]), Double.parseDouble(c[4]));
    }
}
