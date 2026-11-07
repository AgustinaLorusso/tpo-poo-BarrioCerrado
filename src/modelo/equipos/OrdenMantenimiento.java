package modelo.equipos;

import java.io.Serializable;
import java.time.LocalDate;
import modelo.personas.EmpleadoMantenimiento;
import modelo.reclamos.Prioridad;

public class OrdenMantenimiento implements Serializable {
    private static int ultimoNumero = 0;

    private int numero;
    private LocalDate fechaApertura;
    private Prioridad prioridad;
    private String descripcionTrabajo;
    private LocalDate fechaCierre;
    private String descripcionReparacion;
    private Falla falla;
    private Equipo equipo;
    private EmpleadoMantenimiento asignadaA;
    private EmpleadoMantenimiento tecnico;

    // Persistencia: el contador es static y no se guarda con el objeto
    public static int getUltimoNumero() {
        return ultimoNumero;
    }

    public static void setUltimoNumero(int numero) {
        ultimoNumero = numero;
    }

    // RF23: prioridad automatica segun el tipo de equipo
    public OrdenMantenimiento(Falla falla, EmpleadoMantenimiento empleado) {
        if (falla.getEquipo().getEstado() != EstadoEquipo.CON_FALLA) {
            throw new IllegalStateException("El equipo no esta con falla.");
        }
        if (falla.getEquipo().tieneOrdenAbierta()) {
            throw new IllegalStateException("El equipo ya tiene una orden abierta.");
        }
        ultimoNumero++;
        this.numero = ultimoNumero;
        this.fechaApertura = LocalDate.now();
        this.falla = falla;
        this.equipo = falla.getEquipo();
        this.prioridad = equipo.calcularPrioridadOrden();
        this.descripcionTrabajo = falla.getDescripcion();
        falla.setOrden(this);
        equipo.agregarOrden(this);
        // 4a: si no hay empleado, la orden queda sin asignar
        if (empleado != null) {
            asignar(empleado);
        }
    }

    // Supuesto: el equipo pasa a "en reparacion" cuando la orden tiene un empleado
    public void asignar(EmpleadoMantenimiento empleado) {
        asignadaA = empleado;
        equipo.cambiarEstado(EstadoEquipo.EN_REPARACION);
    }

    // RF24, RN16: el equipo vuelve a operativo
    public void cerrar(LocalDate fecha, EmpleadoMantenimiento tecnico, String descripcion) {
        registrarCierre(fecha, tecnico, descripcion);
        equipo.cambiarEstado(EstadoEquipo.OPERATIVO);
    }

    // RF24 4a: no se pudo reparar, el equipo queda fuera de servicio
    public void cerrarSinReparar(LocalDate fecha, EmpleadoMantenimiento tecnico, String descripcion) {
        registrarCierre(fecha, tecnico, descripcion);
        equipo.cambiarEstado(EstadoEquipo.FUERA_DE_SERVICIO);
    }

    private void registrarCierre(LocalDate fecha, EmpleadoMantenimiento tecnico, String descripcion) {
        if (estaCerrada()) {
            throw new IllegalStateException("La orden ya esta cerrada.");
        }
        // RN16: fecha, tecnico y descripcion obligatorios
        if (fecha == null || tecnico == null || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Faltan la fecha, el tecnico o la descripcion.");
        }
        this.fechaCierre = fecha;
        this.tecnico = tecnico;
        this.descripcionReparacion = descripcion;
    }

    public boolean estaCerrada() {
        return fechaCierre != null;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getDescripcionTrabajo() {
        return descripcionTrabajo;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public String getDescripcionReparacion() {
        return descripcionReparacion;
    }

    public Falla getFalla() {
        return falla;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public EmpleadoMantenimiento getAsignadaA() {
        return asignadaA;
    }

    public EmpleadoMantenimiento getTecnico() {
        return tecnico;
    }
}
