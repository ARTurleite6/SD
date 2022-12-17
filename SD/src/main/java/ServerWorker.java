import Client.Handler;
import connection.TaggedConnection;
import utils.Ponto;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerWorker implements Runnable {
    private final Business  business;
    private final TaggedConnection taggedConnection;
    private String username;

    private List<ServerHandler> handlers;

    public ServerWorker(Business business, Socket socket) throws IOException {
        this.business = business;
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
            if (this.business.registaUtilizador(username, password)) {
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
            if (this.business.login(username, password)) {
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
            var lista = this.business.getPontosVizinhosComTrotinete(ponto);
            var bytesOut = new ByteArrayOutputStream();
            var streamOut = new DataOutputStream(bytesOut);
            streamOut.writeInt(lista.size());
            for(var p : lista) {
                p.serialize(streamOut);
            }
            streamOut.flush();
            this.taggedConnection.send(data.getTag(), bytesOut.toByteArray());
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
                this.taggedConnection.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
