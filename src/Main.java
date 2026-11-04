import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import modelo.barrio.Barrio;
import modelo.barrio.Lote;
import modelo.barrio.TipoResidente;
import modelo.equipos.Camara;
import modelo.equipos.Equipo;
import modelo.equipos.Falla;
import modelo.equipos.Luminaria;
import modelo.equipos.OrdenMantenimiento;
import modelo.equipos.Sector;
import modelo.equipos.TipoSector;
import modelo.expensas.Expensa;
import modelo.expensas.MedioPago;
import modelo.expensas.Pago;
import modelo.expensas.PresupuestoMensual;
import modelo.personas.Administrador;
import modelo.personas.EmpleadoMantenimiento;
import modelo.personas.Especialidad;
import modelo.personas.Guardia;
import modelo.personas.Persona;
import modelo.personas.Personal;
import modelo.personas.Puesto;
import modelo.personas.Turno;
import modelo.personas.Vecino;
import modelo.reclamos.CambioEstado;
import modelo.reclamos.Comentario;
import modelo.reclamos.EstadoReclamo;
import modelo.reclamos.Reclamo;
import modelo.reclamos.TipoReclamo;
import modelo.visitas.Visita;
import modelo.visitas.VisitaPuntual;
import modelo.visitas.VisitaRecurrente;

// Menu de consola. No hay login: cada opcion pide el documento o legajo de quien la usa.
public class Main {
    private static Scanner teclado = new Scanner(System.in);
    private static Barrio barrio = new Barrio("Los Aromos");
    private static Administrador admin;
    private static List<Vecino> vecinos = new ArrayList<>();

