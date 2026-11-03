package modelo.expensas;

import java.time.LocalDate;
import java.util.Objects;

/** Un importe negativo reduce el pago; uno positivo lo incrementa. */
public class AsientoAjuste {
    private final LocalDate fecha;
    private final double importe;
    private final String motivo;
    private final Pago pago;

    AsientoAjuste(Pago pago, LocalDate fecha, double importe, String motivo) {
        this.pago = Objects.requireNonNull(pago);
        this.fecha = Objects.requireNonNull(fecha);
        if (fecha.isBefore(pago.getFecha()) || !Double.isFinite(importe) || importe == 0
                || motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Datos de ajuste invalidos.");
        }
        this.importe = importe;
        this.motivo = motivo;
    }

    public Pago getPago() { return pago; }
    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public String getMotivo() { return motivo; }
}
