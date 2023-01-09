import org.jetbrains.annotations.NotNull;
import utils.Ponto;
import utils.Recompensa;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe que representa um ponto do mapa
 */
public class Parque {
    /**
     * localização do parque no mapa
     */
    private final Ponto localizacao;
    /**
     * Numero de trotinetes num determinado parque do mapa
     */
    private int numeroTrotinetes;
    /**
     * Verifica se pode existir alguma recompensa no parque do mapa
     */
    private boolean recompensa;
    /**
     * Lista de pontos vizinhos do parque do mapa
     */
    private final Set<Ponto> vizinhos;

    /**
     * Lista com Pontos que gerarão recompensa deste ponto para o proximo
     */
    private final Set<Ponto> recompensas = new HashSet<>();

    /**
     * Construtor parametrizado do Parque
     * @param localizacao localização do parque no mapa
     * @param D diâmetro a ser considerado na app
     * @param N numero de pontos do mapa
     */
    public Parque(Ponto localizacao, int D, int N) {
        this.localizacao = localizacao;
        this.numeroTrotinetes = 0;
        this.recompensa = false;
        this.vizinhos = this.loadVizinhos(D, N);
    }

    /**
     * Metodo que dá load dos vizinhos de um parque
     * @param raio raio a ser considerado
     * @param tam tamanho a ser considerado
     * @return lista de pontos vizinhos
     */
    private Set<Ponto> loadVizinhos(int raio, int tam) {
        Set<Ponto> pontos = new HashSet<>();
        for(int y = Math.max(this.localizacao.getY() - raio, 0); y < tam && y <= this.localizacao.getY() + raio; ++y) {
            for(int x = Math.max(this.localizacao.getX() - raio, 0); x < tam && x <= this.localizacao.getX() + raio; ++x) {
                var ponto = new Ponto(x, y);
                System.out.println(ponto);
                var distancia = this.localizacao.distancia(ponto);
                System.out.println(distancia);
                if(Double.compare(this.localizacao.distancia(ponto), raio) <= 0) pontos.add(ponto);
            }
        }
        System.out.println("pontos = " + pontos);
        return pontos;
    }

    /**
     * Metodo que adiciona um ponto para onde terá recompensa
     * @param p ponto onde terá recompensa através de uma viagem
     */
    public void addRecompensa(Ponto p) {
        this.recompensas.add(p);
    }

    /**
     * Metodo que remove ponto como possivel recompensa numa viagem
     * @param p ponto a ser adicionado
     */
    public void removeRecompensa(Ponto p) {
        this.recompensas.remove(p);
    }

    /**
     * Metodo que retorna a lista de recompensas a partir deste ponto
     * @return lista de recompensas a partir deste ponto
     */
    public List<Recompensa> getRecompensas() {
        if(this.numeroTrotinetes < 2) return new ArrayList<>();
        return this.recompensas.stream().map(ponto -> new Recompensa(this.localizacao, ponto, 0)).collect(Collectors.toList());
    }

    /**
     * Lista com pontos vizinhos
     * @return pontos vizinhos
     */
    public Set<Ponto> getVizinhos() {
        return new HashSet<>(this.vizinhos);
    }

    /**
     * Testa se um ponto é vizinho deste parque
     * @param p ponto a testar
     * @return true se for vizinho, false caso contrário
     */
    public boolean isVizinho(Ponto p) {
        return this.vizinhos.contains(p);
    }

    /**
     * Metodo que verifica se tem recompensa num ponto(não possui pontos vizinhos com trotinetes num raio D)
     * @return
     */
    public boolean hasRecompensa() {
        return this.recompensa;
    }

    public void setRecompensa(boolean value) {
        this.recompensa = value;
    }

    public Ponto getLocalizacao() {
        return localizacao;
    }

    public int getNumeroTrotinetes() {
        return this.numeroTrotinetes;
    }

    public void reservaTrotinete() {
        --this.numeroTrotinetes;
    }

    public void estacionaTrotinete() {
        ++this.numeroTrotinetes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parque parque = (Parque) o;

        if (getNumeroTrotinetes() != parque.getNumeroTrotinetes()) return false;
        if (recompensa != parque.recompensa) return false;
        if (!getLocalizacao().equals(parque.getLocalizacao())) return false;
        return getVizinhos().equals(parque.getVizinhos());
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + getLocalizacao().hashCode();
        result = 31 * result + getNumeroTrotinetes();
        result = 31 * result + (recompensa ? 1 : 0);
        result = 31 * result + getVizinhos().hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Parque{");
        sb.append(", localizacao=").append(localizacao);
        sb.append(", numeroTrotinetes=").append(numeroTrotinetes);
        sb.append(", recompensa=").append(recompensa);
        sb.append(", vizinhos=").append(vizinhos);
        sb.append('}');
        return sb.toString();
    }
}
