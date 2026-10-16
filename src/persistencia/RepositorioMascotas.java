package persistencia;

import java.time.LocalDate;
import java.util.ArrayList;

import modelo.Ave;
import modelo.Conejo;
import modelo.Dueno;
import modelo.Especie;
import modelo.Gato;
import modelo.Mascota;
import modelo.MascotaFactory;
import modelo.Perro;
import modelo.Vacuna;

// Formato: especie;id;nombre;fechaNacimiento;peso;dniDueno;dato1;dato2;marca;vacunas
// Las vacunas van todas en el último campo: nombre#aplicada#vence#veterinario|nombre#...
// Si no tiene vacunas, el campo queda vacío.
public class RepositorioMascotas extends RepositorioArchivo<Mascota> {
    private ArrayList<Dueno> duenos; // para encontrar el dueño de cada mascota al leer

    public RepositorioMascotas(String rutaArchivo, ArrayList<Dueno> duenos) {
        super(rutaArchivo);
        this.duenos = duenos;
    }

    @Override
    protected String aLinea(Mascota m) {
        // Datos propios de cada subclase
        String dato1 = "";
        String dato2 = "";
        boolean marca = false;
        if (m instanceof Perro) {
            dato1 = ((Perro) m).getRaza();
            dato2 = ((Perro) m).getTamanio();
        } else if (m instanceof Gato) {
            dato1 = ((Gato) m).getRaza();
            marca = ((Gato) m).isEsCastrado();
        } else if (m instanceof Conejo) {
            dato1 = ((Conejo) m).getRaza();
            dato2 = ((Conejo) m).getDieta();
        } else if (m instanceof Ave) {
            dato1 = ((Ave) m).getTipoAve();
            marca = ((Ave) m).isPuedeVolar();
        }

        String vacunas = "";
        for (Vacuna v : m.getVacunas()) {
            if (!vacunas.isEmpty()) {
                vacunas = vacunas + "|";
            }
            vacunas = vacunas + limpiar(v.getNombre()) + "#" + v.getFechaAplicacion() + "#"
                    + v.getFechaVencimiento() + "#" + limpiar(v.getVeterinario());
        }

        return m.getEspecie() + SEPARADOR + m.getIdMascota() + SEPARADOR + limpiar(m.getNombre())
                + SEPARADOR + m.getFechaNacimiento() + SEPARADOR + m.getPeso()
                + SEPARADOR + m.getDueno().getDni() + SEPARADOR + limpiar(dato1)
                + SEPARADOR + limpiar(dato2) + SEPARADOR + marca + SEPARADOR + vacunas;
    }

    @Override
    protected Mascota desdeLinea(String[] c) {
        Dueno dueno = buscarDueno(c[5]);
        if (dueno == null) {
            return null; // dueño inexistente: se ignora la línea
        }

        Mascota m = MascotaFactory.crear(Especie.valueOf(c[0]), Integer.parseInt(c[1]), c[2],
                LocalDate.parse(c[3]), Double.parseDouble(c[4]), dueno, c[6], c[7], Boolean.parseBoolean(c[8]));

        if (!c[9].isEmpty()) {
            String[] vacunas = c[9].split("\\|"); // | es especial en split, por eso va con \\
            for (String textoVacuna : vacunas) {
                String[] v = textoVacuna.split("#", -1);
                m.agregarVacuna(new Vacuna(v[0], LocalDate.parse(v[1]), LocalDate.parse(v[2]), v[3]));
            }
        }
        return m;
    }

    private Dueno buscarDueno(String dni) {
        for (Dueno d : duenos) {
            if (d.getDni().equals(dni)) {
                return d;
            }
        }
        return null;
    }
}
