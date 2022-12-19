import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    private final ServerSocket socket;
    private final Business business;

    public Server() throws IOException {
        this.socket = new ServerSocket(8080);
        this.business = new Business();
    }

    public void run() throws IOException {
        new Thread(new RewardWorker(this.business)).start();
        try {
            while (true) {
                System.out.println(this.business);
                var clientSocket = this.socket.accept();
                new Thread(new ServerWorker(this.business, clientSocket)).start();
            }
        } finally {
            this.socket.close();
        }
    }

}
