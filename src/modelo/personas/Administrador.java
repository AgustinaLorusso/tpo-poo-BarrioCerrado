package modelo.personas;

import java.util.ArrayList;
import java.util.List;
import modelo.barrio.Barrio;
import modelo.barrio.Lote;
import modelo.barrio.Residencia;
import modelo.barrio.TipoResidente;
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

    // RF02: alta de una persona del barrio
    public Vecino darDeAltaVecino(String nombre, String apellido, String documento,
                                  String telefono, String email) {
        return new Vecino(nombre, apellido, documento, telefono, email);
    }

    // RF01: alta de un lote con su titular
    public Lote darDeAltaLote(int numero, double superficie, Vecino titular) {
        Lote lote = new Lote(numero, superficie, titular);
        barrio.agregarLote(lote);
        return lote;
    }

    // RF01: baja de lote. Barrio controla que no tenga reclamos abiertos ni deuda (flujo 3a)
    public void darDeBajaLote(Lote lote) {
        barrio.quitarLote(lote);
    }

    // RF02: baja de un vecino. Si es titular de algun lote, primero hay que cambiar el titular
    public void darDeBajaVecino(Vecino vecino) {
        for (Lote lote : barrio.getLotes()) {
            if (lote.getTitular().getDocumento().equals(vecino.getDocumento())) {
                throw new IllegalStateException("Es titular del lote " + lote.getNumero()
                        + ": primero cambie el titular.");
            }
        }
        // Se lo desvincula de los lotes donde vive o esta autorizado
        for (Lote lote : barrio.getLotes()) {
            lote.quitarResidente(vecino);
            lote.revocarAutorizacion(lote.getTitular(), vecino);
        }
    }

    // RF02: asignar rol de residente (familiar o inquilino) en un lote
    public Residencia asignarRolEnLote(Lote lote, Vecino vecino, TipoResidente tipo) {
        return lote.agregarResidente(vecino, tipo);
    }

    // RF02: reemplazar al titular de un lote (flujo 5b)
    public void cambiarTitular(Lote lote, Vecino nuevoTitular) {
        lote.setTitular(nuevoTitular);
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
