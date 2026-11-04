package modelo.equipos;

import java.time.LocalDate;
import modelo.reclamos.Prioridad;

public class Luminaria extends Equipo {
    private String tipoLampara;
    private int potencia;

    public Luminaria(String codigo, Sector sector, LocalDate fechaInstalacion,
                     String tipoLampara, int potencia) {
        super(codigo, sector, fechaInstalacion);
        this.tipoLampara = tipoLampara;
        this.potencia = potencia;
    }

    // RN14: normal, o alta si fallan mas de 3 luminarias del sector
    @Override
    public Prioridad calcularPrioridadOrden() {
        if (sector.contarLuminariasConFalla() > 3) {
            return Prioridad.ALTA;
        }
        return Prioridad.NORMAL;
    }

    public String getTipoLampara() {
        return tipoLampara;
    }

    public void setTipoLampara(String tipoLampara) {
        this.tipoLampara = tipoLampara;
    }

    public int getPotencia() {
        return potencia;
    }

    public void setPotencia(int potencia) {
        this.potencia = potencia;
    }
}
