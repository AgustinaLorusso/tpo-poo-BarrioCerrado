package modelo.barrio;

import java.io.Serializable;
import modelo.expensas.PresupuestoMensual;
import modelo.expensas.Expensa;
import modelo.equipos.Sector;
import modelo.visitas.Visita;
import java.time.LocalDateTime;
import java.util.*;

public class Barrio implements Serializable {
    private final String nombre;
    private final List<Lote> lotes = new ArrayList<>();
    private final List<Sector> sectores = new ArrayList<>();

    public Barrio(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) { throw new IllegalArgumentException("Falta el nombre."); }
        this.nombre = nombre;
    }
    public void agregarLote(Lote lote) {
        Objects.requireNonNull(lote);
        if (lotes.size() >= 150 || buscarLote(lote.getNumero()) != null) {
            throw new IllegalArgumentException("Numero duplicado o capacidad de 150 lotes alcanzada.");
        }
        lotes.add(lote);
    }
    public Lote buscarLote(int numero) {
        return lotes.stream().filter(l -> l.getNumero() == numero).findFirst().orElse(null);
    }
    public double getSuperficieTotal() { return lotes.stream().mapToDouble(Lote::getSuperficie).sum(); }
    public void emitirExpensas(PresupuestoMensual presupuesto) {
        Objects.requireNonNull(presupuesto);
        if (lotes.isEmpty()) { throw new IllegalStateException("No hay lotes cargados."); }
        double total = getSuperficieTotal();
        // Validar todos antes de emitir para evitar una emision parcial.
        for (Lote lote : lotes) {
            if (lote.tieneExpensa(presupuesto.getPeriodo())
                    || Expensa.redondear(presupuesto.getMonto() * lote.calcularCoeficiente(total)) <= 0) {
                throw new IllegalArgumentException("Periodo duplicado o importe menor a un centavo.");
            }
        }
        for (Lote lote : lotes) { lote.generarExpensa(presupuesto, total); }
    }
    // RF01: baja de lote
    public void quitarLote(Lote lote) {
        if (!lote.puedeDarseDeBaja()) {
            throw new IllegalStateException("El lote tiene reclamos abiertos o deuda.");
        }
        lotes.remove(lote);
    }
    public void agregarSector(Sector sector) { sectores.add(Objects.requireNonNull(sector)); }
    public List<Sector> getSectores() { return Collections.unmodifiableList(sectores); }
    // RF29: busca una visita vigente por documento
    public Visita buscarVisitaVigente(String documento, LocalDateTime momento) {
        for (Lote lote : lotes) {
            for (Visita visita : lote.getVisitas()) {
                if (visita.getDocumentoVisitante().equals(documento) && visita.estaVigente(momento)) {
                    return visita;
                }
            }
        }
        return null;
    }
    public String getNombre() { return nombre; }
    public List<Lote> getLotes() { return Collections.unmodifiableList(lotes); }
}
