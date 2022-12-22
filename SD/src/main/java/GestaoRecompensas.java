import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GestaoRecompensas {

    private final Lock pontoLock = new ReentrantLock();
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
        try {
            this.pontoLock.lock();
            this.pontoAlterado = p;
            this.alteracaoFeita.signal();
        } finally {
            this.pontoLock.unlock();
        }
    }

    private boolean atualizaPonto(@NotNull Ponto p) {
            var parquePonto = this.mapa[p.getY()][p.getX()];
            var pontos = parquePonto.getVizinhos().stream().map(ponto -> this.mapa[ponto.getY()][ponto.getX()]).toList();
            try {
                pontos.forEach(ponto -> ponto.lock.lock());
                boolean recompensa = true;
                for(var parque : pontos) {
                    if (parque.getNumeroTrotinetes() != 0) {
                        recompensa = false;
                        break;
                    }
                }
                parquePonto.setRecompensa(recompensa);
                return recompensa;
            } finally {
                pontos.forEach(ponto -> ponto.lock.unlock());
            }
    }

    public void atualizaRecompensas() throws InterruptedException {
        try {
            this.pontoLock.lock();
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
        } finally {
            this.pontoLock.unlock();
        }
    }
}
