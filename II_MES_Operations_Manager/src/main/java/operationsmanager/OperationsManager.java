package operationsmanager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class OperationsManager extends Thread {

    private static int orderNumber;
    private static String from = "";
    private static String to = "";
    private static int max;
    private static int quantity;
    private static String type;
    private static String destination = "";
    private static int quantity1;
    private static String tipoo;
    private final Map<String, Integer> pieceTypes = new HashMap<>();
    private final Map<String, Integer> loputestination = new HashMap<>();
    public static boolean receivedPending, receivedWaiting;
    public static BlockingQueue<String> toDataManager;
    private final BlockingQueue<String> queue;
    private final Task task;
    public static final String TRANSFORM = "Transform", UNLOAD = "Unload";

    public OperationsManager(BlockingQueue<String> toDataManager, BlockingQueue<String> fromServer) {

        task = Task.getInstance();
        OperationsManager.toDataManager = toDataManager;
        this.queue = fromServer;
        receivedPending = false;
        receivedWaiting = false;
        pieceTypes.put("P1", 1);
        pieceTypes.put("P2", 2);
        pieceTypes.put("P3", 3);
        pieceTypes.put("P4", 4);
        pieceTypes.put("P5", 5);
        pieceTypes.put("P6", 6);
        pieceTypes.put("P7", 7);
        pieceTypes.put("P8", 8);
        pieceTypes.put("P9", 9);

        loputestination.put("D1", 1);
        loputestination.put("D2", 2);
        loputestination.put("D3", 3);
    }

    @Override
    public void run() {
        String message;

        try {
            while (!this.isInterrupted()) {
                message = queue.take();
                getAckMessage(message);
            }
        } catch (InterruptedException ignored) { }
    }

    private void getAckMessage(String message) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(message)));
            document.getDocumentElement().normalize();
            Element root = document.getDocumentElement();
            NodeList orderList;
            switch (root.getNodeName()) {
                case "Order":
                    orderList = document.getElementsByTagName("Order");
                    visitChildNodes(orderList);
                    if (tipoo.equals(TRANSFORM)) {
                        task.createTransformOrder(orderNumber, pieceTypes.get(from), pieceTypes.get(to), quantity, max);
                    } else if (tipoo.equals(UNLOAD)) {
                        task.createUnloadOrder(orderNumber, pieceTypes.get(type), loputestination.get(destination), quantity1);
                    }
                    break;
                case "WaitingQueue":
                    if (!receivedWaiting) {
                        orderList = document.getElementsByTagName("WaitingQueue");
                        insertOnWaiting(orderList);
                        receivedWaiting = true;
                    }
                    break;
                case "Statistics":
                    new Thread(() -> sendStatisticsToDM(task.getCurrent())).start();
                    break;
                default:
                    break;
            }
            if(!OperationsManager.receivedWaiting){
                askForWaitingQueue() ;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void visitChildNodes(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                switch (node.getNodeName()) {
                    case "Order":
                        orderNumber = Integer.parseInt(eElement.getAttribute("Number"));
                        break;
                    case "Transform":
                        tipoo = TRANSFORM;
                        from = eElement.getAttribute("From");
                        to = eElement.getAttribute("To");
                        quantity = Integer.parseInt(eElement.getAttribute("Quantity"));
                        max = Integer.parseInt(eElement.getAttribute("MaxDelay"));
                        break;
                    case "Unload":
                        tipoo = UNLOAD;
                        type = eElement.getAttribute("Type");
                        destination = eElement.getAttribute("Destination");
                        quantity1 = Integer.parseInt(eElement.getAttribute("Quantity"));
                        break;
                }
                if (node.hasAttributes()) {
                    if (node.hasChildNodes()) {
                        visitChildNodes(node.getChildNodes());
                    }
                }
            }
        }
    }

    public static void sendAvailabilityRequestToDM(int piece) {
        StringBuilder send= new StringBuilder();
        send.append("<Request_Stores>\n").append("<Workpiece type=\"").append(piece)
                .append("\"/>\n").append("</Request_Stores>\n");
        try {
            OperationsManager.toDataManager.put(String.valueOf(send));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void insertOnWaiting(NodeList orderList) {
        visitChildNodesQueues(orderList);
    }

    private void visitChildNodesQueues(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
               if(node.getNodeName().equals("waiting") ) {
                   OrderMes order = new OrderMes(eElement.getAttribute("OrderType"),
                           Integer.parseInt(eElement.getAttribute("Number")),
                           Integer.parseInt(eElement.getAttribute("px")),
                           Integer.parseInt(eElement.getAttribute("py")),
                           Integer.parseInt(eElement.getAttribute("Quantity")),
                           0,
                           LocalDateTime.parse(eElement.getAttribute("EntryHour")),
                           LocalDateTime.parse(eElement.getAttribute("MaxFinishingTime")),
                           Integer.parseInt(eElement.getAttribute("Id")));
                   if(order.getType().equals(UNLOAD)) {
                       order.setPath(0, null);
                   }
                   task.insertCurrent(order);
                }
                if (node.hasChildNodes()) {
                    visitChildNodesQueues(node.getChildNodes());
                }
            }
        }
    }

    private void sendStatisticsToDM(OrderQueueMes current) {
        StringBuilder send = new StringBuilder();
        send.append("<Statistics>\n");
        PriorityBlockingQueue<OrderMes> currentCopy = current.getCopy();

        for (OrderMes o : currentCopy) {
            send.append("<waiting Number=\"").append(o.getNumber()).append("\" OrderType=\"")
                    .append(o.getType()).append("\" px=\"")
                    .append(o.getPx()).append("\" py=\"").append(o.getPy())
                    .append("\" Quantity=\"").append(o.getTotalQuantity())
                    .append("\" Deadline=\"").append(o.getDeadline()).
                    append("\" MaxFinishingTime=\"").append(o.getMaxFinishingTime())
                    .append("\" EntryHour=\"").append(o.getEntryHour())
                    .append("\" Id=\"").append(o.getId()).append("\"/>\n");
        }
        send.append("</Statistics>\n");

        // Send to DM
        try {
            OperationsManager.toDataManager.put(send.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void askForWaitingQueue()  {
        try {
            OperationsManager.toDataManager.put("<waiting></waiting>");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}