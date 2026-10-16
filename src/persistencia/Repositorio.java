package persistencia;

import java.util.ArrayList;

// Interfaz genérica: cualquier repositorio sabe guardar y cargar una lista de objetos
public interface Repositorio<T> {
    void guardarTodos(ArrayList<T> lista);

    ArrayList<T> cargarTodos();
}
