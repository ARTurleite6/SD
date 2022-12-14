package connection;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection {

    private static class Frame {
        private final int tag;
        private final byte[] data;

        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Lock readLock;
    private Lock writeLock;

    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.readLock = new ReentrantLock();
        this.writeLock = new ReentrantLock();
    }

    public void send(Frame frame) throws IOException {
        try {
            this.writeLock.lock();
            this.out.writeInt(frame.tag);
            this.out.writeInt(frame.data.length);
            this.out.write(frame.data);
            this.out.flush();
        } finally {
            this.writeLock.unlock();
        }
    }

    public Frame receive() throws IOException {
        try {
            this.writeLock.lock();
            int tag = this.in.readInt();
            int tam = this.in.readInt();
            byte[] data = new byte[tam];
            this.in.readFully(data);
            return new Frame(tag, data);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void close() throws IOException {
        this.socket.shutdownInput();
        this.socket.shutdownOutput();
    }
}
