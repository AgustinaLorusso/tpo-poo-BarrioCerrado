package modelo.barrio;

import java.io.Serializable;
import modelo.personas.Vecino;
import modelo.expensas.*;
import modelo.visitas.Visita;
import modelo.reclamos.EstadoReclamo;
import modelo.reclamos.Reclamo;
import java.time.LocalDate;
import java.util.*;

public class Lote implements Serializable {
    private final int numero;
    private double superficie;
    private Vecino titular;
    private boolean moroso;
    private final List<Residencia> residencias = new ArrayList<>();
    private final List<Vecino> autorizados = new ArrayList<>();
    private final List<Expensa> expensas = new ArrayList<>();
    private final List<String> notificaciones = new ArrayList<>();
    private final List<Visita> visitas = new ArrayList<>();
    private final List<Reclamo> reclamos = new ArrayList<>();

    public Lote(int numero, double superficie, Vecino titular) {
        if (numero <= 0) { throw new IllegalArgumentException("Numero de lote invalido."); }
        this.numero = numero;
        setSuperficie(superficie);
        this.titular = Objects.requireNonNull(titular);
    }

    public void setSuperficie(double superficie) {
        if (!Double.isFinite(superficie) || superficie <= 0) {
            throw new IllegalArgumentException("La superficie debe ser positiva.");
        }
        this.superficie = superficie;
    }
    public void setTitular(Vecino titular) {
        this.titular = Objects.requireNonNull(titular);
        residencias.removeIf(r -> mismaPersona(r.getVecino(), titular));
        autorizados.clear();
    }
    private boolean mismaPersona(Vecino a, Vecino b) {
        return Objects.equals(a.getDocumento(), b.getDocumento());
    }
    public Residencia agregarResidente(Vecino vecino, TipoResidente tipo) {
        Objects.requireNonNull(vecino);
        if (mismaPersona(vecino, titular) || residencias.stream().anyMatch(r -> mismaPersona(r.getVecino(), vecino))) {
            throw new IllegalArgumentException("El vecino ya esta vinculado al lote.");
        }
        Residencia residencia = new Residencia(vecino, this, tipo);
        residencias.add(residencia);
        return residencia;
    }
    public void quitarResidente(Vecino vecino) {
        residencias.removeIf(r -> mismaPersona(r.getVecino(), vecino));
    }
    public void autorizarPersona(Vecino solicitante, Vecino autorizado) {
        if (!mismaPersona(Objects.requireNonNull(solicitante), titular)) {
            throw new IllegalArgumentException("Solo el titular puede autorizar personas.");
        }
        Objects.requireNonNull(autorizado);
        if (puedePresentarReclamo(autorizado)) { throw new IllegalArgumentException("La persona ya esta autorizada."); }
        autorizados.add(autorizado);
    }
    public void revocarAutorizacion(Vecino solicitante, Vecino autorizado) {
        if (!mismaPersona(Objects.requireNonNull(solicitante), titular)) {
            throw new IllegalArgumentException("Solo el titular puede revocar autorizaciones.");
        }
        autorizados.removeIf(v -> mismaPersona(v, autorizado));
    }
    public boolean puedePresentarReclamo(Vecino vecino) {
        return vecino != null && (mismaPersona(titular, vecino)
                || autorizados.stream().anyMatch(v -> mismaPersona(v, vecino)));
    }
    // Titular o residente (familiar o inquilino)
    public boolean estaVinculado(Vecino vecino) {
        return vecino != null && (mismaPersona(titular, vecino)
                || residencias.stream().anyMatch(r -> mismaPersona(r.getVecino(), vecino)));
    }
    public void agregarReclamo(Reclamo reclamo) { reclamos.add(reclamo); }
    public List<Reclamo> getReclamos() { return Collections.unmodifiableList(reclamos); }
    // RF01 3a: no se da de baja un lote con reclamos abiertos o deuda
    public boolean puedeDarseDeBaja() {
        boolean reclamosAbiertos = reclamos.stream().anyMatch(r -> r.getEstado() != EstadoReclamo.CERRADO);
        return !reclamosAbiertos && getDeudaTotal() == 0;
    }
    public void agregarVisita(Visita visita) { visitas.add(visita); }
    public List<Visita> getVisitas() { return Collections.unmodifiableList(visitas); }
    public double calcularCoeficiente(double superficieTotal) {
        if (!Double.isFinite(superficieTotal) || superficieTotal < superficie) {
            throw new IllegalArgumentException("Superficie total invalida.");
        }
        return superficie / superficieTotal;
    }
    public Expensa generarExpensa(PresupuestoMensual presupuesto, double superficieTotal) {
        Objects.requireNonNull(presupuesto);
        if (tieneExpensa(presupuesto.getPeriodo())) { throw new IllegalArgumentException("El periodo ya fue emitido."); }
        Expensa expensa = new Expensa(presupuesto.getPeriodo(), presupuesto.getMonto() * calcularCoeficiente(superficieTotal));
        expensas.add(expensa);
        expensas.sort(Comparator.comparing(Expensa::getPeriodo));
        return expensa;
    }
    public boolean tieneExpensa(String periodo) {
        return expensas.stream().anyMatch(e -> e.getPeriodo().equals(periodo));
    }
    public void registrarPago(double importe, MedioPago medioPago) {
        registrarPago(LocalDate.now(), importe, medioPago);
    }
    public void registrarPago(LocalDate fecha, double importe, MedioPago medioPago) {
        Objects.requireNonNull(fecha);
        Objects.requireNonNull(medioPago);
        if (!Double.isFinite(importe) || Expensa.redondear(importe) <= 0 || Expensa.redondear(importe) > getDeudaTotal(fecha)) {
            throw new IllegalArgumentException("El importe debe ser positivo y no superar la deuda.");
        }
        for (Expensa expensa : expensas) {
            if (fecha.isBefore(expensa.getUltimaFechaMovimiento()) && !expensa.getPagos().isEmpty()) {
                throw new IllegalArgumentException("El pago debe respetar el orden cronologico del lote.");
            }
        }
        double restante = Expensa.redondear(importe);
        for (Expensa expensa : expensas) {
            if (restante == 0) { break; }
            if (fecha.isBefore(java.time.YearMonth.parse(expensa.getPeriodo()).atDay(1))) { continue; }
            double abono = Math.min(restante, expensa.getSaldo(fecha));
            if (abono > 0) {
                expensa.agregarPago(fecha, abono, medioPago);
                restante = Expensa.redondear(restante - abono);
            }
        }
        actualizarMorosidad(fecha);
    }
    public double getDeudaTotal() { return getDeudaTotal(LocalDate.now()); }
    public double getDeudaTotal(LocalDate fecha) {
        return Expensa.redondear(expensas.stream()
                .filter(e -> !java.time.YearMonth.parse(e.getPeriodo()).atDay(1).isAfter(fecha))
                .mapToDouble(e -> e.getSaldo(fecha)).sum());
    }
    public int contarPeriodosImpagos() { return contarPeriodosImpagos(LocalDate.now()); }
    public int contarPeriodosImpagos(LocalDate fecha) {
        return (int) expensas.stream().filter(e -> e.estaVencida(fecha)).count();
    }
    public void actualizarMorosidad() { actualizarMorosidad(LocalDate.now()); }
    public void actualizarMorosidad(LocalDate fecha) {
        int periodos = contarPeriodosImpagos(fecha);
        boolean nuevaMarca = periodos > 3;
        if (nuevaMarca && !moroso) {
            notificaciones.add(fecha + " - Notificacion formal a " + titular.getNombreCompleto()
                    + " (" + titular.getDocumento() + "): lote " + numero + ", " + periodos
                    + " periodos vencidos. Deuda: $" + getDeudaTotal(fecha));
        }
        moroso = nuevaMarca;
    }
    public boolean esMoroso() { actualizarMorosidad(); return moroso; }
    public boolean esMoroso(LocalDate fecha) { actualizarMorosidad(fecha); return moroso; }
    public int getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public Vecino getTitular() { return titular; }
    public List<Residencia> getResidencias() { return Collections.unmodifiableList(residencias); }
    public List<Vecino> getAutorizados() { return Collections.unmodifiableList(autorizados); }
    public List<Expensa> getExpensas() { return Collections.unmodifiableList(expensas); }
    public List<String> getNotificaciones() { return Collections.unmodifiableList(notificaciones); }
}