    public static void main(String[] args) {
        cargarDatosDePrueba();
        int opcion;
        do {
            System.out.println();
            System.out.println("===== Barrio " + barrio.getNombre() + " =====");
            System.out.println("1. Reclamos");
            System.out.println("2. Expensas y pagos");
            System.out.println("3. Equipos y mantenimiento");
            System.out.println("4. Visitas e ingresos");
            System.out.println("5. Reportes");
            System.out.println("6. Ejecutar procesos automaticos");
            System.out.println("0. Salir");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: menuReclamos(); break;
                    case 2: menuExpensas(); break;
                    case 3: menuEquipos(); break;
                    case 4: menuVisitas(); break;
                    case 5: menuReportes(); break;
                    case 6: procesosAutomaticos(); break;
                    case 0: System.out.println("Hasta luego."); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    // ===================== Reclamos =====================

    private static void menuReclamos() {
        System.out.println("1. Registrar reclamo");
        System.out.println("2. Asignar reclamo a un empleado");
        System.out.println("3. Avanzar estado");
        System.out.println("4. Reabrir reclamo");
        System.out.println("5. Agregar comentario");
        System.out.println("6. Ver historial");
        System.out.println("7. Consultar reclamos con filtro");
        int opcion = leerEntero("Opcion: ");
        switch (opcion) {
            case 1: {
                Vecino vecino = buscarVecino(leerTexto("Documento del vecino: "));
                Lote lote = buscarLote(leerEntero("Numero de lote: "));
                TipoReclamo tipo = TipoReclamo.valueOf(
                        leerTexto("Tipo (SEGURIDAD, MANTENIMIENTO, CONVIVENCIA, ADMINISTRATIVO): ").toUpperCase());
                Reclamo reclamo = vecino.presentarReclamo(lote, tipo, leerTexto("Descripcion: "));
                System.out.println("Reclamo N° " + reclamo.getNumero() + " registrado con prioridad "
                        + reclamo.getPrioridad() + ".");
                break;
            }
            case 2: {
                Reclamo reclamo = buscarReclamo(leerEntero("Numero de reclamo: "));
                Personal empleado = buscarPersonal(leerEntero("Legajo del empleado: "));
                admin.asignarReclamo(reclamo, empleado);
                System.out.println("Reclamo asignado a " + empleado.getNombreCompleto() + ".");
                break;
            }
            case 3: {
                Reclamo reclamo = buscarReclamo(leerEntero("Numero de reclamo: "));
                reclamo.avanzarEstado(leerTexto("Observacion: "));
                System.out.println("Nuevo estado: " + reclamo.getEstado());
                break;
            }
            case 4: {
                Reclamo reclamo = buscarReclamo(leerEntero("Numero de reclamo: "));
                reclamo.reabrir(leerTexto("Motivo: "));
                System.out.println("Reclamo reabierto.");
                break;
            }
            case 5: {
                Reclamo reclamo = buscarReclamo(leerEntero("Numero de reclamo: "));
                Persona autor = buscarPersona(leerTexto("Documento del autor: "));
                reclamo.agregarComentario(autor, leerTexto("Comentario: "));
                System.out.println("Comentario agregado.");
                break;
            }
            case 6: {
                Reclamo reclamo = buscarReclamo(leerEntero("Numero de reclamo: "));
                System.out.println("Historial del reclamo N° " + reclamo.getNumero() + ":");
                for (CambioEstado cambio : reclamo.getHistorial()) {
                    System.out.println("  " + cambio.getFecha().toLocalDate() + " " + cambio.getEstado()
                            + " - " + cambio.getObservacion());
                }
                System.out.println("Comentarios:");
                for (Comentario comentario : reclamo.getComentarios()) {
                    System.out.println("  " + comentario.getFecha().toLocalDate() + " "
                            + comentario.getAutor().getNombreCompleto() + ": " + comentario.getTexto());
                }
                break;
            }
            case 7:
                consultarReclamos();
                break;
            default:
                System.out.println("Opcion invalida.");
        }
    }

    // RF14
    private static void consultarReclamos() {
        System.out.println("Filtrar por: 1. Estado  2. Lote  3. Tipo  4. Responsable");
        int filtro = leerEntero("Opcion: ");
        String valor = leerTexto("Valor (estado, numero de lote, tipo o legajo): ").toUpperCase();
        int encontrados = 0;
        for (Lote lote : barrio.getLotes()) {
            for (Reclamo reclamo : lote.getReclamos()) {
                boolean coincide;
                switch (filtro) {
                    case 1: coincide = reclamo.getEstado() == EstadoReclamo.valueOf(valor); break;
                    case 2: coincide = lote.getNumero() == Integer.parseInt(valor); break;
                    case 3: coincide = reclamo.getTipo() == TipoReclamo.valueOf(valor); break;
                    case 4: coincide = reclamo.getResponsable() != null
                            && reclamo.getResponsable().getLegajo() == Integer.parseInt(valor); break;
                    default: coincide = false;
                }
                if (coincide) {
                    System.out.println("N° " + reclamo.getNumero() + " | Lote " + lote.getNumero() + " | "
                            + reclamo.getTipo() + " | " + reclamo.getEstado() + " | " + reclamo.getDescripcion());
                    encontrados++;
                }
            }
        }
        if (encontrados == 0) {
            System.out.println("Ningun reclamo coincide.");
        }
    }

    // ===================== Expensas =====================

    private static void menuExpensas() {
        System.out.println("1. Registrar presupuesto y emitir expensas");
        System.out.println("2. Registrar pago");
        System.out.println("3. Registrar asiento de ajuste");
        System.out.println("4. Consultar estado de cuenta");
        int opcion = leerEntero("Opcion: ");
        switch (opcion) {
            case 1: {
                String periodo = leerTexto("Periodo (AAAA-MM): ");
                double monto = leerDecimal("Monto del presupuesto: ");
                barrio.emitirExpensas(new PresupuestoMensual(periodo, monto));
                System.out.println("Expensas emitidas para " + barrio.getLotes().size() + " lotes.");
                break;
            }
            case 2: {
                Lote lote = buscarLote(leerEntero("Numero de lote: "));
                System.out.println("Deuda actual: $" + lote.getDeudaTotal());
                double importe = leerDecimal("Importe: ");
                MedioPago medio = MedioPago.valueOf(
                        leerTexto("Medio de pago (EFECTIVO, TRANSFERENCIA, TARJETA): ").toUpperCase());
                lote.registrarPago(importe, medio);
                System.out.println("Pago registrado. Deuda restante: $" + lote.getDeudaTotal());
                break;
            }
            case 3: {
                Lote lote = buscarLote(leerEntero("Numero de lote: "));
                List<Pago> pagos = new ArrayList<>();
                List<Expensa> expensasDePago = new ArrayList<>();
                for (Expensa expensa : lote.getExpensas()) {
                    for (Pago pago : expensa.getPagos()) {
                        System.out.println((pagos.size() + 1) + ". " + expensa.getPeriodo() + " - "
                                + pago.getFecha() + " $" + pago.getImporte());
                        pagos.add(pago);
                        expensasDePago.add(expensa);
                    }
                }
                if (pagos.isEmpty()) {
                    System.out.println("El lote no tiene pagos.");
                    break;
                }
                int indice = leerEntero("Pago a corregir: ") - 1;
                double importe = leerDecimal("Importe del ajuste (negativo para restar): ");
                String motivo = leerTexto("Motivo: ");
                expensasDePago.get(indice).registrarAjuste(pagos.get(indice), LocalDate.now(), importe, motivo);
                System.out.println("Ajuste registrado. Deuda actual: $" + lote.getDeudaTotal());
                break;
            }
            case 4: {
                Lote lote = buscarLote(leerEntero("Numero de lote: "));
                System.out.println("Estado de cuenta del lote " + lote.getNumero() + ":");
                for (Expensa expensa : lote.getExpensas()) {
                    System.out.println("  " + expensa.getPeriodo() + " | Importe $" + expensa.getImporte()
                            + " | Vence " + expensa.getFechaVencimiento() + " | " + expensa.getEstado()
                            + " | Interes $" + expensa.calcularInteres(LocalDate.now())
                            + " | Saldo $" + expensa.getSaldo());
                    for (Pago pago : expensa.getPagos()) {
                        System.out.println("      Pago " + pago.getFecha() + " $" + pago.getImporte()
                                + " (" + pago.getMedioPago() + ")");
                    }
                }
                System.out.println("Deuda total: $" + lote.getDeudaTotal()
                        + (lote.esMoroso() ? " - MOROSO" : ""));
                break;
            }
            default:
                System.out.println("Opcion invalida.");
        }
    }

    // ===================== Equipos =====================

    private static void menuEquipos() {
        System.out.println("1. Registrar falla");
        System.out.println("2. Generar orden de mantenimiento");
        System.out.println("3. Cerrar orden de mantenimiento");
        System.out.println("4. Consultar equipo e historial");
        int opcion = leerEntero("Opcion: ");
        switch (opcion) {
            case 1: {
                Equipo equipo = buscarEquipo(leerTexto("Codigo del equipo: "));
                Persona persona = buscarPersona(leerTexto("Documento de quien reporta: "));
                equipo.registrarFalla(persona, leerTexto("Descripcion de la falla: "));
                System.out.println("Falla registrada. Estado: " + equipo.getEstado());
                break;
            }
            case 2: {
                Equipo equipo = buscarEquipo(leerTexto("Codigo del equipo: "));
                int legajo = leerEntero("Legajo del empleado de mantenimiento (0 para dejarla sin asignar): ");
                EmpleadoMantenimiento empleado = legajo == 0 ? null : buscarEmpleadoMantenimiento(legajo);
                OrdenMantenimiento orden = admin.generarOrden(equipo.getUltimaFalla(), empleado);
                System.out.println("Orden N° " + orden.getNumero() + " generada con prioridad "
                        + orden.getPrioridad() + ".");
                break;
            }
            case 3: {
                Equipo equipo = buscarEquipo(leerTexto("Codigo del equipo: "));
                OrdenMantenimiento orden = buscarOrdenAbierta(equipo);
                EmpleadoMantenimiento tecnico = buscarEmpleadoMantenimiento(leerEntero("Legajo del tecnico: "));
                String descripcion = leerTexto("Descripcion de la reparacion: ");
                if (leerTexto("Se pudo reparar? (S/N): ").equalsIgnoreCase("S")) {
                    orden.cerrar(LocalDate.now(), tecnico, descripcion);
                } else {
                    orden.cerrarSinReparar(LocalDate.now(), tecnico, descripcion);
                }
                System.out.println("Orden cerrada. Estado del equipo: " + equipo.getEstado());
                break;
            }
            case 4: {
                Equipo equipo = buscarEquipo(leerTexto("Codigo del equipo: "));
                System.out.println(equipo.getCodigo() + " | Sector: " + equipo.getSector().getNombre()
                        + " | Estado: " + equipo.getEstado());
                for (OrdenMantenimiento orden : equipo.getOrdenes()) {
                    System.out.println("  Orden N° " + orden.getNumero() + " | Apertura " + orden.getFechaApertura()
                            + " | " + orden.getPrioridad() + " | " + orden.getDescripcionTrabajo()
                            + (orden.estaCerrada() ? " | Cerrada " + orden.getFechaCierre()
                            + ": " + orden.getDescripcionReparacion() : " | Abierta"));
                }
                break;
            }
            default:
                System.out.println("Opcion invalida.");
        }
    }

    // ===================== Visitas =====================

    private static void menuVisitas() {
        System.out.println("1. Autorizar visita puntual");
        System.out.println("2. Autorizar visita recurrente (proveedor)");
        System.out.println("3. Consultar autorizacion vigente");
        System.out.println("4. Registrar ingreso");
        System.out.println("5. Registrar egreso");
        int opcion = leerEntero("Opcion: ");
        switch (opcion) {
            case 1: {
                Vecino vecino = buscarVecino(leerTexto("Documento del vecino: "));
                Lote lote = buscarLote(leerEntero("Numero de lote: "));
                String nombre = leerTexto("Nombre del visitante: ");
                String documento = leerTexto("Documento del visitante: ");
                LocalDate fecha = LocalDate.parse(leerTexto("Fecha (AAAA-MM-DD): "));
                LocalTime desde = LocalTime.parse(leerTexto("Desde (HH:MM): "));
                LocalTime hasta = LocalTime.parse(leerTexto("Hasta (HH:MM): "));
                vecino.autorizarVisita(new VisitaPuntual(nombre, documento, lote, fecha, desde, hasta));
                System.out.println("Visita autorizada.");
                break;
            }
            case 2: {
                Vecino vecino = buscarVecino(leerTexto("Documento del vecino: "));
                Lote lote = buscarLote(leerEntero("Numero de lote: "));
                String nombre = leerTexto("Nombre del proveedor: ");
                String documento = leerTexto("Documento del proveedor: ");
                LocalDate desdeFecha = LocalDate.parse(leerTexto("Vigente desde (AAAA-MM-DD): "));
                LocalDate hastaFecha = LocalDate.parse(leerTexto("Vigente hasta (AAAA-MM-DD): "));
                String[] numeros = leerTexto("Dias (1=lunes ... 7=domingo, separados por coma): ").split(",");
                List<DayOfWeek> dias = new ArrayList<>();
                for (String numero : numeros) {
                    dias.add(DayOfWeek.of(Integer.parseInt(numero.trim())));
                }
                LocalTime desde = LocalTime.parse(leerTexto("Desde (HH:MM): "));
                LocalTime hasta = LocalTime.parse(leerTexto("Hasta (HH:MM): "));
                vecino.autorizarVisita(new VisitaRecurrente(nombre, documento, lote,
                        desdeFecha, hastaFecha, dias, desde, hasta));
                System.out.println("Visita recurrente autorizada.");
                break;
            }
            case 3: {
                Visita visita = barrio.buscarVisitaVigente(leerTexto("Documento: "), LocalDateTime.now());
                if (visita == null) {
                    System.out.println("No hay autorizacion vigente.");
                } else {
                    System.out.println("Autorizado: " + visita.getNombreVisitante()
                            + " | Lote " + visita.getLote().getNumero());
                }
                break;
            }
            case 4: {
                Guardia guardia = buscarGuardia(leerEntero("Legajo del guardia: "));
                Visita visita = barrio.buscarVisitaVigente(leerTexto("Documento del visitante: "), LocalDateTime.now());
                if (visita == null) {
                    System.out.println("Ingreso rechazado: no hay autorizacion vigente.");
                } else {
                    guardia.registrarIngreso(visita);
                    System.out.println("Ingreso registrado: " + visita.getNombreVisitante()
                            + " al lote " + visita.getLote().getNumero() + ".");
                }
                break;
            }
            case 5: {
                Guardia guardia = buscarGuardia(leerEntero("Legajo del guardia: "));
                guardia.registrarEgreso(leerTexto("Documento del visitante: "));
                System.out.println("Egreso registrado.");
                break;
            }
            default:
                System.out.println("Opcion invalida.");
        }
    }

    // ===================== Reportes y procesos =====================

    private static void menuReportes() {
        System.out.println("1. Reclamos pendientes");
        System.out.println("2. Morosidad por lote");
        System.out.println("3. Equipos fuera de servicio");
        int opcion = leerEntero("Opcion: ");
        switch (opcion) {
            case 1: admin.getReportes().reporteReclamosPendientes(); break;
            case 2: admin.getReportes().reporteMorosidad(); break;
            case 3: admin.getReportes().reporteEquiposFueraDeServicio(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    // Supuesto: los procesos automaticos se ejecutan desde el menu
    private static void procesosAutomaticos() {
        System.out.println("=== Procesos automaticos ===");
        for (Lote lote : barrio.getLotes()) {
            for (Reclamo reclamo : lote.getReclamos()) {
                // RF12
                if (reclamo.debeCerrarse()) {
                    reclamo.cerrarAutomaticamente();
                    System.out.println("Reclamo N° " + reclamo.getNumero() + " cerrado automaticamente.");
                }
                // RF13
                if (reclamo.debeEscalarse()) {
                    reclamo.escalar();
                    System.out.println("Aviso al administrador: reclamo N° " + reclamo.getNumero()
                            + " de prioridad alta sin atender hace mas de 24 horas.");
                }
            }
            // RF20
            int notificacionesPrevias = lote.getNotificaciones().size();
            lote.actualizarMorosidad();
            for (int i = notificacionesPrevias; i < lote.getNotificaciones().size(); i++) {
                System.out.println(lote.getNotificaciones().get(i));
            }
        }
        // RF26
        for (Sector sector : barrio.getSectores()) {
            for (Equipo equipo : sector.getEquipos()) {
                if (equipo instanceof Camara && ((Camara) equipo).superaLimiteSinFuncionar()) {
                    System.out.println("Aviso al administrador: camara " + equipo.getCodigo()
                            + " del sector " + sector.getNombre() + " lleva mas de 48 horas sin funcionar.");
                }
            }
        }
        System.out.println("Procesos terminados.");
    }

    // ===================== Busquedas =====================

    private static Vecino buscarVecino(String documento) {
        for (Vecino vecino : vecinos) {
            if (vecino.getDocumento().equals(documento)) {
                return vecino;
            }
        }
        throw new IllegalArgumentException("No existe un vecino con ese documento.");
    }

    private static Personal buscarPersonal(int legajo) {
        for (Personal empleado : admin.getPersonal()) {
            if (empleado.getLegajo() == legajo) {
                return empleado;
            }
        }
        throw new IllegalArgumentException("No existe un empleado con ese legajo.");
    }

    private static Guardia buscarGuardia(int legajo) {
        Personal empleado = buscarPersonal(legajo);
        if (!(empleado instanceof Guardia)) {
            throw new IllegalArgumentException("El legajo no es de un guardia.");
        }
        return (Guardia) empleado;
    }

    private static EmpleadoMantenimiento buscarEmpleadoMantenimiento(int legajo) {
        Personal empleado = buscarPersonal(legajo);
        if (!(empleado instanceof EmpleadoMantenimiento)) {
            throw new IllegalArgumentException("El legajo no es de un empleado de mantenimiento.");
        }
        return (EmpleadoMantenimiento) empleado;
    }

    private static Persona buscarPersona(String documento) {
        if (admin.getDocumento().equals(documento)) {
            return admin;
        }
        for (Personal empleado : admin.getPersonal()) {
            if (empleado.getDocumento().equals(documento)) {
                return empleado;
            }
        }
        return buscarVecino(documento);
    }

    private static Lote buscarLote(int numero) {
        Lote lote = barrio.buscarLote(numero);
        if (lote == null) {
            throw new IllegalArgumentException("No existe el lote.");
        }
        return lote;
    }

    private static Reclamo buscarReclamo(int numero) {
        for (Lote lote : barrio.getLotes()) {
            for (Reclamo reclamo : lote.getReclamos()) {
                if (reclamo.getNumero() == numero) {
                    return reclamo;
                }
            }
        }
        throw new IllegalArgumentException("No existe el reclamo.");
    }

    private static Equipo buscarEquipo(String codigo) {
        for (Sector sector : barrio.getSectores()) {
            for (Equipo equipo : sector.getEquipos()) {
                if (equipo.getCodigo().equalsIgnoreCase(codigo)) {
                    return equipo;
                }
            }
        }
        throw new IllegalArgumentException("No existe el equipo.");
    }

    private static OrdenMantenimiento buscarOrdenAbierta(Equipo equipo) {
        for (OrdenMantenimiento orden : equipo.getOrdenes()) {
            if (!orden.estaCerrada()) {
                return orden;
            }
        }
        throw new IllegalArgumentException("El equipo no tiene una orden abierta.");
    }

    // ===================== Lectura de teclado =====================

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!teclado.hasNextLine()) {
            return "0";
        }
        return teclado.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero.");
            }
        }
    }

    private static double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero.");
            }
        }
    }

    // ===================== Datos de prueba =====================

    private static void cargarDatosDePrueba() {
        admin = new Administrador("Carla", "Diaz", "20000000", "1140000000", "carla@losaromos.com", barrio);

        Vecino ana = new Vecino("Ana", "Perez", "30111222", "1141111111", "ana@mail.com");
        Vecino juan = new Vecino("Juan", "Perez", "30333444", "1142222222", "juan@mail.com");
        Vecino luis = new Vecino("Luis", "Gomez", "28555666", "1143333333", "luis@mail.com");
        Vecino sofia = new Vecino("Sofia", "Martinez", "35777888", "1144444444", "sofia@mail.com");
        vecinos.add(ana);
        vecinos.add(juan);
        vecinos.add(luis);
        vecinos.add(sofia);

        Guardia pedro = new Guardia("Pedro", "Lopez", "25000001", "1150000001", "pedro@losaromos.com",
                1, LocalDate.of(2022, 3, 1), Turno.MAÑANA, Puesto.PORTERIA);
        Guardia mario = new Guardia("Mario", "Sosa", "25000002", "1150000002", "mario@losaromos.com",
                2, LocalDate.of(2023, 5, 15), Turno.NOCHE, Puesto.RONDA);
        EmpleadoMantenimiento rosa = new EmpleadoMantenimiento("Rosa", "Ruiz", "25000003", "1150000003",
                "rosa@losaromos.com", 3, LocalDate.of(2021, 8, 1), Especialidad.ELECTRICIDAD);
        EmpleadoMantenimiento jorge = new EmpleadoMantenimiento("Jorge", "Diaz", "25000004", "1150000004",
                "jorge@losaromos.com", 4, LocalDate.of(2024, 2, 10), Especialidad.PLOMERIA);
        admin.agregarPersonal(pedro);
        admin.agregarPersonal(mario);
        admin.agregarPersonal(rosa);
        admin.agregarPersonal(jorge);

        // Lotes: Ana es titular de dos, Juan es familiar autorizado y Sofia es inquilina
        Lote lote1 = new Lote(1, 300, ana);
        Lote lote2 = new Lote(2, 500, ana);
        Lote lote3 = new Lote(3, 400, luis);
        barrio.agregarLote(lote1);
        barrio.agregarLote(lote2);
        barrio.agregarLote(lote3);
        lote1.agregarResidente(juan, TipoResidente.FAMILIAR);
        lote1.autorizarPersona(ana, juan);
        lote3.agregarResidente(sofia, TipoResidente.INQUILINO);

        // Expensas de junio a octubre: los lotes 1 y 2 pagan, el lote 3 queda moroso
        for (int mes = 6; mes <= 10; mes++) {
            barrio.emitirExpensas(new PresupuestoMensual("2026-" + (mes < 10 ? "0" + mes : mes), 1200000));
        }
        lote1.registrarPago(lote1.getDeudaTotal(), MedioPago.TRANSFERENCIA);
        lote2.registrarPago(lote2.getDeudaTotal() / 2, MedioPago.EFECTIVO);
        lote3.actualizarMorosidad();

        // Reclamos en distintos estados
        Reclamo perdida = ana.presentarReclamo(lote1, TipoReclamo.MANTENIMIENTO, "Perdida de agua en la vereda");
        admin.asignarReclamo(perdida, jorge);
        perdida.avanzarEstado("Jorge va a revisar la canilla");
        juan.presentarReclamo(lote1, TipoReclamo.SEGURIDAD, "El porton de servicio queda abierto");
        Reclamo ruidos = luis.presentarReclamo(lote3, TipoReclamo.CONVIVENCIA, "Ruidos molestos de noche");
        admin.asignarReclamo(ruidos, mario);

        // Sectores y equipos
        Sector acceso = new Sector("Acceso principal", TipoSector.ACCESO);
        Sector calle = new Sector("Calle 1", TipoSector.CALLE_INTERNA);
        Sector plaza = new Sector("Plaza central", TipoSector.ESPACIO_COMUN);
        barrio.agregarSector(acceso);
        barrio.agregarSector(calle);
        barrio.agregarSector(plaza);
        new Camara("CAM-1", acceso, LocalDate.of(2024, 1, 10), "1080p", 30);
        Camara cam2 = new Camara("CAM-2", plaza, LocalDate.of(2024, 1, 10), "720p", 15);
        Luminaria luz1 = new Luminaria("LUZ-1", calle, LocalDate.of(2023, 6, 1), "LED", 60);
        new Luminaria("LUZ-2", calle, LocalDate.of(2023, 6, 1), "LED", 60);
        new Luminaria("LUZ-3", plaza, LocalDate.of(2023, 6, 1), "Sodio", 150);
        Falla fallaCamara = cam2.registrarFalla(pedro, "No transmite imagen");
        admin.generarOrden(fallaCamara, rosa);
        luz1.registrarFalla(ana, "No enciende");

        // Visitas: una puntual para hoy y un jardinero recurrente de lunes a viernes
        LocalDate hoy = LocalDate.now();
        ana.autorizarVisita(new VisitaPuntual("Marta Lopez", "40123456", lote1, hoy,
                LocalTime.of(0, 0), LocalTime.of(23, 59)));
        ana.autorizarVisita(new VisitaRecurrente("Carlos Jardinero", "40654321", lote2, hoy, hoy.plusDays(30),
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY), LocalTime.of(8, 0), LocalTime.of(18, 0)));
    }
}
