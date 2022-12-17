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

    public Cliente(Scanner scan) throws IOException {
        this.socket = new Socket("localhost", 8080);
        this.scan = scan;
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
            System.out.println(new String(answer.getData()));
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
        return menu;
    }

    public void run() throws IOException {
        System.out.println("arranquei");
        try(var taggedConnection = new TaggedConnection(this.socket)) {

            var menu = this.initializaMenu(taggedConnection);
            menu.run();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
