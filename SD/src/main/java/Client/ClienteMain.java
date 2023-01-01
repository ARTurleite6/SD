package Client;

import java.io.IOException;
import java.util.Scanner;

public class ClienteMain {


    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        new Cliente(scan).run();
    }
}
