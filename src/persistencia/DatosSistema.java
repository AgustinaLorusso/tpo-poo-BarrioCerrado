package persistencia;

import java.io.Serializable;
import java.util.List;
import modelo.equipos.OrdenMantenimiento;
import modelo.personas.Administrador;
import modelo.personas.Vecino;
import modelo.reclamos.Reclamo;
import modelo.visitas.RegistroIngreso;

// Todo lo que se guarda en el archivo. Desde el administrador se llega al barrio
// (lotes, expensas, reclamos, sectores, equipos, visitas) y al personal.
public class DatosSistema implements Serializable {
    private Administrador admin;
    private List<Vecino> vecinos;
    private List<RegistroIngreso> ingresos;
    private int ultimoReclamo;
    private int ultimaOrden;

    public DatosSistema(Administrador admin, List<Vecino> vecinos) {
        this.admin = admin;
        this.vecinos = vecinos;
        // Los atributos static no se serializan: se copian aca
        this.ingresos = RegistroIngreso.getTodos();
        this.ultimoReclamo = Reclamo.getUltimoNumero();
        this.ultimaOrden = OrdenMantenimiento.getUltimoNumero();
    }

    // Al cargar, se devuelven los valores static a sus clases
    public void restaurarEstaticos() {
        RegistroIngreso.setTodos(ingresos);
        Reclamo.setUltimoNumero(ultimoReclamo);
        OrdenMantenimiento.setUltimoNumero(ultimaOrden);
    }

    public Administrador getAdmin() {
        return admin;
    }

    public List<Vecino> getVecinos() {
        return vecinos;
    }
}
