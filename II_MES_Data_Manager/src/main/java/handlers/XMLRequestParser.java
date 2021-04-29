package handlers;

import data.SubOrder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Vector;

public class XMLRequestParser {
    Document doc;

    public XMLRequestParser(String XMLString)
        throws ParserConfigurationException, IOException, SAXException
    {
        doc = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(XMLString)));
        doc.normalizeDocument();
        doc.getDocumentElement().normalize();
    }

    public String getRootName() {
        return doc.getDocumentElement().getNodeName();
    }

    public boolean requestHasFilter() {
        return doc.getDocumentElement().hasAttribute("type");
    }

    public Integer getRequestFilter() {
        Element element = doc.getDocumentElement();
        Attr e = element.getAttributeNode("type");
        return Integer.parseInt(e.getValue());
    }

    public Integer getLoadType() {
        Element element = doc.getDocumentElement();
        return Integer.parseInt(element.getAttribute("type"));
    }

    public Integer getUnloadType() {
        return getLoadType();
    }

    public ArrayList<SubOrder> getOrders() {
        Element element = doc.getDocumentElement();
        String rootName = element.getNodeName().toLowerCase();
        NodeList children;
        if (rootName.equals("statistics")) {
            children = element.getElementsByTagName("waiting");
            return getOrdersHelper(children, "waiting");
        }
        else if (rootName.equals("ordersstats")) {
            int orderCount = element.getChildNodes().getLength();
            ArrayList<SubOrder> tmp = new ArrayList<>(orderCount);
            for (String orderType : new String[]{"current","finished"}) {
                children = element.getElementsByTagName(orderType);
                tmp.addAll(getOrdersHelper(children, orderType));
            }
            return tmp;
        }
        else {
            return null;
        }
    }

    public Vector<Vector<Integer>> getUnloadZoneStats(int zoneCount, int pieceCount) {
        Element element = doc.getDocumentElement();
        NodeList unloadZoneChildren = element.getElementsByTagName("UnloadZone");
        Vector<Vector<Integer>> thisRun = new Vector<>(zoneCount);
        for (int i = 0; i < zoneCount; i++) {
            thisRun.add(new Vector<>(pieceCount));
            for (int j = 0; j < pieceCount; j++) {
                thisRun.get(i).add(j, 0);
            }
        }
        for (int i = 0; i < unloadZoneChildren.getLength(); i++) {
            Element zone = (Element) unloadZoneChildren.item(i);
            Attr zoneIdAttr = zone.getAttributeNode("id");
            int zoneId = Integer.parseInt(zoneIdAttr.getValue());
            NodeList pieceChildren = zone.getElementsByTagName("Piece");
            for (int j = 0; j < pieceChildren.getLength(); j++) {
                Element piece = (Element) pieceChildren.item(j);
                int type = Integer.parseInt(piece.getAttributeNode("type").getValue());
                int qty = Integer.parseInt(piece.getAttributeNode("qty").getValue());
                thisRun.get(zoneId-1).set(type-1, qty);
            }
        }
        return thisRun;
    }

    public Vector<Vector<Integer>> getMachinePieceStats(
            int machineCount,
            int pieceCount
    ) {
        NodeList machineChildren = doc.getDocumentElement().getElementsByTagName("Machine");
        Vector<Vector<Integer>> thisRun = new Vector<>(machineCount);
        for (int i = 0; i < machineCount; i++) {
            thisRun.add(new Vector<>(pieceCount));
            for (int j = 0; j < pieceCount; j++) {
                thisRun.get(i).add(j, 0);
            }
        }
        for (int i = 0; i < machineChildren.getLength(); i++) {
            Element machine = (Element) machineChildren.item(i);
            Attr machineIdAttr = machine.getAttributeNode("id");
            int machineId = Integer.parseInt(machineIdAttr.getValue());
            NodeList pieceChildren = machine.getElementsByTagName("Piece");
            for (int j = 0; j < pieceChildren.getLength(); j++) {
                Element piece = (Element) pieceChildren.item(j);
                int type = Integer.parseInt(piece.getAttributeNode("type").getValue());
                int qty = Integer.parseInt(piece.getAttributeNode("qty").getValue());
                thisRun.get(machineId-1).set(type-1, qty);
            }
        }
        return thisRun;
    }

    public Vector<Long> getMachineWorkTimeStats(
            int machineCount
    ) {
        NodeList machineChildren = doc.getDocumentElement().getElementsByTagName("Machine");
        Vector<Long> thisRun = new Vector<>(machineCount);
        for (int i = 0; i < machineCount; i++) {
            thisRun.add(i, 0L);
        }
        for (int i = 0; i < machineChildren.getLength(); i++) {
            Element machine = (Element) machineChildren.item(i);
            Element totalTimeNode = (Element) machine.getElementsByTagName("TotalTime").item(0);
            long totalTime = Long.parseLong(totalTimeNode.getAttribute("qty"));
            thisRun.set(i, totalTime);
        }
        return thisRun;
    }

    private ArrayList<SubOrder> getOrdersHelper(NodeList children, String rootname) {
        // TODO filter needed fields by root name
        ArrayList<SubOrder> tmp = new ArrayList<>(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            Node e =  children.item(i);
            NamedNodeMap n = e.getAttributes();
            int orderNumber = Integer.parseInt(
                    n.getNamedItem("Number").getNodeValue()
            );
            String orderType = n.getNamedItem("OrderType").getNodeValue();
            int px = Integer.parseInt(n.getNamedItem("px").getNodeValue());
            int py = Integer.parseInt(n.getNamedItem("py").getNodeValue());
            int quantity = Integer.parseInt(
                    n.getNamedItem("Quantity").getNodeValue()
            );
            LocalDateTime maxFinishingTime = LocalDateTime.parse(
                    n.getNamedItem("MaxFinishingTime").getNodeValue()
            );
            LocalDateTime entryTime = LocalDateTime.parse(
                    n.getNamedItem("EntryHour").getNodeValue()
            );
            int id = Integer.parseInt(n.getNamedItem("Id").getNodeValue());
            SubOrder subOrder;
            if (rootname.equals("current") || rootname.equals("finished")) {
                LocalDateTime startTime = LocalDateTime.parse(
                        n.getNamedItem("StartTime").getNodeValue()
                );
                long timeLeft = Long.parseLong(n.getNamedItem("Folga").getNodeValue());
                if (rootname.equals("finished")) {
                    LocalDateTime endTime = LocalDateTime.parse(
                            n.getNamedItem("EndTime").getNodeValue()
                    );
                    subOrder = new SubOrder(
                            orderNumber, orderType, "finished", px, py, quantity,
                            maxFinishingTime, entryTime, startTime, endTime, timeLeft, id
                    );
                }
                else {
                    subOrder = new SubOrder(
                            orderNumber, orderType, "current", px, py, quantity,
                            maxFinishingTime, entryTime, startTime, timeLeft, id
                    );
                }
            }
            else {
                subOrder = new SubOrder(
                        orderNumber, orderType, "waiting", px, py, quantity,
                        maxFinishingTime, entryTime, id
                );
            }
            tmp.add(subOrder);
        }
        return tmp;
    }

}
