import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Viagem {
    private final int codigo;
    private final Ponto pontoInicial;
    private Ponto pontoFinal;
    private final LocalDateTime tempoComeco;
    private int duracao;
    private float custo;
    private Recompensa recompensa;

    public Viagem(int codigo, Ponto pontoInicial) {
        this.codigo = codigo;
        this.pontoInicial = pontoInicial;
        this.tempoComeco = LocalDateTime.now();
        this.duracao = 0;
        this.custo = 0;
        this.pontoFinal = null;
        this.recompensa = null;
    }

    public Viagem(@NotNull Viagem viagem) {
        this.codigo = viagem.getCodigo();
        this.pontoInicial = viagem.getPontoInicial();
        this.pontoFinal = viagem.getPontoFinal();
        this.tempoComeco = viagem.tempoComeco;
        this.recompensa = viagem.getRecompensa();
    }

    public int getDuracao() {
        return this.duracao;
    }

    public float getCusto() {
        return this.custo;
    }

    public Ponto getPontoFinal() {
        return this.pontoFinal;
    }

    public void terminaViagem(Ponto pontoFinal, LocalDateTime tempoFinal) {
        this.pontoFinal = pontoFinal;
        this.duracao = (int) ChronoUnit.SECONDS.between(this.tempoComeco, tempoFinal);
        float distancia = (float) this.pontoInicial.distancia(pontoFinal);
        this.custo = (int) (distancia * 0.15);
        this.custo += duracao * (0.15 / 60.0);
    }

    public void adicionaRecompensa() {
        float valorRecompensa = (float) ((this.pontoInicial.distancia(this.pontoFinal) * 0.05) - (this.duracao * 0.01));
        this.recompensa = new Recompensa(this.pontoInicial, this.pontoFinal, valorRecompensa);
    }

    public Recompensa getRecompensa() {
        return this.recompensa;
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
}
