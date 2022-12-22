import connection.TaggedConnection;
import utils.Ponto;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServerWorker implements Runnable {
    private final GestaoReservas gestaoReservas;
    private final TaggedConnection taggedConnection;
    private String username;

    private List<ServerHandler> handlers;

    public ServerWorker(GestaoReservas gestaoReservas, Socket socket) throws IOException {
        this.gestaoReservas = gestaoReservas;
        this.taggedConnection = new TaggedConnection(socket);
        this.username = null;
        this.handlers = new ArrayList<>();
    }

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
    }

    @Override
    public void run() {
        this.initHandlers();
        try {
            System.out.println("Started talking to client");
            while(true) {
                var data = this.taggedConnection.receive();
                this.handlers.get(data.getTag() - 1).execute(data);
            }
        } catch (IOException e) {
            try {
                this.gestaoReservas.logOut(this.username);
                this.taggedConnection.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
