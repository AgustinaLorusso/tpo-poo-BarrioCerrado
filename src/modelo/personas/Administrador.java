package modelo.personas;

import modelo.equipos.Falla;
import modelo.equipos.OrdenMantenimiento;
import modelo.reclamos.Reclamo;

public class Administrador extends Persona {

    public Administrador(String nombre, String apellido, String documento,
                         String telefono, String email) {
        super(nombre, apellido, documento, telefono, email);
    }

    public void asignarReclamo(Reclamo reclamo, Personal personal) {
        reclamo.asignar(personal);
    }

    // RF23
    public OrdenMantenimiento generarOrden(Falla falla, EmpleadoMantenimiento empleado) {
        return new OrdenMantenimiento(falla, empleado);
    }
}
