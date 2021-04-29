package server;

import data.Request;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ServerTest {
    private static final byte[] buffer = "Hello World!!!".getBytes(StandardCharsets.UTF_8);
    final LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>();
    private final Logger logger = Logger.getGlobal();


    @BeforeMethod
    public void init() throws SocketException, UnknownHostException {
        DatagramSocket socket = new DatagramSocket(33333, InetAddress.getLocalHost());
    }

    @Test
    public void runTest() throws IOException, InterruptedException {
        Server server = Server.createNew(54322, queue, logger);
        DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length, InetAddress.getLocalHost(), 54322);
        assert server != null;
        server.start();
        server.addOutboundMessage(packet);
        Assert.assertEquals(queue.take().getXMLString(),
                new String(buffer,0,buffer.length,StandardCharsets.UTF_8));
        Assert.assertEquals(queue.size(), 0);
        server.interrupt();
        server.join();
    }

}