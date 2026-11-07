package modelo.visitas;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import modelo.barrio.Lote;
import modelo.personas.Vecino;

public abstract class Visita implements Serializable {
    protected String nombreVisitante;
    protected String documentoVisitante;
    protected LocalTime horaDesde;
    protected LocalTime horaHasta;
    protected Lote lote;
    protected Vecino autorizadaPor;
    protected List<RegistroIngreso> registros = new ArrayList<>();

    public Visita(String nombreVisitante, String documentoVisitante, Lote lote,
                  LocalTime horaDesde, LocalTime horaHasta) {
        if (nombreVisitante == null || nombreVisitante.isBlank()
                || documentoVisitante == null || documentoVisitante.isBlank()) {
            throw new IllegalArgumentException("Faltan el nombre o el documento del visitante.");
        }
        // RF27 3a: franja invalida
        if (!horaDesde.isBefore(horaHasta)) {
            throw new IllegalArgumentException("La franja horaria no es valida.");
        }
        this.nombreVisitante = nombreVisitante;
        this.documentoVisitante = documentoVisitante;
        this.lote = lote;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
    }

    // Liskov: el guardia usa este metodo sin saber el tipo de visita
    public abstract boolean estaVigente(LocalDateTime momento);

    protected boolean estaEnFranja(LocalDateTime momento) {
        LocalTime hora = momento.toLocalTime();
        return !hora.isBefore(horaDesde) && !hora.isAfter(horaHasta);
    }

    public void setAutorizadaPor(Vecino vecino) {
        this.autorizadaPor = vecino;
    }

    public void agregarRegistro(RegistroIngreso registro) {
        registros.add(registro);
    }

    public String getNombreVisitante() {
        return nombreVisitante;
    }

    public String getDocumentoVisitante() {
        return documentoVisitante;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public LocalTime getHoraHasta() {
        return horaHasta;
    }

    public Lote getLote() {
        return lote;
    }

    public Vecino getAutorizadaPor() {
        return autorizadaPor;
    }

    public List<RegistroIngreso> getRegistros() {
        return registros;
    }
}
