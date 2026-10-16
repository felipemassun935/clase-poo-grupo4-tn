package modelo;

import java.time.LocalDate;

public class RegistroComida extends EntradaBitacora {
    private String alimento;
    private double cantidadGramos;
    private boolean comioTodo;

    public RegistroComida(LocalDate fecha, Empleado registradaPor, String alimento,
                          double cantidadGramos, boolean comioTodo) {
        super(fecha, registradaPor);
        this.alimento = alimento;
        this.cantidadGramos = cantidadGramos;
        this.comioTodo = comioTodo;
    }

    @Override
    public String getResumen() {
        String resumen = "COMIDA: " + cantidadGramos + " g de " + alimento;
        if (comioTodo) {
            resumen = resumen + " (comió todo)";
        } else {
            resumen = resumen + " (dejó comida)";
        }
        return resumen;
    }

    public String getAlimento() {
        return alimento;
    }

    public double getCantidadGramos() {
        return cantidadGramos;
    }

    public boolean isComioTodo() {
        return comioTodo;
    }
}
