package server;

import data.Request;
import logger.ClassLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {
    private static final int BUFFER_SIZE = 64000; // Max udp size
    private final DatagramSocket socket;
    private final LinkedBlockingQueue<Request> queue;
    private final LinkedBlockingQueue<DatagramPacket> outbound;
    private final Sender sender;

    private class Sender extends Thread {
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
            } catch (InterruptedException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Server(
            DatagramSocket socket,
            LinkedBlockingQueue<Request> queue,
            LinkedBlockingQueue<DatagramPacket> outbound
    ) {
        this.socket = socket;
        this.outbound = outbound;
        this.setDaemon(true);
        this.queue = queue;
        this.sender = new Sender();
        sender.setDaemon(true);
        sender.start();
    }

    public static Server createNew(
            int port,
            LinkedBlockingQueue<Request> queue,
            Logger parentLogger
    ) {
        Logger logger = ClassLogger.initLogger(Server.class.getName(),
                                               parentLogger);
        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);
            LinkedBlockingQueue<DatagramPacket> outbound =
                    new LinkedBlockingQueue<>(5);
            return new Server(socket, queue, outbound);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize socket", e);
            return null;
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer,BUFFER_SIZE);
        try {
            // Infinite Loop
            while (! interrupted()) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    continue;
                }
                // Put the received request on the processing queue
                Request newRequest = new Request(packet);
                queue.put(newRequest);
            }
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
        try {
            sender.interrupt();
            sender.join();
        } catch (InterruptedException ignored) {
        }
    }

    public void addOutboundMessage(DatagramPacket outboundPacket) {
        outbound.add(outboundPacket);
    }

}
