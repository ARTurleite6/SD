import org.jetbrains.annotations.NotNull;
import utils.Ponto;
import utils.Recompensa;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.stream.Stream;

/**
 * Classe que Realiza a gestão de reservas do sistema
 */
public class GestaoReservas {
    /**
     * Tamanho do mapa, alterar esta variável, gera mapas maiores
     */
    public static final int N = 10;
    /**
     * Raio D a considerar no sistema
     */
    public static final int D = 2;
    /**
     * Mapa da aplicacao
     */
    private final Parque[][] mapa;

    /**
     * Sistema que lida com a gestão de recompensas
     */
    private final GestaoRecompensas gestaoRecompensas;

    /**
     * Lock que lida com o controlo de concorrência do Map dos Users
     */
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    /**
     * Map que guarda os users registados no sistema
     */
    private final Map<String, User> users;

    /**
     * Metodo que inicializa o estado do sistema
     */
    public GestaoReservas() {
        this.mapa = new Parque[N][N];
        this.users = new HashMap<>();
        this.loadMap();
        this.gestaoRecompensas = new GestaoRecompensas(this.mapa);
        new Thread(() -> {
            try {
                    this.gestaoRecompensas.pontoLock.lock();
                    this.gestaoRecompensas.atualizaRecompensas();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                this.gestaoRecompensas.pontoLock.unlock();
            }
        }).start();
    }

    /**
     * Metodo que dá load ao mapa, distribuindo as trotinetes de forma dispar pelo mapa
     */
    private void loadMap() {
        for(int y = 0; y < N; ++y) {
            for(int x = 0; x < N; ++x) {
                this.mapa[y][x] = new Parque(new Ponto(x, y), D, N);
            }
        }
        var rand = new Random();
        for(int i = 0; i < 2 * N; ++i) {
            //generate random number between 0 and N
            var x = rand.nextInt(N);
            var y = rand.nextInt(N);
            this.mapa[y][x].estacionaTrotinete();
        }

    }

    /**
     * Metodo que regita um utilizador
     * @param username username do utilizador
     * @param password password do utilizador
     * @return true se for possivel registar o utilizador, false caso contrario
     */
    public boolean registaUtilizador(String username, String password) {
        try {
            this.usersLock.writeLock().lock();
            if (this.users.containsKey(username)) return false;
            this.users.put(username, new User(username, password));
            return true;
        } finally {
            this.usersLock.writeLock().unlock();
        }
    }

    /**
     * Metodo que autentica um utilizador
     * @param username username do utilizador
     * @param password password do utilizador
     * @return true se autenticação for bem sucedida, falso caso contrário
     */
    public boolean login(String username, String password) {
        try {
            this.usersLock.readLock().lock();
            if (!this.users.containsKey(username)) return false;

            return this.users.get(username).login(password);
        } finally {
            this.usersLock.readLock().unlock();
        }
    }

    /**
     * Metodo que realiza logou to utilizador
     * @param username username do utilizador
     */
    public void logOut(String username) {
        try {
            this.usersLock.readLock().lock();
            var user = this.users.get(username);
            if(user != null) {
                user.logout();
            }
        } finally {
            this.usersLock.readLock().unlock();
        }
    }

    /**
     * Metodo que gera um codigo aleatório para a viagem
     * @return codigo para ser utilizado na viagem
     */
    private int geraCodigo() {
        var rand = new Random();
        int codigo = rand.nextInt(1, 100_000_000);
        return codigo;
    }

    /**
     * Metodo que estaciona uma trotinete
     * @param username username do utilizador
     * @param codigo codigo da viagem
     * @param pontoEstacionamento ponto onde estacionar a trotinete
     * @return Viagem relativa ao estacionamento que utilizador vai realizar
     */
    public Viagem estacionaTrotinete(String username, int codigo, @NotNull Ponto pontoEstacionamento) {
        Parque parque = null;
        Parque parqueInicial = null;
        if(pontoEstacionamento.getY() < 0 || pontoEstacionamento.getY() >= N || pontoEstacionamento.getX() < 0 || pontoEstacionamento.getX() >= N) return null;
        try {
            this.usersLock.readLock().lock();
            var user = this.users.get(username);
            if(user == null) return null;
            var viagem = user.getViagem();
            if(viagem == null) return null;
            if(viagem.getCodigo() != codigo) return null;
            user.setViagem(null);
            parque = this.mapa[pontoEstacionamento.getY()][pontoEstacionamento.getX()];
            this.gestaoRecompensas.pontoLock.lock();
            var numeroTrotinetes = parque.getNumeroTrotinetes();
            parque.estacionaTrotinete();
            viagem.terminaViagem(pontoEstacionamento, LocalDateTime.now());
            parqueInicial = this.mapa[viagem.getPontoInicial().getY()][viagem.getPontoInicial().getX()];
            if(parque.hasRecompensa() && !parque.isVizinho(viagem.getPontoInicial()) && parqueInicial.getNumeroTrotinetes() > 1) {
                viagem.adicionaRecompensa();
            }
            if(numeroTrotinetes == 0 || numeroTrotinetes == 1)
                this.gestaoRecompensas.registaAlteracao(pontoEstacionamento);
            return viagem;
        } finally {
            this.usersLock.readLock().unlock();
            this.gestaoRecompensas.pontoLock.unlock();
        }
    }

