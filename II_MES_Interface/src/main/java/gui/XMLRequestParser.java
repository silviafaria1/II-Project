package gui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class XMLRequestParser {
    Document doc;

    XMLRequestParser(String XMLString) throws ParserConfigurationException, IOException, SAXException {
        doc = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(XMLString)));
        doc.normalizeDocument();
        doc.getDocumentElement().normalize();
    }

    String getRootName() { return doc.getDocumentElement().getNodeName(); }


    ArrayList<Integer> getStocks() {
        Element element = doc.getDocumentElement();
        ArrayList<Integer> stock = new ArrayList<>();
        NodeList list = element.getElementsByTagName("WorkPiece");
        int length = list.getLength();
        for(int i = 0; i<length; i++) {
            Element e = (Element) list.item(i);
            stock.add(i, Integer.parseInt(e.getAttribute("quantity")));
        }
        return stock;
    }

    String[][] getInfoUnload() {
        Element element = doc.getDocumentElement();
        String[][] info = new String[3][10];
        NodeList list = element.getElementsByTagName("UnloadZone");
        int length = list.getLength();
        for(int i = 0; i<length; i++) {
            Element e = (Element) list.item(i);
            NodeList listp = e.getElementsByTagName("Piece");
            for (int j = 0; j < listp.getLength(); j++) {
                Element ep = (Element) listp.item(j);
                info[i][j] = ep.getAttribute("qty");
            }
            Element et = (Element) e.getElementsByTagName("TotalPieces").item(0);
            info[i][9] = et.getAttribute("qty");
        }
        return info;
    }

    String[][] getInfoMachines() {
        Element element = doc.getDocumentElement();
        String [][] info = new String[9][11];
        NodeList list = element.getElementsByTagName("Machine");
        int length = list.getLength();
        for(int i = 0; i<length; i++) {
            Element e = (Element) list.item(i);
            NodeList listp = e.getElementsByTagName("Piece");
            for (int j = 0; j < listp.getLength(); j++) {
                Element ep = (Element) listp.item(j);
                info[i][j] = ep.getAttribute("qty");
            }
            Element et = (Element) e.getElementsByTagName("TotalPieces").item(0);
            info[i][9] = et.getAttribute("qty");
            Element ett = (Element) e.getElementsByTagName("TotalTime").item(0);
            info[i][10] = ett.getAttribute("qty");
        }
        return info;
    }

    void parseOrders(){
        Element element = doc.getDocumentElement();
        String[] orders = new String[10];
        NodeList list = element.getElementsByTagName("Order");
        String[] rows = new String[]{"Id", "OrderType", "State", "EntryHour", "StartTime", "EndTime", "Folga",
                "Nbeing", "Nfinished", "Npending"};
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            Element e = (Element) list.item(i);
            for (int j = 0; j < rows.length; j++) {
                orders[j] = e.getAttribute(rows[j]);
            }
            GUI.putOrders(orders.clone());
        }
    }



}