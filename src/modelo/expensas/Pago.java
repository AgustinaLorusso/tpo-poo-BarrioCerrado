package modelo.expensas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** El pago original es inmutable: las correcciones conservan su historial. */
public class Pago {
    private final LocalDate fecha;
    private final double importe;
    private final MedioPago medioPago;
    private final double importeInteres;
    private final double importeCapital;
    private final List<AsientoAjuste> ajustes = new ArrayList<>();

    Pago(LocalDate fecha, double importe, MedioPago medioPago, double interes, double capital) {
        this.fecha = fecha;
        this.importe = importe;
        this.medioPago = medioPago;
        this.importeInteres = interes;
        this.importeCapital = capital;
    }

    void agregarAjuste(AsientoAjuste ajuste) { ajustes.add(ajuste); }
    void quitarAjuste(AsientoAjuste ajuste) { ajustes.remove(ajuste); }
    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public MedioPago getMedioPago() { return medioPago; }
    public double getImporteInteres() { return importeInteres; }
    public double getImporteCapital() { return importeCapital; }
    public List<AsientoAjuste> getAjustes() { return Collections.unmodifiableList(ajustes); }
    public double getImporteCorregido() {
        return importe + ajustes.stream().mapToDouble(AsientoAjuste::getImporte).sum();
    }
}
