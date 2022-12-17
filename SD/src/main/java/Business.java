import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Business {
    private static final int N = 10;
    private static final int D = 4;
    private final Parque[][] mapa;

    private final Lock pontolock = new ReentrantLock();
    private Ponto pontoAlterado = null;

    private ReadWriteLock usersLock = new ReentrantReadWriteLock();
    private final Map<String, User> users;

    public Business() {
        this.mapa = new Parque[N][N];
        this.users = new HashMap<>();
        this.loadMap();
    }

    private void loadMap() {
        for(int y = 0; y < N; ++y) {
            for(int x = 0; x < N; ++x) {
                this.mapa[y][x] = new Parque(new Ponto(x, y), D, N);
            }
        }
        var rand = new Random();
        var numberParques = N * N;
        for(int i = 0; i < numberParques; ++i) {
            //generate random number between 0 and N
            var x = rand.nextInt(N);
            var y = rand.nextInt(N);
            this.mapa[y][x].estacionaTrotinete();
        }

    }

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

    public boolean login(String username, String password) {
        try {
            this.usersLock.readLock().lock();
            if (!this.users.containsKey(username)) return false;

            return this.users.get(username).login(password);
        } finally {
            this.usersLock.readLock().unlock();
        }
    }

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

    private int geraCodigo() {
        var valido = false;
        var codigo = -1;
        var rand = new Random();
        codigo = rand.nextInt();
        return codigo;
    }

    public Viagem estacionaTrotinete(String username, int codigo, @NotNull Ponto pontoEstacionamento) {
        Parque parque = null;
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
        parque.lock.lock();
        parque.estacionaTrotinete();
        this.pontolock.lock();
        this.pontoAlterado = pontoEstacionamento;
        return viagem;
        } finally {
            this.usersLock.readLock().unlock();
            if(parque != null) {
                parque.lock.unlock();
                this.pontolock.unlock();
            }
        }
    }

    public Viagem reservaTrotinete(String username, @NotNull Ponto p) {
        //TODO so preciso de sinalizar a thread das recompensas caso o valor do numero de trotinetes reservada passar a ser 1
        int x = p.getX();
        int y = p.getY();
        if(x < 0 || x >= N || y < 0 || y >= N) return null;
        var parquePonto = this.mapa[y][x];
        var vizinhosParque = new ArrayList<>(parquePonto
                .getVizinhos()
                .stream()
                .sorted((p1, p2) -> Double.compare(p1.distancia(p), p2.distancia(p)))
                .map(po -> this.mapa[po.getY()][po.getX()]).toList());
        if(vizinhosParque.size() == 0) return null;

        vizinhosParque.forEach(parque -> parque.lock.lock());
        vizinhosParque.sort((v1, v2) -> v2.getNumeroTrotinetes() - v1.getNumeroTrotinetes());
        Parque escolhido = vizinhosParque.get(0);
        if(escolhido.getNumeroTrotinetes() == 0) return null;

        try {
            if (!escolhido.reservaTrotinete()) return null;
            this.usersLock.readLock().lock();
            var viagem = new Viagem(this.geraCodigo(), escolhido.getLocalizacao());
            this.users.get(username).setViagem(viagem);
            this.pontolock.lock();
            this.pontoAlterado = escolhido.getLocalizacao();
            return viagem;
        } finally {
            for(int i = 0; i < vizinhosParque.size(); ++i) {
                vizinhosParque.get(i).lock.unlock();
            }
            this.usersLock.readLock().unlock();
            this.pontolock.unlock();
        }
    }

    public List<Ponto> getPontosVizinhosComTrotinete(Ponto p) {
        int x = p.getX();
        int y = p.getY();
        List<Ponto> res = new ArrayList<>();
        if(x < 0 || x >= N || y < 0 || y >= N) return res;

        var pontosVizinhos = this.mapa[y][x].getVizinhos();
        for(var parque : pontosVizinhos) {
            var ponto = this.mapa[parque.getY()][parque.getX()];
            ponto.lock.lock();
            if(ponto.getNumeroTrotinetes() > 0) res.add(parque);
            ponto.lock.unlock();
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Business business = (Business) o;

        if (!Arrays.deepEquals(mapa, business.mapa)) return false;
        if (!pontoAlterado.equals(business.pontoAlterado)) return false;
        return users.equals(business.users);
    }

    @Override
    public int hashCode() {
        int result = N;
        result = 31 * result + D;
        result = 31 * result + Arrays.deepHashCode(mapa);
        result = 31 * result + pontoAlterado.hashCode();
        result = 31 * result + users.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Business{");
        sb.append("N=").append(N);
        sb.append(", D=").append(D);
        sb.append(", mapa=").append(Arrays.toString(Stream.of(mapa).map(Arrays::toString).toArray()));
        sb.append(", pontoAlterado=").append(pontoAlterado);
        sb.append(", users=").append(users);
        sb.append('}');
        return sb.toString();
    }
}
