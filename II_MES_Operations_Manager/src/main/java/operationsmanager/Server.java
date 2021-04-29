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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class Server extends Thread {
    private final DatagramSocket socket;
    private final BlockingQueue<String> queue, toSendOrders;
    private final BlockingQueue<Integer> stores;
    private final BlockingQueue<Boolean> machine1, machine2, machine3, unloadZone;
    private static final int buffLength = 1000*64;
    private final DatagramPacket packet;
    private byte[] buffer;

    private static int quantity2;

    public String status;
    public Boolean machineInfo1, machineInfo2, machineInfo3, waitForDMdata, unloadZoneF;

    Server(int port, BlockingQueue<String> queue, BlockingQueue<String> toSendOrders,
           BlockingQueue<Boolean> machine1, BlockingQueue<Boolean> machine2,
           BlockingQueue<Boolean> machine3, BlockingQueue<Integer> stores,
           BlockingQueue<Boolean> unloadZone) throws SocketException {

        socket = new DatagramSocket(port);
        socket.setSoTimeout(1000);
        buffer = new byte[buffLength];
        packet = new DatagramPacket(buffer, buffer.length);

        this.queue = queue;
        this.toSendOrders = toSendOrders;
        this.machine1 = machine1;
        this.machine2 = machine2;
        this.machine3 = machine3;
        this.stores = stores;
        this.unloadZone = unloadZone;
        waitForDMdata = true;
    }

    @Override
    public void run() {
        try {
            OperationsManager.askForWaitingQueue();
            while (!isInterrupted()) {
                try {
                    socket.receive(packet);
                    buffer = packet.getData();
                    String message = new String(buffer, 0, packet.getLength(), StandardCharsets.UTF_8);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder;

                    try {
                        builder = factory.newDocumentBuilder();
                        Document document = builder.parse(new InputSource(new StringReader(message)));
                        document.getDocumentElement().normalize();
                        Element root = document.getDocumentElement();
                        NodeList orderList;
                        switch (root.getNodeName()) {
                            case "Current_Stores":
                                orderList = document.getElementsByTagName("Current_Stores");
                                visitRequestStoresChild(orderList);
                                stores.put(quantity2);
                                break;
                            case "OPCUAstatus":
                                orderList = document.getElementsByTagName("OPCUAstatus");
                                getStatus(orderList);
                                toSendOrders.put(status);
                                break;
                            case "InfoM":
                                orderList = document.getElementsByTagName("InfoM");
                                getMachine(orderList);
                                machine1.put(machineInfo1);
                                machine2.put(machineInfo2);
                                machine3.put(machineInfo3);
                                break;
                            case "InfoUZ":
                                orderList = document.getElementsByTagName("InfoUZ");
                                getUnloadZone(orderList);
                                unloadZone.put(unloadZoneF);
                                break;
                            default:
                                queue.put(message);
                                break;
                        }
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                    }
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        } catch (InterruptedException ignored) { }
        socket.close();
    }

    public static void visitRequestStoresChild(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            Element eElement = (Element) node;
            quantity2 = Integer.parseInt(eElement.getAttribute("quantity"));
        }
    }

    private void getStatus(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            Element eElement = (Element) node;
            status = eElement.getAttribute("status");
        }
    }

    private void getMachine(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            Element eElement = (Element) node;
            machineInfo1 = Boolean.parseBoolean(eElement.getAttribute("info1"));
            machineInfo2 = Boolean.parseBoolean(eElement.getAttribute("info2"));
            machineInfo3 = Boolean.parseBoolean(eElement.getAttribute("info3"));
        }
    }

    private void getUnloadZone(NodeList nList) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            Element eElement = (Element) node;
            unloadZoneF = Boolean.parseBoolean(eElement.getAttribute("info"));
        }
    }

}
