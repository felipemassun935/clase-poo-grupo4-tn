package persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Sabe cómo conectarse a la base de datos SQL Server.
// Los datos de conexión (url, usuario y clave) se leen del archivo conexion.txt de la carpeta del proyecto,
// así cada integrante del grupo pone los de su máquina sin tocar el código.
public class ConexionBD {
    private static final String ARCHIVO = "conexion.txt";

    // Valores por defecto si no existe conexion.txt (SQL Server Express en la misma PC, puerto 1433)
    private String url = "jdbc:sqlserver://localhost:1433;databaseName=GuarderiaMascotas;encrypt=true;trustServerCertificate=true";
    private String usuario = "guarderia";
    private String clave = "Guarderia2026!";

    public ConexionBD() {
        leerArchivo();
    }

    // Lee conexion.txt: cada línea es "nombre=valor". Las que empiezan con # son comentarios.
    private void leerArchivo() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            System.out.println("No se encontró " + ARCHIVO + ": se usan los datos de conexión por defecto.");
            return;
        }
        // try-with-resources: el archivo se cierra solo al terminar, aunque haya un error
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea = lector.readLine();
            while (linea != null) {
                linea = linea.trim();
                if (!linea.isEmpty() && !linea.startsWith("#") && linea.contains("=")) {
                    String[] partes = linea.split("=", 2); // 2: la url también tiene "=" adentro
                    String nombre = partes[0].trim();
                    String valor = partes[1].trim();
                    if (nombre.equals("url")) {
                        url = valor;
                    } else if (nombre.equals("usuario")) {
                        usuario = valor;
                    } else if (nombre.equals("clave")) {
                        clave = valor;
                    }
                }
                linea = lector.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error al leer " + ARCHIVO + ": " + e.getMessage());
        }
    }

    // Abre una conexión nueva. Quien la pide la cierra (con try-with-resources).
    // Si el usuario está vacío se conecta solo con la url (por ejemplo, con integratedSecurity=true).
    public Connection conectar() throws SQLException {
        if (usuario.isEmpty()) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    public String getUrl() {
        return url;
    }
}
