import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que representa um utilizador do sistema
 */
public class User {
    /**
     * Nome do cliente
     */
    private final String nome;
    /**
     * Password do utilizador
     */
    private final String palavraPasse;
    /**
     * Lock para realizar controlo de concorrência para autenticação do utilizador
     */
    private final ReentrantLock authLock = new ReentrantLock();
    /**
     * Variavel que verifica se utilizador se encontra autenticado
     */
    private boolean autenticado;
    /**
     * Viagem atual do cliente
     */
    private Viagem viagem;

    /**
     * Construtor parametrizado do Utilizador
     * @param nome nome do utilizador
     * @param palavraPasse password do utilizador
     */
    public User(String nome, String palavraPasse) {
        this.nome = nome;
        this.palavraPasse = palavraPasse;
        this.autenticado = false;
        this.viagem = null;
    }

    /**
     * Construtor de Cópia do utilizador
     * @param u utilizador a copiar
     */
    public User(@NotNull User u) {
        this.nome = u.getNome();
        this.palavraPasse = u.getPalavraPasse();
        this.autenticado = u.isAutenticado();
        this.viagem = u.getViagem();
    }

    /**
     * Setter para uma viagem
     * @param viagem viagem a registar
     */
    public void setViagem(Viagem viagem) {
        this.viagem = viagem;
    }

    /**
     * Metodo Getter da viagem
     * @return viagem a retornar
     */
    public Viagem getViagem() {
        return this.viagem;
    }

    /**
     * Metodo que verifica se user se encontra autenticado
     * @return
     */
    public boolean isAutenticado(){
        return this.autenticado;
    }

    /**
     * Metodo getter de nome do utilizador
     * @return
     */
    public String getNome() {
        return nome;
    }

    /**
     * Metodo getter para a palavra-passe do utilizador
     * @return palavra passe do utilizador
     */
    public String getPalavraPasse() {
        return palavraPasse;
    }

    public User clone() {
        return new User(this);
    }

    /**
     * Metodo que efetua login do utilizador
     * @param password password do utilizador
     * @return true se autenticação for bem sucedida, false caso contrário
     */
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

    /**
     * Metodo que realiza o logout do utilizador
     */
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
