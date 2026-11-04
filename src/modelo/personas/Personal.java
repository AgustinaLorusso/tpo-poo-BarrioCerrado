package modelo.personas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.reclamos.EstadoReclamo;
import modelo.reclamos.Reclamo;

public abstract class Personal extends Persona {
    protected int legajo;
    protected LocalDate fechaIngreso;
    protected List<Reclamo> reclamos = new ArrayList<>();

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

    public void agregarReclamo(Reclamo reclamo) {
        reclamos.add(reclamo);
    }

    public void quitarReclamo(Reclamo reclamo) {
        reclamos.remove(reclamo);
    }

    public List<Reclamo> getReclamos() {
        return reclamos;
    }

    // RN05
    public int cantidadReclamosEnCurso() {
        int cantidad = 0;
        for (Reclamo reclamo : reclamos) {
            if (reclamo.getEstado() == EstadoReclamo.EN_CURSO) {
                cantidad++;
            }
        }
        return cantidad;
    }
}
