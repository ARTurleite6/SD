import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Parque {
    private final Ponto localizacao;
    private int numeroTrotinetes;
    private boolean recompensa;
    private final List<Ponto> vizinhos;

    public Parque() {
        this.localizacao = new Ponto();
        this.numeroTrotinetes = 0;
        this.recompensa = false;
        this.vizinhos = new ArrayList<>();
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
        this.vizinhos = new ArrayList<>(vizinhos);
    }

    public Parque(@NotNull Parque p) {
        this.localizacao = p.getLocalizacao();
        this.numeroTrotinetes = p.getNumeroTrotinetes();
        this.recompensa = p.hasRecompensa();
        this.vizinhos = p.getVizinhos();
    }

    private List<Ponto> loadVizinhos(int raio, int tam) {
        List<Ponto> pontos = new ArrayList<>();
        for(int y = this.localizacao.getY() - raio; y >= 0 && y < tam && y < this.localizacao.getY() + raio; ++y) {
            for(int x = this.localizacao.getX() - raio; x >= 0 && x < tam && x < this.localizacao.getX() + raio; ++x) {
                var ponto = new Ponto(x, y);
                if(this.localizacao.distancia(ponto) <= raio) pontos.add(ponto);
            }
        }
        return pontos;
    }

    public List<Ponto> getVizinhos() {
        return new ArrayList<>(this.vizinhos);
    }

    public boolean hasRecompensa() {
        return this.recompensa;
    }

    public Ponto getLocalizacao() {
        return localizacao;
    }

    public int getNumeroTrotinetes() {
        return this.numeroTrotinetes;
    }

}
