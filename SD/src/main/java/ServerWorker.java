import connection.TaggedConnection;

import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private final Business  business;
    private final TaggedConnection taggedConnection;

    public ServerWorker(Business business, Socket socket) throws IOException {
        this.business = business;
        this.taggedConnection = new TaggedConnection(socket);
    }

    @Override
    public void run() {
        try {
            System.out.println("Started talking to client");
            while(true) {
                var data = this.taggedConnection.receive();
                System.out.println(new String(data.getData()));
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
