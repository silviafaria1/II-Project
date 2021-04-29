package opcua;

import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class OpcuaServer extends Thread {

    public static final String TRANSFORM = "Transform", UNLOAD = "Unload", STATUS="OPCUAstatus";

    private static Short orderNumberT;
    private static Vector<Short> orderPathT = new Vector<>();
    private static final Vector<Short> timeT = new Vector<>();
    private static final Vector<Short> tool = new Vector<>();
    private static final Vector<Integer> machine = new Vector<>();
    private static int quantityT;
    private static Short pxT;
    private static Short pyT;
    private static Short idT;
    private static LocalDateTime maxFinishingTimeT;
    private static LocalDateTime entryHour;

    private static final Semaphore persistencyReceivedSemaphore = new Semaphore(0);
    private boolean persistencyReceived = false;

    static void waitForPersistencyReceived() throws InterruptedException {
        persistencyReceivedSemaphore.acquire();
    }

    ProcessOrders processOrders = ProcessOrders.getInstance();
    private final DatagramSocket socket;
    private static final int buffLength = 10 * 1024;
    static DatagramPacket packet;
    byte[] buffer;
    BlockingQueue<Integer> toMachine1, toMachine2, toMachine3, toUnloadZone;
    private static int machineType1, machineType2, machineType3;
    private static int unloadZoneType;
    private static boolean omAskedForStatus;
    public static final String FREE = "FREE", NOT_FREE = "NOT FREE";
    NodesIds nodesIds = NodesIds.getInstance();
    public static String status = FREE;
    BlockingQueue<OrderPLC> toSendToPlc;

    public OpcuaServer(
            final int port,
            BlockingQueue<Integer> toMachine1,
            BlockingQueue<Integer> toMachine2,
            BlockingQueue<Integer> toMachine3,
            BlockingQueue<Integer> toUnloadZone,
            BlockingQueue<OrderPLC> toSendToPlc
            )
            throws SocketException
    {
        socket = new DatagramSocket(port);
        buffer = new byte[buffLength];
        packet = new DatagramPacket(buffer, buffer.length);
        socket.setSoTimeout(2000);
        this.toMachine1=toMachine1;
        this.toMachine2=toMachine2;
        this.toMachine3=toMachine3;
        this.toUnloadZone=toUnloadZone;
        omAskedForStatus=false;
        this.toSendToPlc=toSendToPlc;
    }

    @Override
    public void run() {
        askForBeingProcessed();
        sendStatus();

        while (!this.isInterrupted()) {
            try {
                socket.receive(packet);
                buffer = packet.getData();
                getAckMessage(buffer);
                if(!persistencyReceived)
                    askForBeingProcessed();
                if(!omAskedForStatus){
                    sendStatus();            
                }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public void getAckMessage(byte[] buffer) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer, 0, packet.getLength());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            Element root = document.getDocumentElement();
            String nodeName = root.getNodeName();
            switch (nodeName) {
/*                case "TRANSFORM": {
                    NodeList orderList = document.getElementsByTagName("TRANSFORM");
                    visitChildNodesT(orderList);
                    OrderPLC order = new OrderPLC(
                            TRANSFORM, orderNumberT, pxT, pyT, quantityT,
                            orderPathT, timeT, tool, maxFinishingTimeT,
                            idT, machine, entryHour
                    );
                    status = NOT_FREE;
                    waitToPlacePiece();
                    sendOrder(order);
                    sendNewUnload(order);
                    fallingEdgeNewOrder();
                    order.setStartTime();
                    processOrders.addOrderToBeingProcessed(order);
                    for (int i = 0; i < order.getMachine().size(); i++) {
                        if(i==0 || (!order.getMachine().get(i).equals(order.getMachine().get(i - 1)))){
                            ReadMachines.updateAvailability(order.getMachine().get(i), 1);
                        }
                    }
                    status=FREE;
                    break;
                }*/
                case "UNLOAD": {
                    NodeList orderList = document.getElementsByTagName("UNLOAD");
                    visitChildNodesU(orderList);
                    OrderPLC order = new OrderPLC(
                            UNLOAD, orderNumberT, pxT, pyT, quantityT,
                            orderPathT, null, null, maxFinishingTimeT,
                            idT, null, entryHour
                    );
                    status = NOT_FREE;
                    waitToPlacePiece();
                    sendOrder(order);
                    sendNewUnload(order);
                    fallingEdgeNewOrder();
                    order.setStartTime();
                    processOrders.addOrderToBeingProcessed(order);
                    status=FREE;
                    break;
                }
                case STATUS: {
                    sendStatus();
                    omAskedForStatus=true;
                    break;
                }
                case "Statistics": {
                    DataManagerClient.putMessage("Statistics");
                    break;
                }
                case "InfoM": {
                    NodeList orderList = document.getElementsByTagName("InfoM");
                    visitChildNodesMF(orderList);
                    toMachine1.put(machineType1);
                    toMachine2.put(machineType2);
                    toMachine3.put(machineType3);
                    break;
                }
                case "CurrentQueue": {
                    if (!persistencyReceived) {
                        NodeList orderList = document.getElementsByTagName("current");
                        insertOnBeingProcessed(orderList);
                        persistencyReceived = true;
                        persistencyReceivedSemaphore.release();
                    }
                    break;
                }
                case "InfoUZ": {
                    NodeList orderList = document.getElementsByTagName("InfoUZ");
                    visitChildNodesUZ(orderList);
                    toUnloadZone.put(unloadZoneType);
                    break;
                }
                default: break;
            }
        } catch (ParserConfigurationException | SAXException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void visitChildNodesUZ(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (node.getNodeName().equals("InfoUZ")) {
                    unloadZoneType = Integer.parseInt(eElement.getAttribute("type"));
                }
                if (node.hasAttributes()) {
                    if (node.hasChildNodes()) {
                        visitChildNodesUZ(node.getChildNodes());
                    }
                }
            }
        }
    }

    private static void visitChildNodesMF(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (node.getNodeName().equals("InfoM")) {
                    machineType1 = Integer.parseInt(eElement.getAttribute("type1"));
                    machineType2 = Integer.parseInt(eElement.getAttribute("type2"));
                    machineType3 = Integer.parseInt(eElement.getAttribute("type3"));
                }
                if (node.hasAttributes()) {
                    if (node.hasChildNodes()) {
                        visitChildNodesMF(node.getChildNodes());
                    }
                }
            }
        }
    }

    private static void visitChildNodesT(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (node.getNodeName().equals("TRANSFORM")) {
                    orderNumberT = Short.valueOf(
                            eElement.getAttribute("orderNumber")
                    );
                    String[] words = eElement.getAttribute("orderPath").split(",");
                    orderPathT.clear();
                    for (String word : words) {
                        orderPathT.add((short) Integer.parseInt(word));
                    }
                    String[] times = eElement.getAttribute("time").split(",");
                    timeT.clear();
                    for (String time: times) {
                        timeT.add(Short.valueOf(time));
                    }
                    tool.clear();
                    String[] tools = eElement.getAttribute("tool").split(",");
                    for (String t: tools) {
                        tool.add(Short.valueOf(t));
                    }
                    machine.clear();
                    String[] machines = eElement.getAttribute("machine").split(",");
                    for (String m: machines) {
                        machine.add(Integer.valueOf(m));
                    }
                    quantityT = Integer.parseInt(eElement.getAttribute("quantity"));
                    maxFinishingTimeT = LocalDateTime.parse(eElement.getAttribute("maxFinishingTime"));
                    pxT = Short.valueOf(eElement.getAttribute("px"));
                    pyT = Short.valueOf(eElement.getAttribute("py"));
                    idT = Short.valueOf(eElement.getAttribute("id"));
                    entryHour = LocalDateTime.parse(eElement.getAttribute("EntryHour"));
                }
                if (node.hasAttributes()) {
                    if (node.hasChildNodes()) {
                        visitChildNodesT(node.getChildNodes());
                    }
                }
            }
        }
    }

    private static void visitChildNodesU(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (node.getNodeName().equals("UNLOAD")) {
                    orderNumberT = Short.valueOf(eElement.getAttribute("orderNumber"));
                    String[] words = eElement.getAttribute("orderPath").split(",");
                    orderPathT = new Vector<>();
                    for (String word : words) {
                        orderPathT.add((short) Integer.parseInt(word));
                    }
                    quantityT = Integer.parseInt(eElement.getAttribute("quantity"));
                    pxT = Short.valueOf(eElement.getAttribute("px"));
                    pyT = Short.valueOf(eElement.getAttribute("py"));
                    maxFinishingTimeT = LocalDateTime.parse(eElement.getAttribute("maxFinishingTime"));
                    idT = Short.valueOf(eElement.getAttribute("id"));
                    entryHour = LocalDateTime.parse(eElement.getAttribute("EntryHour"));
                }
                if (node.hasAttributes()) {
                    if (node.hasChildNodes()) {
                        visitChildNodesT(node.getChildNodes());
                    }
                }
            }
        }
    }

    public void insertOnBeingProcessed(NodeList orderList) {
        visitChildNodesQueues(orderList);
    }

    private void visitChildNodesQueues(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (node.getNodeName().equals("current")) {
                    OrderPLC order = new OrderPLC(
                            eElement.getAttribute("OrderType"),
                            Short.valueOf(eElement.getAttribute("Number")),
                            Short.valueOf(eElement.getAttribute("px")),
                            Short.valueOf(eElement.getAttribute("py")),
                            Integer.parseInt(eElement.getAttribute("Quantity")),
                            LocalDateTime.parse(eElement.getAttribute("MaxFinishingTime")),
                            Short.valueOf((eElement.getAttribute("Id"))),
                            LocalDateTime.parse(eElement.getAttribute("EntryHour")),
                            LocalDateTime.parse(eElement.getAttribute("StartTime")),
                            Integer.parseInt(eElement.getAttribute("Folga"))
                    );
                    processOrders.addOrderToBeingProcessed(order);
                }
                if (node.hasChildNodes()) {
                    visitChildNodesQueues(node.getChildNodes());
                }
            }
        }
    }

    public  void  askForBeingProcessed()  {
        DataManagerClient.putMessage("<beingProcessed></beingProcessed>");
    }

    public void sendStatus(){
        String answer = "<OPCUAstatus status=\"" + status + "\">\n</OPCUAstatus>\n";
        OperationsManagerClient.putMessage(answer);   
    }

    public void waitToPlacePiece() {
        try {
            UaMonitoredItem armazemOutFree = PlcHandler.getItem(UAmaster.itemss, nodesIds.getArmazemOutFree());
            UaMonitoredItem storageInUseItem = PlcHandler.getItem(UAmaster.itemss, nodesIds.getStorageInUse());
            DataValue storageOutFree;
            DataValue storageInUse;
            do {
                storageOutFree = PlcHandler.readValue(armazemOutFree, UAmaster.client);
                storageInUse = PlcHandler.readValue(storageInUseItem, UAmaster.client);
                Thread.sleep(100);
            } while ( !PlcHandler.readBoolValue(storageOutFree) || PlcHandler.readBoolValue(storageInUse));
        } catch (InterruptedException ignored) {}
    }

    private void fallingEdgeNewOrder(){
        PlcHandler.writeBool(UAmaster.client,
        PlcHandler.getItem(UAmaster.itemss, nodesIds.getNewOrder()), true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) { }
        PlcHandler.writeBool(UAmaster.client, PlcHandler.getItem(UAmaster.itemss, nodesIds.getNewOrder()), false);
    }

    public void sendOrder(OrderPLC o){
        // caminho
        PlcHandler.writeInt16Vector(UAmaster.client, PlcHandler.getItem(UAmaster.itemss, nodesIds.getNodePath()), o.getPath(), 26);

        if (o.getType().equals(TRANSFORM)) {
            PlcHandler.writeInt16Vector(UAmaster.client, PlcHandler.getItem(UAmaster.itemss, nodesIds.getNodeTool()), o.getTools(), 3);
            PlcHandler.writeInt16Vector(UAmaster.client, PlcHandler.getItem(UAmaster.itemss, nodesIds.getNodeTime()), o.getTimes(), 3);
        }
        // order number
        PlcHandler.writeInt16(UAmaster.client, PlcHandler.getItem(UAmaster.itemss, nodesIds.getNodeOrderNumber()), o.getId());
        // px
        PlcHandler.writeInt16(UAmaster.client, PlcHandler.getItem(UAmaster.itemss, nodesIds.getNodePieceType()), o.getPx());
    }

    public void sendNewUnload(OrderPLC order) {
        String message = "<StorageOut type=\""+order.getPx()+"\"/>";
        DataManagerClient.putMessage(message);
    }
}


