package Client;

import connection.Demultiplexer;
import connection.TaggedConnection;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;
import utils.Ponto;

public class Cliente {

  private Socket socket;
  private Scanner scan;
  private Map<Ponto, Thread> atualizacaoWorker;
  private String username;

  public Cliente(Scanner scan) throws IOException {
    this.socket = new Socket("localhost", 8080);
    this.scan = scan;
    this.username = null;
    this.atualizacaoWorker = new HashMap<>();
  }

  private @NotNull Menu initializaMenu(Demultiplexer conn) {
    var menu = new Menu(this.scan);
    menu.addOpcao("Regista Utilizador", () -> {
      if(this.username != null) {
        System.out.println("Não pode estar logado ao efetuar registo.");
        return;
      }
      System.out.println("Insira o username desejado");
      var username = scan.nextLine();
      System.out.println("Insira a password desejada");
      var password = scan.nextLine();
      var bytes = new ByteArrayOutputStream();
      var stream = new DataOutputStream(bytes);
      stream.writeUTF(username);
      stream.writeUTF(password);
      stream.flush();
      System.out.println("Ola1");
      conn.send(new TaggedConnection.Frame(1, bytes.toByteArray()));
      System.out.println("Ola");
      var answer = conn.receive(1);
      System.out.println(new String(answer));
    });

    menu.addOpcao("Login", () -> {
      if(this.username != null) {
        System.out.println("Já se encontra logado no sistema.");
        return;
      }
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
      var answer = conn.receive(2);
      var asnwerS = new String(answer);
      System.out.println(asnwerS);
      if (asnwerS.equals("Login efetuado com sucesso")) {
        this.username = username;
      }
    });

    menu.addOpcao("Listar localizacoes com Trotinetes a D de distancia", () -> {
      if (this.username == null) {
        System.out.println("Necessita estar autenticado");
        return;
      }
      System.out.println("Insira o X do ponto que deseja");
      int x = Integer.parseInt(this.scan.nextLine());
      System.out.println("Insira o Y do ponto que deseja");
      int y = Integer.parseInt(this.scan.nextLine());
      var ponto = new Ponto(x, y);
      var data = new ByteArrayOutputStream();
      var stream = new DataOutputStream(data);
      ponto.serialize(stream);
      stream.flush();
      conn.send(3, data.toByteArray());
      var answer = conn.receive(3);
      var bytes = new ByteArrayInputStream(answer);
      var streamIn = new DataInputStream(bytes);
      var numPontos = streamIn.readInt();
      for (int i = 0; i < numPontos; ++i) {
        System.out.println(Ponto.deserialize(streamIn));
      }
    });

    menu.addOpcao("Reserva uma trotinete perto de um determinado local", () -> {
      if (this.username != null) {
        System.out.println("Insira o X do ponto que deseja");
        int x = Integer.parseInt(this.scan.nextLine());
        System.out.println("Insira o Y do ponto que deseja");
        int y = Integer.parseInt(this.scan.nextLine());
        Ponto ponto = new Ponto(x, y);
        var data = new ByteArrayOutputStream();
        var stream = new DataOutputStream(data);
        ponto.serialize(stream);
        stream.flush();
        conn.send(4, data.toByteArray());
        var answer = conn.receive(4);
        var bytes = new ByteArrayInputStream(answer);
        var streamIn = new DataInputStream(bytes);
        int codigo = streamIn.readInt();
        if (codigo != -1) {
          Ponto viagemOrigem = Ponto.deserialize(streamIn);
          System.out.println("Reserva registada com o codigo: " + codigo +
                             " e terá inicio na cordenada de " + viagemOrigem);
        } else {
          System.out.println("Reserva efetuada com insucesso");
        }
      } else {
        System.out.println("Deve estar autenticado para fazer reservas");
      }
    });

    menu.addOpcao("Estaciona uma trotinete em um determinado ponto", () -> {
      if (this.username == null)
        System.out.println("Deve estar autenticado para efetuar uma reserva");
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
        var answer = conn.receive(5);
        try (var streamIn = new DataInputStream(
                 new ByteArrayInputStream(answer))) {
          float custo = streamIn.readFloat();
          if (custo < 0) {
            System.out.println("Operacao ocorreu com insucesso");
          } else {
            boolean hasRecompensa = streamIn.readBoolean();
            if(hasRecompensa) {
              float valorRecompensa = streamIn.readFloat();
              System.out.println(
                      "Estacionou trotinete com sucesso, com um custo de viagem de " +
                              custo + ", sendo que a viagem possuia uma recompensa, no valor de " + valorRecompensa);
            }
            else {
              System.out.println("Estacionou a trotinete com sucesso, com um custo de viagem de " + custo);
            }
          }
        }
      }
    });

