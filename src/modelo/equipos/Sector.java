package modelo.equipos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sector implements Serializable {
    private String nombre;
    private TipoSector tipo;
    private List<Equipo> equipos = new ArrayList<>();

    public Sector(String nombre, TipoSector tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public void agregarEquipo(Equipo equipo) {
        equipos.add(equipo);
    }

    // Experto en informacion: el sector cuenta sus luminarias que no funcionan
    public int contarLuminariasConFalla() {
        int cantidad = 0;
        for (Equipo equipo : equipos) {
            if (equipo instanceof Luminaria && equipo.getEstado() != EstadoEquipo.OPERATIVO) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoSector getTipo() {
        return tipo;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }
}
