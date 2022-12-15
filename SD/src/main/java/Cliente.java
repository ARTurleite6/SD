import connection.TaggedConnection;

import java.io.IOException;
import java.net.Socket;

public class Cliente {
    public static void main(String[] args) throws IOException {
        System.out.println("arranquei");
        var socket = new Socket("localhost", 8080);
        var taggedConnection = new TaggedConnection(socket);
        taggedConnection.send(new TaggedConnection.Frame(1, "ola mundo cruel".getBytes()));
        taggedConnection.close();
    }
}
