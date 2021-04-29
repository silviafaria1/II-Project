package gui;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

import static java.net.InetAddress.getByName;
import static java.net.InetAddress.getLocalHost;

public class GUI extends Thread{
    private static final LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>(10);
    private JTabbedPane tabbedPaneMain;
    private JPanel panelMain;
    private JPanel Stocks;
    private JPanel Ordens;

    private JPanel Zona_Descarga, panelDescargas;
    private JScrollPane descargasScroller;
    private JTextField descarga1P1, descarga1P2, descarga1P3, descarga1P4, descarga1P5, descarga1P6, descarga1P7, descarga1P8;
    private JTextField descarga1P9, descarga1PT;
    private JTextField descarga2P1, descarga2P2, descarga2P3, descarga2P4, descarga2P5, descarga2P6, descarga2P7, descarga2P8;
    private JTextField descarga2P9, descarga2PT;
    private JTextField descarga3P1, descarga3P2, descarga3P3, descarga3P4, descarga3P5, descarga3P6, descarga3P7, descarga3P8;
    private JTextField descarga3P9, descarga3PT;


    private JPanel panelStock;
    private JTextField stockP1, stockP2, stockP3, stockP4, stockP5, stockP6, stockP7, stockP8, stockP9;

    private JPanel Maquinas, panelC1, panelC2, panelC3, C1, C2, C3;
    private JTabbedPane maquinasScroller;
    private JScrollPane scrollerC1, scrollerC2, scrollerC3;


    private JTextField TMaC1P1, TMaC1P2, TMaC1P3, TMaC1P4, TMaC1P5, TMaC1P6, TMaC1P7, TMaC1P8, TMaC1P9, TMaC1PT, TMaC1TT;
    private JTextField TMbC1P1, TMbC1P2, TMbC1P3, TMbC1P4, TMbC1P5, TMbC1P6, TMbC1P7, TMbC1P8, TMbC1P9, TMbC1PT, TMbC1TT;
    private JTextField TMcC1P1, TMcC1P2, TMcC1P3, TMcC1P4, TMcC1P5, TMcC1P6, TMcC1P7, TMcC1P8, TMcC1P9, TMcC1PT, TMcC1TT;
    private JTextField TMaC2P1, TMaC2P2, TMaC2P3, TMaC2P4, TMaC2P5, TMaC2P6, TMaC2P7, TMaC2P8, TMaC2P9, TMaC2PT, TMaC2TT;
    private JTextField TMbC2P1, TMbC2P2, TMbC2P3, TMbC2P4, TMbC2P5, TMbC2P6, TMbC2P7, TMbC2P8, TMbC2P9, TMbC2PT, TMbC2TT;
    private JTextField TMcC2P1, TMcC2P2, TMcC2P3, TMcC2P4, TMcC2P5, TMcC2P6, TMcC2P7, TMcC2P8, TMcC2P9, TMcC2PT, TMcC2TT;
    private JTextField TMaC3P1, TMaC3P2, TMaC3P3, TMaC3P4, TMaC3P5, TMaC3P6, TMaC3P7, TMaC3P8, TMaC3P9, TMaC3PT, TMaC3TT;
    private JTextField TMbC3P1, TMbC3P2, TMbC3P3, TMbC3P4, TMbC3P5, TMbC3P6, TMbC3P7, TMbC3P8, TMbC3P9, TMbC3PT, TMbC3TT;
    private JTextField TMcC3P1, TMcC3P2, TMcC3P3, TMcC3P4, TMcC3P5, TMcC3P6, TMcC3P7, TMcC3P8, TMcC3P9, TMcC3PT, TMcC3TT;
    private JPanel panelMaquinas;
    private JTable tableOrders;
    private JScrollPane tableOrdersPane;

    OrderTableModel orderTableData = new OrderTableModel();
    private Server server;
    private Logger logger;
    private static final Dimension MIN_DIMENSION = new Dimension(800,600);
    private JTextField[] stockFields;
    private JTextField[][] descargaFields;
    private JTextField[][] maquinaFields;

