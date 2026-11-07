import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import modelo.barrio.Barrio;
import modelo.barrio.Lote;
import modelo.barrio.TipoResidente;
import modelo.equipos.Camara;
import modelo.equipos.Equipo;
import modelo.equipos.EstadoEquipo;
import modelo.equipos.Falla;
import modelo.equipos.Luminaria;
import modelo.equipos.OrdenMantenimiento;
import modelo.equipos.Sector;
import modelo.equipos.TipoSector;
import modelo.excepciones.ReglaNegocioException;
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
import persistencia.DatosSistema;
import persistencia.Persistencia;

// Menu de consola con inicio de sesion: cada persona ve solo las opciones de su rol.
// Supuesto: se inicia sesion solo con el documento, sin contraseña.
public class Main {
    private static Scanner teclado = new Scanner(System.in);
    private static Barrio barrio = new Barrio("Los Aromos");
    private static Administrador admin;
    private static List<Vecino> vecinos = new ArrayList<>();
    private static Persona usuario;   // quien inicio sesion

    public static void main(String[] args) {
        cargarDatos();
        while (iniciarSesion()) {
            if (usuario instanceof Administrador) {
                menuAdministrador();
            } else if (usuario instanceof Guardia) {
                menuGuardia((Guardia) usuario);
            } else if (usuario instanceof EmpleadoMantenimiento) {
                menuMantenimiento((EmpleadoMantenimiento) usuario);
            } else {
                menuVecino((Vecino) usuario);
            }
            guardarDatos();   // se guarda al cerrar cada sesion
        }
        guardarDatos();
        System.out.println("Hasta luego.");
    }

    // Si hay datos guardados se usan; si no, se cargan los datos de prueba
    private static void cargarDatos() {
        DatosSistema datos = Persistencia.existenDatos() ? Persistencia.cargar() : null;
        if (datos == null) {
            cargarDatosDePrueba();
            System.out.println("Se cargaron los datos de prueba.");
            return;
        }
        datos.restaurarEstaticos();
        admin = datos.getAdmin();
        barrio = admin.getBarrio();
        vecinos = datos.getVecinos();
        System.out.println("Se cargaron los datos guardados.");
    }

    private static void guardarDatos() {
        if (Persistencia.guardar(new DatosSistema(admin, vecinos))) {
            System.out.println("Datos guardados.");
        }
    }

