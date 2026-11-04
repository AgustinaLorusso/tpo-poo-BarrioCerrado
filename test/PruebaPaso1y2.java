import modelo.barrio.Lote;
import modelo.personas.*;
import modelo.reclamos.*;
import modelo.visitas.VisitaPuntual;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Prueba de personas: herencia y sobreescritura de puedeAtender. */
public class PruebaPaso1y2 {
    private static int comprobaciones;

    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) { throw new AssertionError(mensaje); }
    }

    private static void rechaza(Runnable accion, String mensaje) {
        try {
            accion.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            comprobaciones++;
            return;
        }
        throw new AssertionError(mensaje);
    }

    public static void main(String[] args) {
        // Datos de prueba
        Vecino titular = new Vecino("Ana", "Perez", "123", "111", "ana@example.com");
        Lote lote = new Lote(1, 300, titular);
        Guardia guardia = new Guardia("Pedro", "Lopez", "555", "666", "pedro@example.com",
                1, LocalDate.of(2025, 1, 1), Turno.MAÑANA, Puesto.PORTERIA);
        EmpleadoMantenimiento plomero = new EmpleadoMantenimiento("Rosa", "Ruiz", "777", "888",
                "rosa@example.com", 2, LocalDate.of(2025, 1, 1), Especialidad.PLOMERIA);

        Reclamo deSeguridad = titular.presentarReclamo(lote, TipoReclamo.SEGURIDAD, "Porton abierto");
        Reclamo deMantenimiento = titular.presentarReclamo(lote, TipoReclamo.MANTENIMIENTO, "Perdida de agua");
        Reclamo deConvivencia = titular.presentarReclamo(lote, TipoReclamo.CONVIVENCIA, "Ruidos molestos");

        // Caso 1: el guardia atiende reclamos de seguridad
        verificar(guardia.puedeAtender(deSeguridad), "El guardia atiende seguridad");

        // Caso 2: el guardia no atiende reclamos de mantenimiento
        verificar(!guardia.puedeAtender(deMantenimiento), "El guardia no atiende mantenimiento");

        // Caso 3: el empleado de mantenimiento atiende reclamos de mantenimiento
        verificar(plomero.puedeAtender(deMantenimiento), "El plomero atiende mantenimiento");

        // Caso 4: el empleado de mantenimiento no atiende reclamos de convivencia del barrio
        verificar(!plomero.puedeAtender(deConvivencia), "El plomero no atiende convivencia");

        // Caso 5: de polimorfismo
        List<Personal> personal = List.of(guardia, plomero);
        int puedenAtender = 0;
        for (Personal empleado : personal) {
            if (empleado.puedeAtender(deMantenimiento)) {
                puedenAtender++;
            }
        }
        verificar(puedenAtender == 1, "Solo uno del personal atiende mantenimiento");

        // Caso 6: un empleado nuevo no tiene reclamos en curso (RN05)
        verificar(guardia.cantidadReclamosEnCurso() == 0, "El guardia arranca sin reclamos en curso");

        // Caso 7: solo el guardia de porteria registra ingresos
        Guardia guardiaRonda = new Guardia("Juan", "Sosa", "444", "222", "juan@example.com",
                3, LocalDate.of(2025, 1, 1), Turno.MAÑANA, Puesto.RONDA);
        VisitaPuntual visita = new VisitaPuntual("Marta", "321", lote,
                LocalDate.now(), LocalTime.of(0, 0), LocalTime.of(23, 59));
        rechaza(() -> guardiaRonda.registrarIngreso(visita), "Solo el guardia de porteria registra ingresos");

        System.out.println("PruebaPaso1y2: " + comprobaciones + " comprobaciones OK");
    }
}

