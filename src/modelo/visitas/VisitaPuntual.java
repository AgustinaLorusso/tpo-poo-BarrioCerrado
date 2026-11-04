package modelo.visitas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import modelo.barrio.Lote;

public class VisitaPuntual extends Visita {
    private LocalDate fecha;

    public VisitaPuntual(String nombreVisitante, String documentoVisitante, Lote lote,
                         LocalDate fecha, LocalTime horaDesde, LocalTime horaHasta) {
        super(nombreVisitante, documentoVisitante, lote, horaDesde, horaHasta);
        // RF27 3a: fecha pasada
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de la visita ya paso.");
        }
        this.fecha = fecha;
    }

    @Override
    public boolean estaVigente(LocalDateTime momento) {
        return momento.toLocalDate().equals(fecha) && estaEnFranja(momento);
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
