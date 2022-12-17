import connection.TaggedConnection;

import java.io.IOException;

public interface ServerHandler {
    void execute(TaggedConnection.Frame data) throws IOException;
}
