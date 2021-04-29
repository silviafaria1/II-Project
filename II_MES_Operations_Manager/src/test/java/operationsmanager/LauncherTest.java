package operationsmanager;

import org.testng.annotations.BeforeClass;

import org.testng.annotations.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;


public class LauncherTest {
    private Launcher f;
    
    private DatagramSocket socket;
    private DatagramPacket packet;
    private DatagramSocket socket2;
    private DatagramSocket socket3;

    //private DatagramSocket socket2;

    @BeforeClass
    public void setup() throws IOException {
        socket = new DatagramSocket();
        File file = new File(getClass().getResource("./command1.xml").getFile());
        System.out.println("Using file " + file.getAbsolutePath());
        FileInputStream i = new FileInputStream(file);
        byte[] buffer = i.readAllBytes();
        packet = new DatagramPacket(buffer, buffer.length);
        packet.setLength(buffer.length);
        packet.setAddress(InetAddress.getLocalHost());
        packet.setPort(54322);
        i.close();

    }


    @Test
    public void testRun() throws IOException, InterruptedException {
        
        f = new Launcher();
        


    }
}
