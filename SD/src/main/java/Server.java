import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    private final ServerSocket socket;
    private final GestaoReservas gestaoReservas;

    public Server() throws IOException {
        this.socket = new ServerSocket(8080);
        this.gestaoReservas = new GestaoReservas();
    }

    public void run() throws IOException {
        try {
            while (true) {
                System.out.println(this.gestaoReservas);
                var clientSocket = this.socket.accept();
                new Thread(new ServerWorker(this.gestaoReservas, clientSocket)).start();
            }
        } finally {
            this.socket.close();
        }
    }

}
