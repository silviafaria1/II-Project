package opcua;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Launcher {
    static Timer timer = new Timer(true);
    public static void main(String[] args) {
        try {
            BlockingQueue<Integer> toMachine1 = new LinkedBlockingQueue<>(100);
            BlockingQueue<Integer> toMachine2 = new LinkedBlockingQueue<>(100);
            BlockingQueue<Integer> toMachine3 = new LinkedBlockingQueue<>(100);
            BlockingQueue<Integer> toUnloadZone = new LinkedBlockingQueue<>(100);
            BlockingQueue<OrderPLC> toSendtoPLC= new LinkedBlockingQueue<>(1);

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
                InputStream fileproperties = new FileInputStream("opcua.properties");
                properties.load(fileproperties);
                fileproperties.close();
            } catch (IOException | NullPointerException e) {
                System.out.println("No opcua.properties found, using default-config.properties!\n");
            }

            UAmaster uAmaster = new UAmaster(properties.getProperty("OPCUA_URL"));
            GetFromPLC getFromPLC= new GetFromPLC();
            OpcuaServer opcuaServer= new OpcuaServer(
                    Integer.parseInt(properties.getProperty("OPCUA_PORT")),
                    toMachine1, toMachine2, toMachine3, toUnloadZone,toSendtoPLC);
            DataManagerClient dataManagerClient = new DataManagerClient(
                    Integer.parseInt(properties.getProperty("DM_PORT")));
            OperationsManagerClient operationsManager = new OperationsManagerClient(
                    Integer.parseInt(properties.getProperty("OM_PORT")));
            GetPusher handlePushers = new GetPusher();
            Machine machineAvailability = new Machine();
            UnloadZone unloadZonesPolling = new UnloadZone();

            uAmaster.start();
            uAmaster.join();

            if (!UAmaster.connectionStatus) {
                return;
            }

            ReadUnloadZone unloadZone = new ReadUnloadZone(toUnloadZone);
            ReadMachines machines = new ReadMachines(toMachine1, toMachine2, toMachine3);
            Thread[] threads = new Thread[]{machines,opcuaServer, dataManagerClient,
                     operationsManager, unloadZone};
            for (Thread thread: threads) {
                thread.start();
            }
            int POLLING_INTERVAL = Integer.parseInt(properties.getProperty("POLLING_INTERVAL"));
            OpcuaServer.waitForPersistencyReceived();
            timer.scheduleAtFixedRate(handlePushers, 0,POLLING_INTERVAL);
            timer.scheduleAtFixedRate(getFromPLC, 0,POLLING_INTERVAL);
            timer.scheduleAtFixedRate(machineAvailability, 0, POLLING_INTERVAL);
            timer.scheduleAtFixedRate(unloadZonesPolling, 0, POLLING_INTERVAL);
            
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String value;
                value=scanner.nextLine();
                if(value.equals("exit")) 
                    break;
            }
            scanner.close();
            timer.cancel();
            for (Thread thread: threads) {
                thread.interrupt();
            }
            for (Thread thread: threads) {
                thread.join();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}