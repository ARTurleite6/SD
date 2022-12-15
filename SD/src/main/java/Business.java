import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Business {
    private static final int N = 10;
    private static final int D = 2;
    private final Parque[][] mapa;

    private final Lock lock = new ReentrantLock();
    private final Map<Integer, Viagem> viagens;
    private Ponto pontoAlterado = null;
    private final Map<String, User> users;

    public Business() {
        this.mapa = new Parque[N][N];
        this.viagens = new HashMap<>();
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
            this.lock.lock();
            if (this.users.containsKey(username)) return false;
            this.users.put(username, new User(username, password));
            return true;
        } finally {
            this.lock.unlock();
        }
    }

    public boolean login(String username, String password) {
        try {
            this.lock.lock();
            if (!this.users.containsKey(username)) return false;

            return this.users.get(username).login(password);
        } finally {
            this.lock.unlock();
        }
    }

    private int geraCondigo() {
        var valido = false;
        var codigo = -1;
        var rand = new Random();
        do {
            codigo = rand.nextInt();
            if(!this.viagens.containsKey(codigo)) valido = true;
        } while(!valido);
        return codigo;
    }

    private boolean reservaTrotinete(String username, @NotNull Ponto p) {
        //TODO so preciso de sinalizar a thread das recompensas caso o valor do numero de trotinetes reservada passar a ser 1
        if(p.getX() >= N || p.getY() >= N) return false;
        var numeroTrotinetes = -1;
        var parque = this.mapa[p.getY()][p.getX()];
        try {
            parque.lock.lock();
            if (!parque.reservaTrotinete()) {
                return false;
            }
            numeroTrotinetes = parque.getNumeroTrotinetes();
            this.lock.lock();
        } finally {
            parque.lock.unlock();
        }
        try {
            var codigo = this.geraCondigo();
            this.viagens.put(codigo, new Viagem(codigo, username, p));
            if(numeroTrotinetes == 0) this.pontoAlterado = p;
            return true;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Business business = (Business) o;

        if (!Arrays.deepEquals(mapa, business.mapa)) return false;
        if (!lock.equals(business.lock)) return false;
        if (!viagens.equals(business.viagens)) return false;
        if (!pontoAlterado.equals(business.pontoAlterado)) return false;
        return users.equals(business.users);
    }

    @Override
    public int hashCode() {
        int result = N;
        result = 31 * result + D;
        result = 31 * result + Arrays.deepHashCode(mapa);
        result = 31 * result + lock.hashCode();
        result = 31 * result + viagens.hashCode();
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
        sb.append(", viagensLock=").append(lock);
        sb.append(", viagens=").append(viagens);
        sb.append(", pontoAlterado=").append(pontoAlterado);
        sb.append(", users=").append(users);
        sb.append('}');
        return sb.toString();
    }
}
