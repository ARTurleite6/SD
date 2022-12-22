import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class GestaoRecompensas {

    public final Lock pontoLock = new ReentrantLock();
    private Ponto pontoAlterado;
    private final Condition alteracaoFeita = pontoLock.newCondition();
    private final Parque[][] mapa;


    public GestaoRecompensas(Parque[][] mapa) {
        this.pontoAlterado = null;
        this.mapa = mapa;
        for(int i = 0; i < GestaoReservas.N; ++i) {
            for(int j = 0; j < GestaoReservas.N; ++j) {
                var ponto = new Ponto(i, j);
                this.atualizaPonto(ponto);
            }
        }
    }

    public void registaAlteracao(Ponto p) {
        this.pontoAlterado = p;
        this.alteracaoFeita.signal();
    }

    private boolean atualizaPonto(@NotNull Ponto p) {
            var parquePonto = this.mapa[p.getY()][p.getX()];
            var pontos = parquePonto.getVizinhos().stream().map(ponto -> this.mapa[ponto.getY()][ponto.getX()]).toList();
            try {
                pontos.forEach(ponto -> ponto.lock.lock());
                boolean recompensa = true;
                for(var parque : pontos) {
                    if (parque.getNumeroTrotinetes() != 0) {
                        System.out.println("Ola");
                        recompensa = false;
                        break;
                    }
                }
                System.out.println("Recompensa =" + recompensa + ", ponto = " + p);
                System.out.println(parquePonto);
                parquePonto.setRecompensa(recompensa);
                System.out.println(parquePonto);
                return recompensa;
            } finally {
                pontos.forEach(ponto -> ponto.lock.unlock());
            }
    }

    public List<Recompensa> getRecompensas(@NotNull Ponto p) {
        if(p.getY() >= GestaoReservas.N || p.getY() < 0 || p.getX() >= GestaoReservas.N || p.getX() < 0) return null;
        List<Recompensa> res = new ArrayList<>();
        Set<Ponto> pontosVizinhos = this.mapa[p.getY()][p.getX()].getVizinhos();
        for (int i = 0; i < GestaoReservas.N; ++i) {
            for (int j = 0; j < GestaoReservas.N; ++j) {
                var parque = this.mapa[i][j];
                if (pontosVizinhos.contains(parque.getLocalizacao())) continue;
                try {
                    parque.lock.lock();
                    if (parque.hasRecompensa()) {
                        for (var ponto : pontosVizinhos) {
                            var parqueVizinho = this.mapa[ponto.getY()][ponto.getX()];
                            try {
                                parqueVizinho.lock.lock();
                                if (parqueVizinho.getNumeroTrotinetes() > 1) {
                                    res.add(new Recompensa(parqueVizinho.getLocalizacao(), parque.getLocalizacao(), -1));
                                }
                            } finally {
                                parqueVizinho.lock.unlock();
                            }
                        }
                    }
                } finally {
                    parque.lock.unlock();
                }
            }
        }
        return res;
    }

    private List<Parque> getVizinhosPonto(Ponto ponto) {
        return this.mapa[ponto.getY()][ponto.getX()].getVizinhos().stream().map(vizinho -> this.mapa[vizinho.getY()][vizinho.getX()]).collect(Collectors.toList());
    }

    public void atualizaRecompensas() throws InterruptedException {
        while(true) {
            while(this.pontoAlterado == null) {
                this.alteracaoFeita.await();
            }
            System.out.println("Running");

            var vizinhos = this.mapa[pontoAlterado.getY()][pontoAlterado.getX()].getVizinhos();
            for(var vizinho : vizinhos) {
                System.out.println("Hello");
                if(this.atualizaPonto(vizinho))
                    System.out.println("Ponto alterado: vizinho = " + vizinho);
            }
            this.pontoAlterado = null;
        }
    }
}
