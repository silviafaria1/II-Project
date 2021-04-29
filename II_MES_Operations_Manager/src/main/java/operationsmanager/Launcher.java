package operationsmanager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Launcher {
    public static void main(String[] args) {
        BlockingQueue<String> toOpcua = new LinkedBlockingQueue<>(100);
        BlockingQueue<String> toDataManager = new LinkedBlockingQueue<>(100);
        BlockingQueue<String> fromServer = new LinkedBlockingQueue<>(100);
        BlockingQueue<String> toSendOrders = new LinkedBlockingQueue<>(100);
        BlockingQueue<Boolean> machine1 = new LinkedBlockingQueue<>(1);
        BlockingQueue<Boolean> machine2 = new LinkedBlockingQueue<>(1);
        BlockingQueue<Boolean> machine3 = new LinkedBlockingQueue<>(1);
        BlockingQueue<Integer> stores = new LinkedBlockingQueue<>(100);
        BlockingQueue<Boolean> unloadZone = new LinkedBlockingQueue<>(1);
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
                InputStream fileproperties=new FileInputStream("om.properties");
                properties.load(fileproperties);
                fileproperties.close();
            } catch (IOException | NullPointerException e) {
                System.out.println("No om.properties found, using default-config.properties!\n");
            }
            Server server = new Server(
                    Integer.parseInt(properties.getProperty("OM_PORT")),
                    fromServer, toSendOrders, machine1, machine2, machine3, stores, unloadZone);
            OperationsManager operationsManager= new OperationsManager(  toDataManager, fromServer);
            OpcuaClient opcuaClient = new OpcuaClient(
                    Integer.parseInt(properties.getProperty("OPCUA_PORT")), toOpcua);
            DataManagerClient dataManagerClient = new DataManagerClient(
                    Integer.parseInt(properties.getProperty("DM_PORT")), toDataManager);
            SendOrders sendOrders= new SendOrders(toOpcua, toSendOrders,
                    machine1, machine2, machine3, toDataManager, stores, unloadZone);
            Thread[] threads = new Thread[] { server, operationsManager, dataManagerClient, opcuaClient, sendOrders};

            for (Thread thread : threads) {
                thread.start();
            }

            Scanner scanner = new Scanner(System.in);

            while (true) {
                String value;
                value=scanner.nextLine();
                if(value.equals("exit")) 
                    break;
            }
            for (Thread thread: threads) {
                thread.interrupt();
            }
            for (Thread thread: threads) {
                thread.join();
            }
            scanner.close();

        } catch (SocketException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}