import modelo.excepciones.ReglaNegocioException;
import modelo.barrio.Barrio;
import modelo.barrio.Lote;
import modelo.personas.*;
import modelo.reclamos.*;
import java.time.LocalDate;

/** Prueba del paso 4: reclamos, historial y comentarios. */
public class PruebaPaso4 {
    private static int comprobaciones;

    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) { throw new AssertionError(mensaje); }
    }

    private static void rechaza(Runnable accion, String mensaje) {
        try { accion.run(); } catch (IllegalArgumentException | IllegalStateException | ReglaNegocioException e) {
            comprobaciones++; return;
        }
        throw new AssertionError(mensaje);
    }

    public static void main(String[] args) {
        Vecino titular = new Vecino("Ana", "Perez", "123", "111", "ana@example.com");
        Vecino ajeno = new Vecino("Luis", "Gomez", "789", "333", "luis@example.com");
        Administrador admin = new Administrador("Carla", "Diaz", "999", "444", "carla@example.com",
                new Barrio("Los Aromos"));
        Guardia guardia = new Guardia("Pedro", "Lopez", "555", "666", "pedro@example.com",
                1, LocalDate.of(2025, 1, 1), Turno.MAÑANA, Puesto.PORTERIA);
        EmpleadoMantenimiento plomero = new EmpleadoMantenimiento("Rosa", "Ruiz", "777", "888",
                "rosa@example.com", 2, LocalDate.of(2025, 1, 1), Especialidad.PLOMERIA);
        Lote lote = new Lote(1, 300, titular);

        // RN07
        rechaza(() -> ajeno.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Ruidos"), "Solo titular o autorizado");

        // RF06 y RN01
        Reclamo reclamo = titular.presentarReclamo(lote, TipoReclamo.MANTENIMIENTO, "Perdida de agua");
        verificar(reclamo.getEstado() == EstadoReclamo.ABIERTO, "Arranca abierto");
        verificar(reclamo.getPrioridad() == Prioridad.MEDIA, "Mantenimiento es media");
        verificar(titular.presentarReclamo(lote, TipoReclamo.SEGURIDAD, "Portón roto").getPrioridad() == Prioridad.ALTA,
                "Seguridad es alta");
        verificar(titular.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Perro").getPrioridad() == Prioridad.BAJA,
                "Convivencia es baja");

        // RF07 y puedeAtender
        rechaza(() -> admin.asignarReclamo(reclamo, guardia), "Guardia no atiende mantenimiento");
        rechaza(() -> reclamo.avanzarEstado("Empiezo"), "Abierto pasa con asignar()");
        admin.asignarReclamo(reclamo, plomero);
        verificar(reclamo.getEstado() == EstadoReclamo.ASIGNADO, "Queda asignado");
        verificar(reclamo.getResponsable() == plomero, "Unico responsable");

        // RF08 y RN02
        rechaza(() -> reclamo.avanzarEstado(""), "Requiere observacion");
        reclamo.avanzarEstado("Voy en camino");
        verificar(reclamo.getEstado() == EstadoReclamo.EN_CURSO, "Pasa a en curso");
        verificar(plomero.cantidadReclamosEnCurso() == 1, "Cuenta en curso");

        // RF10
        reclamo.agregarComentario(titular, "Gracias");
        reclamo.agregarComentario(plomero, "Llego en 10 minutos");
        rechaza(() -> reclamo.agregarComentario(ajeno, "Hola"), "Solo participantes comentan");
        rechaza(() -> reclamo.agregarComentario(plomero, " "), "Comentario vacio");
        verificar(reclamo.getComentarios().size() == 2, "Guarda los comentarios");

        // RF11 y RN03
        reclamo.avanzarEstado("Cambie el cano");
        verificar(reclamo.getEstado() == EstadoReclamo.RESUELTO, "Pasa a resuelto");
        verificar(!reclamo.debeCerrarse(), "Todavia en plazo");
        reclamo.reabrir("Sigue perdiendo");
        verificar(reclamo.getEstado() == EstadoReclamo.ABIERTO, "Vuelve a abierto");
        rechaza(() -> reclamo.reabrir("Otra vez"), "Solo se reabre si esta resuelto");

        // Llega a cerrado
        admin.asignarReclamo(reclamo, plomero);
        reclamo.avanzarEstado("Vuelvo");
        reclamo.avanzarEstado("Arreglado");
        reclamo.avanzarEstado("Cierre");
        verificar(reclamo.getEstado() == EstadoReclamo.CERRADO, "Cerrado");
        rechaza(() -> reclamo.avanzarEstado("Mas"), "No avanza despues de cerrado");
        rechaza(() -> reclamo.agregarComentario(titular, "Hola"), "No comenta cerrados");

        // RF09: registrado, asignado, en curso, resuelto, abierto, asignado, en curso, resuelto, cerrado
        verificar(reclamo.getHistorial().size() == 9, "Historial completo");

        // RN05
        for (int i = 0; i < 5; i++) {
            Reclamo r = titular.presentarReclamo(lote, TipoReclamo.MANTENIMIENTO, "Reclamo " + i);
            admin.asignarReclamo(r, plomero);
            r.avanzarEstado("En curso");
        }
        Reclamo sexto = titular.presentarReclamo(lote, TipoReclamo.MANTENIMIENTO, "Sexto");
        admin.asignarReclamo(sexto, plomero);
        rechaza(() -> sexto.avanzarEstado("En curso"), "Maximo 5 en curso");

        // RN06: recien creado no se escala
        verificar(!sexto.debeEscalarse(), "No escala antes de 24 horas");
        rechaza(sexto::cerrarAutomaticamente, "Solo cierra automatico si vencio el plazo");

        System.out.println("PruebaPaso4: " + comprobaciones + " comprobaciones OK");
    }
}
