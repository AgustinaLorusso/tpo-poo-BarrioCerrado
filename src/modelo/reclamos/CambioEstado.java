package modelo.reclamos;

import java.time.LocalDateTime;

public class CambioEstado {
    private LocalDateTime fecha;
    private EstadoReclamo estado;
    private String observacion;

    public CambioEstado(LocalDateTime fecha, EstadoReclamo estado, String observacion) {
        this.fecha = fecha;
        this.estado = estado;
        this.observacion = observacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoReclamo getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }
}
