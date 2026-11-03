package modelo.visitas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import modelo.barrio.Lote;

public class VisitaRecurrente extends Visita {
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private List<DayOfWeek> diasSemana;

    // RF28: el vecino indica los dias de la semana y la vigencia
    public VisitaRecurrente(String nombreVisitante, String documentoVisitante, Lote lote,
                            LocalDate fechaDesde, LocalDate fechaHasta, List<DayOfWeek> diasSemana,
                            LocalTime horaDesde, LocalTime horaHasta) {
        super(nombreVisitante, documentoVisitante, lote, horaDesde, horaHasta);
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.diasSemana = diasSemana;
        if (!validarDuracion()) {
            throw new IllegalArgumentException("La vigencia no es valida (maximo 30 dias).");
        }
    }

    // RN18: vigencia maxima de 30 dias
    public boolean validarDuracion() {
        return !fechaHasta.isBefore(fechaDesde) && !fechaHasta.isAfter(fechaDesde.plusDays(30));
    }

    @Override
    public boolean estaVigente(LocalDateTime momento) {
        LocalDate dia = momento.toLocalDate();
        return !dia.isBefore(fechaDesde) && !dia.isAfter(fechaHasta)
                && diasSemana.contains(dia.getDayOfWeek()) && estaEnFranja(momento);
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public List<DayOfWeek> getDiasSemana() {
        return diasSemana;
    }
}
