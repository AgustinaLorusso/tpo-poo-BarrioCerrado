package modelo.visitas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.personas.Guardia;

public class RegistroIngreso {
    // Todos los ingresos, para controlar RN19 por persona
    private static List<RegistroIngreso> todos = new ArrayList<>();

    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraEgreso;
    private Visita visita;
    private Guardia guardia;

    public RegistroIngreso(Visita visita, Guardia guardia, LocalDateTime fechaHoraIngreso) {
        this.visita = visita;
        this.guardia = guardia;
        this.fechaHoraIngreso = fechaHoraIngreso;
        visita.agregarRegistro(this);
        todos.add(this);
    }

    // RN19: busca un ingreso sin egreso de esa persona
    public static RegistroIngreso buscarIngresoAbierto(String documento) {
        for (RegistroIngreso registro : todos) {
            if (registro.estaAdentro()
                    && registro.visita.getDocumentoVisitante().equals(documento)) {
                return registro;
            }
        }
        return null;
    }

    // RF31
    public void registrarEgreso(LocalDateTime momento) {
        if (!estaAdentro()) {
            throw new IllegalStateException("El egreso ya fue registrado.");
        }
        this.fechaHoraEgreso = momento;
    }

    public boolean estaAdentro() {
        return fechaHoraEgreso == null;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraEgreso() {
        return fechaHoraEgreso;
    }

    public Visita getVisita() {
        return visita;
    }

    public Guardia getGuardia() {
        return guardia;
    }
}
