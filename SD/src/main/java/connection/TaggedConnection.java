package connection;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {

    public static class Frame {
        private final int tag;
        private final byte[] data;

        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }

        public int getTag() {
            return this.tag;
        }

        public byte[] getData() {
            return this.data;
        }
    }

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Lock readLock;
    private final Lock writeLock;

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

    public void send(int tag, byte[] data) throws IOException {
        try {
            this.writeLock.lock();
            this.out.writeInt(tag);
            this.out.writeInt(data.length);
            this.out.write(data);
            this.out.flush();
        } finally {
            this.writeLock.unlock();
        }
    }

    public Frame receive() throws IOException {
        try {
            this.readLock.lock();
            int tag = this.in.readInt();
            int tam = this.in.readInt();
            byte[] data = new byte[tam];
            this.in.readFully(data);
            return new Frame(tag, data);
        } finally {
            this.readLock.unlock();
        }
    }

    public void close() throws IOException {
        this.socket.shutdownInput();
        this.socket.shutdownOutput();
        this.socket.close();
    }
}
