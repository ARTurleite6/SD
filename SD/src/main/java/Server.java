import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    private ServerSocket socket;
    private Business business;

    public Server() throws IOException {
        this.socket = new ServerSocket(8080);
        this.business = new Business();
    }

    public void run() throws IOException {
        try {
            while (true) {
                var clientSocket = this.socket.accept();
            }
        } finally {
            this.socket.close();
        }
    }

}
