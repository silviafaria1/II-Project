package gui;

import org.testng.annotations.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class GUITest {

    @Test
    public void testPutOrders() throws IOException {
        Path filepath = Path.of("build/test/orderRowTest.xml").toAbsolutePath();
//        FileChannel file = FileChannel.open(filepath, StandardOpenOption.READ);
        byte[] bf = Files.readAllBytes(filepath);
        System.out.println(new String(bf, StandardCharsets.UTF_8));
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(bf, bf.length, InetAddress.getLocalHost(),54325);
        socket.send(packet);
    }
}