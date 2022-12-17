package Client;

import connection.TaggedConnection;
import org.jetbrains.annotations.NotNull;
import utils.Ponto;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cliente {

    private Socket socket;
    private Scanner scan;
    private String username;

    public Cliente(Scanner scan) throws IOException {
        this.socket = new Socket("localhost", 8080);
        this.scan = scan;
        this.username = null;
    }

    private @NotNull Menu initializaMenu(TaggedConnection conn) {
        var menu = new Menu(this.scan);
        menu.addOpcao("Regista Utilizador", () ->  {
            System.out.println("Insira o username desejado");
            var username = scan.nextLine();
            System.out.println("Insira a password desejada");
            var password = scan.nextLine();
            var bytes = new ByteArrayOutputStream();
            var stream = new DataOutputStream(bytes);
            stream.writeUTF(username);
            stream.writeUTF(password);
            stream.flush();
            conn.send(new TaggedConnection.Frame(1, bytes.toByteArray()));
            var answer = conn.receive();
            System.out.println(new String(answer.getData()));
        });

        menu.addOpcao("Login", () -> {
            System.out.println("Insira o seu username");
            var username = this.scan.nextLine();
            System.out.println("Insira o seu password");
            var password = scan.nextLine();
            var bytes = new ByteArrayOutputStream();
            var stream = new DataOutputStream(bytes);
            stream.writeUTF(username);
            stream.writeUTF(password);
            stream.flush();
            conn.send(2, bytes.toByteArray());
            var answer= conn.receive();
            var asnwerS = new String(answer.getData());
            System.out.println(asnwerS);
            if(asnwerS.equals("Login efetuado com sucesso")) {
                this.username = username;
            }
        });

        menu.addOpcao("Listar localizacoes com Trotinetes a D de distancia", () -> {
            System.out.println("Insira o X do ponto que deseja");
            int x = Integer.parseInt(this.scan.nextLine());
            System.out.println("Insira o Y do ponto que deseja");
            int y = Integer.parseInt(this.scan.nextLine());
            var ponto = new Ponto(x, y);
            var data = new ByteArrayOutputStream();
            var stream = new DataOutputStream(data);
            ponto.serialize(stream);
            conn.send(3, data.toByteArray());
            var answer = conn.receive();
            var bytes = new ByteArrayInputStream(answer.getData());
            var streamIn = new DataInputStream(bytes);
            var numPontos = streamIn.readInt();
            for(int i = 0; i < numPontos; ++i) {
                System.out.println(Ponto.deserialize(streamIn));
            }
        });

        menu.addOpcao("Reserva uma trotinete perto de um determinado local", () -> {
            if(this.username != null) {
                System.out.println("Insira o X do ponto que deseja");
                int x = Integer.parseInt(this.scan.nextLine());
                System.out.println("Insira o Y do ponto que deseja");
                int y = Integer.parseInt(this.scan.nextLine());
                Ponto ponto = new Ponto(x, y);
                var data = new ByteArrayOutputStream();
                var stream = new DataOutputStream(data);
                ponto.serialize(stream);
                conn.send(4, data.toByteArray());
                var answer = conn.receive();
                var bytes = new ByteArrayInputStream(answer.getData());
                var streamIn = new DataInputStream(bytes);
                int codigo = streamIn.readInt();
                if(codigo != -1) {
                    Ponto viagemOrigem = Ponto.deserialize(streamIn);
                    System.out.println("Reserva registada com o codigo: " + codigo + " e terá inicio na cordenada de " + viagemOrigem);
                }
                else {
                    System.out.println("Reserva efetuada com insucesso");
                }
            } else {
                System.out.println("Deve estar autenticado para fazer reservas");
            }
        });

        menu.addOpcao("Estaciona uma trotinete em um determinado ponto", () -> {
            if(this.username == null) System.out.println("Deve estar autenticado para efetuar uma reserva");
            else {
                System.out.println("Insira o codigo da reserva");
                int codigo = Integer.parseInt(this.scan.nextLine());
                System.out.println("Insira o X do ponto que deseja");
                int x = Integer.parseInt(this.scan.nextLine());
                System.out.println("Insira o Y do ponto que deseja");
                int y = Integer.parseInt(this.scan.nextLine());
                Ponto ponto = new Ponto(x, y);
                var data = new ByteArrayOutputStream();
                var streamOut = new DataOutputStream(data);
                streamOut.writeInt(codigo);
                ponto.serialize(streamOut);
                streamOut.flush();
                conn.send(5, data.toByteArray());
                var answer = conn.receive();
                try(var streamIn = new DataInputStream(new ByteArrayInputStream(answer.getData()))) {
                    float custo = streamIn.readFloat();
                    if (custo < 0) {
                        System.out.println("Operacao ocorreu com insucesso");
                    }
                    else {
                        System.out.println("Estacionou trotinete com sucesso, com um custo de viagem de " + custo);
                    }
                }
            }
        });
        return menu;
    }

    public void run() throws IOException {
        try(var taggedConnection = new TaggedConnection(this.socket)) {

            var menu = this.initializaMenu(taggedConnection);
            menu.run();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
