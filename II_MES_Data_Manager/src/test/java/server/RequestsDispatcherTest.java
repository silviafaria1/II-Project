package server;

import data.Request;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import server.RequestsDispatcher;
import server.Server;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class RequestsDispatcherTest {
    LinkedBlockingQueue<Request> queue;
    RequestsDispatcher dispatcher;
    DatagramSocket socket2;

    @BeforeMethod
    public void init() throws UnknownHostException, SocketException {
        Logger logger = Logger.getGlobal();
        queue = new LinkedBlockingQueue<>();
        socket2 = new DatagramSocket(54322,InetAddress.getLocalHost());
        Server server = Server.createNew(54324, queue, logger);
        dispatcher = RequestsDispatcher.createNew(
                server,
                new String[]{"localhost"},
                new Integer[]{54322},
                logger
        );
    }

    @Test
    public void DispatcherTest() throws IOException {
        Timer t = new Timer(true);
        t.scheduleAtFixedRate(dispatcher, 0, 1000);
        int count = 5;
        byte[] data = new byte[128];
        DatagramPacket p = new DatagramPacket(data,data.length);
        while (count > 0) {
            socket2.receive(p);
            System.out.println(new String(p.getData(), StandardCharsets.UTF_8) );
            count--;
        }
    }

}