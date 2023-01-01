import org.jetbrains.annotations.NotNull;
import utils.Ponto;
import utils.Recompensa;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class GestaoRecompensas {

    public final Lock pontoLock = new ReentrantLock();
    private Ponto pontoAlterado;
    private final Condition alteracaoFeita = pontoLock.newCondition();
    private final Parque[][] mapa;

    private Map<Ponto, Atualizacao> atualizacoes;

    private static class Atualizacao {
        private Condition hasAtualizacao;
        private int number;
        private int recebidos;

        private boolean atualizado;

        public Atualizacao(Condition condition){
            this.hasAtualizacao = condition;
            this.number = 0;
            this.atualizado = false;
            this.recebidos = 0;
        }
    }


    public GestaoRecompensas(Parque[][] mapa) {
        this.pontoAlterado = null;
        this.mapa = mapa;
        this.atualizacoes = new HashMap<>();
        try {
            this.pontoLock.lock();
            for (int i = 0; i < GestaoReservas.N; ++i) {
                for (int j = 0; j < GestaoReservas.N; ++j) {
                    var ponto = new Ponto(j, i);
                    if (this.atualizaPonto(ponto)) {
                        System.out.println("Ponto alterado: vizinho = " + ponto);
                        var parqueVizinho = this.mapa[ponto.getY()][ponto.getX()].getVizinhos();
                        for (int z = 0; z < GestaoReservas.N; ++z) {
                            for (int h = 0; h < GestaoReservas.N; ++h) {
                                if (parqueVizinho.contains(new Ponto(h, z))) continue;
                                this.mapa[h][z].addRecompensa(ponto);
                            }
                        }
                    }
                }
            }
        } finally {
            this.pontoLock.unlock();
        }
    }

    public void registaAlteracao(Ponto p) {
        this.pontoAlterado = p;
        this.alteracaoFeita.signal();
    }

    private boolean atualizaPonto(@NotNull Ponto p) {
        var parquePonto = this.mapa[p.getY()][p.getX()];
        if(parquePonto.getNumeroTrotinetes() > 0) return false;
        var pontos = parquePonto.getVizinhos().stream().map(ponto -> this.mapa[ponto.getY()][ponto.getX()]).toList();
        boolean recompensa = true;
        for(var parque : pontos) {
            if (parque.getNumeroTrotinetes() != 0) {
                System.out.println("Ola");
                recompensa = false;
                break;
            }
        }
        System.out.println("utils.Recompensa =" + recompensa + ", ponto = " + p);
        System.out.println(parquePonto);
        parquePonto.setRecompensa(recompensa);
        System.out.println(parquePonto);
        return recompensa;
    }

    public List<Recompensa> getRecompensas(@NotNull Ponto p) {
        return this.mapa[p.getY()][p.getX()]
                .getVizinhos()
                .stream()
                .flatMap(ponto -> this.mapa[ponto.getY()][ponto.getX()]
                        .getRecompensas()
                        .stream())
                .collect(Collectors.toList());
    }

    private List<Parque> getVizinhosPonto(@NotNull Ponto ponto) {
        return this.mapa[ponto.getY()][ponto.getX()].getVizinhos().stream().map(vizinho -> this.mapa[vizinho.getY()][vizinho.getX()]).collect(Collectors.toList());
    }

    public void atualizaRecompensas() throws InterruptedException {
        while(true) {
            while(this.pontoAlterado == null) {
                this.alteracaoFeita.await();
            }
            System.out.println("Running");

            var parque = this.mapa[pontoAlterado.getY()][pontoAlterado.getX()];
            if(parque.getNumeroTrotinetes() == 2) {
                var at = this.atualizacoes.get(this.pontoAlterado);
                if(at != null) {
                    at.atualizado = true;
                    at.hasAtualizacao.signalAll();
                }
            }
            else {
                var vizinhos = parque.getVizinhos();
                for (var vizinho : vizinhos) {
                    System.out.println("Hello");
                    if (this.atualizaPonto(vizinho)) {
                        System.out.println("Ponto alterado: vizinho = " + vizinho);
                        var parqueVizinho = this.mapa[vizinho.getY()][vizinho.getX()].getVizinhos();
                        for (int i = 0; i < GestaoReservas.N; ++i) {
                            for (int j = 0; j < GestaoReservas.N; ++j) {
                                if (parqueVizinho.contains(new Ponto(j, i))) continue;
                                this.mapa[i][j].addRecompensa(vizinho);
                                this.mapa[i][j].getVizinhos().forEach(vizinhoPoint -> {
                                    var condition = this.atualizacoes.get(vizinhoPoint);
                                    if (condition != null) {
                                        condition.hasAtualizacao.signalAll();
                                        condition.atualizado = true;
                                    }
                                });
                            }
                        }
                    } else {
                        System.out.println("Ponto alterado: vizinho = " + vizinho);
                        var parqueAux = this.mapa[vizinho.getY()][vizinho.getX()];
                        var parqueVizinho = parqueAux.getVizinhos();
                        for (int i = 0; i < GestaoReservas.N; ++i) {
                            for (int j = 0; j < GestaoReservas.N; ++j) {
                                if (parqueVizinho.contains(new Ponto(j, i))) continue;
                                this.mapa[i][j].removeRecompensa(vizinho);
                            }
                        }
                    }
                }
            }
            this.pontoAlterado = null;
        }
    }

    public List<Recompensa> getAtualizacaoRecompensa(@NotNull Ponto ponto) throws InterruptedException {
        try {
            this.pontoLock.lock();
            //nao preciso de dar lock no parque pois o seu numero de trotinetes nunca muda enquanto o pontoLock está
            // ativo
            var condition = this.atualizacoes.get(ponto);
            if(condition == null) {
                System.out.println("Deu null nas atualizacoes");
                return null;
            }
            while(!condition.atualizado || this.getRecompensas(ponto).isEmpty()) {
                condition.atualizado = false;
                condition.hasAtualizacao.await();
            }

            if(++condition.recebidos == condition.number) {
                condition.atualizado = false;
                condition.recebidos = 0;
            }
            return this.getRecompensas(ponto);
        } finally {
            this.pontoLock.unlock();
        }
    }

    public void registaReceberAtualizacoes(@NotNull Ponto ponto) {
        try {
            this.pontoLock.lock();
            var at = this.atualizacoes.get(ponto);
            if(at == null) {
                at = new Atualizacao(this.pontoLock.newCondition());
                this.atualizacoes.put(ponto, at);
            }

            at.number++;
        } finally {
            this.pontoLock.unlock();
        }
    }

    public void eliminaReceberAtualizacoes(@NotNull Ponto ponto) {
        try {
            this.pontoLock.lock();
            var at = this.atualizacoes.get(ponto);
            if(at == null) return;

            if(--at.number == 0) {
                this.atualizacoes.remove(ponto);
            }
        } finally {
            this.pontoLock.unlock();
        }
    }
}
