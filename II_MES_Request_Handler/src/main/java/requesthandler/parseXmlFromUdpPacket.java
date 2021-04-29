package requesthandler;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

public class parseXmlFromUdpPacket extends Thread{
    private static DatagramSocket socket2;
    private static DatagramPacket packet2;
    private final DatagramSocket socket;
    public static String orderNumber="";
    public static int portERP;
    public static InetAddress addressERP;
    final int BUFF_SIZE;
    final int SERVICE_PORT;
    final int OM_PORT;
    final int DM_PORT;

        public parseXmlFromUdpPacket(int DM_PORT, int OM_PORT, int SERVICE_PORT, int BUFF_SIZE) throws SocketException {
            socket = new DatagramSocket(SERVICE_PORT);
            socket.setSoTimeout(1000);
            this.DM_PORT=DM_PORT;
            this.OM_PORT=OM_PORT;
            this.SERVICE_PORT=SERVICE_PORT;
            this.BUFF_SIZE=BUFF_SIZE;
        }

    public void run() {
            try {
                while (! isInterrupted()) {
                    byte[] buff = new byte[BUFF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buff, BUFF_SIZE);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException ignored) {
                        continue;
                    }
                    buff = packet.getData().clone();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(buff, 0, packet.getLength());
                    if(packet.getPort()!=DM_PORT){
                        portERP = packet.getPort();
                        addressERP = packet.getAddress();
                    } else {
                        packet.setAddress(addressERP);
                        packet.setPort(portERP);
                        socket.send(packet);
                        System.out.println("Current_Stores sent to ERP");
                        continue;
                    }
                    try {
                        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder=factory.newDocumentBuilder();
                        Document document=builder.parse(inputStream);
                        document.getDocumentElement().normalize();

                        NodeList orderList=document.getElementsByTagName("Order");
                        NodeList requestList=document.getElementsByTagName("Request_Stores");

                        visitChildNodes(orderList);
                        visitChildNodes(requestList);

                        inputStream.close();

                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket.close();
        }

    private void visitChildNodes(NodeList nList) throws IOException {
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node node = nList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    switch (node.getNodeName()) {
                        case "Order":
                            orderNumber = node.getAttributes().item(0).getNodeValue();
                            break;
                        case "Transform":
                            sendTransformToOperationsManager(node);
                            break;
                        case "Unload":
                            sendUnloadToOperationsManager(node);
                            break;
                        case "Request_Stores":
                            sendRequestToDataManager();
                            break;
                    }
                    //Check all attributes
                    if (node.hasAttributes()) {
                        if (node.hasChildNodes()) {
                            visitChildNodes(node.getChildNodes());
                        }
                    }
                }
            }
    }

    public void sendTransformToOperationsManager(Node node) throws IOException {
            StringBuilder send= new StringBuilder();
            String to="", from="", maxDelay="", quantity="";
            if (node.hasAttributes()) {
                // get attributes names and values
                NamedNodeMap nodeMap = node.getAttributes();
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    Node tempNode = nodeMap.item(i);
                    switch (tempNode.getNodeName()) {
                        case "MaxDelay":
                            maxDelay = tempNode.getNodeValue();
                            break;
                        case "Quantity":
                            quantity = tempNode.getNodeValue();
                            break;
                        case "To":
                            to = tempNode.getNodeValue();
                            break;
                        case "From":
                            from = tempNode.getNodeValue();
                            break;
                    }
                }
            }
            send.append("<Order Number=\"").append(orderNumber).append("\">\n").append("<Transform MaxDelay=\"")
                    .append(maxDelay).append("\" Quantity=\"").append(quantity).append("\" To=\"")
                    .append(to).append("\" From=\"").append(from).append("\"/>\n"+"</Order>\n");
            //Send to Operations Manager
            socket2 = new DatagramSocket();
            byte[] buffer = send.toString().getBytes();
            packet2 = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), OM_PORT);
            socket2.send(packet2);
            socket2.close();
            System.out.println("TRANSFORM sent to Operations Manager\n");
    }
    public void sendUnloadToOperationsManager(Node node) throws IOException {
        StringBuilder send= new StringBuilder();
        String destination="", type="", quantity="";
        if (node.hasAttributes()) {
            // get attributes names and values
            NamedNodeMap nodeMap = node.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Node tempNode = nodeMap.item(i);
                switch (tempNode.getNodeName()) {
                    case "Quantity":
                        quantity = tempNode.getNodeValue();
                        break;
                    case "Destination":
                        destination = tempNode.getNodeValue();
                        break;
                    case "Type":
                        type = tempNode.getNodeValue();
                        break;
                }
            }
        }
        send.append("<Order Number=\"").append(orderNumber).append("\">\n").append("<Unload Quantity=\"")
                .append(quantity).append("\" Destination=\"").append(destination).append("\" Type=\"")
                .append(type).append("\"/>\n").append("</Order>\n");
        socket2 = new DatagramSocket();
        byte[] buffer = send.toString().getBytes();
        packet2 = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), OM_PORT);
        socket2.send(packet2);
        socket2.close();
        System.out.println("UNLOAD sent to Operations Manager\n");
    }

    public void sendRequestToDataManager() throws IOException {
        String send="<Request_Stores/>";
        socket2 = new DatagramSocket();
        byte[] buffer = send.getBytes();
        packet2 = new DatagramPacket(buffer, buffer.length,InetAddress.getLocalHost(),DM_PORT);
        socket2.send(packet2);
        socket2.close();
        System.out.println("REQUEST_STORES sent to Data Manager\n");
    }
}
