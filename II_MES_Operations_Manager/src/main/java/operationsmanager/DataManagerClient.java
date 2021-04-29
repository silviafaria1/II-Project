package operationsmanager;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class DataManagerClient extends Thread {

    private final BlockingQueue<String> fromManager;
    private final int PORT ;
    private final DatagramSocket socket;

    public DataManagerClient(int clientPort,  BlockingQueue<String> fromServer) throws SocketException {
        PORT=clientPort;
        socket = new DatagramSocket();
        fromManager = fromServer;
    }

    @Override
    public void run() {
        DatagramPacket packet;
        try {
            while (!this.isInterrupted()) {
                String message = fromManager.take();
                try {
                    packet = new DatagramPacket(message.getBytes(StandardCharsets.UTF_8),message.length(),
                    InetAddress.getLocalHost(), PORT);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return;
                }
                socket.send(packet);
            }
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }
}