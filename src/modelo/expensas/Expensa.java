package modelo.expensas;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Expensa {
    private final String periodo;
    private final double importe;
    private final LocalDate fechaVencimiento;
    private final List<Pago> pagos = new ArrayList<>();

    public Expensa(String periodo, double importe) {
        YearMonth mes = YearMonth.parse(Objects.requireNonNull(periodo));
        if (!Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo.");
        }
        this.periodo = mes.toString();
        this.importe = redondear(importe);
        this.fechaVencimiento = mes.atDay(10);
    }

    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Reconstruye el saldo cronologicamente, sin cobrar interes sobre interes.
    // Los ajustes corrigen el importe efectivo en la fecha del pago original.
    private double[] saldos(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        double capital = importe;
        double interes = 0;
        LocalDate desde = fechaVencimiento;
        for (Pago pago : pagos) {
            if (pago.getFecha().isAfter(fecha)) { break; }
            if (pago.getFecha().isAfter(desde)) {
                interes += capital * 0.03 * ChronoUnit.DAYS.between(desde, pago.getFecha()) / 30.0;
                desde = pago.getFecha();
            }
            double efectivo = pago.getImporte();
            for (AsientoAjuste ajuste : pago.getAjustes()) {
                if (!ajuste.getFecha().isAfter(fecha)) { efectivo += ajuste.getImporte(); }
            }
            if (redondear(efectivo) > redondear(capital + interes)) {
                throw new IllegalArgumentException("El ajuste genera un pago superior a la deuda.");
            }
            double aInteres = Math.min(efectivo, interes);
            interes -= aInteres;
            capital = Math.max(0, capital - (efectivo - aInteres));
        }
        if (fecha.isAfter(desde)) {
            interes += capital * 0.03 * ChronoUnit.DAYS.between(desde, fecha) / 30.0;
        }
        return new double[] { redondear(capital), redondear(interes) };
    }

    public Pago agregarPago(LocalDate fecha, double importe, MedioPago medioPago) {
        Objects.requireNonNull(fecha);
        Objects.requireNonNull(medioPago);
        if (fecha.isBefore(YearMonth.parse(periodo).atDay(1))
                || fecha.isBefore(getUltimaFechaMovimiento())) {
            throw new IllegalArgumentException("El pago debe respetar el orden cronologico.");
        }
        importe = validarImporte(importe);
        double[] saldo = saldos(fecha);
        if (importe > redondear(saldo[0] + saldo[1])) {
            throw new IllegalArgumentException("El pago supera el saldo de la expensa.");
        }
        double aInteres = Math.min(importe, saldo[1]);
        Pago pago = new Pago(fecha, importe, medioPago, aInteres, redondear(importe - aInteres));
        pagos.add(pago);
        return pago;
    }

    private double validarImporte(double valor) {
        if (!Double.isFinite(valor) || redondear(valor) <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo.");
        }
        return redondear(valor);
    }

    public AsientoAjuste registrarAjuste(Pago pago, LocalDate fecha, double importe, String motivo) {
        if (!pagos.contains(pago)) { throw new IllegalArgumentException("El pago no pertenece a esta expensa."); }
        Objects.requireNonNull(fecha);
        if (fecha.isBefore(getUltimaFechaMovimiento())) {
            throw new IllegalArgumentException("El ajuste debe respetar el orden cronologico.");
        }
        if (!Double.isFinite(importe)) { throw new IllegalArgumentException("Importe invalido."); }
        importe = redondear(importe);
        double corregido = redondear(pago.getImporteCorregido() + importe);
        if (corregido < 0) {
            throw new IllegalArgumentException("El ajuste deja un pago negativo.");
        }
        AsientoAjuste ajuste = new AsientoAjuste(pago, fecha, importe, motivo);
        pago.agregarAjuste(ajuste);
        try {
            saldos(fecha);
        } catch (IllegalArgumentException error) {
            pago.quitarAjuste(ajuste);
            throw error;
        }
        return ajuste;
    }

    public LocalDate getUltimaFechaMovimiento() {
        LocalDate ultima = YearMonth.parse(periodo).atDay(1);
        for (Pago pago : pagos) {
            if (pago.getFecha().isAfter(ultima)) { ultima = pago.getFecha(); }
            for (AsientoAjuste ajuste : pago.getAjustes()) {
                if (ajuste.getFecha().isAfter(ultima)) { ultima = ajuste.getFecha(); }
            }
        }
        return ultima;
    }
    public double calcularInteres(LocalDate fecha) { return saldos(fecha)[1]; }
    public double getSaldo(LocalDate fecha) {
        double[] saldo = saldos(fecha);
        return redondear(saldo[0] + saldo[1]);
    }
    public double getSaldo() { return getSaldo(LocalDate.now()); }
    public boolean estaVencida(LocalDate fecha) { return fecha.isAfter(fechaVencimiento) && getSaldo(fecha) > 0; }
    public EstadoExpensa getEstado(LocalDate fecha) {
        if (getSaldo(fecha) == 0) { return EstadoExpensa.PAGADA; }
        if (estaVencida(fecha)) { return EstadoExpensa.VENCIDA; }
        return pagos.stream().anyMatch(p -> !p.getFecha().isAfter(fecha))
                ? EstadoExpensa.PARCIAL : EstadoExpensa.PENDIENTE;
    }
    public EstadoExpensa getEstado() { return getEstado(LocalDate.now()); }
    public String getPeriodo() { return periodo; }
    public double getImporte() { return importe; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public List<Pago> getPagos() { return Collections.unmodifiableList(pagos); }
}
