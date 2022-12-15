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
        System.out.println(this.business);
        try {
            while (true) {
                var clientSocket = this.socket.accept();
                new Thread(new ServerWorker(this.business, clientSocket)).start();
            }
        } finally {
            this.socket.close();
        }
    }

}
