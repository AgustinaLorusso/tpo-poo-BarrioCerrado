import modelo.barrio.*;
import modelo.expensas.*;
import modelo.personas.Vecino;
import java.time.LocalDate;

/** Pruebas ejecutables sin bibliotecas adicionales. */
public class PruebaPasos3y5 {
    private static int comprobaciones;
    private static void verificar(boolean condicion, String mensaje) {
        comprobaciones++;
        if (!condicion) { throw new AssertionError(mensaje); }
    }
    private static void igual(double esperado, double actual, String mensaje) {
        verificar(Math.abs(esperado - actual) < 0.005, mensaje + ": " + actual);
    }
    private static void rechaza(Runnable accion, String mensaje) {
        try { accion.run(); } catch (IllegalArgumentException | UnsupportedOperationException e) {
            comprobaciones++; return;
        }
        throw new AssertionError(mensaje);
    }
    public static void main(String[] args) {
        Vecino titular = new Vecino("Ana", "Perez", "123", "111", "ana@example.com");
        Vecino familiar = new Vecino("Juan", "Perez", "456", "222", "juan@example.com");
        Barrio barrio = new Barrio("Los Aromos");
        Lote primero = new Lote(1, 300, titular);
        Lote segundo = new Lote(2, 700, titular);
        barrio.agregarLote(primero);
        barrio.agregarLote(segundo);
        igual(1000, barrio.getSuperficieTotal(), "Superficie total");
        primero.agregarResidente(familiar, TipoResidente.FAMILIAR);
        verificar(!primero.puedePresentarReclamo(familiar), "Residir no autoriza reclamos");
        primero.autorizarPersona(titular, familiar);
        verificar(primero.puedePresentarReclamo(familiar), "Autorizacion expresa");
        rechaza(() -> primero.autorizarPersona(familiar, titular), "Solo autoriza el titular");
        rechaza(() -> barrio.agregarLote(new Lote(1, 100, titular)), "Numero duplicado");
        barrio.emitirExpensas(new PresupuestoMensual("2026-01", 10000));
        igual(3000, primero.getExpensas().get(0).getImporte(), "Coeficiente primer lote");
        igual(7000, segundo.getExpensas().get(0).getImporte(), "Coeficiente segundo lote");
        rechaza(() -> barrio.emitirExpensas(new PresupuestoMensual("2026-01", 10000)), "Emision duplicada");
        verificar(primero.getExpensas().size() == 1, "No duplica expensas");

        Expensa expensa = new Expensa("2026-01", 1000);
        LocalDate vencimiento = LocalDate.of(2026, 1, 10);
        LocalDate treintaDias = vencimiento.plusDays(30);
        igual(0, expensa.calcularInteres(vencimiento), "Sin interes al vencer");
        igual(30, expensa.calcularInteres(treintaDias), "Tasa mensual");
        Pago parcial = expensa.agregarPago(treintaDias, 130, MedioPago.TRANSFERENCIA);
        igual(30, parcial.getImporteInteres(), "Interes primero");
        igual(100, parcial.getImporteCapital(), "Capital despues");
        igual(900, expensa.getSaldo(treintaDias), "Saldo tras pago");
        igual(27, expensa.calcularInteres(treintaDias.plusDays(30)), "Interes sobre capital restante");
        igual(900, expensa.getSaldo(treintaDias), "Consultar no acumula interes");
        expensa.registrarAjuste(parcial, treintaDias, -30, "Importe registrado de mas");
        igual(930, expensa.getSaldo(treintaDias), "Ajuste negativo");
        igual(130, parcial.getImporte(), "Pago original conservado");
        expensa.registrarAjuste(parcial, treintaDias, 30, "Correccion de ajuste");
        igual(900, expensa.getSaldo(treintaDias), "Ajuste positivo");
        rechaza(() -> expensa.registrarAjuste(parcial, treintaDias, -200, "Error"), "No pago negativo");
        rechaza(() -> expensa.registrarAjuste(parcial, treintaDias, 1000, "Error"), "No sobrepago");
        verificar(parcial.getAjustes().size() == 2, "Ajuste rechazado no altera historial");
        rechaza(() -> expensa.getPagos().clear(), "No eliminar pagos");
        expensa.agregarPago(treintaDias.plusDays(30), 927, MedioPago.EFECTIVO);
        verificar(expensa.getEstado(treintaDias.plusDays(30)) == EstadoExpensa.PAGADA, "Cancelacion");
        igual(0, expensa.getSaldo(treintaDias.plusDays(60)), "No interes despues de cancelar");
        expensa.registrarAjuste(parcial, treintaDias.plusDays(60), -100, "Correccion de pago anterior");
        verificar(expensa.getSaldo(treintaDias.plusDays(60)) > 100, "Recalcula interes tras ajuste de pago anterior");
        igual(0, expensa.getSaldo(treintaDias.plusDays(30)), "Consulta historica anterior al ajuste");

        Lote deudor = new Lote(3, 100, titular);
        for (int mes = 1; mes <= 4; mes++) {
            deudor.generarExpensa(new PresupuestoMensual("2026-0" + mes, 1000), 100);
        }
        LocalDate fecha = LocalDate.of(2026, 5, 1);
        verificar(!deudor.esMoroso(LocalDate.of(2026, 4, 1)), "Tres periodos no marcan moroso");
        verificar(deudor.esMoroso(fecha), "Cuatro periodos marcan moroso");
        deudor.actualizarMorosidad(fecha);
        verificar(deudor.getNotificaciones().size() == 1, "Notificacion unica");
        double deudaAntigua = deudor.getExpensas().get(0).getSaldo(fecha);
        deudor.registrarPago(fecha, deudaAntigua + 50, MedioPago.TARJETA);
        igual(0, deudor.getExpensas().get(0).getSaldo(fecha), "Periodo mas antiguo primero");
        verificar(deudor.getExpensas().get(1).getPagos().size() == 1, "Excedente al periodo siguiente");
        verificar(!deudor.esMoroso(fecha), "Desmarca al bajar a tres periodos");
        deudor.registrarPago(fecha, deudor.getDeudaTotal(fecha), MedioPago.TRANSFERENCIA);
        igual(0, deudor.getDeudaTotal(fecha), "Cancelacion total lote");
        rechaza(() -> deudor.registrarPago(fecha, 1, MedioPago.EFECTIVO), "No saldo a favor");
        System.out.println("OK: " + comprobaciones + " comprobaciones de pasos 3 y 5.");
    }
}
