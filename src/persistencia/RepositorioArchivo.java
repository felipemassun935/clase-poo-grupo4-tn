package persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

// Guarda cada objeto como una línea de un archivo .txt, con los campos separados por ";"
// Las subclases solo dicen cómo pasar un objeto a línea y una línea a objeto
// (patrón Template Method: el "esqueleto" de leer y escribir está acá una sola vez).
public abstract class RepositorioArchivo<T> implements Repositorio<T> {
    protected static final String SEPARADOR = ";";
    private String rutaArchivo;

    public RepositorioArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    protected abstract String aLinea(T objeto);

    protected abstract T desdeLinea(String[] campos);

    @Override
    public void guardarTodos(ArrayList<T> lista) {
        File archivo = new File(rutaArchivo);
        archivo.getParentFile().mkdirs(); // crea la carpeta "datos" si no existe

        // try-with-resources: el archivo se cierra solo al terminar
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            for (T objeto : lista) {
                pw.println(aLinea(objeto));
            }
        } catch (IOException e) {
            System.out.println("Error al guardar " + rutaArchivo + ": " + e.getMessage());
        }
    }

    @Override
    public ArrayList<T> cargarTodos() {
        ArrayList<T> lista = new ArrayList<>();
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return lista; // la primera vez no hay archivo: lista vacía
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea = br.readLine();
            while (linea != null) {
                if (!linea.isBlank()) {
                    try {
                        // el -1 hace que split no descarte los campos vacíos del final
                        T objeto = desdeLinea(linea.split(SEPARADOR, -1));
                        if (objeto != null) {
                            lista.add(objeto);
                        }
                    } catch (RuntimeException e) {
                        System.out.println("Línea con errores en " + rutaArchivo + ", se ignora: " + linea);
                    }
                }
                linea = br.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error al leer " + rutaArchivo + ": " + e.getMessage());
        }
        return lista;
    }

    // Saca del texto los caracteres que usamos como separadores en el archivo
    protected static String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(";", ",").replace("|", "/").replace("#", " ");
    }
}
