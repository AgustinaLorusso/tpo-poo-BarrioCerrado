package modelo.personas;

import modelo.barrio.Lote;
import modelo.reclamos.Reclamo;
import modelo.reclamos.TipoReclamo;
import modelo.visitas.Visita;

public class Vecino extends Persona {

    public Vecino(String nombre, String apellido, String documento,
                  String telefono, String email) {
        super(nombre, apellido, documento, telefono, email);
    }

    public Reclamo presentarReclamo(Lote lote, TipoReclamo tipo, String descripcion) {
        Reclamo reclamo = new Reclamo(lote, this, tipo, descripcion);
        lote.agregarReclamo(reclamo);
        return reclamo;
    }

    // RF27, RF28: el vecino tiene que estar vinculado al lote
    public void autorizarVisita(Visita visita) {
        if (!visita.getLote().estaVinculado(this)) {
            throw new IllegalArgumentException("El vecino no esta vinculado al lote.");
        }
        visita.setAutorizadaPor(this);
        visita.getLote().agregarVisita(visita);
    }
}
