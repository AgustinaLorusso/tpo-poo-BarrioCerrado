package modelo.equipos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.personas.Persona;
import modelo.reclamos.Prioridad;

public abstract class Equipo {
    protected String codigo;
    protected Sector sector;
    protected LocalDate fechaInstalacion;
    protected EstadoEquipo estado;
    protected List<Falla> fallas = new ArrayList<>();
    protected List<OrdenMantenimiento> ordenes = new ArrayList<>();

    public Equipo(String codigo, Sector sector, LocalDate fechaInstalacion) {
        this.codigo = codigo;
        this.sector = sector;
        this.fechaInstalacion = fechaInstalacion;
        this.estado = EstadoEquipo.OPERATIVO;
        sector.agregarEquipo(this);
    }

    // RF22. Creador: el equipo crea sus fallas
    public Falla registrarFalla(Persona persona, String descripcion) {
        if (estado != EstadoEquipo.OPERATIVO) {
            throw new IllegalStateException("El equipo ya tiene una falla registrada.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Falta la descripcion de la falla.");
        }
        Falla falla = new Falla(this, persona, descripcion);
        fallas.add(falla);
        estado = EstadoEquipo.CON_FALLA;
        return falla;
    }

    public void cambiarEstado(EstadoEquipo estado) {
        this.estado = estado;
    }

    public void agregarOrden(OrdenMantenimiento orden) {
        ordenes.add(orden);
    }

    public boolean tieneOrdenAbierta() {
        for (OrdenMantenimiento orden : ordenes) {
            if (!orden.estaCerrada()) {
                return true;
            }
        }
        return false;
    }

    public Falla getUltimaFalla() {
        if (fallas.isEmpty()) {
            return null;
        }
        return fallas.get(fallas.size() - 1);
    }

    // Open/Closed: cada tipo de equipo define su prioridad (RN14)
    public abstract Prioridad calcularPrioridadOrden();

    public String getCodigo() {
        return codigo;
    }

    public Sector getSector() {
        return sector;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public List<Falla> getFallas() {
        return fallas;
    }

    // RF25
    public List<OrdenMantenimiento> getOrdenes() {
        return ordenes;
    }
}
