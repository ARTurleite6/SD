import org.jetbrains.annotations.NotNull;
import utils.Ponto;
import utils.Recompensa;

import java.util.*;
import java.util.stream.Collectors;

public class Parque {
    private final Ponto localizacao;
    private int numeroTrotinetes;
    private boolean recompensa;
    private final Set<Ponto> vizinhos;

    private final Set<Ponto> recompensas = new HashSet<>();

    public Parque() {
        this.localizacao = new Ponto();
        this.numeroTrotinetes = 0;
        this.recompensa = false;
        this.vizinhos = new HashSet<>();
    }

    public Parque(Ponto localizacao, int D, int N) {
        this.localizacao = localizacao;
        this.numeroTrotinetes = 0;
        this.recompensa = false;
        this.vizinhos = this.loadVizinhos(D, N);
    }

    public Parque(Ponto localizacao, List<Ponto> vizinhos) {
        this.localizacao = localizacao;
        this.numeroTrotinetes = 0;
        this.recompensa = false;
        this.vizinhos = new HashSet<>(vizinhos);
    }

    public Parque(@NotNull Parque p) {
        this.localizacao = p.getLocalizacao();
        this.numeroTrotinetes = p.getNumeroTrotinetes();
        this.recompensa = p.hasRecompensa();
        this.vizinhos = p.getVizinhos();
    }

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

    public void addRecompensa(Ponto p) {
        this.recompensas.add(p);
    }

    public void removeRecompensa(Ponto p) {
        this.recompensas.remove(p);
    }

    public List<Recompensa> getRecompensas() {
        if(this.numeroTrotinetes < 2) return new ArrayList<>();
        return this.recompensas.stream().map(ponto -> new Recompensa(this.localizacao, ponto, 0)).collect(Collectors.toList());
    }

    public Set<Ponto> getVizinhos() {
        return new HashSet<>(this.vizinhos);
    }

    public boolean isVizinho(Ponto p) {
        return this.vizinhos.contains(p);
    }

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

    public boolean podeReservar() {
        return this.numeroTrotinetes > 0;
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
