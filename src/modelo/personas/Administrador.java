package modelo.personas;

import java.util.ArrayList;
import java.util.List;
import modelo.barrio.Barrio;
import modelo.equipos.Falla;
import modelo.equipos.OrdenMantenimiento;
import modelo.reclamos.Reclamo;
import modelo.reportes.GeneradorReportes;

public class Administrador extends Persona {
    private Barrio barrio;                                  // "a cargo de"
    private List<Personal> personal = new ArrayList<>();    // "coordina"
    private GeneradorReportes reportes;                     // "usa"

    public Administrador(String nombre, String apellido, String documento,
                         String telefono, String email, Barrio barrio) {
        super(nombre, apellido, documento, telefono, email);
        this.barrio = barrio;
        this.reportes = new GeneradorReportes(barrio);
    }

    public void agregarPersonal(Personal empleado) {
        personal.add(empleado);
    }

    public void asignarReclamo(Reclamo reclamo, Personal personal) {
        reclamo.asignar(personal);
    }

    // RF23
    public OrdenMantenimiento generarOrden(Falla falla, EmpleadoMantenimiento empleado) {
        return new OrdenMantenimiento(falla, empleado);
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public List<Personal> getPersonal() {
        return personal;
    }

    public GeneradorReportes getReportes() {
        return reportes;
    }
}