    /**
     * Metodo que inicia uma viagem reservando uma trotinete num determinado ponto
     * @param username username do utilizador da viagem
     * @param p ponto onde se deseja realizar a reserva de uma trotinete
     * @return Viagem criada
     */
    public Viagem reservaTrotinete(String username, @NotNull Ponto p) {
        int x = p.getX();
        int y = p.getY();
        if(x < 0 || x >= N || y < 0 || y >= N) return null;
        var parquePonto = this.mapa[y][x];
        var vizinhosParque = new ArrayList<>(parquePonto
                .getVizinhos()
                .stream()
                .sorted(Comparator.comparingDouble(p2 -> p2.distancia(p)))
                .map(po -> this.mapa[po.getY()][po.getX()]).toList());
        if(vizinhosParque.size() == 0) return null;

        vizinhosParque.sort((v1, v2) -> v2.getNumeroTrotinetes() - v1.getNumeroTrotinetes());
        Parque escolhido = vizinhosParque.get(0);
        try {
            var numeroAnterior = escolhido.getNumeroTrotinetes();
            if(numeroAnterior == 0) return null;
            this.gestaoRecompensas.pontoLock.lock();
            escolhido.reservaTrotinete();
            if(numeroAnterior == 1)
                this.gestaoRecompensas.registaAlteracao(escolhido.getLocalizacao());
            var viagem = new Viagem(this.geraCodigo(), escolhido.getLocalizacao());
            this.usersLock.readLock().lock();
            this.users.get(username).setViagem(viagem);
            return viagem;
        } finally {
            this.usersLock.readLock().unlock();
            this.gestaoRecompensas.pontoLock.unlock();
        }
    }

    /**
     * Metodo que retorna uma lista com pontos vizinhos que possuem pelo menos uma trotinete
     * @param p ponto onde se deseja realizar uma reserva
     * @return Lista com pontos vizinhos que possuem pelo menos uma trotinete
     */
    public List<Ponto> getPontosVizinhosComTrotinete(@NotNull Ponto p) {
        int x = p.getX();
        int y = p.getY();
        List<Ponto> res = new ArrayList<>();
        if(x < 0 || x >= N || y < 0 || y >= N) return res;

        var pontosVizinhos = this.mapa[y][x].getVizinhos();
        for(var parque : pontosVizinhos) {
            var ponto = this.mapa[parque.getY()][parque.getX()];
            if(ponto.getNumeroTrotinetes() > 0) res.add(parque);
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GestaoReservas gestaoReservas = (GestaoReservas) o;

        if (!Arrays.deepEquals(mapa, gestaoReservas.mapa)) return false;
        return users.equals(gestaoReservas.users);
    }

    @Override
    public int hashCode() {
        int result = N;
        result = 31 * result + D;
        result = 31 * result + Arrays.deepHashCode(mapa);
        result = 31 * result + users.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Business{");
        sb.append("N=").append(N);
        sb.append(", D=").append(D);
        sb.append(", mapa=").append(Arrays.toString(Stream.of(mapa).map(Arrays::toString).toArray()));
        sb.append(", users=").append(users);
        sb.append('}');
        return sb.toString();
    }

    public String imprimeMapa() {
        try {
            this.gestaoRecompensas.pontoLock.lock();
            final StringBuilder sb = new StringBuilder("\nmapa = ");
            for (int i = 0; i < N; ++i) {
                sb.append("\n").append(i + 1).append(": ");
                for (int j = 0; j < N; ++j) {
                    var parque = this.mapa[i][j];
                        sb.append(" (").append(parque.getNumeroTrotinetes()).append(", ").append(parque.hasRecompensa()).append(")");
                }
            }
            return sb.toString();
        } finally {
            this.gestaoRecompensas.pontoLock.unlock();
        }
    }

    /**
     * Metodo que regista que um utilizador deseja receber atualizacoes
     * @param ponto ponto onde se deseja receber atualizacoes
     */
    public void registaRecebimentoRecompensa(@NotNull Ponto ponto) {
        this.gestaoRecompensas.registaReceberAtualizacoes(ponto);
    }

    public List<Recompensa> recebeRecompensasAtualizacao(@NotNull Ponto ponto) throws InterruptedException {
        return this.gestaoRecompensas.getAtualizacaoRecompensa(ponto);
    }

    public void cancelaRecebimentoRecompensa(@NotNull Ponto ponto) {
        this.gestaoRecompensas.eliminaReceberAtualizacoes(ponto);
    }

    public List<Recompensa> getRecompensas(Ponto ponto) {
        return this.gestaoRecompensas.getRecompensas(ponto);
    }
}
