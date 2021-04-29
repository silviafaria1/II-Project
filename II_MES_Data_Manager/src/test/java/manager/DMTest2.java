package manager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.*;

public class DMTest2 {
    private DatagramSocket socket;
    private DatagramSocket socket2;

    @BeforeClass
    public void init() throws SocketException {
        socket = new DatagramSocket(54322);
        socket2 = new DatagramSocket(54321);
    }

    @Test
    public void RequestStoresTest() throws IOException {
        byte[] buff2 = new byte[64000];
        DatagramPacket packet2 = new DatagramPacket(buff2, buff2.length);

        InetAddress address = InetAddress.getLocalHost();
        for (int i = 0; i < 10000; i++) {
            byte[] buf = ("<Request_Stores type=\"" + (int) ((Math.random() * 8.0) + 1.0) + "\"/>").getBytes();
            System.out.println(new String(buf));
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address,54324);
            socket.send(packet);
            socket.receive(packet2);
            buff2 = packet2.getData();
            System.out.println(new String(buff2));
        }
    }

}
