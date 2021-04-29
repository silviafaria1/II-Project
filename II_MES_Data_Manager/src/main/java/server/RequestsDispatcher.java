package server;

import logger.ClassLogger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;
import java.util.logging.Logger;

public class RequestsDispatcher extends TimerTask {
    private final DatagramPacket[] requests;
    private final Server server;
    private final Logger logger;

    RequestsDispatcher(
            Server server,
            DatagramPacket[] packets,
            Logger logger
    ) {
        this.server = server;
        this.requests = packets;
        this.logger = logger;
    }

    public static RequestsDispatcher createNew(
            final Server server,
            String[] Dest_Addresses,
            Integer[] Dest_Ports,
            Logger parentLogger
    ) {
        Logger logger = ClassLogger.initLogger(
                RequestsDispatcher.class.getName(),
                parentLogger
        );
        byte[] request = "<Statistics/>\n".getBytes(StandardCharsets.UTF_8);
        DatagramPacket[] packets = new DatagramPacket[Dest_Addresses.length];
        try {
            for (int i = 0; i < Dest_Addresses.length; i++) {
                packets[i] = new DatagramPacket(
                        request,
                        request.length,
                        InetAddress.getByName(Dest_Addresses[i]),
                        Dest_Ports[i]
                );
            }
            return new RequestsDispatcher(server, packets, logger);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        for (DatagramPacket request: requests) {
            server.addOutboundMessage(request);
        }
        logger.fine("Requests sent");
    }

}