    // Devuelve false si el usuario elige salir del sistema
    private static boolean iniciarSesion() {
        System.out.println();
        System.out.println("===== Barrio " + barrio.getNombre() + " =====");
        while (true) {
            String documento = leerTexto("Documento para iniciar sesion (0 para salir): ");
            if (documento.equals("0")) {
                return false;
            }
            try {
                usuario = buscarPersona(documento);
                System.out.println("Bienvenido/a, " + usuario.getNombreCompleto() + ".");
                return true;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ===================== Menus por rol =====================

    private static void menuVecino(Vecino vecino) {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== Vecino: " + vecino.getNombreCompleto() + " ===");
            System.out.println("1. Registrar reclamo");
            System.out.println("2. Reabrir reclamo");
            System.out.println("3. Agregar comentario a un reclamo");
            System.out.println("4. Ver historial de un reclamo");
            System.out.println("5. Consultar estado de cuenta");
            System.out.println("6. Registrar falla de un equipo");
            System.out.println("7. Autorizar visita puntual");
            System.out.println("8. Autorizar visita recurrente (proveedor)");
            System.out.println("9. Registrar persona autorizada a reclamar");
            System.out.println("0. Cerrar sesion");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarReclamo(vecino); break;
                    case 2: reabrirReclamo(); break;
                    case 3: agregarComentario(); break;
                    case 4: verHistorial(); break;
                    case 5: consultarEstadoDeCuenta(); break;
                    case 6: registrarFalla(); break;
                    case 7: autorizarVisitaPuntual(vecino); break;
                    case 8: autorizarVisitaRecurrente(vecino); break;
                    case 9: registrarPersonaAutorizada(vecino); break;
                    case 0: System.out.println("Sesion cerrada."); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void menuGuardia(Guardia guardia) {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== Guardia: " + guardia.getNombreCompleto() + " (" + guardia.getPuesto() + ") ===");
            System.out.println("1. Consultar autorizacion vigente");
            System.out.println("2. Registrar ingreso");
            System.out.println("3. Registrar egreso");
            System.out.println("4. Registrar falla de un equipo");
            System.out.println("5. Reclamos");
            System.out.println("0. Cerrar sesion");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: consultarAutorizacion(); break;
                    case 2: registrarIngreso(guardia); break;
                    case 3: registrarEgreso(guardia); break;
                    case 4: registrarFalla(); break;
                    case 5: menuReclamosPersonal(guardia); break;
                    case 0: System.out.println("Sesion cerrada."); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    // Submenu de reclamos para el personal: solo trabaja sobre sus reclamos asignados
    private static void menuReclamosPersonal(Personal empleado) {
        System.out.println("1. Ver mis reclamos asignados");
        System.out.println("2. Avanzar estado de un reclamo");
        System.out.println("3. Agregar comentario a un reclamo");
        System.out.println("4. Ver historial de un reclamo");
        switch (leerEntero("Opcion: ")) {
            case 1: verMisReclamos(empleado); break;
            case 2: avanzarEstado(); break;
            case 3: agregarComentario(); break;
            case 4: verHistorial(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    private static void menuMantenimiento(EmpleadoMantenimiento empleado) {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== Mantenimiento: " + empleado.getNombreCompleto()
                    + " (" + empleado.getEspecialidad() + ") ===");
            System.out.println("1. Ver mis reclamos asignados");
            System.out.println("2. Avanzar estado de un reclamo");
            System.out.println("3. Agregar comentario a un reclamo");
            System.out.println("4. Ver historial de un reclamo");
            System.out.println("5. Cerrar orden de mantenimiento");
            System.out.println("6. Consultar equipo e historial");
            System.out.println("0. Cerrar sesion");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: verMisReclamos(empleado); break;
                    case 2: avanzarEstado(); break;
                    case 3: agregarComentario(); break;
                    case 4: verHistorial(); break;
                    case 5: cerrarOrden(empleado); break;
                    case 6: consultarEquipo(); break;
                    case 0: System.out.println("Sesion cerrada."); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void menuAdministrador() {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== Administracion: " + admin.getNombreCompleto() + " ===");
            System.out.println("1. Reclamos");
            System.out.println("2. Expensas y pagos");
            System.out.println("3. Equipos y mantenimiento");
            System.out.println("4. Reportes");
            System.out.println("5. Ejecutar procesos automaticos");
            System.out.println("6. Lotes y vecinos");
            System.out.println("0. Cerrar sesion");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: menuReclamosAdministrador(); break;
                    case 2: menuExpensas(); break;
                    case 3: menuEquiposAdministrador(); break;
                    case 4: menuReportes(); break;
                    case 5: procesosAutomaticos(); break;
                    case 6: menuLotesYVecinos(); break;
                    case 0: System.out.println("Sesion cerrada."); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void menuReclamosAdministrador() {
        System.out.println("1. Asignar reclamo a un empleado");
        System.out.println("2. Avanzar estado");
        System.out.println("3. Ver historial");
        System.out.println("4. Consultar reclamos con filtro");
        switch (leerEntero("Opcion: ")) {
            case 1: asignarReclamo(); break;
            case 2: avanzarEstado(); break;
            case 3: verHistorial(); break;
            case 4: consultarReclamos(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    private static void menuExpensas() {
        System.out.println("1. Registrar presupuesto y emitir expensas");
        System.out.println("2. Registrar pago");
        System.out.println("3. Registrar asiento de ajuste");
        System.out.println("4. Consultar estado de cuenta");
        switch (leerEntero("Opcion: ")) {
            case 1: emitirExpensas(); break;
            case 2: registrarPago(); break;
            case 3: registrarAjuste(); break;
            case 4: consultarEstadoDeCuenta(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    private static void menuLotesYVecinos() {
        System.out.println("1. Dar de alta vecino");
        System.out.println("2. Modificar vecino");
        System.out.println("3. Dar de baja vecino");
        System.out.println("4. Dar de alta lote");
        System.out.println("5. Modificar lote");
        System.out.println("6. Dar de baja lote");
        System.out.println("7. Asignar rol en lote");
        switch (leerEntero("Opcion: ")) {
            case 1: darDeAltaVecino(); break;
            case 2: modificarVecino(); break;
            case 3: darDeBajaVecino(); break;
            case 4: darDeAltaLote(); break;
            case 5: modificarLote(); break;
            case 6: darDeBajaLote(); break;
            case 7: asignarRolEnLote(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    // RF02
    private static void darDeAltaVecino() {
        String documento = leerTexto("Documento: ");
        if (existePersona(documento)) {
            throw new IllegalArgumentException("Ya existe una persona con ese documento.");
        }
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String telefono = leerTexto("Telefono: ");
        String email = leerTexto("Email: ");
        Vecino vecino = admin.darDeAltaVecino(nombre, apellido, documento, telefono, email);
        vecinos.add(vecino);
        System.out.println("Vecino " + vecino.getNombreCompleto() + " dado de alta.");
    }

    // RF01
    private static void darDeAltaLote() {
        int numero = leerEntero("Numero de lote: ");
        double superficie = leerDecimal("Superficie (m2): ");
        Vecino titular = buscarVecino(leerTexto("Documento del titular: "));
        Lote lote = admin.darDeAltaLote(numero, superficie, titular);
        System.out.println("Lote " + lote.getNumero() + " dado de alta. Titular: "
                + titular.getNombreCompleto() + ".");
    }

    // RF02: el documento no se modifica porque identifica a la persona
    private static void modificarVecino() {
        Vecino vecino = buscarVecino(leerTexto("Documento del vecino: "));
        System.out.println("Deje vacio para no cambiar el dato.");
        String nombre = leerTexto("Nombre (" + vecino.getNombre() + "): ");
        String apellido = leerTexto("Apellido (" + vecino.getApellido() + "): ");
        String telefono = leerTexto("Telefono (" + vecino.getTelefono() + "): ");
        String email = leerTexto("Email (" + vecino.getEmail() + "): ");
        if (!nombre.isEmpty()) { vecino.setNombre(nombre); }
        if (!apellido.isEmpty()) { vecino.setApellido(apellido); }
        if (!telefono.isEmpty()) { vecino.setTelefono(telefono); }
        if (!email.isEmpty()) { vecino.setEmail(email); }
        System.out.println("Datos actualizados: " + vecino.getNombreCompleto() + ".");
    }

    // RF02
    private static void darDeBajaVecino() {
        Vecino vecino = buscarVecino(leerTexto("Documento del vecino: "));
        admin.darDeBajaVecino(vecino);
        vecinos.remove(vecino);
        System.out.println("Vecino " + vecino.getNombreCompleto() + " dado de baja.");
    }

    // RF01: se puede cambiar la superficie y el titular
    private static void modificarLote() {
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        System.out.println("Deje vacio para no cambiar el dato.");
        String superficie = leerTexto("Superficie en m2 (" + lote.getSuperficie() + "): ");
        String documento = leerTexto("Documento del titular (" + lote.getTitular().getNombreCompleto() + "): ");
        // Primero se valida todo y despues se cambia, para no dejar el lote a medio modificar
        Vecino nuevoTitular = documento.isEmpty() ? null : buscarVecino(documento);
        if (!superficie.isEmpty()) { lote.setSuperficie(Double.parseDouble(superficie)); }
        if (nuevoTitular != null) { lote.setTitular(nuevoTitular); }
        System.out.println("Lote " + lote.getNumero() + " actualizado. Superficie: " + lote.getSuperficie()
                + " m2. Titular: " + lote.getTitular().getNombreCompleto() + ".");
    }

    // RF01
    private static void darDeBajaLote() {
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        admin.darDeBajaLote(lote);
        System.out.println("Lote " + lote.getNumero() + " dado de baja.");
    }

    // RF02: Asignar rol en lote
    private static void asignarRolEnLote() {
        Vecino vecino = buscarVecino(leerTexto("Documento del vecino: "));
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        System.out.println("1. Titular");
        System.out.println("2. Residente familiar");
        System.out.println("3. Inquilino");
        int rol = leerEntero("Rol: ");
        if (rol == 1) {
            // Flujo 5b: el lote ya tiene titular, se pide confirmar el reemplazo
            System.out.println("El lote ya tiene titular: " + lote.getTitular().getNombreCompleto()
                    + ". Se quitaran las personas autorizadas por el titular anterior.");
            if (leerTexto("Reemplazarlo? (s/n): ").equalsIgnoreCase("s")) {
                admin.cambiarTitular(lote, vecino);
                System.out.println("Nuevo titular del lote " + lote.getNumero() + ": "
                        + vecino.getNombreCompleto() + ".");
            } else {
                System.out.println("Operacion cancelada.");
            }
        } else if (rol == 2 || rol == 3) {
            TipoResidente tipo = (rol == 2) ? TipoResidente.FAMILIAR : TipoResidente.INQUILINO;
            admin.asignarRolEnLote(lote, vecino, tipo);
            System.out.println(vecino.getNombreCompleto() + " quedo como " + tipo
                    + " del lote " + lote.getNumero() + ".");
        } else {
            System.out.println("Rol invalido.");
        }
    }

    private static boolean existePersona(String documento) {
        try {
            buscarPersona(documento);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void menuEquiposAdministrador() {
        System.out.println("1. Generar orden de mantenimiento");
        System.out.println("2. Consultar equipo e historial");
        switch (leerEntero("Opcion: ")) {
            case 1: generarOrden(); break;
            case 2: consultarEquipo(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    private static void menuReportes() {
        System.out.println("1. Reclamos pendientes");
        System.out.println("2. Morosidad por lote");
        System.out.println("3. Equipos fuera de servicio");
        switch (leerEntero("Opcion: ")) {
            case 1: admin.getReportes().reporteReclamosPendientes(); break;
            case 2: admin.getReportes().reporteMorosidad(); break;
            case 3: admin.getReportes().reporteEquiposFueraDeServicio(); break;
            default: System.out.println("Opcion invalida.");
        }
    }

    // ===================== Control de acceso =====================

    private static boolean esElUsuario(Persona persona) {
        return persona != null && persona.getDocumento().equals(usuario.getDocumento());
    }

    // Buscar reclamo, flujo 2a: si el actor no tiene acceso, el sistema lo informa.
    // Admin: todos. Personal: los que tiene asignados. Vecino: los que presento o los de su lote.
    private static Reclamo buscarReclamoConAcceso(int numero) {
        Reclamo reclamo = buscarReclamo(numero);
        boolean tieneAcceso;
        if (usuario instanceof Administrador) {
            tieneAcceso = true;
        } else if (usuario instanceof Personal) {
            tieneAcceso = esElUsuario(reclamo.getResponsable());
        } else {
            tieneAcceso = esElUsuario(reclamo.getPresentadoPor())
                    || esElUsuario(reclamo.getLote().getTitular());
        }
        if (!tieneAcceso) {
            throw new IllegalArgumentException("No tiene acceso a ese reclamo.");
        }
        return reclamo;
    }

    // El administrador ve cualquier lote; un vecino solo los lotes a los que esta vinculado
    private static Lote buscarLoteConAcceso(int numero) {
        Lote lote = buscarLote(numero);
        if (usuario instanceof Vecino && !lote.estaVinculado((Vecino) usuario)) {
            throw new IllegalArgumentException("No tiene acceso a ese lote.");
        }
        return lote;
    }

    // ===================== Reclamos =====================

    // RF06
        private static void registrarReclamo(Vecino vecino) {
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        System.out.println("Tipo de reclamo:");
        TipoReclamo[] tipos = TipoReclamo.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + ". " + tipos[i]);
        }
        int opcionTipo = leerEntero("Opcion: ");
        if (opcionTipo < 1 || opcionTipo > tipos.length) {
            throw new IllegalArgumentException("Tipo de reclamo invalido.");
        }
        TipoReclamo tipo = tipos[opcionTipo - 1];
        Reclamo reclamo = vecino.presentarReclamo(lote, tipo, leerTexto("Descripcion: "));
        System.out.println("Reclamo N° " + reclamo.getNumero() + " registrado con prioridad "
                + reclamo.getPrioridad() + ".");
    }

    // RF07
    private static void asignarReclamo() {
        // Solo se muestran los reclamos abiertos, que todavia no tienen responsable
        System.out.println("Reclamos sin asignar:");
        int sinAsignar = 0;
        for (Lote lote : barrio.getLotes()) {
            for (Reclamo r : lote.getReclamos()) {
                if (r.getEstado() == EstadoReclamo.ABIERTO) {
                    System.out.println("  N° " + r.getNumero() + " | " + r.getTipo() + " | Prioridad "
                            + r.getPrioridad() + " | Lote " + lote.getNumero() + " | " + r.getDescripcion());
                    sinAsignar++;
                }
            }
        }
        if (sinAsignar == 0) {
            System.out.println("No hay reclamos sin asignar.");
            return;
        }
        Reclamo reclamo = buscarReclamo(leerEntero("Numero de reclamo: "));
        
        // Solo el personal que puede atender este tipo de reclamo (polimorfismo)
        System.out.println("Empleados que pueden atender un reclamo de " + reclamo.getTipo() + ":");
        for (Personal p : admin.getPersonal()) {
            if (p.puedeAtender(reclamo)) {
                System.out.println("  Legajo " + p.getLegajo() + " | " + p.getNombreCompleto()
                        + " | " + p.getClass().getSimpleName()
                        + " | En curso: " + p.cantidadReclamosEnCurso());
            }
        }
        Personal empleado = buscarPersonal(leerEntero("Legajo del empleado: "));
        admin.asignarReclamo(reclamo, empleado);
        System.out.println("Reclamo asignado a " + empleado.getNombreCompleto() + ".");
    }


    // RF08
    private static void avanzarEstado() {
        // El admin ve todos los reclamos; el personal, solo los que tiene asignados
        System.out.println("Reclamos:");
        if (usuario instanceof Administrador) {
            for (Lote lote : barrio.getLotes()) {
                for (Reclamo r : lote.getReclamos()) {
                    System.out.println("  N° " + r.getNumero() + " | " + r.getEstado());
                }
            }
        } else {
            for (Reclamo r : ((Personal) usuario).getReclamos()) {
                System.out.println("  N° " + r.getNumero() + " | " + r.getEstado());
            }
        }
        Reclamo reclamo = buscarReclamoConAcceso(leerEntero("Numero de reclamo: "));
        reclamo.avanzarEstado(leerTexto("Observacion: "));
        System.out.println("Nuevo estado: " + reclamo.getEstado());
    }

    // RF11
    private static void reabrirReclamo() {
        Reclamo reclamo = buscarReclamoConAcceso(leerEntero("Numero de reclamo: "));
        reclamo.reabrir(leerTexto("Motivo: "));
        System.out.println("Reclamo reabierto.");
    }

    // RF10: el autor es quien inicio sesion
    private static void agregarComentario() {
        Reclamo reclamo = buscarReclamoConAcceso(leerEntero("Numero de reclamo: "));
        reclamo.agregarComentario(usuario, leerTexto("Comentario: "));
        System.out.println("Comentario agregado.");
    }

    // RF09
    private static void verHistorial() {
        Reclamo reclamo = buscarReclamoConAcceso(leerEntero("Numero de reclamo: "));
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
    }

    private static void verMisReclamos(Personal empleado) {
        if (empleado.getReclamos().isEmpty()) {
            System.out.println("No tiene reclamos asignados.");
        }
        for (Reclamo reclamo : empleado.getReclamos()) {
            System.out.println("N° " + reclamo.getNumero() + " | Lote " + reclamo.getLote().getNumero() + " | "
                    + reclamo.getTipo() + " | " + reclamo.getEstado() + " | " + reclamo.getDescripcion());
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

    // RF15, RF16
    private static void emitirExpensas() {
        String periodo = leerTexto("Periodo (AAAA-MM): ");
        double monto = leerDecimal("Monto del presupuesto: ");
        barrio.emitirExpensas(new PresupuestoMensual(periodo, monto));
        System.out.println("Expensas emitidas para " + barrio.getLotes().size() + " lotes.");
    }

    // RF17
    private static void registrarPago() {
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        System.out.println("Deuda actual: $" + lote.getDeudaTotal());
        double importe = leerDecimal("Importe: ");
        MedioPago medio = MedioPago.valueOf(
                leerTexto("Medio de pago (EFECTIVO, TRANSFERENCIA, TARJETA): ").toUpperCase());
        lote.registrarPago(importe, medio);
        System.out.println("Pago registrado. Deuda restante: $" + lote.getDeudaTotal());
    }

    // RF19
    private static void registrarAjuste() {
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
            return;
        }
        int indice = leerEntero("Pago a corregir: ") - 1;
        double importe = leerDecimal("Importe del ajuste (negativo para restar): ");
        String motivo = leerTexto("Motivo: ");
        expensasDePago.get(indice).registrarAjuste(pagos.get(indice), LocalDate.now(), importe, motivo);
        System.out.println("Ajuste registrado. Deuda actual: $" + lote.getDeudaTotal());
    }

    // RF21
    private static void consultarEstadoDeCuenta() {
        Lote lote = buscarLoteConAcceso(leerEntero("Numero de lote: "));
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
    }

    // ===================== Equipos =====================

    // RF22: quien reporta es quien inicio sesion
    // Muestra los equipos que cumplen el filtro y pide el codigo de uno.
    // El filtro es una lambda: recibe un equipo y devuelve true si se muestra.
    private static Equipo elegirEquipo(String titulo, Predicate<Equipo> filtro) {
        System.out.println(titulo);
        int cantidad = 0;
        for (Sector sector : barrio.getSectores()) {
            for (Equipo equipo : sector.getEquipos()) {
                if (filtro.test(equipo)) {
                    System.out.println("  " + equipo.getCodigo() + " | " + equipo.getClass().getSimpleName()
                            + " | " + sector.getNombre() + " | " + equipo.getEstado());
                    cantidad++;
                }
            }
        }
        if (cantidad == 0) {
            throw new IllegalStateException("No hay equipos para esta opcion.");
        }
        return buscarEquipo(leerTexto("Codigo del equipo: "));
    }

    private static void registrarFalla() {
        // Solo se puede reportar la falla de un equipo que funciona (RF22 4a)
        Equipo equipo = elegirEquipo("Equipos operativos:", e -> e.getEstado() == EstadoEquipo.OPERATIVO);
        equipo.registrarFalla(usuario, leerTexto("Descripcion de la falla: "));
        System.out.println("Falla registrada. Estado: " + equipo.getEstado());
    }

    // RF23
    private static void generarOrden() {
        // Solo equipos con falla que todavia no tienen una orden abierta
        Equipo equipo = elegirEquipo("Equipos con falla sin orden:",
                e -> e.getEstado() == EstadoEquipo.CON_FALLA && !e.tieneOrdenAbierta());
        int legajo = leerEntero("Legajo del empleado de mantenimiento (0 para dejarla sin asignar): ");
        EmpleadoMantenimiento empleado = legajo == 0 ? null : buscarEmpleadoMantenimiento(legajo);
        OrdenMantenimiento orden = admin.generarOrden(equipo.getUltimaFalla(), empleado);
        System.out.println("Orden N° " + orden.getNumero() + " generada con prioridad "
                + orden.getPrioridad() + ".");
    }

    // RF24: el tecnico es quien inicio sesion
    private static void cerrarOrden(EmpleadoMantenimiento tecnico) {
        // Solo las ordenes abiertas asignadas a este empleado o sin asignar
        Equipo equipo = elegirEquipo("Equipos con orden abierta para usted:", e -> e.tieneOrdenAbierta()
                && (buscarOrdenAbierta(e).getAsignadaA() == null || esElUsuario(buscarOrdenAbierta(e).getAsignadaA())));
        OrdenMantenimiento orden = buscarOrdenAbierta(equipo);
        if (orden.getAsignadaA() != null && !esElUsuario(orden.getAsignadaA())) {
            throw new IllegalArgumentException("La orden esta asignada a otro empleado.");
        }
        String descripcion = leerTexto("Descripcion de la reparacion: ");
        if (leerTexto("Se pudo reparar? (S/N): ").equalsIgnoreCase("S")) {
            orden.cerrar(LocalDate.now(), tecnico, descripcion);
        } else {
            orden.cerrarSinReparar(LocalDate.now(), tecnico, descripcion);
        }
        System.out.println("Orden cerrada. Estado del equipo: " + equipo.getEstado());
    }

    // RF25
    private static void consultarEquipo() {
        Equipo equipo = elegirEquipo("Equipos del barrio:", e -> true);
        System.out.println(equipo.getCodigo() + " | Sector: " + equipo.getSector().getNombre()
                + " | Estado: " + equipo.getEstado());
        for (OrdenMantenimiento orden : equipo.getOrdenes()) {
            System.out.println("  Orden N° " + orden.getNumero() + " | Apertura " + orden.getFechaApertura()
                    + " | " + orden.getPrioridad() + " | " + orden.getDescripcionTrabajo()
                    + (orden.estaCerrada() ? " | Cerrada " + orden.getFechaCierre()
                    + ": " + orden.getDescripcionReparacion() : " | Abierta"));
        }
    }

    // ===================== Visitas =====================

    // RF03
    private static void registrarPersonaAutorizada(Vecino titular) {
        // Solo el titular del lote puede autorizar (lo valida Lote.autorizarPersona)
        Lote lote = buscarLoteConAcceso(leerEntero("Numero de lote: "));
        String documento = leerTexto("Documento de la persona: ");
        Vecino autorizado;
        boolean esNuevo = false;
        try {
            autorizado = buscarVecino(documento);
        } catch (IllegalArgumentException e) {
            if (existePersona(documento)) {
                throw new IllegalArgumentException("Ese documento pertenece a un empleado o al administrador.");
            }
            // Si la persona no esta registrada se piden sus datos (flujo 4b: datos incompletos)
            String nombre = leerTexto("Nombre: ");
            String apellido = leerTexto("Apellido: ");
            if (nombre.isBlank() || apellido.isBlank()) {
                throw new IllegalArgumentException("Faltan datos de la persona.");
            }
            autorizado = new Vecino(nombre, apellido, documento, "", "");
            esNuevo = true;
        }
        lote.autorizarPersona(titular, autorizado);
        if (esNuevo) {
            vecinos.add(autorizado);
        }
        System.out.println(autorizado.getNombreCompleto() + " quedo autorizado a reclamar por el lote "
                + lote.getNumero() + ".");
    }

    // RF27
        private static void autorizarVisitaPuntual(Vecino vecino) {
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        String nombre = leerTexto("Nombre del visitante: ");
        String documento = leerTexto("Documento del visitante: ");
        LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
        LocalTime desde = leerHora("Desde (HH:MM): ");
        LocalTime hasta = leerHora("Hasta (HH:MM): ");
        vecino.autorizarVisita(new VisitaPuntual(nombre, documento, lote, fecha, desde, hasta));
        System.out.println("Visita autorizada.");
    }

    // RF28
    private static void autorizarVisitaRecurrente(Vecino vecino) {
        Lote lote = buscarLote(leerEntero("Numero de lote: "));
        String nombre = leerTexto("Nombre del proveedor: ");
        String documento = leerTexto("Documento del proveedor: ");
        LocalDate desdeFecha = leerFecha("Vigente desde (AAAA-MM-DD): ");
        LocalDate hastaFecha = leerFecha("Vigente hasta (AAAA-MM-DD): ");
        String[] numeros = leerTexto("Dias (1=lunes ... 7=domingo, separados por coma): ").split(",");
        List<DayOfWeek> dias = new ArrayList<>();
        for (String numero : numeros) {
            dias.add(DayOfWeek.of(Integer.parseInt(numero.trim())));
        }
        LocalTime desde = leerHora("Desde (HH:MM): ");
        LocalTime hasta = leerHora("Hasta (HH:MM): ");
        vecino.autorizarVisita(new VisitaRecurrente(nombre, documento, lote,
                desdeFecha, hastaFecha, dias, desde, hasta));
        System.out.println("Visita recurrente autorizada.");
    }

    // RF29
    private static void consultarAutorizacion() {
        Visita visita = barrio.buscarVisitaVigente(leerTexto("Documento: "), LocalDateTime.now());
        if (visita == null) {
            System.out.println("No hay autorizacion vigente.");
        } else {
            System.out.println("Autorizado: " + visita.getNombreVisitante()
                    + " | Lote " + visita.getLote().getNumero());
        }
    }

    // RF30
    private static void registrarIngreso(Guardia guardia) {
        Visita visita = barrio.buscarVisitaVigente(leerTexto("Documento del visitante: "), LocalDateTime.now());
        if (visita == null) {
            System.out.println("Ingreso rechazado: no hay autorizacion vigente.");
        } else {
            guardia.registrarIngreso(visita);
            System.out.println("Ingreso registrado: " + visita.getNombreVisitante()
                    + " al lote " + visita.getLote().getNumero() + ".");
        }
    }

    // RF31
    private static void registrarEgreso(Guardia guardia) {
        guardia.registrarEgreso(leerTexto("Documento del visitante: "));
        System.out.println("Egreso registrado.");
    }

    // ===================== Procesos automaticos =====================

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

    // Acepta 2026-11-5 o 2026-11-05; si esta mal, vuelve a preguntar
    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje), DateTimeFormatter.ofPattern("uuuu-M-d")
                        .withResolverStyle(ResolverStyle.STRICT));
            } catch (DateTimeParseException e) {
                System.out.println("Fecha invalida. Use el formato AAAA-MM-DD, por ejemplo 2026-11-05.");
            }
        }
    }

    // Acepta 9:00 o 09:00; si esta mal, vuelve a preguntar
    private static LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                return LocalTime.parse(leerTexto(mensaje), DateTimeFormatter.ofPattern("H:mm"));
            } catch (DateTimeParseException e) {
                System.out.println("Hora invalida. Use el formato HH:MM, por ejemplo 09:30.");
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
