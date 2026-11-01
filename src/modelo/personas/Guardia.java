package modelo.personas;

import java.time.LocalDate;
import modelo.reclamos.Reclamo;

public class Guardia extends Personal {
    private Turno turno;
    private Puesto puesto;

    public Guardia(String nombre, String apellido, String documento,
                   String telefono, String email,
                   int legajo, LocalDate fechaIngreso,
                   Turno turno, Puesto puesto) {
        super(nombre, apellido, documento, telefono, email, legajo, fechaIngreso);
        this.turno = turno;
        this.puesto = puesto;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    //  falta Reclamo.getTipo()
    @Override
    public boolean puedeAtender(Reclamo reclamo) {
        return false; 
    }
}

