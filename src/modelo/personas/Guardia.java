package modelo.personas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import modelo.reclamos.Reclamo;
import modelo.reclamos.TipoReclamo;
import modelo.visitas.RegistroIngreso;
import modelo.visitas.Visita;

public class Guardia extends Personal {
    private Turno turno;
    private Puesto puesto;

    public Guardia(String nombre, String apellido, String documento,
                   String telefono, String email,
                   int legajo, LocalDate fechaIngreso,
                   Turno turno, Puesto puesto) {
        super(nombre, apellido, documento, telefono, email, legajo, fechaIngreso);
        this.turno = turno;
        this.puesto = puesto;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Puesto getPuesto() {
        return puesto;
    }

    public void setPuesto(Puesto puesto) {
        this.puesto = puesto;
    }

    // Supuesto: seguridad, convivencia y administrativo
    @Override
    public boolean puedeAtender(Reclamo reclamo) {
        return reclamo.getTipo() != TipoReclamo.MANTENIMIENTO;
    }

    // RF30
    public RegistroIngreso registrarIngreso(Visita visita) {
        return registrarIngreso(visita, LocalDateTime.now());
    }

    public RegistroIngreso registrarIngreso(Visita visita, LocalDateTime momento) {
        // Enunciado: el guardia de la porteria registra los ingresos
        if (puesto != Puesto.PORTERIA) {
            throw new IllegalStateException("Solo el guardia de porteria registra ingresos.");
        }
        // RN17
        if (!visita.estaVigente(momento)) {
            throw new IllegalArgumentException("La visita no tiene una autorizacion vigente.");
        }
        // RN19
        if (RegistroIngreso.buscarIngresoAbierto(visita.getDocumentoVisitante()) != null) {
            throw new IllegalStateException("La persona tiene un ingreso sin egreso.");
        }
        return new RegistroIngreso(visita, this, momento);
    }

    // RF31
    public void registrarEgreso(String documento) {
        registrarEgreso(documento, LocalDateTime.now());
    }

    public void registrarEgreso(String documento, LocalDateTime momento) {
        RegistroIngreso registro = RegistroIngreso.buscarIngresoAbierto(documento);
        if (registro == null) {
            throw new IllegalArgumentException("No hay un ingreso abierto para esa persona.");
        }
        registro.registrarEgreso(momento);
    }
}

