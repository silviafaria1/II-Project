package handlers;

import data.PieceStorage;
import data.SubOrder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class XMLBuilder {
    private final Document doc;

    public XMLBuilder() throws ParserConfigurationException {
        doc = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
        doc.setXmlVersion("1.0");
    }

    private Element initStoresElement() {
        Element rootElement = doc.createElement("Current_Stores");
        doc.appendChild(rootElement);
        return rootElement;
    }

    private void addStockAttributes(int pieceType, int stock, Element element) {
        Attr type = doc.createAttribute("type");
        type.setValue("P" + pieceType);
        Attr qty = doc.createAttribute("quantity");
        qty.setValue(String.valueOf(stock));
        element.setAttributeNode(type);
        element.setAttributeNode(qty);
    }

    public void buildFilteredStockResponse(int pieceType, int quantity) {
        Element rootElement = initStoresElement();
        addStockAttributes(
                pieceType,
                quantity,
                rootElement
        );
    }

    private void addWorkpieceStock(int pieceType, int stock, Element rootElement) {
        Element pelement = doc.createElement("WorkPiece");
        addStockAttributes(pieceType, stock, pelement);
        rootElement.appendChild(pelement);
    }

    public void buildFullStockResponse(PieceStorage pieceStorage) {
        Element rootElement = initStoresElement();
        HashMap<Integer, Integer> stocks = pieceStorage.getMapCopy();
        for (Integer p : stocks.keySet()) {
            addWorkpieceStock(p, stocks.get(p), rootElement);
        }
    }

    public void buildOrderStats(
            List<SubOrder> subOrders,
            String rootName,
            String childName
    ) {
        Element rootElement = doc.createElement(rootName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        doc.appendChild(rootElement);
        for (SubOrder subOrder : subOrders) {
            Attr orderNumber = doc.createAttribute("Number");
            Attr Id = doc.createAttribute("Id");
            Attr orderType = doc.createAttribute("OrderType");
            Attr state = doc.createAttribute("State");
            Attr entryHour = doc.createAttribute("EntryHour");
            Attr startTime = doc.createAttribute("StartTime");
            Attr endTime = doc.createAttribute("EndTime");
            Attr folga = doc.createAttribute("Folga");
            Attr nBeing = doc.createAttribute("Nbeing");
            Attr nPending = doc.createAttribute("Npending");
            Attr nFinished = doc.createAttribute("Nfinished");
            Attr px = doc.createAttribute("px");
            Attr py = doc.createAttribute("py");
            Attr maxFinishingTime = doc.createAttribute("MaxFinishingTime");
            Attr quantity = doc.createAttribute("Quantity");
            LocalDateTime orderStartTime = subOrder.getStartTime();
            LocalDateTime orderEndTime = subOrder.getEndTime();

            Element orderElement = doc.createElement(childName);
            Integer id = subOrder.getId();
            Id.setValue(id != null ? String.valueOf(id) : "");
            orderType.setValue(subOrder.getOrderType());
            state.setValue(subOrder.getState());
            if (childName.equals("Order")) {
                entryHour.setValue(formatter.format(subOrder.getEntryTime()));
                startTime.setValue(orderStartTime != null ? formatter.format(orderStartTime) : "");
                endTime.setValue(orderEndTime != null ? formatter.format(orderEndTime) : "");
            }
            else {
                entryHour.setValue(subOrder.getEntryTime().toString());
                startTime.setValue(orderStartTime != null ? orderStartTime.toString() : "");
                endTime.setValue(orderEndTime != null ? orderEndTime.toString() : "");
            }
            Long timeLeft = subOrder.getTimeLeft();
            folga.setValue(timeLeft != null ? String.valueOf(timeLeft) : "");
            nBeing.setValue(String.valueOf(subOrder.getNbeing()));
            nPending.setValue(String.valueOf(subOrder.getNpending()));
            nFinished.setValue(String.valueOf(subOrder.getNfinished()));
            px.setValue(String.valueOf(subOrder.getPx()));
            py.setValue(String.valueOf(subOrder.getPy()));
            LocalDateTime maxFinishingTimeVal = subOrder.getMaxFinishingTime();
            maxFinishingTime.setValue(maxFinishingTimeVal != null ? maxFinishingTimeVal.toString() : "");
            orderNumber.setValue(String.valueOf(subOrder.getOrderNumber()));
            quantity.setValue(String.valueOf(subOrder.getQuantity()));

            orderElement.setAttributeNode(Id);
            orderElement.setAttributeNode(orderType);
            orderElement.setAttributeNode(state);
            orderElement.setAttributeNode(entryHour);
            orderElement.setAttributeNode(startTime);
            orderElement.setAttributeNode(endTime);
            orderElement.setAttributeNode(folga);
            orderElement.setAttributeNode(nBeing);
            orderElement.setAttributeNode(nPending);
            orderElement.setAttributeNode(nFinished);
            orderElement.setAttributeNode(px);
            orderElement.setAttributeNode(py);
            orderElement.setAttributeNode(maxFinishingTime);
            orderElement.setAttributeNode(orderNumber);
            orderElement.setAttributeNode(quantity);

            rootElement.appendChild(orderElement);
        }
    }

    public void buildUnloadZoneStats(
            Vector<Vector<Integer>> uzStats
    ) {
        Element rootElement = doc.createElement("UnloadZonesStats");
        for (int i = 0; i < uzStats.size(); i++) {
            Element zoneElement = doc.createElement("UnloadZone");
            Attr unloadZoneId = doc.createAttribute("id");
            unloadZoneId.setValue(String.valueOf(i+1));
            zoneElement.setAttributeNode(unloadZoneId);
            int total = 0;
            for (int j = 0; j < uzStats.get(0).size(); j++) {
               Element pieceElement = doc.createElement("Piece");
               Attr pieceType = doc.createAttribute("type");
               Attr pieceCount = doc.createAttribute("qty");

               int value = uzStats.get(i).get(j);
               total += value;
               pieceType.setValue(String.valueOf(j+1));
               pieceCount.setValue(String.valueOf(value));

               pieceElement.setAttributeNode(pieceType);
               pieceElement.setAttributeNode(pieceCount);
               zoneElement.appendChild(pieceElement);
            }
            Element totalPieces = doc.createElement("TotalPieces");
            totalPieces.setAttribute("qty", String.valueOf(total));
            zoneElement.appendChild(totalPieces);

            rootElement.appendChild(zoneElement);
        }
        doc.appendChild(rootElement);
    }

    public void buildMachineStats(
            Vector<Vector<Integer>> machinePieceStats,
            Vector<Long> machineWorkTimes
    ) {
        Element rootElement = doc.createElement("MachinesStats");
        for (int i = 0; i < machinePieceStats.size(); i++) {
            Element machineElement = doc.createElement("Machine");
            Attr unloadZoneId = doc.createAttribute("id");
            unloadZoneId.setValue(String.valueOf(i+1));
            machineElement.setAttributeNode(unloadZoneId);
            int total = 0;
            for (int j = 0; j < machinePieceStats.get(0).size(); j++) {
                Element pieceElement = doc.createElement("Piece");
                Attr pieceType = doc.createAttribute("type");
                Attr pieceCount = doc.createAttribute("qty");

                int value = machinePieceStats.get(i).get(j);
                total += value;
                pieceType.setValue(String.valueOf(j+1));
                pieceCount.setValue(String.valueOf(value));

                pieceElement.setAttributeNode(pieceType);
                pieceElement.setAttributeNode(pieceCount);
                machineElement.appendChild(pieceElement);
            }
            Element totalPieces = doc.createElement("TotalPieces");
            totalPieces.setAttribute("qty", String.valueOf(total));
            Element totalWorkTime = doc.createElement("TotalTime");
            totalWorkTime.setAttribute("qty", String.valueOf(machineWorkTimes.get(i)));
            machineElement.appendChild(totalPieces);
            machineElement.appendChild(totalWorkTime);

            rootElement.appendChild(machineElement);
        }
        doc.appendChild(rootElement);
    }

    public String getXMLAsString() throws TransformerException {
        DOMSource source = new DOMSource(doc);
        StringWriter tmp = new StringWriter();
        StreamResult output = new StreamResult(tmp);
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        doc.normalizeDocument();
        transformer.transform(source, output);
        return tmp.toString();
    }

    public void buildMaxIdResponse(int maxId) {
        Element rootElement = doc.createElement("id");
        rootElement.setAttribute("value", String.valueOf(maxId));
        doc.appendChild(rootElement);
    }
}
