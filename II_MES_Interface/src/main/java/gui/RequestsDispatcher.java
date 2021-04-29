package gui;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;

public class RequestsDispatcher extends TimerTask {
    private final DatagramPacket DMPacket;

    RequestsDispatcher(final int DMPORT) throws UnknownHostException {
        InetAddress OMAddress = InetAddress.getLocalHost();
        byte[] string = "<Interface/>\n".getBytes(StandardCharsets.UTF_8);

        DMPacket = new DatagramPacket( string, string.length, OMAddress, DMPORT);
    }

    @Override
    public void run() {
        for (DatagramPacket p: new DatagramPacket[]{DMPacket}) {
            Server.addOutboundMessage(p);
        }
    }

}
