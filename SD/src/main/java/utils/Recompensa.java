package utils;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    public void serialize(DataOutputStream dos) throws IOException {
        this.origem.serialize(dos);
        this.destino.serialize(dos);
        dos.writeFloat(this.valorRecompensa);
    }

    public static @NotNull Recompensa deserialize(DataInputStream dis) throws IOException {
        Ponto origem = Ponto.deserialize(dis);
        Ponto destino = Ponto.deserialize(dis);
        float valor = dis.readFloat();
        return new Recompensa(origem, destino, valor);
    }
}
