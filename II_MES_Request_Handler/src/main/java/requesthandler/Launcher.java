package requesthandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Properties;
import java.util.Scanner;

public class Launcher extends Thread {

    public static void main(String[] args) {
        try {
            Properties defaultproperties = new Properties();
            try {
                InputStream filedefault= Launcher.class.getResourceAsStream("default-config.properties");
                defaultproperties.load(filedefault);
                filedefault.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            Properties properties = new Properties(defaultproperties);
            try {
                InputStream fileproperties = new FileInputStream("rh.properties");
                properties.load(fileproperties);
                fileproperties.close();
            } catch (IOException | NullPointerException e) {
                System.out.println("No rh.properties found, using default-config.properties!\n");
            }
            parseXmlFromUdpPacket launch = new parseXmlFromUdpPacket(
                    Integer.parseInt(properties.getProperty("DM_PORT")),
                    Integer.parseInt(properties.getProperty("OM_PORT")),
                    Integer.parseInt(properties.getProperty("SERVICE_PORT")),
                    Integer.parseInt(properties.getProperty("BUFF_SIZE")));
            Thread[] threads = new Thread[]{ launch };
            for (Thread thread: threads) {
                thread.start();
            }
            Scanner scanner = new Scanner(System.in);

            String value;
            do {
                value=scanner.nextLine();
            } while (!value.equals("exit"));
            scanner.close();
            for (Thread thread: threads) {
                thread.interrupt();
            }
            for (Thread thread: threads) {
                thread.join();
            }
        } catch (SocketException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}