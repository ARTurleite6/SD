import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class Viagem {
    private final int codigo;
    private final Ponto pontoInicial;
    private final LocalDateTime tempoComeco;

    public Viagem(int codigo, Ponto pontoInicial) {
        this.codigo = codigo;
        this.pontoInicial = pontoInicial;
        this.tempoComeco = LocalDateTime.now();
    }

    public Viagem(@NotNull Viagem viagem) {
        this.codigo = viagem.getCodigo();
        this.pontoInicial = viagem.getPontoInicial();
        this.tempoComeco = viagem.tempoComeco;
    }

    public float getCusto(@NotNull Ponto destino, LocalDateTime t) {
        return 0;
    }

    public int getCodigo() {
        return codigo;
    }

    public Ponto getPontoInicial() {
        return pontoInicial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Viagem viagem = (Viagem) o;

        if (getCodigo() != viagem.getCodigo()) return false;
        return getPontoInicial().equals(viagem.getPontoInicial());
    }

    @Override
    public int hashCode() {
        int result = getCodigo();
        result = 31 * result + getPontoInicial().hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Viagem{");
        sb.append("codigo=").append(codigo);
        sb.append(", pontoInicial=").append(pontoInicial);
        sb.append(", tempoComeco=").append(this.tempoComeco);
        sb.append('}');
        return sb.toString();
    }

    public Viagem clone() {
        return new Viagem(this);
    }

    public void serialize(@NotNull DataOutputStream dos) throws IOException {
        dos.writeInt(this.codigo);
        this.pontoInicial.serialize(dos);
        dos.flush();
    }

    public static @NotNull Viagem deserialize(@NotNull DataInputStream dis) throws IOException {
        int codigo = dis.readInt();
        String username = dis.readUTF();
        Ponto ponto = Ponto.deserialize(dis);
        return new Viagem(codigo, ponto);
    }
}
