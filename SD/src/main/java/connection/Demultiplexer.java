package connection;

import connection.TaggedConnection;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {
    private static class Message {

        private int waiters;
        private final Condition condition;
        private final Queue<byte[]> queue;

        public Message(Lock l) {
            this.waiters = 0;
            this.condition = l.newCondition();
            this.queue = new ArrayDeque<>();
        }
    }

    private final Map<Integer, Message> messages;
    private final TaggedConnection conn;
    private final ReentrantLock lock;
    private Thread reader;

    public Demultiplexer(TaggedConnection conn) {
        this.conn = conn;
        this.messages = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void start() {
        this.reader = new Thread(() -> {
            try {
                while (true) {
                    TaggedConnection.Frame f = this.conn.receive();
                    try {
                        this.lock.lock();
                        var message = this.messages.get(f.getTag());
                        if (message == null) {
                            message = new Message(this.lock);
                            this.messages.put(f.getTag(), message);
                        }
                        message.queue.add(f.getData());
                        message.condition.signal();
                    } finally {
                        this.lock.unlock();
                    }
                }
            } catch(IOException e) {
                System.out.println("Ended connection");
            }
        });
        this.reader.start();
    }

    public void send(TaggedConnection.Frame frame) throws IOException {
        System.out.println("Demult antes");
        this.conn.send(frame);
        System.out.println("Demult depois");
    }

    public void send(int tag, byte[] data) throws IOException {
        this.conn.send(tag, data);
    }

    public byte[] receive(int tag) throws IOException, InterruptedException {
        try {
            this.lock.lock();
            Message message = this.messages.get(tag);
            if (message == null) {
                message = new Message(this.lock);
                this.messages.put(tag, message);
            }
            ++message.waiters;
            while (message.queue.isEmpty()) {
                message.condition.await();
            }
            --message.waiters;
            var ans = message.queue.poll();
            if (message.waiters == 0 && message.queue.isEmpty()) {
                this.messages.remove(tag);
            }
            return ans;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void close() throws IOException {
        if(this.reader != null) this.reader.interrupt();
        this.conn.close();
    }

}
