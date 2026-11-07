package modelo.reportes;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import modelo.barrio.Barrio;
import modelo.barrio.Lote;
import modelo.equipos.Camara;
import modelo.equipos.Equipo;
import modelo.equipos.EstadoEquipo;
import modelo.equipos.Falla;
import modelo.equipos.OrdenMantenimiento;
import modelo.equipos.Sector;
import modelo.expensas.Expensa;
import modelo.reclamos.EstadoReclamo;
import modelo.reclamos.Reclamo;

// Fabricacion pura: arma los reportes sin cargar a Administrador ni a las clases del modelo
public class GeneradorReportes implements Serializable {
    private Barrio barrio;

    public GeneradorReportes(Barrio barrio) {
        this.barrio = barrio;
    }

    // RF32
    public void reporteReclamosPendientes() {
        System.out.println("=== Reclamos pendientes ===");
        List<Reclamo> pendientes = new ArrayList<>();
        for (Lote lote : barrio.getLotes()) {
            for (Reclamo reclamo : lote.getReclamos()) {
                if (reclamo.getEstado() != EstadoReclamo.CERRADO) {
                    pendientes.add(reclamo);
                }
            }
        }
        // Orden natural de Reclamo (Comparable): prioridad y luego antiguedad
        Collections.sort(pendientes);
        for (Reclamo reclamo : pendientes) {
            String responsable = reclamo.getResponsable() == null
                    ? "sin asignar" : reclamo.getResponsable().getNombreCompleto();
            long dias = ChronoUnit.DAYS.between(reclamo.getFecha(), LocalDateTime.now());
            System.out.println("N° " + reclamo.getNumero() + " | Lote " + reclamo.getLote().getNumero()
                    + " | " + reclamo.getTipo() + " | Prioridad " + reclamo.getPrioridad()
                    + " | " + reclamo.getEstado() + " | Responsable: " + responsable
                    + " | Antiguedad: " + dias + " dias");
        }
        if (pendientes.isEmpty()) {
            System.out.println("No hay reclamos pendientes.");
        }
    }

    // RF33
    public void reporteMorosidad() {
        System.out.println("=== Morosidad por lote ===");
        int cantidad = 0;
        List<Lote> lotes = new ArrayList<>(barrio.getLotes());
        // Orden personalizado (Comparator): de mayor a menor deuda
        lotes.sort(Comparator.comparingDouble((Lote lote) -> lote.getDeudaTotal()).reversed());
        for (Lote lote : lotes) {
            if (lote.esMoroso()) {
                double intereses = 0;
                for (Expensa expensa : lote.getExpensas()) {
                    intereses += expensa.calcularInteres(LocalDate.now());
                }
                System.out.println("Lote " + lote.getNumero()
                        + " | Titular: " + lote.getTitular().getNombreCompleto()
                        + " | Periodos adeudados: " + lote.contarPeriodosImpagos()
                        + " | Deuda: $" + lote.getDeudaTotal()
                        + " | Intereses: $" + Expensa.redondear(intereses));
                cantidad++;
            }
        }
        if (cantidad == 0) {
            System.out.println("No hay lotes morosos.");
        }
    }

    // RF34: equipos con falla o fuera de servicio
    public void reporteEquiposFueraDeServicio() {
        System.out.println("=== Equipos fuera de servicio ===");
        int cantidad = 0;
        for (Sector sector : barrio.getSectores()) {
            for (Equipo equipo : sector.getEquipos()) {
                if (equipo.getEstado() == EstadoEquipo.CON_FALLA
                        || equipo.getEstado() == EstadoEquipo.FUERA_DE_SERVICIO) {
                    String tipo = equipo instanceof Camara ? "Camara" : "Luminaria";
                    Falla falla = equipo.getUltimaFalla();
                    String fecha = falla == null ? "-" : falla.getFecha().toLocalDate().toString();
                    OrdenMantenimiento orden = falla == null ? null : falla.getOrden();
                    String numeroOrden = orden == null ? "sin orden" : "N° " + orden.getNumero();
                    System.out.println(equipo.getCodigo() + " | " + tipo + " | Sector: " + sector.getNombre()
                            + " | " + equipo.getEstado() + " | Falla: " + fecha
                            + " | Orden: " + numeroOrden);
                    cantidad++;
                }
            }
        }
        if (cantidad == 0) {
            System.out.println("Todos los equipos estan operativos.");
        }
    }
}