    menu.addOpcao("Listar recompensas com origem até distancia D de um ponto", () ->  {
      if(this.username == null) System.out.println("Deve estar autenticado para realizar esta operacao");
      else {
        System.out.println("Insira o X do ponto que deseja");
        int x = Integer.parseInt(this.scan.nextLine());
        System.out.println("Insira o Y do ponto que deseja");
        int y = Integer.parseInt(this.scan.nextLine());
        var ponto = new Ponto(x, y);
        var data = new ByteArrayOutputStream();
        var streamOut = new DataOutputStream(data);
        streamOut.writeInt(x);
        streamOut.writeInt(y);
        conn.send(6, data.toByteArray());
        var answer = conn.receive(6);
        try (var streamIn = new DataInputStream(new ByteArrayInputStream(answer))) {
          int length = streamIn.readInt();
          System.out.println("\n------------------Recompensas----------------------");
          for(int i = 0; i < length; ++i) {
            System.out.println("\nutils.Recompensa nº" + i + ": ");
            var pontoInicio = Ponto.deserialize(streamIn);
            var pontoFinal = Ponto.deserialize(streamIn);
            System.out.println("Origem = " + pontoInicio + ", Destino = " + pontoFinal);
          }
          System.out.println("\n---------------------------------------------------");
        }
      }
    });

    menu.addOpcao("Receber atualizacões de recompensas com origem em um ponto", () -> {
      if(this.username == null) System.out.println("Precisa de estar autenticado para realizar esta funcionalidade");
      System.out.println("Insira o X do ponto");
      int x = Integer.parseInt(this.scan.nextLine());
      System.out.println("Insira o Y do ponto");
      int y = Integer.parseInt(this.scan.nextLine());
      var data = new ByteArrayOutputStream();
      var streamOut = new DataOutputStream(data);
      streamOut.writeInt(x);
      streamOut.writeInt(y);
      var ponto = new Ponto(x, y);
      if(this.atualizacaoWorker.containsKey(ponto)) return;
      conn.send(7, data.toByteArray());
      var worker = new Thread(new AtualizacaoWorker(conn));
      this.atualizacaoWorker.put(ponto, worker);
      worker.start();
    });

    menu.addOpcao("Deixar de receber atualizacões de recompensas com origem em um ponto", () -> {
      if(this.username == null) System.out.println("Precisa de estar autenticado para realizar esta funcionalidade");
      System.out.println("Insira o X do ponto");
      int x = Integer.parseInt(this.scan.nextLine());
      System.out.println("Insira o Y do ponto");
      int y = Integer.parseInt(this.scan.nextLine());
      var data = new ByteArrayOutputStream();
      var streamOut = new DataOutputStream(data);
      streamOut.writeInt(x);
      streamOut.writeInt(y);
      var ponto = new Ponto(x, y);
      if(!this.atualizacaoWorker.containsKey(ponto)) return;
      conn.send(8, data.toByteArray());
      var worker = this.atualizacaoWorker.get(ponto);
      worker.interrupt();
      this.atualizacaoWorker.remove(ponto);
    });
    return menu;
  }

  public void run() {
    try (var dem = new Demultiplexer(new TaggedConnection(this.socket))) {
      dem.start();
      var menu = this.initializaMenu(dem);
      menu.run();

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
