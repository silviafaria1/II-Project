package manager;

import data.MachineStats;
import data.PieceStorage;
import data.Request;
import data.UnloadZoneStats;
import handlers.SQLHandler;
import server.RequestsDispatcher;
import server.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Launcher {
    private static final LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>(10);

    public static void main(String[] args) {
        final Logger logger = initLogger();

        Properties defaults = new Properties();
        try {
            InputStream file = Launcher.class.getResourceAsStream("default-config.properties");
            defaults.load(file);
        } catch (NullPointerException | IOException e) {
            logger.log(Level.SEVERE, "Can't find default configuration file", e);
            System.exit(-1);
        }

        Properties properties = new Properties(defaults);
        try {
            FileInputStream file = new FileInputStream("dm.properties");
            properties.load(file);
        } catch (IOException | NullPointerException e) {
            logger.log(Level.WARNING,
                    "No \"dm.properties\" config file found, using default values");
        }

        Level level = Level.parse(properties.getProperty("log_level").toUpperCase());
        logger.setLevel(level);

        SQLHandler sqlHandler = SQLHandler.createNew(properties, logger);
        if (sqlHandler == null)
            System.exit(-1);
        sqlHandler.setPieces(Integer.parseInt(properties.getProperty("PIECE_TYPE_COUNT")));
        sqlHandler.setMachine(Integer.parseInt(properties.getProperty("MACHINE_COUNT")));
        Server server = Server.createNew(
                Integer.parseInt(properties.getProperty("SERVICE_PORT")),
                queue,
                logger
        );
        HashMap<Integer, Integer> stocks = sqlHandler.getStock();
        if (stocks == null) {
            logger.severe("Failed to initialize stocks");
            System.exit(-2);
        }
        PieceStorage pieceStorage = PieceStorage.createNew(stocks, logger);
        sqlHandler.updateStock(stocks);
        Vector<Vector<Integer>> unloadZoneStatsVector = sqlHandler.getUnloadZoneStats();
        UnloadZoneStats unloadZoneStats = UnloadZoneStats.createNew(
                properties,
                unloadZoneStatsVector,
                logger
        );
        sqlHandler.upsertUnloadZones(unloadZoneStats);
        Vector<Vector<Integer>> machineStatsVector = sqlHandler.getMachineStats();
        Vector<Long> machineWorkTimeVector = sqlHandler.getMachineWorkTimeStats();
        MachineStats machineStats = MachineStats.createNew(
                properties,
                machineStatsVector,
                machineWorkTimeVector,
                logger
        );
        sqlHandler.upsertMachineStats(machineStats);

        int OM_PORT = Integer.parseInt(properties.getProperty("OM_PORT"));
        int OPC_PORT = Integer.parseInt(properties.getProperty("OPC_PORT"));
        int INTERFACE_PORT = Integer.parseInt(properties.getProperty("INTERFACE_PORT"));
        DataManager dataManager = DataManager.createNew(
                server,
                queue,
                pieceStorage,
                sqlHandler,
                unloadZoneStats,
                machineStats,
                logger,
                OM_PORT,
                Integer.parseInt(properties.getProperty("RH_PORT")),
                OPC_PORT,
                INTERFACE_PORT
        );

        String OMAddress = properties.getProperty("OMAddress");
        String OPCAddress = properties.getProperty("OPCAddress");
        TimerTask pollingTask = RequestsDispatcher.createNew(
                server,
                new String[]{OMAddress, OPCAddress},
                new Integer[]{OM_PORT, OPC_PORT},
                logger
        );
        Timer pollingTimer = new Timer(true);

        Thread[] threads = new Thread[]{server, dataManager};
        for (Thread thread : threads)
            thread.start();

        int POLLING_INTERVAL =
                Integer.parseInt(properties.getProperty("POLLING_INTERVAL"));
        pollingTimer.scheduleAtFixedRate(
                pollingTask,
                POLLING_INTERVAL,
                POLLING_INTERVAL
        );

        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            input = scanner.nextLine();
        } while (!input.toLowerCase().equals("exit"));

        scanner.close();
        pollingTimer.cancel();
        try {
            for (Thread thread : threads) {
                thread.interrupt();
                thread.join();
            }
        } catch (InterruptedException ignored) { }
    }

    private static Logger initLogger() {
        final Logger logger = Logger.getLogger(Launcher.class.getName());
        SimpleFormatter formatter = new SimpleFormatter();
        FileHandler logfile;
        try {
            logfile = new FileHandler(Launcher.class.getCanonicalName()+".log", 1_000_000,3,false);
            logfile.setFormatter(formatter);
            logger.addHandler(logfile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not create logfile", e);
        }
        logger.fine("Logger initialized");
        return logger;
    }

}
