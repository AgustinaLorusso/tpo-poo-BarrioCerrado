package modelo.equipos;

import java.time.LocalDateTime;
import modelo.personas.Persona;

public class Falla {
    private LocalDateTime fecha;
    private String descripcion;
    private Equipo equipo;
    private Persona reportadaPor;
    private OrdenMantenimiento orden;

    public Falla(Equipo equipo, Persona reportadaPor, String descripcion) {
        this.fecha = LocalDateTime.now();
        this.equipo = equipo;
        this.reportadaPor = reportadaPor;
        this.descripcion = descripcion;
    }

    public void setOrden(OrdenMantenimiento orden) {
        this.orden = orden;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public Persona getReportadaPor() {
        return reportadaPor;
    }

    public OrdenMantenimiento getOrden() {
        return orden;
    }
}
