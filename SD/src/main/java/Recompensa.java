import org.jetbrains.annotations.NotNull;
import utils.Ponto;

public class Recompensa {
    private final Ponto origem;
    private final Ponto destino;
    private final float valorRecompensa;

    public Recompensa() {
        this.origem = new Ponto();
        this.destino = new Ponto();
        this.valorRecompensa = 0;
    }

    public Recompensa(Ponto origem, Ponto destino, float valorRecompensa) {
        this.origem = origem;
        this.destino = destino;
        this.valorRecompensa = valorRecompensa;
    }

    public Recompensa(@NotNull Recompensa p) {
        this.origem = p.getOrigem();
        this.destino = p.getDestino();
        this.valorRecompensa = p.getValorRecompensa();
    }

    public Ponto getOrigem() {
        return origem;
    }

    public Ponto getDestino() {
        return destino;
    }

    public float getValorRecompensa() {
        return valorRecompensa;
    }
}
