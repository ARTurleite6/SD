import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.stream.Stream;

public class Business {
    private static final int N = 10;
    private static final int D = 1;
    private final Parque[][] mapa;

    public final Lock pontolock = new ReentrantLock();

    private final Condition updateRewards = this.pontolock.newCondition();
    public Ponto pontoAlterado = null;

    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    private final Map<String, User> users;

    public Business() {
        this.mapa = new Parque[N][N];
        this.users = new HashMap<>();
        this.loadMap();
    }

    public Ponto getPontoAlterado() {
        try {
            this.pontolock.lock();
            return this.pontoAlterado;
        } finally {
            this.pontolock.unlock();
        }
    }

    public Condition getUpdateRewards() {
        return this.updateRewards;
    }

    private void loadMap() {
        for(int y = 0; y < N; ++y) {
            for(int x = 0; x < N; ++x) {
                this.mapa[y][x] = new Parque(new Ponto(x, y), D, N);
            }
        }
        var rand = new Random();
        var numberParques = N;
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
            this.pontolock.lock();
            parque.estacionaTrotinete();
            this.pontoAlterado = pontoEstacionamento;
            this.updateRewards.signal();
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
        try {
            if(escolhido.getNumeroTrotinetes() == 0) return null;

            if (!escolhido.reservaTrotinete()) return null;
            this.usersLock.readLock().lock();
            var viagem = new Viagem(this.geraCodigo(), escolhido.getLocalizacao());
            this.users.get(username).setViagem(viagem);
            this.pontolock.lock();
            if(escolhido.getNumeroTrotinetes() == 0) {
                this.pontoAlterado = escolhido.getLocalizacao();
                this.updateRewards.signal();
            }
            return viagem;
        } finally {
            for (Parque parque : vizinhosParque) {
                parque.lock.unlock();
            }
            this.usersLock.readLock().unlock();
            this.pontolock.unlock();
        }
    }

    public Set<Ponto> getPontosVizinhoPonto(@NotNull Ponto ponto) {
        return this.mapa[ponto.getY()][ponto.getX()].getVizinhos();
    }

    public List<Ponto> getPontosVizinhosComTrotinete(@NotNull Ponto p) {
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

    public boolean atualizaRewardsPonto(@NotNull Ponto p) {
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
