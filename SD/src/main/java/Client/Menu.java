package Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private List<String> opcoes;
    private List<Handler> handlersOpcoes;

    private Scanner scan;

    public Menu(Scanner scan) {
        this.opcoes = new ArrayList<>();
        this.handlersOpcoes = new ArrayList<>();
        this.scan = scan;
    }

    public Menu(List<String> opcoes, List<Handler> handlers, Scanner scan) {
        this.opcoes = new ArrayList<>(opcoes);
        this.handlersOpcoes = new ArrayList<>(handlers);
        this.scan = scan;
    }

    public void addOpcao(String opcao, Handler handler) {
        this.opcoes.add(opcao);
        this.handlersOpcoes.add(handler);
    }

    private void imprimeOpcoes() {
        for(int i = 0; i < opcoes.size(); ++i) {
            System.out.println((i + 1) + "- " + opcoes.get(i));
        }
    }

    public void run() throws IOException, InterruptedException {
        int choice = -1;
        while (choice != 0) {
            this.imprimeOpcoes();
            try {
                var line = this.scan.nextLine();
                choice = Integer.parseInt(line);
            } catch(NumberFormatException e) {
                choice = -1;
            }
            if(choice != 0) this.handlersOpcoes.get(choice - 1).execute();
        }
    }
}
