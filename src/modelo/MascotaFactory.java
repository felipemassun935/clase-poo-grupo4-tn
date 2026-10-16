package modelo;

import java.time.LocalDate;

// Patrón Creador / Factory: un solo lugar sabe qué subclase crear según la especie.
// Lo usan la pantalla de mascotas y el repositorio que lee el archivo.
//   dato1 = raza (o tipo de ave)
//   dato2 = tamaño del perro / dieta del conejo
//   marca = gato castrado / ave que puede volar
public class MascotaFactory {

    public static Mascota crear(Especie especie, int id, String nombre, LocalDate fechaNacimiento,
                                double peso, Dueno dueno, String dato1, String dato2, boolean marca) {
        Mascota m = null;
        switch (especie) {
            case PERRO:
                m = new Perro(id, nombre, fechaNacimiento, peso, dueno, dato1, dato2);
                break;
            case GATO:
                m = new Gato(id, nombre, fechaNacimiento, peso, dueno, dato1, marca);
                break;
            case CONEJO:
                m = new Conejo(id, nombre, fechaNacimiento, peso, dueno, dato1, dato2);
                break;
            case AVE:
                m = new Ave(id, nombre, fechaNacimiento, peso, dueno, dato1, marca);
                break;
        }
        return m;
    }
}
