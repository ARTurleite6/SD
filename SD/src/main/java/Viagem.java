public class Viagem {
    private final int codigo;
    private final String username;
    private final Ponto pontoInicial;

    public Viagem(int codigo, String username, Ponto pontoInicial) {
        this.codigo = codigo;
        this.username = username;
        this.pontoInicial = pontoInicial;
    }

    public Viagem(Viagem viagem) {
        this.codigo = viagem.getCodigo();
        this.username = viagem.getUsername();
        this.pontoInicial = viagem.getPontoInicial();
    }

    public int getCodigo() {
        return codigo;
    }

    public String getUsername() {
        return username;
    }

    public Ponto getPontoInicial() {
        return pontoInicial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Viagem viagem = (Viagem) o;

        if (getCodigo() != viagem.getCodigo()) return false;
        if (!getUsername().equals(viagem.getUsername())) return false;
        return getPontoInicial().equals(viagem.getPontoInicial());
    }

    @Override
    public int hashCode() {
        int result = getCodigo();
        result = 31 * result + getUsername().hashCode();
        result = 31 * result + getPontoInicial().hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Viagem{");
        sb.append("codigo=").append(codigo);
        sb.append(", username='").append(username).append('\'');
        sb.append(", pontoInicial=").append(pontoInicial);
        sb.append('}');
        return sb.toString();
    }

    public Viagem clone() {
        return new Viagem(this);
    }
}
