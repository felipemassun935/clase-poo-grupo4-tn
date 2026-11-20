package modelo.sucursal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.enums.Especie;
import modelo.persona.Empleado;

// Sucursal de la guardería: tiene sus alojamientos, sus empleados y un encargado.
public class Sucursal {
    private Integer id;
    private String nombre;
    private String direccion;
    private String telefono;
    private Empleado encargado;
    private List<Alojamiento> alojamientos;
    private List<Empleado> empleados;

    public Sucursal(Integer id, String nombre, String direccion, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.alojamientos = new ArrayList<Alojamiento>();
        this.empleados = new ArrayList<Empleado>();
    }

    // Agrega el alojamiento y le deja anotado que pertenece a esta sucursal
    public void agregarAlojamiento(Alojamiento a) {
        alojamientos.add(a);
        a.setSucursal(this);
    }

    // Agrega el empleado y le deja anotado que trabaja en esta sucursal
    public void agregarEmpleado(Empleado e) {
        empleados.add(e);
        e.setSucursal(this);
    }

    // Devuelve solo los alojamientos pensados para la especie indicada
    public List<Alojamiento> getAlojamientosDe(Especie especie) {
        List<Alojamiento> resultado = new ArrayList<Alojamiento>();
        for (Alojamiento a : alojamientos) {
            if (a.getEspecie() == especie) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    // Cuenta cuántos alojamientos activos hay para cada especie.
    // Todas las especies arrancan en 0 para que aparezcan aunque no tengan alojamientos.
    public Map<Especie, Integer> getCapacidadPorEspecie() {
        Map<Especie, Integer> capacidad = new HashMap<Especie, Integer>();
        for (Especie e : Especie.values()) {
            capacidad.put(e, 0);
        }
        for (Alojamiento a : alojamientos) {
            if (a.isActivo()) {
                int cantidadActual = capacidad.get(a.getEspecie());
                capacidad.put(a.getEspecie(), cantidadActual + 1);
            }
        }
        return capacidad;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Empleado getEncargado() {
        return encargado;
    }

    public void setEncargado(Empleado encargado) {
        this.encargado = encargado;
    }

    public List<Alojamiento> getAlojamientos() {
        return alojamientos;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
