import org.jetbrains.annotations.NotNull;
import utils.Ponto;
import utils.Recompensa;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Viagem {
    /**
     * Codigo da viagem
     */
    private final int codigo;
    /**
     * Ponto inicial da viagem
     */
    private final Ponto pontoInicial;
    /**
     * Ponto final da viagem
     */
    private Ponto pontoFinal;
    /**
     * Tempo de começo da viagem
     */
    private final LocalDateTime tempoComeco;
    /**
     * Duração da viagem
     */
    private int duracao;
    /**
     * Custo de uma viagem
     */
    private float custo;
    /**
     * Recompensa associada à viagem
     */
    private Recompensa recompensa;

    /**
     * Construtor parametrizado da viagem
     * @param codigo codigo da viagem
     * @param pontoInicial ponto inicial da viagem
     */
    public Viagem(int codigo, Ponto pontoInicial) {
        this.codigo = codigo;
        this.pontoInicial = pontoInicial;
        this.tempoComeco = LocalDateTime.now();
        this.duracao = 0;
        this.custo = 0;
        this.pontoFinal = null;
        this.recompensa = null;
    }

    /**
     * Construtor de cópia de viagem
     * @param viagem viagem a copiar
     */
    public Viagem(@NotNull Viagem viagem) {
        this.codigo = viagem.getCodigo();
        this.pontoInicial = viagem.getPontoInicial();
        this.pontoFinal = viagem.getPontoFinal();
        this.tempoComeco = viagem.tempoComeco;
        this.recompensa = viagem.getRecompensa();
    }

    /**
     * Metodo que retorna custo de uma viagem
     * @return custo da viagem
     */
    public float getCusto() {
        return this.custo;
    }

    /**
     * Metodo que retorna o ponto destino da viagem
     * @return ponto destino da viagem
     */
    public Ponto getPontoFinal() {
        return this.pontoFinal;
    }

    /**
     * Metodo que sinaliza que a viagem já terminou, e regista o custo da viagem
     * @param pontoFinal ponto final da viagem
     * @param tempoFinal tempo final da viagem
     */
    public void terminaViagem(Ponto pontoFinal, LocalDateTime tempoFinal) {
        this.pontoFinal = pontoFinal;
        this.duracao = (int) ChronoUnit.SECONDS.between(this.tempoComeco, tempoFinal);
        float distancia = (float) this.pontoInicial.distancia(pontoFinal);
        this.custo = (int) (distancia * 0.15);
        this.custo += duracao * (0.15 / 60.0);
    }

    /**
     * Metodo que adiciona uma recompensa à viagem
     */
    public void adicionaRecompensa() {
        float valorRecompensa = (float) ((this.pontoInicial.distancia(this.pontoFinal) * 0.05) - (this.duracao * 0.01));
        this.recompensa = new Recompensa(this.pontoInicial, this.pontoFinal, valorRecompensa);
    }

    /**
     * Metodo que retorna a recompensa da viagem
     * @return recompensa da viagem
     */
    public Recompensa getRecompensa() {
        return this.recompensa;
    }

    /**
     * Metodo que retorna o codigo da viagem
     * @return
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Metodo que retorna o ponto origem da viagem
     * @return
     */
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
