package gui;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

class Request {
    private final String XMLString;

    Request(DatagramPacket packet) {
        if (packet.getLength() == 0)
            throw new IllegalArgumentException("Packet data was empty");
        XMLString = new String(packet.getData(),0, packet.getLength(), StandardCharsets.UTF_8);
    }

    public String getXMLString() {
        return XMLString;
    }

}
