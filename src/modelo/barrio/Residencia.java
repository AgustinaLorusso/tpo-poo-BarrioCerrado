package modelo.barrio;

import modelo.personas.Vecino;
import java.util.Objects;

/** Vincula un vecino a un lote; el titular se registra directamente en Lote. */
public class Residencia {
    private final Vecino vecino;
    private final Lote lote;
    private final TipoResidente tipo;

    Residencia(Vecino vecino, Lote lote, TipoResidente tipo) {
        this.vecino = Objects.requireNonNull(vecino);
        this.lote = Objects.requireNonNull(lote);
        this.tipo = Objects.requireNonNull(tipo);
    }

    public Vecino getVecino() { return vecino; }
    public Lote getLote() { return lote; }
    public TipoResidente getTipo() { return tipo; }
}
