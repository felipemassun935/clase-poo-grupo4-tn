package modelo;

import java.time.LocalDate;

public class RegistroMedicacion extends EntradaBitacora {
    private String medicamento;
    private String dosis;

    public RegistroMedicacion(LocalDate fecha, Empleado registradaPor, String medicamento, String dosis) {
        super(fecha, registradaPor);
        this.medicamento = medicamento;
        this.dosis = dosis;
    }

    @Override
    public String getResumen() {
        return "MEDICACIÓN: " + medicamento + " - dosis: " + dosis;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public String getDosis() {
        return dosis;
    }
}
