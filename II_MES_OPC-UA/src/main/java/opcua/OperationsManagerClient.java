package opcua;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

public class OperationsManagerClient extends Thread {

    private static final LinkedBlockingQueue<String> messagePool= new LinkedBlockingQueue<>();
    private final int PORT ;
    private final DatagramSocket socket;

    public OperationsManagerClient(int clientPort) throws SocketException {
        PORT=clientPort;
        socket = new DatagramSocket();
    }

    @Override
    public void run() {
        DatagramPacket packet;
        try {
            while (!this.isInterrupted()) {
                String message = messagePool.take();
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

    static void putMessage(String message) {
        try {
            messagePool.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
