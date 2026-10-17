package modelo.persona;

// Clase abstracta: tiene los datos comunes de Cliente y Empleado.
// No se puede crear una "Persona" sola, siempre es un Cliente o un Empleado.
public abstract class Persona {
    private Integer id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;

    public Persona(Integer id, String nombre, String apellido, String dni, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
    }

    // Devuelve "Nombre Apellido"
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // Método abstracto: cada subclase dice qué rol tiene (polimorfismo)
    public abstract String getRol();

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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (DNI " + dni + ")";
    }
}
