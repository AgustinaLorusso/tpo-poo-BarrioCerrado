package modelo.personas;

import java.time.LocalDate;
import modelo.reclamos.Reclamo;

public abstract class Personal extends Persona {
    protected int legajo;
    protected LocalDate fechaIngreso;

    public Personal(String nombre, String apellido, String documento,
                    String telefono, String email,
                    int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, documento, telefono, email);
        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso;
    }

    public int getLegajo() {
        return legajo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public abstract boolean puedeAtender(Reclamo reclamo);
}
