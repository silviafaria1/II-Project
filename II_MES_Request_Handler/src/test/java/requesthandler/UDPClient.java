package requesthandler;

import java.io.*;
import java.net.*;

public class UDPClient {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public UDPClient() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }
//54322 - Operation manager  | 54324 - Data manager
    public String sendEcho(String msg) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 54322);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        return received;
    }

    public void close() {
        socket.close();
    }
}
