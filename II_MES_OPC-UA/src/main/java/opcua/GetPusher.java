package opcua;


import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;

import java.util.TimerTask;

public class GetPusher extends TimerTask {
    NodesIds nodes = NodesIds.getInstance();
    ProcessOrders processOrders = ProcessOrders.getInstance();
    private final int[] pusherIDs = {1,2,3};

    public GetPusher() { }

    @Override
    public void run() { checkNewPiece(); }

    public void checkNewPiece() {
        Short orderNumber;
        DataValue value;

        for(int pusherID : pusherIDs) {
            value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getPusherLoadNotified(pusherID)), UAmaster.client);
            if (value == null) {
                System.err.println("Failed to get pusherID DataValue");
                return;
            }
            if (PlcHandler.readBoolValue(value)) {
                // new piece to enter unload area
                value = PlcHandler.readValue(PlcHandler.getItem(UAmaster.itemss, nodes.getPusherOrderNumber(pusherID)), UAmaster.client);
                assert value != null;
                orderNumber = (short) PlcHandler.readInt16Value(value);
                // signal order number read ok
                PlcHandler.writeBool(UAmaster.client,
                        PlcHandler.getItem(UAmaster.itemss, nodes.getPusherNumberRead(pusherID)), true);
                // proccess income order
                processOrders.processEntry(orderNumber);
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // tell plc the order number was read
                PlcHandler.writeBool(UAmaster.client,
                        PlcHandler.getItem(UAmaster.itemss, nodes.getPusherNumberRead(pusherID)), false);
            }
        }
    }
}
