package operationsmanager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.*;

public class DMAvailabilityTest {
    private Launcher f;
    private DatagramSocket socket;
    private DatagramPacket packet;

    private DatagramSocket socket2;
    TransformationTable table= TransformationTable.getInstance();
    //int availability;

    @BeforeClass
    public void setup() throws IOException {
        socket = new DatagramSocket();
        File file = new File(getClass().getResource("./test.xml").getFile());
        System.out.println("Using file " + file.getAbsolutePath());
        FileInputStream i = new FileInputStream(file);
        byte[] buffer = i.readAllBytes();
        packet = new DatagramPacket(buffer, buffer.length);
        packet.setLength(buffer.length);
        packet.setAddress(InetAddress.getLocalHost());
        packet.setPort(54322);
        i.close();
        socket2 = new DatagramSocket(54323, InetAddress.getLocalHost());
    }

    /*@Test
    public void sendtoDM() throws IOException, InterruptedException{
        System.out.println("Ola1");
        availability =  table.getPieceAvailability(2);
        System.out.println("Ola");
    }*/

    @Test
    public void testRun() throws IOException, InterruptedException {
        f = new Launcher();
        OperationsManager.sendAvailabilityRequestToDM(2);

        //DM recebe
        try {
            //while (true) {
            Thread.sleep(500);
            byte[] buff = new byte[10 * 1024];
            DatagramPacket packet2 = new DatagramPacket(buff, 10 * 1024);
            try {
                socket2.setSoTimeout(1);
            } catch (SocketException e) {
                e.printStackTrace();
                return;
            }
            try {
                //DATA MANAGER
                socket2.receive(packet2);
                System.out.println("DATA MANAGER RECEIVED");
                buff = packet2.getData();
                String print_buff = new String(buff, StandardCharsets.UTF_8);
                System.out.println(print_buff);

            } catch (SocketTimeoutException e) {
                System.out.println("Nothing received");
                //continue;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            //}
        }catch (InterruptedException ignored) {
        }
        //DM envia
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        socket.send(packet);
        System.out.println("Send");
        socket.close();
        Thread.sleep(3000);


    }

}
