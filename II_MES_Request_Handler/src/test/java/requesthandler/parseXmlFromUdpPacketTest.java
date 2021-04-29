/*package requesthandler;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.*;

public class parseXmlFromUdpPacketTest {
    private parseXmlFromUdpPacket f;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private DatagramSocket socket2;
    private DatagramSocket socket3;

    @BeforeClass
    public void setup() throws IOException {
        socket = new DatagramSocket();
        File file = new File(getClass().getResource("../command5.xml").getFile());
        System.out.println("Using file " + file.getAbsolutePath());
        FileInputStream i = new FileInputStream(file);
        byte[] buffer = i.readAllBytes();
        packet = new DatagramPacket(buffer, buffer.length);
        packet.setLength(buffer.length);
        packet.setAddress(InetAddress.getLocalHost());
        packet.setPort(54321);

        socket2 = new DatagramSocket(54322, InetAddress.getLocalHost());
        socket3 = new DatagramSocket(54323, InetAddress.getLocalHost());
    }


    @Test
    public void testRun() throws IOException, InterruptedException {
        f = new parseXmlFromUdpPacket();
        f.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        socket.send(packet);
        socket.close();
        Thread.sleep(3000);
        f.interrupt();
        f.join();

        try {
            while (true) {
                Thread.sleep(500);
                byte[] buff = new byte[10 * 1024];
                byte[] buff2 = new byte[10 * 1024];
                DatagramPacket packet2 = new DatagramPacket(buff, 10 * 1024);
                DatagramPacket packet3 = new DatagramPacket(buff, 10 * 1024);
                try {
                    socket2.setSoTimeout(1);
                    socket3.setSoTimeout(1);
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    *//*
                    //OPERATIONS MANAGER
                    socket2.receive(packet2);
                    System.out.println("OPERATIONS_MANAGER RECEIVED");
                    buff = packet2.getData();
                    String print_buff = new String(buff, StandardCharsets.UTF_8);
                    System.out.println(print_buff);
                    *//*

                    // DATA MANAGER
                    socket3.receive(packet3);
                    System.out.println("DATA_MANAGER RECEIVED");
                    buff2 = packet3.getData();
                    String print_buff2 = new String(buff2, StandardCharsets.UTF_8);
                    System.out.println(print_buff2);

                } catch (SocketTimeoutException e) {
                    System.out.println("Nothing received");
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }catch (InterruptedException ignored) {
        }
    }
}*/