    private static LinkedBlockingQueue<String[]> order = new LinkedBlockingQueue<>();

    class TableUpdater extends Thread {
        @Override
        public void run(){
            try {
                while (!interrupted()){
                    String[] newOrder = order.take();
                    //Update or insert
                    int actual = getExistingRow(newOrder[0]);
                    if(actual >= 0) {
                        orderTableData.removeRow(actual);
                        orderTableData.insertRow(actual, newOrder);
                    }
                    else orderTableData.addRow(newOrder);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        private int getExistingRow(String id){
            int rowCount = orderTableData.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                if (orderTableData.getValueAt(i, 0).equals(id)) {
                    return i;
                }
            }
            return -1;
        }
    }

    static void putOrders(String[] newOrder){
        try {
            order.put(newOrder);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void putRequest(Request request) {
        try {
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting up...");

        Properties defaultproperties = new Properties();
        try {
            InputStream filedefault= GUI.class.getResourceAsStream("default-config.properties");
            defaultproperties.load(filedefault);
            filedefault.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Properties properties = new Properties(defaultproperties);
        try {
            InputStream fileproperties = new FileInputStream("interface.properties");
            properties.load(fileproperties);
            fileproperties.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("No interface.properties found, using default-config.properties!\n");
        }

        GUI gui = new GUI(Integer.parseInt(properties.getProperty("DM_PORT")),
                Integer.parseInt(properties.getProperty("INT_PORT")),
                Integer.parseInt(properties.getProperty("POLLING_INTERVAL")));
        JFrame frame = new JFrame("MES – Manufacturing Execution System");
        frame.setContentPane(gui.panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(MIN_DIMENSION);
        // Center the window on the screen
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gui.start();
    }

    private GUI(int DM_PORT, int INT_PORT, int POLLING_INTERVAL) {
        initLogger();
        TableUpdater tableUpdater = new TableUpdater();
        tableUpdater.start();
        stockFields = new JTextField[]{stockP1, stockP2, stockP3, stockP4, stockP5, stockP6, stockP7, stockP8, stockP9};
        JTextField [] descarga1Field = new JTextField[]{descarga1P1, descarga1P2, descarga1P3, descarga1P4, descarga1P5,
                descarga1P6, descarga1P7, descarga1P8, descarga1P9, descarga1PT};
        JTextField [] descarga2Field = new JTextField[]{descarga2P1, descarga2P2, descarga2P3, descarga2P4, descarga2P5,
                descarga2P6, descarga2P7, descarga2P8, descarga2P9, descarga2PT};
        JTextField [] descarga3Field = new JTextField[]{descarga3P1, descarga3P2, descarga3P3, descarga3P4, descarga3P5,
                descarga3P6, descarga3P7, descarga3P8, descarga3P9, descarga3PT};

        descargaFields = new JTextField[][]{descarga1Field, descarga2Field, descarga3Field};

        JTextField [] c1MaField = new JTextField[]{TMaC1P1, TMaC1P2, TMaC1P3, TMaC1P4, TMaC1P5, TMaC1P6, TMaC1P7,
                TMaC1P8, TMaC1P9, TMaC1PT, TMaC1TT};
        JTextField [] c1MbField = new JTextField[]{TMbC1P1, TMbC1P2, TMbC1P3, TMbC1P4, TMbC1P5, TMbC1P6, TMbC1P7,
                TMbC1P8, TMbC1P9, TMbC1PT, TMbC1TT};
        JTextField [] c1McField = new JTextField[]{TMcC1P1, TMcC1P2, TMcC1P3, TMcC1P4, TMcC1P5, TMcC1P6, TMcC1P7,
                TMcC1P8, TMcC1P9, TMcC1PT, TMcC1TT};
        JTextField [] c2MaField = new JTextField[]{TMaC2P1, TMaC2P2, TMaC2P3, TMaC2P4, TMaC2P5, TMaC2P6, TMaC2P7,
                TMaC2P8, TMaC2P9, TMaC2PT, TMaC2TT};
        JTextField [] c2MbField = new JTextField[]{TMbC2P1, TMbC2P2, TMbC2P3, TMbC2P4, TMbC2P5, TMbC2P6, TMbC2P7,
                TMbC2P8, TMbC2P9, TMbC2PT, TMbC2TT};
        JTextField [] c2McField = new JTextField[]{TMcC2P1, TMcC2P2, TMcC2P3, TMcC2P4, TMcC2P5, TMcC2P6, TMcC2P7,
                TMcC2P8, TMcC2P9, TMcC2PT, TMcC2TT};
        JTextField [] c3MaField = new JTextField[]{TMaC3P1, TMaC3P2, TMaC3P3, TMaC3P4, TMaC3P5, TMaC3P6, TMaC3P7,
                TMaC3P8, TMaC3P9, TMaC3PT, TMaC3TT};
        JTextField [] c3MbField = new JTextField[]{TMbC3P1, TMbC3P2, TMbC3P3, TMbC3P4, TMbC3P5, TMbC3P6, TMbC3P7,
                TMbC3P8, TMbC3P9, TMbC3PT, TMbC3TT};
        JTextField [] c3McField = new JTextField[]{TMcC3P1, TMcC3P2, TMcC3P3, TMcC3P4, TMcC3P5, TMcC3P6, TMcC3P7,
                TMcC3P8, TMcC3P9, TMcC3PT, TMcC3TT};

        maquinaFields = new JTextField[][]{c1MaField, c1MbField, c1McField, c2MaField, c2MbField, c2McField, c3MaField,
                c3MbField, c3McField};

        tableOrders.setModel(orderTableData);
        tableOrders.setFont(new Font("Arial", Font.PLAIN, 16));

        try {
            server = new Server(logger, INT_PORT);
            server.start();
            TimerTask pollingTask = new RequestsDispatcher(DM_PORT);
            java.util.Timer pollingTimer = new Timer(true);
            pollingTimer.scheduleAtFixedRate(pollingTask, 0, POLLING_INTERVAL);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void initLogger() {
        logger = Logger.getLogger(getClass().getName());
        logger.setLevel(Level.FINEST);
        logger.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();
        StreamHandler stderr = new StreamHandler(System.err, new SimpleFormatter());
        stderr.setLevel(Level.FINEST);
        stderr.setFormatter(formatter);
        logger.addHandler(stderr);
        try {
            FileHandler logfile = new FileHandler(getClass().getPackageName() + ".log");
            logfile.setLevel(Level.FINEST);
            logfile.setFormatter(formatter);
            logger.addHandler(logfile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to initialize the logger file", e);
        }
        logger.info("Logger initialized");
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            Request request;
            try {
                request = queue.take();
            } catch (InterruptedException ignored) {
                return;
            }

            XMLRequestParser parser;
            try {
                parser = new XMLRequestParser(request.getXMLString());
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
                return;
            }

            switch (parser.getRootName().toLowerCase()) {
                case "current_stores":
                    updateCurrentStores(parser);
                    break;
                case "machinesstats":
                    updateMachinesInfo(parser);
                    break;
                case "unloadzonesstats":
                    updateUnloadZoneInfo(parser);
                    break;
                case "orders":
                    parser.parseOrders();
                    break;
            }

        }

    }

    private void updateCurrentStores(XMLRequestParser parser){
       ArrayList<Integer> stocks = parser.getStocks();
        for (int i = 0; i < stocks.size(); i++) {
            stockFields[i].setText( stocks.get(i).toString() );
        }
    }

    private void updateMachinesInfo(XMLRequestParser parser){
        String[][] infoM;
        infoM = parser.getInfoMachines();
        for (int i = 0; i < infoM.length; i++) {
            for (int j = 0; j < infoM[i].length; j++) {
                maquinaFields[i][j].setText(infoM[i][j]);
            }
        }
    }

    private void updateUnloadZoneInfo(XMLRequestParser parser){
        String[][] infoU;
        infoU = parser.getInfoUnload();
        for (int i = 0; i < infoU.length; i++) {
            for (int j = 0; j < infoU[i].length; j++) {
                descargaFields[i][j].setText(infoU[i][j]);
            }
        }
    }

}
