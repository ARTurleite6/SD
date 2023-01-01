package Client;

import java.io.IOException;

public interface Handler {
    void execute() throws IOException, InterruptedException;
}
