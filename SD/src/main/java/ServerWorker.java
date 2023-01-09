import connection.TaggedConnection;
import utils.Ponto;
import utils.Recompensa;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerWorker implements Runnable {
    private final GestaoReservas gestaoReservas;
    private final TaggedConnection taggedConnection;
    private String username;

    private final List<ServerHandler> handlers;

    private final Map<Ponto, Thread> atualizacoesCliente;

    /**
     * Metodo que inicializa ServerWorker
     * @param gestaoReservas Facade para gestao de reservas
     * @param socket socket do cliente
     * @throws IOException caso ocorra erro na criacao do socket
     */
    public ServerWorker(GestaoReservas gestaoReservas, Socket socket) throws IOException {
        this.gestaoReservas = gestaoReservas;
        this.taggedConnection = new TaggedConnection(socket);
        this.username = null;
        this.handlers = new ArrayList<>();
        this.atualizacoesCliente = new HashMap<>();
    }

    /**
     * Metodo que inicializa os vários handlers para cada pedido do cliente
     */
    private void initHandlers() {
        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var stream = new DataInputStream(bytes);
            var username = stream.readUTF();
            var password = stream.readUTF();
            System.out.println("username = " + username + " , password = " + password);
            if (this.gestaoReservas.registaUtilizador(username, password)) {
                this.taggedConnection.send(new TaggedConnection.Frame(data.getTag(), "Registado com sucesso".getBytes()));
            } else {
                this.taggedConnection.send(new TaggedConnection.Frame(data.getTag(), "Registado com insucesso".getBytes()));
            }
        });

        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var stream = new DataInputStream(bytes);
            var username = stream.readUTF();
            var password = stream.readUTF();
            System.out.println("username = " + username + " , password = " + password);
            if (this.gestaoReservas.login(username, password)) {
                this.taggedConnection.send(new TaggedConnection.Frame(data.getTag(), "Login efetuado com sucesso".getBytes()));
                this.username = username;
            } else {
                this.taggedConnection.send(new TaggedConnection.Frame(data.getTag(), "Login efetuado com insucesso".getBytes()));
            }
        });

        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var stream = new DataInputStream(bytes);
            var ponto = Ponto.deserialize(stream);
            var lista = this.gestaoReservas.getPontosVizinhosComTrotinete(ponto);
            var bytesOut = new ByteArrayOutputStream();
            var streamOut = new DataOutputStream(bytesOut);
            streamOut.writeInt(lista.size());
            for(var p : lista) {
                p.serialize(streamOut);
            }
            streamOut.flush();
            this.taggedConnection.send(data.getTag(), bytesOut.toByteArray());
        });

        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var stream = new DataInputStream(bytes);
            var ponto = Ponto.deserialize(stream);
            var viagem = this.gestaoReservas.reservaTrotinete(this.username, ponto);
            var dataSend = new ByteArrayOutputStream();
            var streamOut = new DataOutputStream(dataSend);
            if(viagem == null) {
               streamOut.writeInt(-1);
            }
            else {
                streamOut.writeInt(viagem.getCodigo());
                viagem.getPontoInicial().serialize(streamOut);
            }
            streamOut.flush();
            this.taggedConnection.send(data.getTag(), dataSend.toByteArray());
        });

        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var streamIn = new DataInputStream(bytes);
            int codigo = streamIn.readInt();
            Ponto ponto = Ponto.deserialize(streamIn);
            var viagem = this.gestaoReservas.estacionaTrotinete(this.username, codigo, ponto);
            var answer = new ByteArrayOutputStream();
            var streamOut = new DataOutputStream(answer);
            if(viagem != null) {
                float custo = viagem.getCusto();
                streamOut.writeFloat(custo);
                var recompensa = viagem.getRecompensa();
                if(recompensa != null) {
                    streamOut.writeBoolean(true);
                    streamOut.writeFloat(recompensa.getValorRecompensa());
                }
                else {
                    streamOut.writeBoolean(false);
                }
                streamOut.flush();
                this.taggedConnection.send(data.getTag(), answer.toByteArray());
            }
            else {
                streamOut.writeFloat(-1);
                streamOut.flush();
                this.taggedConnection.send(data.getTag(), answer.toByteArray());
            }
        });
        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var streamIn = new DataInputStream(bytes);
            Ponto ponto = Ponto.deserialize(streamIn);
            List<Recompensa> res = this.gestaoReservas.getRecompensas(ponto);
            if(res == null) res = new ArrayList<>();
            var bytesAnswer = new ByteArrayOutputStream();
            var answer = new DataOutputStream(bytesAnswer);
            answer.writeInt(res.size());
            res.forEach(recompensa -> {
                try {
                    recompensa.getOrigem().serialize(answer);
                    recompensa.getDestino().serialize(answer);
                    answer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            this.taggedConnection.send(data.getTag(), bytesAnswer.toByteArray());
        });

        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var streamIn = new DataInputStream(bytes);
            Ponto ponto = Ponto.deserialize(streamIn);
            this.gestaoReservas.registaRecebimentoRecompensa(ponto);
            var worker = new Thread(() -> {
                try {
                    while(true) {
                        var ans = this.gestaoReservas.recebeRecompensasAtualizacao(ponto);
                        System.out.println(ans);
                        if(ans == null) continue;
                        var byteArray = new ByteArrayOutputStream();
                        var streamOut = new DataOutputStream(byteArray);
                        streamOut.writeInt(ans.size());
                        for (var recompensa : ans) {
                            recompensa.serialize(streamOut);
                        }
                        streamOut.flush();
                        this.taggedConnection.send(10, byteArray.toByteArray());
                    }
                } catch (InterruptedException | IOException ignored) {
                }
            });
            this.atualizacoesCliente.put(ponto, worker);
            worker.start();
        });

        this.handlers.add(data -> {
            var bytes = new ByteArrayInputStream(data.getData());
            var streamIn = new DataInputStream(bytes);
            Ponto ponto = Ponto.deserialize(streamIn);
            this.atualizacoesCliente.get(ponto).interrupt();
            this.atualizacoesCliente.remove(ponto);
            this.gestaoReservas.cancelaRecebimentoRecompensa(ponto);
        });

    }

    /**
     * Metodo que inicia servidor worker para cliente
     */
    @Override
    public void run() {
        this.initHandlers();
        try {
            System.out.println("Started talking to client");
            while(true) {
                System.out.println(this.gestaoReservas.imprimeMapa());
                System.out.println("Estou a receber atualizacoes nos pontos = " + this.atualizacoesCliente.keySet());
                var data = this.taggedConnection.receive();
                System.out.println("Ola");
                this.handlers.get(data.getTag() - 1).execute(data);
            }
        } catch (IOException e) {
            try {
                System.out.println("Erro");
                this.gestaoReservas.logOut(this.username);
                this.taggedConnection.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
