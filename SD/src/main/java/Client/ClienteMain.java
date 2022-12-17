package Client;

import connection.TaggedConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClienteMain {


    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        new Cliente(scan).run();
    }
}
