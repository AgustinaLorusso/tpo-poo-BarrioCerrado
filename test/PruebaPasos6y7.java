import modelo.barrio.*;
import modelo.equipos.*;
import modelo.personas.*;
import modelo.reclamos.Prioridad;
import modelo.visitas.*;
import java.time.*;
import java.util.List;

/** Prueba de los pasos 6 y 7: equipos, mantenimiento y visitas. */
public class PruebaPasos6y7 {
    private static int comprobaciones;

    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) { throw new AssertionError(mensaje); }
    }

    private static void rechaza(Runnable accion, String mensaje) {
        try { accion.run(); } catch (IllegalArgumentException | IllegalStateException e) {
            comprobaciones++; return;
        }
        throw new AssertionError(mensaje);
    }

    public static void main(String[] args) {
        Vecino titular = new Vecino("Ana", "Perez", "123", "111", "ana@example.com");
        Vecino ajeno = new Vecino("Luis", "Gomez", "789", "333", "luis@example.com");
        Administrador admin = new Administrador("Carla", "Diaz", "999", "444", "carla@example.com");
        Guardia portero = new Guardia("Pedro", "Lopez", "555", "666", "pedro@example.com",
                1, LocalDate.of(2025, 1, 1), Turno.MAÑANA, Puesto.PORTERIA);
        Guardia ronda = new Guardia("Mario", "Sosa", "556", "667", "mario@example.com",
                3, LocalDate.of(2025, 1, 1), Turno.NOCHE, Puesto.RONDA);
        EmpleadoMantenimiento electricista = new EmpleadoMantenimiento("Rosa", "Ruiz", "777", "888",
                "rosa@example.com", 2, LocalDate.of(2025, 1, 1), Especialidad.ELECTRICIDAD);
        Barrio barrio = new Barrio("Los Aromos");
        Lote lote = new Lote(1, 300, titular);
        barrio.agregarLote(lote);

        // ===== Paso 6 =====
        Sector calle = new Sector("Calle 1", TipoSector.CALLE_INTERNA);
        barrio.agregarSector(calle);
        Camara camara = new Camara("CAM-1", calle, LocalDate.of(2025, 1, 1), "1080p", 30);
        verificar(camara.getEstado() == EstadoEquipo.OPERATIVO, "Equipo nuevo operativo");
        verificar(calle.getEquipos().size() == 1, "Sector conoce sus equipos");

        // RF22
        Falla fallaCamara = camara.registrarFalla(portero, "No graba");
        verificar(camara.getEstado() == EstadoEquipo.CON_FALLA, "Pasa a con falla");
        rechaza(() -> camara.registrarFalla(titular, "Otra"), "No duplica la falla");
        verificar(!camara.superaLimiteSinFuncionar(), "Todavia no pasaron 48 horas");

        // RF23, RN14
        OrdenMantenimiento orden = admin.generarOrden(fallaCamara, electricista);
        verificar(orden.getPrioridad() == Prioridad.ALTA, "Camara es prioridad alta");
        verificar(camara.getEstado() == EstadoEquipo.EN_REPARACION, "Asignada queda en reparacion");
        rechaza(() -> admin.generarOrden(fallaCamara, electricista), "Una orden abierta por equipo");

        // RN16, RF24
        rechaza(() -> orden.cerrar(LocalDate.now(), electricista, ""), "Cierre sin descripcion");
        rechaza(() -> orden.cerrar(LocalDate.now(), null, "Listo"), "Cierre sin tecnico");
        orden.cerrar(LocalDate.now(), electricista, "Cambie la fuente");
        verificar(orden.estaCerrada(), "Orden cerrada");
        verificar(camara.getEstado() == EstadoEquipo.OPERATIVO, "Vuelve a operativo");
        verificar(camara.getOrdenes().size() == 1, "Historial de ordenes");

        // RF24 4a
        Falla otra = camara.registrarFalla(titular, "Lente roto");
        admin.generarOrden(otra, electricista).cerrarSinReparar(LocalDate.now(), electricista, "Sin repuesto");
        verificar(camara.getEstado() == EstadoEquipo.FUERA_DE_SERVICIO, "Queda fuera de servicio");

        // RN14 luminarias: normal hasta 3 con falla, alta con mas de 3
        Luminaria[] luces = new Luminaria[4];
        for (int i = 0; i < 4; i++) {
            luces[i] = new Luminaria("LUZ-" + i, calle, LocalDate.of(2025, 1, 1), "LED", 50);
        }
        Falla f1 = luces[0].registrarFalla(titular, "No prende");
        verificar(admin.generarOrden(f1, null).getPrioridad() == Prioridad.NORMAL, "Luminaria es normal");
        verificar(luces[0].getEstado() == EstadoEquipo.CON_FALLA, "Sin empleado sigue con falla");
        luces[1].registrarFalla(titular, "No prende");
        luces[2].registrarFalla(titular, "No prende");
        Falla f4 = luces[3].registrarFalla(titular, "No prende");
        verificar(calle.contarLuminariasConFalla() == 4, "Cuenta luminarias con falla");
        verificar(admin.generarOrden(f4, electricista).getPrioridad() == Prioridad.ALTA, "Mas de 3 pasa a alta");

        // ===== Paso 7 =====
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDateTime mananaDiez = manana.atTime(10, 0);

        // RF27, RN17
        rechaza(() -> new VisitaPuntual("Juan", "111", lote, LocalDate.now().minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(12, 0)), "Fecha pasada");
        rechaza(() -> new VisitaPuntual("Juan", "111", lote, manana,
                LocalTime.of(12, 0), LocalTime.of(9, 0)), "Franja invalida");
        VisitaPuntual visita = new VisitaPuntual("Juan", "111", lote, manana,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        rechaza(() -> ajeno.autorizarVisita(visita), "Solo autoriza un vecino del lote");
        titular.autorizarVisita(visita);
        verificar(visita.estaVigente(mananaDiez), "Vigente en la franja");
        verificar(!visita.estaVigente(manana.atTime(13, 0)), "Fuera de la franja");
        verificar(barrio.buscarVisitaVigente("111", mananaDiez) == visita, "El guardia la encuentra");
        verificar(barrio.buscarVisitaVigente("000", mananaDiez) == null, "Sin autorizacion");

        // RF30, RN19
        rechaza(() -> portero.registrarIngreso(visita, manana.atTime(13, 0)), "No entra sin vigencia");
        rechaza(() -> ronda.registrarIngreso(visita, mananaDiez), "Solo el guardia de porteria");
        RegistroIngreso ingreso = portero.registrarIngreso(visita, mananaDiez);
        verificar(ingreso.estaAdentro(), "Esta adentro");
        rechaza(() -> portero.registrarIngreso(visita, mananaDiez.plusMinutes(5)), "No entra dos veces sin salir");

        // RF31
        portero.registrarEgreso("111", mananaDiez.plusHours(1));
        verificar(!ingreso.estaAdentro(), "Salio");
        rechaza(() -> portero.registrarEgreso("111", mananaDiez.plusHours(2)), "No hay ingreso abierto");
        portero.registrarIngreso(visita, mananaDiez.plusHours(1).plusMinutes(30));
        verificar(visita.getRegistros().size() == 2, "Vuelve a entrar despues de salir");

        // RF28, RN18
        LocalDate hoy = LocalDate.now();
        rechaza(() -> new VisitaRecurrente("Jardinero", "222", lote, hoy, hoy.plusDays(31),
                List.of(DayOfWeek.MONDAY), LocalTime.of(8, 0), LocalTime.of(17, 0)), "Maximo 30 dias");
        VisitaRecurrente jardinero = new VisitaRecurrente("Jardinero", "222", lote, hoy, hoy.plusDays(30),
                List.of(hoy.getDayOfWeek()), LocalTime.of(8, 0), LocalTime.of(17, 0));
        titular.autorizarVisita(jardinero);
        verificar(jardinero.validarDuracion(), "Duracion valida");
        verificar(jardinero.estaVigente(hoy.atTime(9, 0)), "Vigente el dia indicado");
        verificar(!jardinero.estaVigente(hoy.plusDays(1).atTime(9, 0)), "No vigente otro dia de la semana");
        verificar(jardinero.estaVigente(hoy.plusDays(7).atTime(9, 0)), "Vigente la semana siguiente");
        verificar(!jardinero.estaVigente(hoy.plusDays(35).atTime(9, 0)), "Vencida");

        System.out.println("PruebaPasos6y7: " + comprobaciones + " comprobaciones OK");
    }
}
