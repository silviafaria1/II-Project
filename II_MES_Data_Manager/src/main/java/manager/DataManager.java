package manager;

import data.*;
import handlers.SQLHandler;
import handlers.XMLBuilder;
import handlers.XMLRequestParser;
import logger.ClassLogger;
import org.xml.sax.SAXException;
import server.Server;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager extends Thread {
    final int OM_PORT;
    final int RH_PORT;
    final int OPC_PORT;
    final int INTERFACE_PORT;

    private final LinkedBlockingQueue<Request> queue;
    private final Server server;
    private final PieceStorage pieceStorage;
    private final Logger logger;
    private final SQLHandler sqlHandler;
    private final UnloadZoneStats unloadZoneStats;
    private final MachineStats machineStats;

    private DataManager(
            Server server,
            LinkedBlockingQueue<Request> queue,
            PieceStorage pieceStorage,
            SQLHandler sqlHandler,
            UnloadZoneStats unloadZoneStats,
            MachineStats machineStats,
            Logger logger,
            int OM_PORT,
            int RH_PORT,
            int OPC_PORT,
            int INTERFACE_PORT
    ) {
        this.logger = logger;
        this.OM_PORT = OM_PORT;
        this.RH_PORT = RH_PORT;
        this.OPC_PORT = OPC_PORT;
        this.INTERFACE_PORT = INTERFACE_PORT;
        this.server = server;
        this.queue = queue;
        this.pieceStorage = pieceStorage;
        this.sqlHandler = sqlHandler;
        this.unloadZoneStats = unloadZoneStats;
        this.machineStats = machineStats;
    }

    static DataManager createNew(
            Server server,
            LinkedBlockingQueue<Request> queue,
            PieceStorage pieceStorage,
            SQLHandler sqlHandler,
            UnloadZoneStats unloadZoneStats,
            MachineStats machineStats,
            Logger parentLogger,
            int OM_PORT,
            int RH_PORT,
            int OPC_PORT,
            int INTERFACE_PORT
    ) {
        Logger logger = ClassLogger.initLogger(DataManager.class.getName(), parentLogger);
        return new DataManager(
                server,
                queue,
                pieceStorage,
                sqlHandler,
                unloadZoneStats,
                machineStats,
                logger,
                OM_PORT,
                RH_PORT,
                OPC_PORT,
                INTERFACE_PORT
        );
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Request request = queue.take();
                logger.info("Message received from " +
                        request.getOriginHost() + ":" + request.getOriginPort());
                XMLRequestParser parser;
                try {
                    String msg = request.getXMLString();
                    logger.fine(msg);
                    parser = new XMLRequestParser(msg);
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    e.printStackTrace();
                    continue;
                }
                String rootName = parser.getRootName();
                logger.info("Root element name: " + rootName);
                switch (rootName.toLowerCase()) {
                    case "request_stores":
                        request.setDestPort(RH_PORT);
                        new Thread(() -> handleStoreRequest(parser,request)).start();
                        break;
                    case "storagein":
                        new Thread(() -> handleLoadRequest(parser)).start();
                        break;
                    case "storageout":
                        new Thread(() -> handleUnloadRequest(parser)).start();
                        break;
                    case "beingprocessed":
                    case "waiting":
                        new Thread(() -> sendPersistencyQueue(rootName, request)).start();
                        break;
                    case "machinesstats":
                        // TODO handle machine and unload zone stats
                        new Thread(() -> handleMachinesStats(parser)).start();
                        break;
                    case "unloadzonesstats":
                        new Thread(() -> handleUnloadZoneStats(parser)).start();
                        break;
                    case "ordersstats":
                    case "statistics":
                        new Thread(() -> handleOrderStats(parser)).start();
                        break;
                    case "interface":
                        request.setDestPort(INTERFACE_PORT);
                        new Thread(() -> handleStoreRequest(parser,request)).start();
                        new Thread(() -> sendOrderStats(request)).start();
                        new Thread(() -> sendUnloadZoneStats(request)).start();
                        new Thread(() -> sendMachineStats(request)).start();
                        break;
                    case "id":
                        request.setDestPort(OM_PORT);
                        new Thread(() -> sendLastId(request)).start();
                        break;
                    default:
                        logger.warning("Request not recognised\n" + request.getXMLString());
                }
            }
        } catch (InterruptedException ignored) { }
    }

    private void sendPersistencyQueue(
            String stateFilter,
            Request request
    ) {
        try {
            if (stateFilter.equals("beingProcessed")) stateFilter = "current";
            logger.fine("Getting persistency queue for " + stateFilter + " orders");
            List<SubOrder> orders;
            orders = sqlHandler.getStateFilteredOrders(stateFilter);
            if (orders == null) {
                logger.severe(
                        "Failed to fetch " + stateFilter + " orders for persistency"
                );
                return;
            }
            String msgString;
            XMLBuilder builder = new XMLBuilder();
            switch (stateFilter) {
                case "waiting":
                    request.setDestPort(OM_PORT);
                    if (orders.size() == 0)
                        msgString = "<WaitingQueue/>";
                    else {
                        builder.buildOrderStats(orders, "WaitingQueue", "waiting");
                        msgString = builder.getXMLAsString();
                    }
                    break;
                case "current":
                    request.setDestPort(OPC_PORT);
                    if (orders.size() == 0)
                        msgString = "<CurrentQueue/>";
                    else {
                        builder.buildOrderStats(orders, "CurrentQueue", "current");
                        msgString = builder.getXMLAsString();
                    }
                    break;
                default:
                    return;
            }
            byte[] msg = msgString.getBytes(StandardCharsets.UTF_8);
            DatagramPacket dp = new DatagramPacket(
                    msg,
                    msg.length,
                    request.getOriginHost(),
                    request.getDestPort()
            );
            server.addOutboundMessage(dp);
            logger.fine("Generated Reply:\n#### BEGIN ####\n" +
                    msgString + "\n#### END ####"
            );
        } catch (ParserConfigurationException | TransformerException e) {
            logger.log(Level.SEVERE, "Failed to build XML for persistency", e);
        }
    }

    private void handleStoreRequest(
            XMLRequestParser parser,
            Request request
    ) {
        try {
            XMLBuilder builder = new XMLBuilder();
            boolean isFiltered = parser.requestHasFilter();
            if (isFiltered) {
                logger.info("Filtered Store Request");
                request.setDestPort(OM_PORT);
                int type = parser.getRequestFilter();
                builder.buildFilteredStockResponse(type, pieceStorage.getQuantity(type));
            } else {
                logger.info("Full Store Request");
                builder.buildFullStockResponse(pieceStorage);
            }
            String resp = builder.getXMLAsString();
            logger.fine("Generated Reply:\n#### BEGIN ####\n" + resp + "#### END ####");
            DatagramPacket response =
                    new DatagramPacket(
                            resp.getBytes(),
                            resp.length(),
                            request.getOriginHost(),
                            request.getDestPort()
                    );
            server.addOutboundMessage(response);
        } catch (ParserConfigurationException e) {
            logger.log(Level.SEVERE, "Failed to initialize XML builder", e);
            System.exit(-1);
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "Failed to transform DOM to XML", e);
            System.exit(-1);
        }
    }

    private void handleLoadRequest(XMLRequestParser parser) {
        int type = parser.getLoadType();
        logger.info("Type loaded: " + type);
        int qty = pieceStorage.addQuantity(type, 1);
        sqlHandler.updateStock(type, qty);
    }

    private void handleUnloadRequest(XMLRequestParser parser) {
        int type = parser.getUnloadType();
        logger.info("Type unloaded: " + type);
        int qty = pieceStorage.subQuantity(type, 1);
        sqlHandler.updateStock(type, qty);
    }

    private void handleOrderStats(XMLRequestParser parser) {
        List<SubOrder> subOrders = parser.getOrders();
        if (subOrders == null) {
            logger.warning("Failure parsing XML for order statistics\n");
            return;
        }
        sqlHandler.upsertSubOrders(subOrders);
        for (SubOrder subOrder : subOrders)
            logger.fine(subOrder.toString());
    }

    private void sendOrderStats(Request request) {
        try {
            List<SubOrder> subOrders = sqlHandler.getSubOrders();
            XMLBuilder builder = new XMLBuilder();
            builder.buildOrderStats(subOrders, "Orders", "Order");
            request.setDestPort(INTERFACE_PORT);
            byte[] msg = builder.getXMLAsString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    msg,
                    msg.length,
                    request.getOriginHost(),
                    request.getDestPort()
            );
            server.addOutboundMessage(packet);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private void handleUnloadZoneStats(
            XMLRequestParser parser
    ) {
        Vector<Vector<Integer>> thisRun = parser.getUnloadZoneStats(
                unloadZoneStats.getUnloadZoneCount(),
                unloadZoneStats.getPiecetypeCount()
        );
        unloadZoneStats.update(thisRun);
        sqlHandler.upsertUnloadZones(unloadZoneStats);
    }

    private void handleMachinesStats(
            XMLRequestParser parser
    ) {
        Vector<Vector<Integer>> thisRun = parser.getMachinePieceStats(
                machineStats.getMachineCount(),
                machineStats.getPiecetypeCount()
        );
        Vector<Long> thisRunWorkTimes = parser.getMachineWorkTimeStats(
                machineStats.getMachineCount()
        );
        machineStats.update(thisRun);
        machineStats.updateWorktimes(thisRunWorkTimes);
        sqlHandler.upsertMachineStats(machineStats);
    }

    private void sendUnloadZoneStats(
            Request request
    ) {
        try {
            XMLBuilder builder = new XMLBuilder();
            builder.buildUnloadZoneStats(unloadZoneStats.getCopy());
            String xmlMsg = builder.getXMLAsString();
            byte[] msg = xmlMsg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    msg,
                    msg.length,
                    request.getOriginHost(),
                    request.getDestPort()
            );
            server.addOutboundMessage(packet);
            logger.info("Unload zone statistics sent");
            logger.fine(xmlMsg);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private void sendMachineStats(
            Request request
    ) {
        try {
            XMLBuilder builder = new XMLBuilder();
            builder.buildMachineStats(machineStats.getCopy(), machineStats.getWorkTimesCopy());
            String xmlMsg = builder.getXMLAsString();
            byte[] msg = xmlMsg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    msg,
                    msg.length,
                    request.getOriginHost(),
                    request.getDestPort()
            );
            server.addOutboundMessage(packet);
            logger.info("Machine statistics sent");
            logger.fine(xmlMsg);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private void sendLastId(Request request) {
        try {
            int maxId = sqlHandler.getLastId();
            XMLBuilder builder = new XMLBuilder();
            builder.buildMaxIdResponse(maxId);
            String xmlMsg = builder.getXMLAsString();
            byte[] msg = xmlMsg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    msg,
                    msg.length,
                    request.getOriginHost(),
                    request.getDestPort()
            );
            server.addOutboundMessage(packet);
            logger.info("MaxID sent to OM");
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

}
