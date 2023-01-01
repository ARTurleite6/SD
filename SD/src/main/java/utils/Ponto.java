package utils;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Ponto {
    private final int x;
    private final int y;

    public Ponto() {
        this.x = 0;
        this.y = 0;
    }

    public Ponto(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Ponto(@NotNull Ponto p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double distancia(Ponto p) {
        return Math.sqrt(Math.pow(this.x - p.getX(), 2) + Math.pow(this.y - p.getY(), 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ponto ponto = (Ponto) o;

        if (getX() != ponto.getX()) return false;
        return getY() == ponto.getY();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("utils.Ponto{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + getY();
        return result;
    }

    public Ponto clone() {
        return new Ponto(this);
    }

    public void serialize(@NotNull DataOutputStream dos) throws IOException {
        dos.writeInt(this.x);
        dos.writeInt(this.y);
    }

    public static @NotNull Ponto deserialize(@NotNull DataInputStream dis) throws IOException {
        int x = dis.readInt();
        int y = dis.readInt();
        return new Ponto(x, y);
    }
}
