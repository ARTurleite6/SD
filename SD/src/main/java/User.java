import org.jetbrains.annotations.NotNull;

public class User {
    private final String nome;
    private final String palavraPasse;

    public User() {
        this.nome = "";
        this.palavraPasse = "";
    }

    public User(String nome, String palavraPasse) {
        this.nome = nome;
        this.palavraPasse = palavraPasse;
    }

    public User(@NotNull User u) {
        this.nome = u.getNome();
        this.palavraPasse = u.getPalavraPasse();
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
}
