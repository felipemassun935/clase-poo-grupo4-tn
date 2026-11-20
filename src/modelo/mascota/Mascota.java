package modelo.mascota;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import modelo.enums.Especie;
import modelo.persona.Cliente;

// Clase abstracta: tiene los datos comunes de todas las mascotas.
// Cada especie (Perro, Gato, Conejo, Ave) hereda de esta clase.
public abstract class Mascota {
    private Integer id;
    private String codigoCollar; // identificador que lleva la mascota en el collar, para no confundirla con otra
    private String nombre;
    private LocalDate fechaNacimiento;
    private double pesoKg;
    private String observaciones;
    private LocalDate fechaUltimoControl;
    private Cliente cliente;
    private List<Vacuna> vacunas;

    public Mascota(Integer id, String nombre, LocalDate fechaNacimiento, double pesoKg, String observaciones,
            LocalDate fechaUltimoControl) {
        this.id = id;
        this.codigoCollar = generarCodigoCollar(id);
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.pesoKg = pesoKg;
        this.observaciones = observaciones;
        this.fechaUltimoControl = fechaUltimoControl;
        this.vacunas = new ArrayList<Vacuna>();
    }

    // Método abstracto: cada subclase dice de qué especie es
    public abstract Especie getEspecie();

    // Método abstracto (polimorfismo): cada especie revisa sus propios requisitos para entrar.
    // Devuelve la lista de problemas; si está vacía, la mascota puede entrar.
    public abstract List<String> validarIngreso(LocalDate fecha);

    // El código del collar sale del id, que es único: el 7 queda "COL-00007".
    // Es static porque no usa ningún dato del objeto, solo el número que recibe.
    public static String generarCodigoCollar(Integer id) {
        return "COL-" + String.format("%05d", id);
    }

    public void agregarVacuna(Vacuna v) {
        vacunas.add(v);
    }

    // true si tiene una vacuna con ese nombre que esté vigente en la fecha indicada
    public boolean tieneVacunaVigente(String nombre, LocalDate fecha) {
        for (Vacuna v : vacunas) {
            if (v.getNombre().equalsIgnoreCase(nombre) && v.estaVigente(fecha)) {
                return true;
            }
        }
        return false;
    }

    // true si tuvo un control veterinario hace "dias" días o menos.
    // Es protected: solo lo usan las subclases (Conejo y Ave).
    protected boolean tieneControlVigente(LocalDate fecha, int dias) {
        if (fechaUltimoControl == null) {
            return false;
        }
        long diasPasados = ChronoUnit.DAYS.between(fechaUltimoControl, fecha);
        return diasPasados <= dias;
    }

    // Edad de la mascota en meses, contando hasta hoy
    public int getEdadEnMeses() {
        return (int) ChronoUnit.MONTHS.between(fechaNacimiento, LocalDate.now());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
        this.codigoCollar = generarCodigoCollar(id);
    }

    public String getCodigoCollar() {
        return codigoCollar;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaUltimoControl() {
        return fechaUltimoControl;
    }

    public void setFechaUltimoControl(LocalDate fechaUltimoControl) {
        this.fechaUltimoControl = fechaUltimoControl;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Vacuna> getVacunas() {
        return vacunas;
    }

    @Override
    public String toString() {
        return nombre + " (" + getEspecie().getNombreVisible() + ", " + codigoCollar + ")";
    }

    // Clase anidada estática: una vacuna no necesita acceder a los datos de una Mascota en particular,
    // solo vive "adentro" de Mascota para que quede claro que es parte de ella.
    // Desde afuera se usa como Mascota.Vacuna.
    public static class Vacuna {
        private String nombre;
        private LocalDate fechaAplicacion;
        private LocalDate fechaVencimiento;

        public Vacuna(String nombre, LocalDate fechaAplicacion, LocalDate fechaVencimiento) {
            this.nombre = nombre;
            this.fechaAplicacion = fechaAplicacion;
            this.fechaVencimiento = fechaVencimiento;
        }

        // true si en la fecha indicada la vacuna ya estaba aplicada y todavía no venció
        public boolean estaVigente(LocalDate fecha) {
            if (fecha.isBefore(fechaAplicacion)) {
                return false;
            }
            if (fecha.isAfter(fechaVencimiento)) {
                return false;
            }
            return true;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public LocalDate getFechaAplicacion() {
            return fechaAplicacion;
        }

        public void setFechaAplicacion(LocalDate fechaAplicacion) {
            this.fechaAplicacion = fechaAplicacion;
        }

        public LocalDate getFechaVencimiento() {
            return fechaVencimiento;
        }

        public void setFechaVencimiento(LocalDate fechaVencimiento) {
            this.fechaVencimiento = fechaVencimiento;
        }

        @Override
        public String toString() {
            return nombre + " (vence " + fechaVencimiento + ")";
        }
    }
}
