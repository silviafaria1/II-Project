package opcua;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

import java.util.TimerTask;

public class GetFromPLC extends TimerTask {
    NodesIds nodes = NodesIds.getInstance();
    ProcessOrders processOrders = ProcessOrders.getInstance();

    public GetFromPLC() {}

    @Override
    public void run() { checkNewPiece(); }

    public void checkNewPiece() {
        short orderNumber;
        DataValue value;
        value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getStorageInPiece()), UAmaster.client);
        assert value != null;
        if (PlcHandler.readBoolValue(value)) {

            value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getOrderNumberOut()), UAmaster.client);
            assert value != null;
            orderNumber = (short) PlcHandler.readInt16Value(value);

            PlcHandler.writeBool(UAmaster.client,
                    PlcHandler.getItem(UAmaster.itemss, nodes.getOrderNumberReadBool()), true);

            if (orderNumber < 0) {
                DataManagerClient.putMessage("<StorageIn type=\"" + -orderNumber + "\"/>");
            }
            else{
                sendNewLoad(orderNumber);
                processOrders.processEntry(orderNumber);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            
            PlcHandler.writeBool(UAmaster.client,
                    PlcHandler.getItem(UAmaster.itemss, nodes.getOrderNumberReadBool()), false);
        }
    }

    public void sendNewLoad(Short orderNumber) {
        OrderPLC order = processOrders.getOrder(orderNumber);
        if (order == null) { return; }
        StringBuilder message=new StringBuilder();
        message.append("<StorageIn type=\"").append(order.getPy()).append("\"/>");
        DataManagerClient.putMessage(message.toString());
    }
}
