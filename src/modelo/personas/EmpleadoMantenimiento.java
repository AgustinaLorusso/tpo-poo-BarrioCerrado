package modelo.personas;

import java.time.LocalDate;
import modelo.reclamos.Reclamo;
import modelo.reclamos.TipoReclamo;

public class EmpleadoMantenimiento extends Personal {
    private Especialidad especialidad;

    public EmpleadoMantenimiento(String nombre, String apellido, String documento,
                                 String telefono, String email,
                                 int legajo, LocalDate fechaIngreso,
                                 Especialidad especialidad) {
        super(nombre, apellido, documento, telefono, email, legajo, fechaIngreso);
        this.especialidad = especialidad;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    // Supuesto: mantenimiento y administrativo
    @Override
    public boolean puedeAtender(Reclamo reclamo) {
        return reclamo.getTipo() == TipoReclamo.MANTENIMIENTO
                || reclamo.getTipo() == TipoReclamo.ADMINISTRATIVO;
    }
}

