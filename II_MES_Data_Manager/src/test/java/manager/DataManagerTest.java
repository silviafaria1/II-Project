package manager;

import data.PieceStorage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DataManagerTest {

    public static class Receiver extends Thread {
        private final BlockingQueue<byte[]> q;
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        public Receiver(
                DatagramSocket socket,
                DatagramPacket packet,
                BlockingQueue<byte[]> q
        ) {
            this.socket = socket;
            this.packet = packet;
            this.q = q;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    socket.receive(packet);
                    q.put(packet.getData().clone());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void DMTest() throws IOException {
        Logger logger = Logger.getGlobal();
        logger.setUseParentHandlers(true);
        Path filePath = Path.of("output.txt");
        if (Files.exists(filePath)) Files.delete(filePath);
        Files.createFile(filePath);
        FileChannel fc = FileChannel.open(filePath, StandardOpenOption.WRITE);
//        LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>(10);
//        Server server = Server.createNew(54324, queue, logger);
        HashMap<Integer, Integer> stocks = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            stocks.put(i+1,54);
        }
        PieceStorage pieceStorage = PieceStorage.createNew(
                stocks,
                logger
        );
        /*DataManager DM = DataManager.createNew(
                server,
                queue,
                pieceStorage,
                logger,
                54322,
                54321,
                54323
        );*/
        DatagramSocket socket = new DatagramSocket(33333);
        socket.setSendBufferSize(1_000_000);
        DatagramSocket om_socket = new DatagramSocket(54322);
        DatagramSocket rh_socket = new DatagramSocket(54321);
        DatagramSocket opc_socket = new DatagramSocket(54323);

        byte[] om_buff = new byte[64000];
        DatagramPacket om_packet = new DatagramPacket(om_buff, om_buff.length);
//        om_socket.setSoTimeout(500);
        byte[] rh_buff = new byte[64000];
        DatagramPacket rh_packet = new DatagramPacket(rh_buff, rh_buff.length);
//        rh_socket.setSoTimeout(500);
        byte[] opc_buff = new byte[64000];
        DatagramPacket opc_packet = new DatagramPacket(opc_buff, opc_buff.length);
//        opc_socket.setSoTimeout(500);

        byte[] buffer = new byte[64000];
        String filename = this.getClass().getResource("stores_test.xml").getFile();
        File f = new File(filename);
        String msg = new String(new FileInputStream(f).readAllBytes(), StandardCharsets.UTF_8);
        DatagramPacket dummy = new DatagramPacket(
                buffer,
                buffer.length
        );
        DatagramPacket packet = new DatagramPacket(
                msg.getBytes(StandardCharsets.UTF_8),
                msg.length(),
                InetAddress.getLocalHost(),
                54324
        );
        String msg2 = "<StorageIn type=\"1\"/>\n";
        DatagramPacket packet2 = new DatagramPacket(
                msg2.getBytes(StandardCharsets.UTF_8),
                msg2.length(),
                InetAddress.getLocalHost(),
                54324
        );
        String msg3 = "<StorageOut type=\"2\"/>\n";
        DatagramPacket packet3 = new DatagramPacket(
                msg3.getBytes(StandardCharsets.UTF_8),
                msg3.length(),
                InetAddress.getLocalHost(),
                54324
        );
        String msg4 = "<Request_Stores/>\n";
        DatagramPacket packet4 = new DatagramPacket(
                msg4.getBytes(StandardCharsets.UTF_8),
                msg4.length(),
                InetAddress.getLocalHost(),
                54324
        );
        String msg5 = "<waiting></waiting>\n";
        DatagramPacket packet5 = new DatagramPacket(
                msg5.getBytes(StandardCharsets.UTF_8),
                msg5.length(),
                InetAddress.getLocalHost(),
                54324
        );
        String msg6 = "<beingProcessed></beingProcessed>\n";
        DatagramPacket packet6 = new DatagramPacket(
                msg6.getBytes(StandardCharsets.UTF_8),
                msg6.length(),
                InetAddress.getLocalHost(),
                54324
        );
//        assert server != null;
//        server.start();
//        DM.start();

        ArrayBlockingQueue<byte[]> q = new ArrayBlockingQueue<>(200);


        Thread t1 = new Receiver(om_socket, om_packet, q);
        Thread t2 = new Receiver(rh_socket, rh_packet, q);
        Thread t3 = new Receiver(opc_socket, opc_packet, q);
        t1.start();
        t2.start();
        t3.start();

        int qty = 100;
        ByteBuffer bf;

        try {
            for (int i=0;i<qty;i++) {
                socket.send(packet);
                socket.send(packet2);
                socket.send(packet3);
                socket.send(packet4);
                socket.send(packet5);
                socket.send(packet6);
            }
            for(int i=0;i<qty*4;i++) {
                bf = ByteBuffer.wrap(q.take());
                fc.write(bf);
            }
        } catch (SocketTimeoutException | InterruptedException e) {
            Assert.fail("Response not received in time", e);
        }

//        DM.interrupt();
//        DM.join();
    }

}