package modelo.reclamos;

import java.io.Serializable;
import java.time.LocalDateTime;
import modelo.personas.Persona;

public class Comentario implements Serializable {
    private LocalDateTime fecha;
    private String texto;
    private Persona autor;

    public Comentario(LocalDateTime fecha, String texto, Persona autor) {
        this.fecha = fecha;
        this.texto = texto;
        this.autor = autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getTexto() {
        return texto;
    }

    public Persona getAutor() {
        return autor;
    }
}
