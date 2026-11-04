package modelo.equipos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import modelo.reclamos.Prioridad;

public class Camara extends Equipo {
    private String resolucion;
    private int diasGrabacion;

    public Camara(String codigo, Sector sector, LocalDate fechaInstalacion,
                  String resolucion, int diasGrabacion) {
        super(codigo, sector, fechaInstalacion);
        this.resolucion = resolucion;
        this.diasGrabacion = diasGrabacion;
    }

    // RN14: una camara siempre genera prioridad alta
    @Override
    public Prioridad calcularPrioridadOrden() {
        return Prioridad.ALTA;
    }

    // RN15: mas de 48 horas sin funcionar desde la ultima falla
    public boolean superaLimiteSinFuncionar() {
        Falla falla = getUltimaFalla();
        return estado != EstadoEquipo.OPERATIVO && falla != null
                && LocalDateTime.now().isAfter(falla.getFecha().plusHours(48));
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public int getDiasGrabacion() {
        return diasGrabacion;
    }

    public void setDiasGrabacion(int diasGrabacion) {
        this.diasGrabacion = diasGrabacion;
    }
}
