package gui;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

public class Server extends Thread {
    private DatagramSocket socket;
    private Logger logger;
    private static final int BUFFER_SIZE = 64000;
    byte[] buffer;
    private static LinkedBlockingQueue<DatagramPacket> outbound;
    private final Sender sender;

    class Sender extends Thread {
        Sender() {
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while(! isInterrupted()) {
                    DatagramPacket p = outbound.take();
                    socket.send(p);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    Server(Logger parentLogger, int port) {
        logger = Logger.getLogger(getClass().getName());
        logger.setParent(parentLogger);
        logger.setUseParentHandlers(true);
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);
            this.setDaemon(true);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Failed to initialize " + getClass().getName() + " socket.", e);
            System.exit(-1);
        }
        outbound = new LinkedBlockingQueue<>();
        sender = new Sender();
        sender.start();
        buffer = new byte[BUFFER_SIZE];
        logger.log(Level.INFO, getClass().getName() + " object initialized");
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer,BUFFER_SIZE);
        try {
            while (! isInterrupted()) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ignored) {
                    continue;
                }
                Request newRequest = new Request(packet);
                GUI.putRequest(newRequest);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to receive UDP message", e);
        }
        socket.close();
    }

    static void addOutboundMessage(DatagramPacket outboundPacket) {
        outbound.add(outboundPacket);
    }
}
