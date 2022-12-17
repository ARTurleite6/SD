import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

public class User {
    private final String nome;
    private final String palavraPasse;
    private ReentrantLock authLock = new ReentrantLock();
    private boolean autenticado;
    private Viagem viagem;

    public User() {
        this.nome = "";
        this.palavraPasse = "";
        this.autenticado = false;
        this.viagem = null;
    }

    public User(String nome, String palavraPasse) {
        this.nome = nome;
        this.palavraPasse = palavraPasse;
        this.autenticado = false;
        this.viagem = null;
    }

    public User(String nome, String palavraPasse, boolean autenticado) {
        this.nome = nome;
        this.palavraPasse = palavraPasse;
        this.autenticado = autenticado;
        this.viagem = null;
    }

    public User(@NotNull User u) {
        this.nome = u.getNome();
        this.palavraPasse = u.getPalavraPasse();
        this.autenticado = u.isAutenticado();
        this.viagem = u.getViagem();
    }

    public void setViagem(Viagem viagem) {
        this.viagem = viagem;
    }

    public Viagem getViagem() {
        return this.viagem;
    }

    public boolean isAutenticado(){
        return this.autenticado;
    }

    public String getNome() {
        return nome;
    }

    public String getPalavraPasse() {
        return palavraPasse;
    }

    public User clone() {
        return new User(this);
    }

    public boolean login(String password) {
        try {
            this.authLock.lock();
            if(this.autenticado) return false;
            if(!this.palavraPasse.equals(password)) return false;
            this.autenticado = true;
            return true;
        } finally {
            this.authLock.unlock();
        }
    }

    public void logout() {
        this.autenticado = false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("nome='").append(nome).append('\'');
        sb.append(", palavraPasse='").append(palavraPasse).append('\'');
        sb.append(", autenticado=").append(autenticado);
        sb.append(", viagem=").append(viagem);
        sb.append('}');
        return sb.toString();
    }
}
