package operationsmanager;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class OpcuaClient extends Thread {

    private final BlockingQueue<String> fromManager;
    private final int PORT;
    private final DatagramSocket socket;

    public OpcuaClient(int port, BlockingQueue<String> fromServer) throws SocketException {
        PORT = port;
        fromManager = fromServer ;
        socket = new DatagramSocket();
    }

    @Override
    public void run() {
        sendToOPCUA();
    }

    private void sendToOPCUA() {
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