package modelo.reclamos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.barrio.Lote;
import modelo.excepciones.ReglaNegocioException;
import modelo.personas.Persona;
import modelo.personas.Personal;
import modelo.personas.Vecino;

// Comparable: orden natural por prioridad (ALTA primero) y, a igual prioridad, por antiguedad
public class Reclamo implements Serializable, Comparable<Reclamo> {
    private static int ultimoNumero = 0;

    private int numero;
    private LocalDateTime fecha;
    private TipoReclamo tipo;
    private String descripcion;
    private Prioridad prioridad;
    private EstadoReclamo estado;
    private LocalDateTime fechaResolucion;
    private Lote lote;
    private Vecino presentadoPor;
    private Personal responsable;
    private boolean escalado;
    private List<CambioEstado> historial = new ArrayList<>();
    private List<Comentario> comentarios = new ArrayList<>();

    // RF06: numero, fecha, prioridad y estado "abierto" se asignan solos
    public Reclamo(Lote lote, Vecino presentadoPor, TipoReclamo tipo, String descripcion)
            throws ReglaNegocioException {
        // RN07: solo el titular o una persona autorizada por el
        if (!lote.puedePresentarReclamo(presentadoPor)) {
            throw new ReglaNegocioException("La persona no puede presentar reclamos para este lote.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Falta la descripcion.");
        }
        ultimoNumero++;
        this.numero = ultimoNumero;
        this.fecha = LocalDateTime.now();
        this.lote = lote;
        this.presentadoPor = presentadoPor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.prioridad = calcularPrioridad(tipo);
        this.estado = EstadoReclamo.ABIERTO;
        historial.add(new CambioEstado(fecha, estado, "Reclamo registrado"));
    }

    // RN01
    private Prioridad calcularPrioridad(TipoReclamo tipo) {
        switch (tipo) {
            case SEGURIDAD:
                return Prioridad.ALTA;
            case MANTENIMIENTO:
                return Prioridad.MEDIA;
            default:
                return Prioridad.BAJA;
        }
    }

    // RF07, RN04: un unico responsable
    public void asignar(Personal personal) throws ReglaNegocioException {
        if (estado != EstadoReclamo.ABIERTO && estado != EstadoReclamo.ASIGNADO) {
            throw new IllegalStateException("Solo se asignan reclamos abiertos o asignados.");
        }
        if (!personal.puedeAtender(this)) {
            throw new ReglaNegocioException("El empleado no puede atender este tipo de reclamo.");
        }
        if (responsable != null) {
            responsable.quitarReclamo(this);
        }
        responsable = personal;
        personal.agregarReclamo(this);
        estado = EstadoReclamo.ASIGNADO;
        historial.add(new CambioEstado(LocalDateTime.now(), estado,
                "Asignado a " + personal.getNombreCompleto()));
    }

    // RF08, RN02: solo se avanza al estado siguiente
    public void avanzarEstado(String observacion) throws ReglaNegocioException {
        if (observacion == null || observacion.isBlank()) {
            throw new IllegalArgumentException("Falta la observacion.");
        }
        EstadoReclamo siguiente;
        switch (estado) {
            case ASIGNADO:
                // RN05: maximo 5 reclamos en curso por empleado
                if (responsable.cantidadReclamosEnCurso() >= 5) {
                    throw new ReglaNegocioException("El empleado ya tiene 5 reclamos en curso.");
                }
                siguiente = EstadoReclamo.EN_CURSO;
                break;
            case EN_CURSO:
                siguiente = EstadoReclamo.RESUELTO;
                fechaResolucion = LocalDateTime.now();
                break;
            case RESUELTO:
                siguiente = EstadoReclamo.CERRADO;
                break;
            case ABIERTO:
                throw new IllegalStateException("Un reclamo abierto pasa a asignado con asignar().");
            default:
                throw new IllegalStateException("El reclamo ya esta cerrado.");
        }
        estado = siguiente;
        historial.add(new CambioEstado(LocalDateTime.now(), estado, observacion));
    }

    // RF11, RN03: reabrir dentro de los 5 dias desde la resolucion
    public void reabrir(String motivo) throws ReglaNegocioException {
        if (estado != EstadoReclamo.RESUELTO) {
            throw new IllegalStateException("Solo se puede reabrir un reclamo resuelto.");
        }
        if (debeCerrarse()) {
            throw new ReglaNegocioException("Vencio el plazo de 5 dias para reabrir.");
        }
        estado = EstadoReclamo.ABIERTO;
        fechaResolucion = null;
        historial.add(new CambioEstado(LocalDateTime.now(), estado, "Reabierto: " + motivo));
    }

    // RF10
    public void agregarComentario(Persona autor, String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El comentario esta vacio.");
        }
        if (estado == EstadoReclamo.CERRADO) {
            throw new IllegalStateException("No se comentan reclamos cerrados.");
        }
        // Solo comenta quien lo presento o quien lo tiene asignado
        if (!esLaMismaPersona(autor, presentadoPor) && !esLaMismaPersona(autor, responsable)) {
            throw new IllegalArgumentException("Solo comenta quien presento el reclamo o su responsable.");
        }
        comentarios.add(new Comentario(LocalDateTime.now(), texto, autor));
    }

    private boolean esLaMismaPersona(Persona a, Persona b) {
        return a != null && b != null && a.getDocumento().equals(b.getDocumento());
    }

    // RN06: prioridad alta sin pasar a "en curso" dentro de las 24 horas.
    // RF13 3a: si ya estaba escalado no se vuelve a escalar.
    public boolean debeEscalarse() {
        boolean sinAtender = estado == EstadoReclamo.ABIERTO || estado == EstadoReclamo.ASIGNADO;
        return prioridad == Prioridad.ALTA && sinAtender && !escalado
                && LocalDateTime.now().isAfter(fecha.plusHours(24));
    }

    // RF13: se marca como escalado para notificar al administrador
    public void escalar() {
        escalado = true;
    }

    // RF12: cierre automatico con esa observacion en el historial
    public void cerrarAutomaticamente() {
        if (!debeCerrarse()) {
            throw new IllegalStateException("El reclamo no esta en condiciones de cerrarse.");
        }
        avanzarEstado("Cierre automatico");
    }

    // RN03: resuelto hace mas de 5 dias sin reapertura
    public boolean debeCerrarse() {
        return estado == EstadoReclamo.RESUELTO
                && LocalDateTime.now().isAfter(fechaResolucion.plusDays(5));
    }

    // Orden natural: primero la prioridad (ALTA, MEDIA, BAJA) y luego el mas antiguo
    @Override
    public int compareTo(Reclamo otro) {
        int porPrioridad = prioridad.compareTo(otro.prioridad);
        if (porPrioridad != 0) {
            return porPrioridad;
        }
        return fecha.compareTo(otro.fecha);
    }

    // Persistencia: el contador es static y no se guarda con el objeto
    public static int getUltimoNumero() {
        return ultimoNumero;
    }

    public static void setUltimoNumero(int numero) {
        ultimoNumero = numero;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoReclamo getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public EstadoReclamo getEstado() {
        return estado;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public Lote getLote() {
        return lote;
    }

    public Vecino getPresentadoPor() {
        return presentadoPor;
    }

    public Personal getResponsable() {
        return responsable;
    }

    public boolean isEscalado() {
        return escalado;
    }

    // RF09
    public List<CambioEstado> getHistorial() {
        return historial;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }
}
