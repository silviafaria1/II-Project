package opcua;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class DataManagerClient extends Thread {

    static ProcessOrders queues = ProcessOrders.getInstance();
    private static LinkedBlockingQueue<String> messagePool;
    private static int PORT;
    private final DatagramSocket socket;
    static DatagramPacket packet;

    public DataManagerClient(int clientPort) throws SocketException {
        PORT = clientPort; // comunica com o dataManager server
        socket = new DatagramSocket();
        messagePool = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        try {
            while (!this.isInterrupted()) {

                String message = messagePool.take(); // get xml in string to send to DataManager
                if( message.equals("Statistics") ){
                    sendStatistics();
                    continue;
                }
                try {
                    packet = new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.length(),
                            InetAddress.getLocalHost(), PORT);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return;
                }
                socket.send(packet);
            }
        } catch (InterruptedException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }

    public void sendStatistics() {
        // TODO clean this
        NodesIds nodes = NodesIds.getInstance();
        StringBuilder send = new StringBuilder();
        DataValue value;
        int qty;
        send.append("<MachinesStats>\n");
        for(int i=1;i<=9;i++) {
            send.append("<Machine id=\"").append(i).append("\">\n");
            for (int j = 1; j <= 9; j++) {
                value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getNodeMachine(i, j)), UAmaster.client);
                assert value != null;
                qty = PlcHandler.readInt16Value(value);
                send.append("<Piece type=\"").append(j).append("\" qty=\"").append(qty).append("\"/>\n");
            }
            value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getNodeMachineTotalPieces(i)),
                    UAmaster.client);
            assert value != null;
            qty = PlcHandler.readInt16Value(value);
            send.append("<TotalPieces qty=\"").append(qty).append("\"/>\n");
            value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getNodeMachineTotalTime(i)),
                    UAmaster.client);
            assert value != null;
            qty = PlcHandler.readInt16Value(value);
            send.append("<TotalTime qty=\"").append(qty).append("\"/>\n</Machine>\n");
        }
        send.append("</MachinesStats>\n");
        try {
            packet = new DatagramPacket(send.toString().getBytes(StandardCharsets.UTF_8), send.toString().length(),
                    InetAddress.getLocalHost(), PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send.delete(0, send.length());
        send.append("<UnloadZonesStats>\n");
        for(int i=1;i<=3;i++) {
            send.append("<UnloadZone id=\"").append(i).append("\">\n");
            for(int j=1;j<=9;j++){
                value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getNodeUnloading(i,j)),
                        UAmaster.client);
                assert value != null;
                qty = PlcHandler.readInt16Value(value);
                send.append("<Piece type=\"").append(j).append("\" qty=\"").append(qty).append("\"/>\n");
            }
            value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getNodeUnloadingTotalPieces(i)),
                    UAmaster.client);
            assert value != null;
            qty = PlcHandler.readInt16Value(value);
            send.append("<TotalPieces qty=\"").append(qty).append("\"/>\n</UnloadZone>\n");
        }
        send.append("</UnloadZonesStats>\n");
        try {
            packet = new DatagramPacket(send.toString().getBytes(StandardCharsets.UTF_8), send.toString().length(),
                    InetAddress.getLocalHost(), PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send.delete(0, send.length());
        send.append("<OrdersStats>\n");
        send.append(sendQueues()).append("</OrdersStats>\n");
        try {
            packet = new DatagramPacket(send.toString().getBytes(StandardCharsets.UTF_8), send.toString().length(),
                    InetAddress.getLocalHost(), PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send.delete(0, send.length());
    }

    private StringBuilder sendQueues() {
        StringBuilder send = new StringBuilder();
        Iterator<OrderPLC> current = queues.getBeingProcessed().iterator();
        Iterator<OrderPLC> finished = queues.getDone().iterator();

        while (current.hasNext() && queues.getBeingProcessed()!=null) {
            OrderPLC o = current.next();
            o.setFolga(false);
            send.append("<current Number=\"").append(o.getNumber())
                    .append("\" OrderType=\"").append(o.getType())
                    .append("\" px=\"").append(o.getPx())
                    .append("\" py=\"").append(o.getPy())
                    .append("\" Quantity=\"").append(o.getTotalQuantity())
                    .append("\" MaxFinishingTime=\"").append(o.getMaxFinishingTime())
                    .append("\" EntryHour=\"").append(o.getEntryHour())
                    .append("\" StartTime=\"").append(o.getStartTime())
                    .append("\" Folga=\"").append(queues.getFolgaAtual(o))
                    .append("\" Id=\"").append(o.getId())
                    .append("\"/>\n");
        }

        while (finished.hasNext()  && queues.getDone()!=null) {
            OrderPLC o = finished.next();
            send.append("<finished Number=\"").append(o.getNumber())
                    .append("\" OrderType=\"").append(o.getType())
                    .append("\" px=\"").append(o.getPx())
                    .append("\" py=\"").append(o.getPy())
                    .append("\" Quantity=\"").append(o.getTotalQuantity())
                    .append("\" MaxFinishingTime=\"").append(o.getMaxFinishingTime())
                    .append("\" EntryHour=\"").append(o.getEntryHour())
                    .append("\" StartTime=\"").append(o.getStartTime())
                    .append("\" EndTime=\"").append(o.getfinishTime())
                    .append("\" Folga=\"").append(o.getFolga())
                    .append("\" Id=\"").append(o.getId())
                    .append("\"/>\n");
        }
        return send;
    }

    static void putMessage(String message) {
        try {
            messagePool.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
