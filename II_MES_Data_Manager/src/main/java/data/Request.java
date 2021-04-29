package data;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Request {
    private final String XMLString;
    private final InetAddress originHost;
    private final int originPort;
    private int destPort;

    public Request(DatagramPacket packet) {
        if (packet.getLength() == 0)
            throw new IllegalArgumentException("Packet data was empty");
        XMLString = new String(packet.getData(),0, packet.getLength(), StandardCharsets.UTF_8);
        originHost = packet.getAddress();
        originPort = packet.getPort();
        destPort = originPort;
    }

    public String getXMLString() {
        return XMLString;
    }

    public InetAddress getOriginHost() {
        return originHost;
    }

    public int getOriginPort() {
        return originPort;
    }

    public void setDestPort(int port) { this.destPort = port; }

    public int getDestPort() { return destPort; }

}

