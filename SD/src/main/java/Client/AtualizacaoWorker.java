package Client;

import connection.Demultiplexer;
import connection.TaggedConnection;
import utils.Recompensa;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AtualizacaoWorker implements Runnable {

    private final Demultiplexer connection;

    public AtualizacaoWorker(Demultiplexer conn) {
        this.connection = conn;
    }

    @Override
    public void run() {

        while(true) {
            try {
                var data = this.connection.receive(10);
                var streamIn = new DataInputStream(new ByteArrayInputStream(data));
                int size = streamIn.readInt();
                Recompensa[] resposta = new Recompensa[size];
                for(int i = 0; i < size; ++i) {
                    var recompensa = Recompensa.deserialize(streamIn);
                    resposta[i] = recompensa;
                }

                System.out.println("Foram recebidas atualizacoes");
                System.out.println("-----------------Atualizacoes existentes nesses pontos---------------------------");
                for(int i = 0; i < resposta.length; ++i) {
                    System.out.println("Recompensa nº" + i);
                    System.out.println("Origem = " + resposta[i].getOrigem());
                    System.out.println("Destino = " + resposta[i].getDestino());
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Ola falhei");
                throw new RuntimeException(e);
            }
        }

    }
}
