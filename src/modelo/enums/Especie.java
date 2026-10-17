package modelo.enums;

// Especies que acepta la guardería.
// Enum con atributos: cada especie guarda el nombre que se muestra en pantalla.
public enum Especie {
    PERRO("Perro"),
    GATO("Gato"),
    CONEJO("Conejo"),
    AVE("Ave");

    private String nombreVisible;

    // El constructor de un enum siempre es privado: solo lo usan las constantes de arriba
    private Especie(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
