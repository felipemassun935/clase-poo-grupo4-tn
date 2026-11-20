package modelo.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import modelo.enums.AccionLog;

// Una línea del log del sistema: cuándo, quién (el empleado que estaba operando), qué acción y el detalle.
public class EntradaLog {
    private LocalDateTime fechaHora;
    private String usuario;
    private AccionLog accion;
    private String detalle;

    public EntradaLog(LocalDateTime fechaHora, String usuario, AccionLog accion, String detalle) {
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.accion = accion;
        this.detalle = detalle;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getFechaHoraTexto() {
        return fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getUsuario() {
        return usuario;
    }

    public AccionLog getAccion() {
        return accion;
    }

    public String getDetalle() {
        return detalle;
    }

    @Override
    public String toString() {
        return getFechaHoraTexto() + " | " + usuario + " | " + accion + " | " + detalle;
    }
}
