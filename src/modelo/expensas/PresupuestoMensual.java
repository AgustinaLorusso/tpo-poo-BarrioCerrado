package modelo.expensas;

import java.time.YearMonth;
import java.util.Objects;

public class PresupuestoMensual {
    private final String periodo;
    private final double monto;

    public PresupuestoMensual(String periodo, double monto) {
        this.periodo = YearMonth.parse(Objects.requireNonNull(periodo)).toString();
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("El presupuesto debe ser positivo.");
        }
        this.monto = monto;
    }

    public String getPeriodo() { return periodo; }
    public double getMonto() { return monto; }
}
